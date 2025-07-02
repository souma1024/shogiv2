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

            // 対局が始まっている or すでに開始可能なら遷移
            if (data.status === 'started' || data.status === 'waiting') {
                window.location.href = `/games/${roomId}?playerId=${playerId}`;
            } else {
                message.textContent = '状態が不明です。もう一度お試しください。';
            }          
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

});
