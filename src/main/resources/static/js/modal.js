document.getElementById('openModalBtn').addEventListener('click', () => {
    document.getElementById('modal').classList.remove('hidden');
});

document.getElementById('closeModalBtn').addEventListener('click', () => {
     document.getElementById('modal').classList.add('hidden');
});

document.getElementById('createRoomForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const timeLimit = e.target.timeLimit.value;
    console.log('ルーム作成: 持ち時間 = ' + timeLimit + '分');

    // AjaxやFetchでSpring Bootにルーム作成リクエストを送る処理を追加してもOK
    document.getElementById('modal').classList.add('hidden');
});


document.getElementById("createRoomForm").addEventListener("submit", function(event) {
    event.preventDefault();

    const timeLimit = this.timeLimit.value;
    const payload = {
        timeLimit: parseInt(timeLimit, 10)
    };

    fetch("/api/rooms", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
    })
    .then(async response => {
        const data = await response.json();
        const resultArea = document.getElementById("createRoomResult");
        resultArea.innerHTML = ""; // 前回の結果を消す

        if (response.ok) {
            resultArea.innerHTML = `
                <div class="success">
                    <p><strong>ルーム作成に成功しました！</strong></p>
                    <p>ルームID: <code>${data.roomId}</code></p>
                    <p>持ち時間: ${data.timeLimit}分</p>
                </div>
            `;
            document.getElementById("openModalBtn").disabled = true;
            document.getElementById("roomIdInput").disabled = true;
            document.getElementById("joinRoomButton").disabled = true;
            document.getElementById("roomCancelBtn").classList.remove("hidden");
            currentRoomId = data.roomId;
            currentPlayerId = data.playerId;

            // 画面表示や開始ボタンに反映
            document.getElementById('startGameBtn').dataset.roomId = currentRoomId;
            document.getElementById('startGameBtn').dataset.playerId = currentPlayerId;
        } else {
            resultArea.innerHTML = `
                <div class="error">
                    <p>ルーム作成に失敗しました。</p>
                    <p>理由: ${data.message || "不明なエラー"}</p>
                </div>
            `;
        }
    })
    .catch(err => {
        const resultArea = document.getElementById("createRoomResult");
        resultArea.innerHTML = `
            <div class="error">
                <p>通信エラーが発生しました: ${err.message}</p>
            </div>
        `;
    });
});

