"use strict";

window.addEventListener("DOMContentLoaded", (event) => {

    // --- 工程(Process)の「+」ボタン処理 ---
    const addProcessBtn = document.getElementById("add-process"); 
    if (addProcessBtn) {
        addProcessBtn.addEventListener("click", function() {
            const container = document.getElementById("process-inputs"); 
            
            let defaultStartDate = "";
            const lastRow = container.lastElementChild;
            if (lastRow) {
                const lastEndDateInput = lastRow.querySelector('input[name="processEndDate"]');
                if (lastEndDateInput && lastEndDateInput.value) {
                    defaultStartDate = lastEndDateInput.value;
                }
            }
            if (!defaultStartDate) {
                const taskStartDateInput = document.querySelector('input[name="startDate"]');
                if (taskStartDateInput && taskStartDateInput.value) {
                    defaultStartDate = taskStartDateInput.value;
                }
            }

            const newRow = document.createElement("div");
            newRow.className = "row mb-2"; 
            
            newRow.innerHTML = `
                <div class="col-4">
                    <input type="text" name="processName" class="form-control" placeholder="工程名">
                </div>
                <div class="col-3">
                    <input type="date" name="processStartDate" class="form-control" value="${defaultStartDate}" title="開始日">
                </div>
                <div class="col-3">
                    <input type="date" name="processEndDate" class="form-control" title="終了日">
                </div>
                <div class="col-2">
                    <button type="button" class="btn btn-sm btn-danger remove-row">削除</button>
                </div>
            `;
            container.appendChild(newRow);
        });
    }

    // --- 関連URL ---
    const addUrlBtn = document.getElementById("add-url");
    if (addUrlBtn) {
        addUrlBtn.addEventListener("click", function() {
            const container = document.getElementById("url-inputs");
            const newRow = document.createElement("div");
            newRow.className = "row mb-2";
            newRow.innerHTML = `
                <div class="col-5">
                    <input type="text" name="urlName" class="form-control" placeholder="URL名">
                </div>
                <div class="col-5">
                    <input type="text" name="urlLink" class="form-control" placeholder="http://...">
                </div>
                <div class="col-2">
                    <button type="button" class="btn btn-sm btn-danger remove-row">削除</button>
                </div>
            `;
            container.appendChild(newRow);
        });
    }

    // --- 削除ボタン ---
    document.addEventListener("click", function(e) {
        if (e.target && e.target.classList.contains("remove-row")) {
            e.target.closest(".row").remove();
        }
    });

    // --- ガントチャート処理 (Google Charts) ---
    google.charts.load('current', {'packages':['gantt'], 'language': 'ja'});
    google.charts.setOnLoadCallback(drawChart);

    function drawChart() {
        const tasksData = window.taskDataForGantt || [];
        const data = new google.visualization.DataTable();
        
        data.addColumn('string', 'Task ID');
        data.addColumn('string', 'Task Name');
        data.addColumn('string', 'Resource');
        data.addColumn('date', 'Start Date');
        data.addColumn('date', 'End Date');
        data.addColumn('number', 'Duration');
        data.addColumn('number', 'Percent Complete');
        data.addColumn('string', 'Dependencies');

        const rows = [];
        
        tasksData.forEach(t => {
            // 1. 親タスクの期間計算
            // 親タスクの日付計算
            let pStartStr = t.start;
            let pEndStr = null; // 親の終了日は一旦無視

            // 子工程がある場合、その全範囲を親の期間とする
            if (t.processes && t.processes.length > 0) {
                const validChildren = t.processes.filter(p => p.start && p.end);
                
                if (validChildren.length > 0) {
                    // 子の中で一番早い開始日を探す
                    const earliestChildStart = validChildren.reduce((min, p) => p.start < min ? p.start : min, validChildren[0].start);
                    
                    // 子の中で一番遅い終了日を探す
                    const latestChildEnd = validChildren.reduce((max, p) => p.end > max ? p.end : max, validChildren[0].end);

                    // ★修正: 子工程があるなら、親の開始日・終了日を強制的に上書きする
                    pStartStr = earliestChildStart;
                    pEndStr = latestChildEnd;
                }
            }

            // もし子工程がなく、親タスクだけの日付が入っている場合のバックアップ
            // (t.end が有効な日付なら pEndStr に採用)
            if (!pEndStr && t.end && t.end.indexOf("9999") === -1) {
                pEndStr = t.end;
            }

            // 2. 親タスクの描画
            if (pStartStr && pEndStr) {
                const pStart = new Date(pStartStr);
                const pEnd = new Date(pEndStr);
                
                // 逆転防止
                if (pEnd < pStart) pEnd.setDate(pStart.getDate());

                rows.push([
                    t.id, 
                    t.name, 
                    'Task', 
                    pStart, 
                    pEnd, 
                    null, 
                    t.progress, 
                    null
                ]);
            }

            // 3. 子工程の描画
            if (t.processes && t.processes.length > 0) {
                t.processes.forEach(p => {
                    if (!p.start || !p.end) return; 
                    const subStart = new Date(p.start);
                    const subEnd = new Date(p.end);
                    if (subEnd < subStart) subEnd.setDate(subStart.getDate());

                    rows.push([
                        p.id, 
                        "　↳ " + p.name, 
                        'Process', 
                        subStart, 
                        subEnd, 
                        null, 
                        p.progress, 
                        null 
                    ]);
                });
            }
        });

        if (rows.length > 0) {
            data.addRows(rows);

            // --- 全日付の目盛り(ticks)を作成 ---
            let minDate = new Date(8640000000000000);
            let maxDate = new Date(-8640000000000000);

            rows.forEach(r => {
                if (r[3] < minDate) minDate = r[3];
                if (r[4] > maxDate) maxDate = r[4];
            });
            minDate.setDate(minDate.getDate() - 1);
            maxDate.setDate(maxDate.getDate() + 1);

            let ticks = [];
            let currDate = new Date(minDate);
            while (currDate <= maxDate) {
                ticks.push(new Date(currDate));
                currDate.setDate(currDate.getDate() + 1);
            }

            const options = {
                height: rows.length * 42 + 50,
                
                // ★修正: フォーマットに曜日 (E) を追加
                hAxis: {
                    ticks: ticks,
                    gridlines: {
                        color: '#eee',
                        count: -1,
                        units: {
                            days: {format: ['M/d (E)']} // ★日付フォーマットを強制指定
                        }
                    }
                },

                gantt: {
                    trackHeight: 30,
                    barCornerRadius: 4,
                    backgroundColor: { fill: '#fff' },
                    palette: [
                        { "color": "#4285F4", "dark": "#2a56c6", "light": "#c6dafc" },
                        { "color": "#db4437", "dark": "#a52714", "light": "#f4c7c3" }
                    ]
                }
            };
            
            const chart = new google.visualization.Gantt(document.getElementById('gantt-chart'));

            // ★シンプル版: 文字に「(土)」「(日)」が含まれていたらクラスを追加
            google.visualization.events.addListener(chart, 'ready', function () {
                document.querySelectorAll('#gantt-chart text').forEach(text => {
                    const val = text.textContent;
                    
                    // "土" または "Sat" が含まれていれば青
                    if (val.includes('土') || val.includes('Sat')) {
                        text.classList.add('gantt-sat');
                    } 
                    // "日" または "Sun" が含まれていれば赤
                    else if (val.includes('日') || val.includes('Sun')) {
                        text.classList.add('gantt-sun');
                    }
                });
            });

            chart.draw(data, options);

        } else {
            const chartDiv = document.getElementById('gantt-chart');
            if(chartDiv) chartDiv.innerHTML = "<div class='alert alert-light'>表示できるタスクがありません</div>";
        }
    }
});