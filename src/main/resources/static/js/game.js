let myPlayerId = ""; // ページ読み込み時にURLなどからセットする
socket = null;
let selectedFrom = null;
let currentBoard = [];
const urlParams = new URLSearchParams(window.location.search);
const roomId = location.pathname.split("/").pop();
const playerId = urlParams.get("playerId");



function setupWebSocket(roomId, playerId) {
    myPlayerId = playerId;

    socket = new WebSocket(`ws://${location.host}/ws/shogi?roomId=${roomId}&playerId=${playerId}`);

    socket.onopen = () => {
        console.log("✅Websocket 接続完了");

        const connectedMsg = {
            type: "start_game_request",
            payload : {
                roomId: roomId,
                playerId: playerId
            }
        }

        socket.send(JSON.stringify(connectedMsg));
    };

    socket.onmessage = (event) => {
        const msg = JSON.parse(event.data);
        console.log("📥 メッセージ受信:", msg);

        if (msg.type === "game_state") {
            const state = msg.payload;
            drawBoard(state.board);
            updateCapturedPieces(state.capturedPieces);
            updateTurnIndicator(state.currentPlayerId);
        }

        if (msg.type === "movable_position_response") {
            const payload = msg.payload;
            highlightMovableCells(payload.movable); // この関数を後述
        }
    };
}

function drawBoard(board) {
    currentBoard = board;
    for (let y = 0; y < 9; y++) {
        for (let x = 0; x < 9; x++) {
            const cellId = `cell-${x}-${y}`;
            const cell = document.getElementById(cellId);
            if (!cell) {
                console.warn("⚠️ cell not found:", cellId);
                continue;
            }

            const piece = board[y][x];
            cell.innerHTML = piece === 0 ? "" : getPieceImage(piece);
        }
    }
    setupPieceClickHandlers();
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
    setupWebSocket(roomId, playerId);
});

function getPieceImage(piece) {
    if (piece === 0) return "";

    const abs = Math.abs(piece);
    const isPromoted = abs >= 100 && abs < 200;

    let base = isPromoted ? abs - 100 : abs;
    let name = {
        1: "fu", 2: "kyo", 3: "kei", 4: "gin", 5: "kin",
        6: "kaku", 7: "hisya", 8: "uma", 9: "ryu", 77: "gyoku"
    }[base];

    if (!name) return "";

    if (isPromoted && base !== 5) {
        name = "promoted_" + name;
    }

    if (piece > 0) {
        return `<img src="/images/piece/sente_${name}.png" class="piece-image sente-image" />`;
    } else {
        return `<img src="/images/piece/gote_${name}.png" class="piece-image gote-image" />`;
    }

}

function setupPieceClickHandlers() {
    for (let y = 0; y < 9; y++) {
        for (let x = 0; x < 9; x++) {
            const cellId = `cell-${x}-${y}`;
            const cell = document.getElementById(cellId);
            
            if (!cell) continue;

            cell.addEventListener("click", () => {
                const clickedPos = [x, y];

                if (selectedFrom === null) {
                    selectedFrom = clickedPos;

                    const piece = currentBoard[y][x];
                    const kind = Math.abs(piece);
                    const promotion = piece >= 100;

                    const request = {
                        type: "movable_position_request",
                        payload: {
                            roomId,
                            playerId: myPlayerId,
                            from: clickedPos,
                            kind,
                            promotion
                        }
                    };
                    socket.send(JSON.stringify(request));
                    console.log("📤 movable_position_request:", request);

                    
                    


                } else {
                    const from = selectedFrom;
                    const to = clickedPos;
                    const piece = currentBoard[from[1]][from[0]];
                    const kind = Math.abs(piece);
                    const promotion = piece >= 100;

                    const moveMsg = {
                        type: "move_request",
                        payload: {
                            roomId,
                            playerId: myPlayerId,
                            from,
                            to,
                            kind,
                            promotion
                        }
                    };

                    socket.send(JSON.stringify(moveMsg));
                    console.log("📤 move_request:", moveMsg);

                    // 状態リセット
                    selectedFrom = null;
                    document.querySelectorAll(".board-cell").forEach(c => c.classList.remove("movable-highlight"));
                }
            });
        }   
    }
}
    

function highlightMovableCells(movableList) {
    // 既存ハイライトをすべてクリア
    document.querySelectorAll(".board-cell").forEach(cell => {
        cell.classList.remove("movable-highlight");
    });

    // 新しく合法手セルにハイライト追加
    movableList.forEach(([x, y]) => {
        const cell = document.getElementById(`cell-${x}-${y}`);
        if (cell) {
            cell.classList.add("movable-highlight");
        }
    });
}