/**
 * main.js
 */
"use strict";

window.addEventListener("DOMContentLoaded", (event) => {

    // --- 納期(Deadline)の「+」ボタン処理 ---
    const addDeadlineBtn = document.getElementById("add-deadline");
    if (addDeadlineBtn) {
        addDeadlineBtn.addEventListener("click", function() {
            const container = document.getElementById("deadline-inputs");
            
            // ★自動入力ロジック: 直前の行の終了日を探す
            let defaultStartDate = "";
            
            // 1. もし既に納期行があるなら、最後の行の終了日を取得
            const lastRow = container.lastElementChild;
            if (lastRow) {
                // 直前の行の「終了日(deadlineEndDate)」を探す
                const lastEndDateInput = lastRow.querySelector('input[name="deadlineEndDate"]');
                if (lastEndDateInput && lastEndDateInput.value) {
                    defaultStartDate = lastEndDateInput.value;
                }
            }
            // 2. もし納期がまだなくて、タスク自体の開始日が入力されていたら、それを使う
            if (!defaultStartDate) {
                const taskStartDateInput = document.querySelector('input[name="startDate"]');
                if (taskStartDateInput && taskStartDateInput.value) {
                    defaultStartDate = taskStartDateInput.value;
                }
            }

            const newRow = document.createElement("div");
            newRow.className = "row mb-2"; 
            
            // ★ここが重要！ name属性をコントローラに合わせる
            // deadlineStartDate と deadlineEndDate の2つを作る
            newRow.innerHTML = `
                <div class="col-4">
                    <input type="text" name="deadlineName" class="form-control" placeholder="工程名">
                </div>
                <div class="col-3">
                    <input type="date" name="deadlineStartDate" class="form-control" value="${defaultStartDate}" title="開始日">
                </div>
                <div class="col-3">
                    <input type="date" name="deadlineEndDate" class="form-control" title="終了日">
                </div>
                <div class="col-2">
                    <button type="button" class="btn btn-sm btn-danger remove-row">削除</button>
                </div>
            `;
            container.appendChild(newRow);
        });
    }

    // --- 関連URLの「+」ボタン処理 ---
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

    // --- 共通の「削除」ボタン処理 ---
    document.addEventListener("click", function(e) {
        if (e.target && e.target.classList.contains("remove-row")) {
            e.target.closest(".row").remove();
        }
    });

    // --- ガントチャート処理 (Google Charts) ---
    google.charts.load('current', {'packages':['gantt']});
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
            if (!t.start || !t.end) return;
            // 異常値除外 (9999年など)
            if (t.end.indexOf("9999") !== -1) return;

            const startDate = new Date(t.start);
            const endDate = new Date(t.end);

            if (endDate < startDate) {
                endDate.setDate(startDate.getDate());
            }

            rows.push([
                t.id, t.name, 'Task',
                startDate, endDate, null, t.progress, null
            ]);
        });

        if (rows.length > 0) {
            data.addRows(rows);
            const options = {
                height: rows.length * 42 + 50,
                gantt: {
                    trackHeight: 30,
                    barCornerRadius: 4,
                    backgroundColor: { fill: '#fff' }
                }
            };
            const chart = new google.visualization.Gantt(document.getElementById('gantt-chart'));
            chart.draw(data, options);
        } else {
            const chartDiv = document.getElementById('gantt-chart');
            if(chartDiv) chartDiv.innerHTML = "<div class='alert alert-light'>表示できるタスクがありません</div>";
        }
    }
});