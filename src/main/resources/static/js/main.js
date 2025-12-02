/**
 * 書き忘れた変数（var/let）など、あいまいなコードをエラーとして知らせてくれるため、バグ防止になります。
 */
"use strict";

/**
 * (2) DOMContentLoaded イベント
 * 「HTML（DOM）の読み込みがすべて完了したら、
 * この中括弧 { ... } の中の処理を実行してね」
 * という、非常に重要なイベントリスナーです。
 *
 * (これがないと、HTMLが読み込まれる前にJSが動こうとして、
 * 「ボタンが見つからない」等のエラーになります)
 */
window.addEventListener("DOMContentLoaded", (event) => {
  
  // ページ（DOM）が読み込まれたら実行

    // --- 納期(Deadline)の「+」ボタン処理 ---
    document.getElementById("add-deadline").addEventListener("click", function() {
        const container = document.getElementById("deadline-inputs");
        
        // (A) 新しい入力欄（div）を作成
        const newRow = document.createElement("div");
        newRow.className = "row mb-2"; // BootstrapのCSS
        
        // (B) ★ name="deadlineName" と name="deadlineDate" でHTMLを作成
        //    (これがコントローラの List<String> deadlineNames に対応する)
        newRow.innerHTML = `
            <div class="col-5">
                <input type="text" name="deadlineName" class="form-control" placeholder="期限名 (例: キー期限)">
            </div>
            <div class="col-5">
                <input type="date" name="deadlineDate" class="form-control">
            </div>
            <div class="col-2">
                <button type="button" class="btn btn-sm btn-danger remove-row">削除</button>
            </div>
        `;
        container.appendChild(newRow);
    });

    // --- 関連URLの「+」ボタン処理 ---
    document.getElementById("add-url").addEventListener("click", function() {
        const container = document.getElementById("url-inputs");
        
        // (A) 新しい入力欄（div）を作成
        const newRow = document.createElement("div");
        newRow.className = "row mb-2";
        
        // (B) ★ name="urlName" と name="urlLink" でHTMLを作成
        newRow.innerHTML = `
            <div class="col-5">
                <input type="text" name="urlName" class="form-control" placeholder="URL名 (例: Figma)">
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

    // --- 共通の「削除」ボタン処理 ---
    // (document全体で "remove-row" クラスのクリックを監視)
    document.addEventListener("click", function(e) {
        if (e.target && e.target.classList.contains("remove-row")) {
            // クリックされたボタンの親の親（div.row）を削除
            e.target.closest(".row").remove();
        }
    });

});