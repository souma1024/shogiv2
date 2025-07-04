let myPlayerId = ""; // ページ読み込み時にURLなどからセットする
socket = null;
let selectedFrom = null;
let currentBoard = [];
const urlParams = new URLSearchParams(window.location.search);
const roomId = location.pathname.split("/").pop();
const playerId = urlParams.get("playerId");
let currentTurnPlayerId = null;


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

        if (msg.type === "start_game_response") {
            const state = msg.payload;
            const isSente = state.senteId === playerId ? true : false;
            drawBoard(state.board, isSente);
            setupPieceClickHandlers();
            currentTurnPlayerId = state.senteId;
        }

        if (msg.type === "movable_position_response") {
            const payload = msg.payload;
            console.log("受信：move_postition_response", payload);
            highlightMovableCells(payload.movable); // この関数を後述
        }

        if (msg.type == "move_response") {
            const payload = msg.payload
            handleMoveResponse(payload);
            applyCapturedPieces(payload.captured);
        }
    };
}

function handleMoveResponse(res) {
    console.log("受信：move_response", res);
    if (!res.success) {
        resetSelectionAndHighlight();
        return;
    }

    applyMoveToBoard(res.from, res.to, res.piece, res.promotion);
    drawCell(res.from, res.to);
    currentTurnPlayerId = res.nextPlayerId;
    resetSelectionAndHighlight();
}

function applyMoveToBoard(from, to, piece, promotion) {
    const [fromX, fromY] = from;
    const [toX, toY] = to;

    currentBoard[fromY][fromX] = 0;

    if (promotion) {
        piece = piece + 100 * Math.sign(piece); // 成りを反映
    }

    currentBoard[toY][toX] = piece;
}

function resetSelectionAndHighlight() {
    selectedFrom = null;
    document.querySelectorAll(".board-cell").forEach(c => c.classList.remove("movable-highlight"));
}

function drawCell(from, to) {
    const [fromX, fromY] = from;
    const [toX, toY] = to;

    const fromCell = document.getElementById(`cell-${fromX}-${fromY}`);
    const toCell = document.getElementById(`cell-${toX}-${toY}`);

    if (fromCell) fromCell.innerHTML = "";
    if (toCell) {
        const piece = currentBoard[toY][toX];
        toCell.innerHTML = getPieceImage(piece);
    }
}

function drawBoard(board, isSente) {
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
    if (!isSente) {
        document.getElementById("game-layout").classList.add("gote-board");
    }
}

// ページ読み込み後にWebSocket接続を開始
document.addEventListener("DOMContentLoaded", () => {
    setupWebSocket(roomId, playerId);
});

function applyCapturedPieces(captured) {
    if (captured === null) return;
    const kind = captured.piece > 0 ? captured.piece : -1 * captured.piece;
    const capturedCellId = `capturedCell-${captured.owner}-${kind}`;
    const cell = document.getElementById(capturedCellId);
    if (!cell) {
        console.warn("⚠️ 持ち駒セルが見つかりません:", capturedCellId);
        return;
    }
    cell.innerHTML = captured.piece === 0 ? "" : getPieceImage(-1 * captured.piece);
    document.getElementById(capturedCellId).addEventListener("click", () => {   
    });
}

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
    document.querySelectorAll(".board-cell").forEach(cell => {
        cell.addEventListener("click", () => {
            const [_, x, y] = cell.id.split("-").map(Number);
            const clickedPos = [x, y];

            if (myPlayerId !== currentTurnPlayerId) {
                console.log("⛔ あなたの手番ではありません");
                return;
            }


            if (selectedFrom === null) {
                handleFirstClick(clickedPos);
            } else {
                handleSecondClick(clickedPos);
            }
        });
    })
}
    
function handleFirstClick(clickedPos) {
    selectedFrom = clickedPos;

    const [x, y] = clickedPos;
    const piece = currentBoard[y][x];
    const promotion = piece >= 100;

    const request = {
        type: "movable_position_request",
        payload: {
            roomId,
            playerId: myPlayerId,
            from: clickedPos,
            piece,
            promotion
        }
    };

    socket.send(JSON.stringify(request));
    console.log("📤 movable_position_request:", request);
}

function handleSecondClick(clickedPos) {
    const from = selectedFrom;
    const to = clickedPos;

    const piece = currentBoard[from[1]][from[0]];
    const promotion = piece >= 100;

    const moveMsg = {
        type: "move_request",
        payload: {
            roomId,
            playerId: myPlayerId,
            from,
            to,
            piece,
            promotion
        }
    };

    socket.send(JSON.stringify(moveMsg));
    console.log("📤 move_request:", moveMsg);

    // 状態リセット
    selectedFrom = null;
    document.querySelectorAll(".board-cell").forEach(c => c.classList.remove("movable-highlight"));
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