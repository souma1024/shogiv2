const board = document.getElementById('shogiBoard');

// セルを動的に生成（81個）
for (let y = 1; y <= 9; y++) {
  for (let x = 1; x <= 9; x++) {
    const cell = document.createElement('div');
    cell.className = 'cell';
    cell.dataset.x = x;
    cell.dataset.y = y;
    cell.addEventListener('click', () => {
      console.log(`Clicked cell (${x}, ${y})`);
    });
    board.appendChild(cell);
  }
}


document.addEventListener("DOMContentLoaded", () => {
    const urlParams = new URLSearchParams(window.location.search);
    const roomId = window.location.pathname.split("/").pop();
    const playerId = urlParams.get("playerId");

    setupWebSocket(roomId, playerId);
});


function setupWebSocket(roomId, playerId) {
    const socket = new WebSocket(`ws://${location.host}/ws/shogi?roomId=${roomId}&playerId=${playerId}`);

    socket.onopen = () => {
        console.log("✅ WebSocket接続完了");

        // 🔄 再接続時に対局状態を復元するリクエストを送信
        const reconnectRequest = {
            type: "reconnect_request",
            payload: {
                roomId: roomId,
                playerId: playerId
            }
        };
        socket.send(JSON.stringify(reconnectRequest));
    };

    socket.onmessage = (event) => {
        const msg = JSON.parse(event.data);
        if (msg.type === "reconnect_response") {
            console.log("♻️ 対局状態復元応答:", msg.payload);
            // → 現在の局面・持ち駒などを取得して描画
        }
        // 他の message.type にも対応（move, game_over など）
    };
}
