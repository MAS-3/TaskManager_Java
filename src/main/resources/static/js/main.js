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
  
  // (3) 動作確認用のログ
  // ブラウザの「開発者ツール(F12)」の「コンソール」に
  // このメッセージが出れば、JSが正しく読み込まれています。
//   console.log("main.js loaded!");

  // --- 将来、ここから下にコードを書いていきます ---
  // 例: const deleteButtons = document.querySelectorAll(".btn-delete");
  // deleteButtons.forEach(button => { ... });

});