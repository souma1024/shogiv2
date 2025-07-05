socket = null;
let selectedFrom = null;
let currentBoard = [];
const urlParams = new URLSearchParams(window.location.search);
const roomId = location.pathname.split("/").pop();
const playerId = urlParams.get("playerId");
let currentPlayerId = null;

function setupWebSocket(roomId, playerId) {

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
            const payload = msg.payload;
            const isSente = payload.senteId === playerId;
            currentPlayerId = payload.senteId;
            drawBoard(payload.board, isSente);
            setupPieceClickHandlers();
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
            currentPlayerId = payload.nextPlayerId;
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
    drawCell(res.from, res.to, res.piece);
    resetSelectionAndHighlight();
}

function applyMoveToBoard(from, to, piece, promotion) {
    const [toX, toY] = to;

    if (from === null) {
        // 🟡 打ち駒（持ち駒 → 盤面）
        currentBoard[toY][toX] = piece;
        return;
    }

    const [fromX, fromY] = from;
    currentBoard[fromY][fromX] = 0;

    if (promotion) piece = promote(piece); // 成りを反映
    currentBoard[toY][toX] = piece;
}

function resetSelectionAndHighlight() {
    selectedFrom = null;
    document.querySelectorAll(".board-cell").forEach(c => c.classList.remove("movable-highlight"));
}

function drawCell(from, to, piece) {
    const [toX, toY] = to;

    if (from !== null) {
        const [fromX, fromY] = from;
        const fromCell = document.getElementById(`cell-${fromX}-${fromY}`);
        if (fromCell) fromCell.innerHTML = "";
    }

    if (from === null) {
        // 打ち駒のときは、元が持ち駒フィールド
        const kind = Math.abs(piece);
        const capturedCellId = `capturedCell-${playerId}-${kind}`;
        const cell = document.getElementById(capturedCellId);
        if (cell) {
            cell.innerHTML = ""; // 手動で消す
        }
    }
    
    const toCell = document.getElementById(`cell-${toX}-${toY}`);

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
    const capturedPiece = captured.piece * -1;
    const capturedCellId = `capturedCell-${captured.owner}-${kind}`;
    const cell = document.getElementById(capturedCellId);
    if (!cell) {
        console.warn("⚠️ 持ち駒セルが見つかりません:", capturedCellId);
        return;
    }
    cell.innerHTML = captured.piece === 0 ? "" : getPieceImage(-1 * captured.piece);
    document.getElementById(capturedCellId).addEventListener("click", () => {   
        if (playerId !== currentPlayerId) {
            console.log("⛔ あなたの手番ではありません");
            return;
        }

        selectedFrom = {
            from: null,
            piece: capturedPiece // kindだけでなく符号つけたいなら Math.sign(...)
        };

        // 打てる場所の取得要求を送る
        const request = {
            type: "movable_position_request",
            payload: {
                roomId,
                playerId: playerId,
                from: null,
                piece: capturedPiece,
                promotion: false
            }
        };

        socket.send(JSON.stringify(request));
        console.log("📤 持ち駒からの movable_position_request:", request);
    });
}

function getPieceImage(piece) {
    if (piece === 0) return "";

    const abs = Math.abs(piece);
    const promotion = isPromoted(abs);

    let base = promotion ? abs - 100 : abs;
    let name = {
        1: "fu", 2: "kyo", 3: "kei", 4: "gin", 5: "kin",
        6: "kaku", 7: "hisya", 8: "uma", 9: "ryu", 77: "gyoku"
    }[base];

    if (!name) return "";

    if (promotion && base !== 5) name = "promoted_" + name;
    const prefix = piece > 0 ? "sente" : "gote";

    return `<img src="/images/piece/${prefix}_${name}.png" class="piece-image ${prefix}-image" />`;
}

function setupPieceClickHandlers() {
    document.querySelectorAll(".board-cell").forEach(cell => {
        cell.addEventListener("click", () => {

            if (playerId !== currentPlayerId) {
                console.log("⛔ あなたの手番ではありません");
                return;
            }

            const [_, x, y] = cell.id.split("-").map(Number);
            const clickedPos = [x, y];

            handleClick(clickedPos);
        });
    })
}

function handleClick(clickedPos) {
    if (selectedFrom === null) {
        handleFirstClick(clickedPos);
    } else {
        handleSecondClick(clickedPos);
    }
}
    
function handleFirstClick(clickedPos) {
    const [x, y] = clickedPos;
    const piece = currentBoard[y][x];
    const promotion = isPromoted(piece);

    selectedFrom = {
        from: clickedPos,
        piece
    };

    const request = {
        type: "movable_position_request",
        payload: {
            roomId,
            playerId: playerId,
            from: clickedPos,
            piece,
            promotion
        }
    };

    socket.send(JSON.stringify(request));
    console.log("📤 movable_position_request:", request);
}

function handleSecondClick(clickedPos) {
    const to = clickedPos;

    if (selectedFrom.from === null) {
        // 🟡 打ちの処理
        const moveMsg = {
            type: "move_request",
            payload: {
                roomId,
                playerId: playerId,
                from: null,
                to,
                piece: selectedFrom.piece,
                promotion: false
            }
        };

        socket.send(JSON.stringify(moveMsg));
        console.log("📤 打ち move_request:", moveMsg);
    } else {
        const from = selectedFrom;
        const piece = currentBoard[selectedFrom.from[1]][selectedFrom.from[0]];
        const promotion = isPromoted(piece);

        const moveMsg = {
            type: "move_request",
            payload: {
                roomId,
                playerId: playerId,
                from: from.from,
                to,
                piece,
                promotion
            }
        };

        socket.send(JSON.stringify(moveMsg));
        console.log("📤 move_request:", moveMsg);
    }
    

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

function drawCapturedPieces(capturedMap) {
    for (const playerId in capturedMap) {
        const pieceList = capturedMap[playerId];
        const kindCountMap = {};

        // 駒の種類ごとにカウント
        for (const piece of pieceList) {
            const kind = Math.abs(piece);
            kindCountMap[kind] = (kindCountMap[kind] || 0) + 1;
        }

        for (const kind in kindCountMap) {
            const cellId = `capturedCell-${playerId}-${kind}`;
            const cell = document.getElementById(cellId);

            const count = kindCountMap[kind];
            cell.innerHTML = imgHtml + (count > 1 ? `<span class="piece-count">x${count}</span>` : "");
        }
    }
}

function promote(piece) {
    return piece + 100 * Math.sign(piece);
}

function isPromoted(piece) {
    return Math.abs(piece) > 100; 
}