let currentRoomId = null;
let currentPlayerId = null;
let socket = null;

function setupWebSocket(roomId, playerId) {
    socket = new WebSocket(`ws://${location.host}/ws/shogi?roomId=${roomId}&playerId=${playerId}`);

    socket.onmessage = (event) => {
        const message = JSON.parse(event.data);

        if (message.type === "start_game_response" && message.payload.status === "started") {
            window.location.href = `/games/${roomId}?playerId=${playerId}`;
        }
    };
}

// ルーム参加処理
document.getElementById("joinRoomForm").addEventListener("submit", function(event) {
    event.preventDefault();
    const roomId = document.getElementById("roomIdInput").value;

    fetch(`/api/rooms/${roomId}/join`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        }
    })
    .then(async response => {
        const data = await response.json();
        const resultDiv = document.getElementById("joinResult");

        if (response.ok) {
            resultDiv.innerText = `ルームに参加しました! 持ち時間: ${data.timeLimit}分`;

            currentRoomId = data.roomId;
            currentPlayerId = data.playerId;

            // WebSocket 接続
            setupWebSocket(currentRoomId, currentPlayerId);

            // 開始ボタン用データ
            document.getElementById('startGameBtn').dataset.roomId = currentRoomId;
            document.getElementById('startGameBtn').dataset.playerId = currentPlayerId;

            // UI 更新
            document.getElementById("openModalBtn").disabled = true;
            document.getElementById("roomIdInput").disabled = true;
            document.getElementById("joinRoomButton").disabled = true;
            document.getElementById("roomCancelBtn").classList.remove("hidden");

        } else {
            resultDiv.innerText = `エラー: ${data.message || "不明なエラーが発生しました"}`;
        }
    })
    .catch(error => {
        document.getElementById("joinResult").innerText = `通信エラー: ${error.message}`;
    });
});

// 対局開始ボタン処理
document.addEventListener('DOMContentLoaded', function () {
    const startBtn = document.getElementById('startGameBtn');
    const message = document.getElementById('startGameMessage');

    startBtn.addEventListener('click', async () => {
        const roomId = startBtn.dataset.roomId;
        const playerId = startBtn.dataset.playerId;

        // 先に WebSocket が確立されていなければ接続
        if (!socket || socket.readyState !== WebSocket.OPEN) {
            setupWebSocket(roomId, playerId);
        }

        // 対局開始リクエスト送信
        const response = await fetch(`/api/rooms/${roomId}/start`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ playerId })
        });

        if (response.ok) {
            const data = await response.json();

            if (data.status !== 'started') {
                message.textContent = '対局開始待機中...（相手の開始を待っています）';
                startBtn.disabled = true;
            }
            // ✅ 遷移は WebSocket メッセージで統一（ここでは行わない）
        } else {
            message.textContent = '開始に失敗しました。もう一度お試しください。';
        }
    }); 
});

// キャンセルボタン
document.getElementById("roomCancelBtn").addEventListener("click", () => {
    document.getElementById("openModalBtn").disabled = false;
    document.getElementById("roomIdInput").disabled = false;
    document.getElementById("joinRoomButton").disabled = false;
    document.getElementById("roomCancelBtn").classList.add("hidden");

    document.getElementById("createRoomResult").innerHTML = "";
    document.getElementById("joinResult").innerHTML = "";
    document.getElementById("roomIdInput").value = "";

    // ソケット切断
    if (socket && socket.readyState === WebSocket.OPEN) {
        socket.close();
    }
});
