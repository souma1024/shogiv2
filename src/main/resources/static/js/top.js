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
            // 必要ならここで画面遷移なども追加可能

            currentRoomId = data.roomId;
            currentPlayerId = data.playerId;

            // 画面表示や開始ボタンに反映
            document.getElementById('startGameBtn').dataset.roomId = currentRoomId;
            document.getElementById('startGameBtn').dataset.playerId = currentPlayerId;

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

document.addEventListener('DOMContentLoaded', function () {
    const startBtn = document.getElementById('startGameBtn');
    const message = document.getElementById('startGameMessage');

    startBtn.addEventListener('click', async () => {
        const roomId = startBtn.dataset.roomId;
        const playerId = startBtn.dataset.playerId;

        const response = await fetch(`/api/rooms/${roomId}/start`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ playerId })
        });

        if (response.ok) {
            const data = await response.json();

            if (data.status === 'started') {
                // 対局開始！ ゲーム画面へ遷移
                window.location.href = `/games/${roomId}`;
            } else {
                // 相手待ち
                message.textContent = '対局開始待機中...（相手の開始を待っています）';
                startBtn.disabled = true;
            }
        } else {
            message.textContent = '開始に失敗しました。もう一度お試しください。';
        }
    }); 
});

document.getElementById("roomCancelBtn").addEventListener("click", () => {
    document.getElementById("openModalBtn").disabled = false;
    document.getElementById("roomIdInput").disabled = false;
    document.getElementById("joinRoomButton").disabled = false;
    document.getElementById("roomCancelBtn").classList.add("hidden");

    document.getElementById("createRoomResult").innerHTML = "";
    document.getElementById("joinResult").innerHTML = "";
    document.getElementById("roomIdInput").value = "";

});