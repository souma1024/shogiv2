let myPlayerId = ""; // ページ読み込み時にURLなどからセットする

function setupWebSocket(roomId, playerId) {
    myPlayerId = playerId;

    const socket = new WebSocket(`ws://${location.host}/ws/shogi?roomId=${roomId}&playerId=${playerId}`);

    socket.onopen = () => {
        socket.send(JSON.stringify({
            type: "reconnect_request",
            payload: { roomId, playerId }
        }));
    };

    socket.onmessage = (event) => {
        const msg = JSON.parse(event.data);

        if (msg.type === "reconnect_response") {
            if (!msg.payload.success) {
                alert("再接続失敗: " + msg.payload.message);
            } else {
                console.log("✅ 再接続成功");
            }
        }

        if (msg.type === "game_state") {
            const state = msg.payload;
            drawBoard(state.board);
            updateCapturedPieces(state.capturedPieces);
            updateTurnIndicator(state.currentPlayerId);
        }

        // 他の type: move_response, game_over_response 等の処理もここに追記
    };
}

function drawBoard(board) {
    for (let y = 0; y < 9; y++) {
        for (let x = 0; x < 9; x++) {
            const cell = document.getElementById(`cell-${x}-${y}`);
            const piece = board[y][x];
            cell.textContent = piece === 0 ? "" : getPieceName(piece);
        }
    }
}

function updateCapturedPieces(captured) {
    const sente = document.getElementById("sente-hand");
    const gote = document.getElementById("gote-hand");

    const senteId = sente.dataset.playerId;
    const goteId = gote.dataset.playerId;

    sente.textContent = "先手持ち駒：" + (captured[senteId]?.map(getPieceName).join(" ") || "-");
    gote.textContent = "後手持ち駒：" + (captured[goteId]?.map(getPieceName).join(" ") || "-");
}

function updateTurnIndicator(currentPlayerId) {
    const indicator = document.getElementById("turn-indicator");
    indicator.textContent = currentPlayerId === myPlayerId
        ? "あなたの番です"
        : "相手の番です";
}

function getPieceName(piece) {
    const abs = Math.abs(piece);
    const promoted = abs >= 100 && abs < 200;
    const base = promoted ? (piece > 0 ? piece - 100 : piece + 100) : piece;

    const names = {
        1: "歩", 2: "香", 3: "桂", 4: "銀", 5: "金",
        6: "角", 7: "飛", 8: "馬", 9: "龍", 77: piece > 0 ? "王" : "玉"
    };

    let name = names[Math.abs(base)] || "？";
    if (promoted && base !== 5) name = "成" + name;
    return name;
}

// ページ読み込み後にWebSocket接続を開始
document.addEventListener("DOMContentLoaded", () => {
    const urlParams = new URLSearchParams(window.location.search);
    const roomId = location.pathname.split("/").pop();
    const playerId = urlParams.get("playerId");

    setupWebSocket(roomId, playerId);
});
