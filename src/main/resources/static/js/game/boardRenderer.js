import { state } from './stateManager.js';
import { getPieceImage, promote } from './pieceUtils.js';
import { sendMovablePositionRequest, sendMoveRequest } from './socketHandler.js';

export function drawBoard(board, isSente) {
    for (let y = 0; y < 9; y++) {
        for (let x = 0; x < 9; x++) {
            const cell = document.getElementById(`cell-${x}-${y}`);
            const piece = board[y][x];
            cell.innerHTML = piece ? getPieceImage(piece) : "";
        }
    }

    if (!isSente) {
        document.getElementById("game-layout").classList.add("gote-board");
    }
}

export function drawCell(from, to, piece) {
    const [toX, toY] = to;

    if (from !== null) {
        const [fromX, fromY] = from;
        const fromCell = document.getElementById(`cell-${fromX}-${fromY}`);
        if (fromCell) fromCell.innerHTML = "";
    }

    if (from === null) {
        // 打ち駒のときは、元が持ち駒フィールド
        const kind = Math.abs(piece);
        const capturedCellId = `capturedCell-${state.playerId}-${kind}`;
        const cell = document.getElementById(capturedCellId);
        if (cell) {
            cell.innerHTML = ""; // 手動で消す
        }
    }
    
    const toCell = document.getElementById(`cell-${toX}-${toY}`);

    if (toCell) {
        const piece = state.board[toY][toX];
        toCell.innerHTML = getPieceImage(piece);
    }
}

export function highlightMovableCells(movableList) {
    document.querySelectorAll(".board-cell").forEach(cell => cell.classList.remove("movable-highlight"));
    movableList.forEach(([x, y]) => {
        const cell = document.getElementById(`cell-${x}-${y}`);
        if (cell) cell.classList.add("movable-highlight");
    });
}

export function initBoardClickHandlers() {
    document.querySelectorAll(".board-cell").forEach(cell => {
        cell.addEventListener("click", () => {
            const [_, x, y] = cell.id.split("-").map(Number);
            const clickedPos = [x, y];

            if (state.playerId !== state.currentPlayerId) return;

            if (state.selectedFrom === null) {
                const piece = state.board[y][x];
                state.selectedFrom = { from: clickedPos, piece };
                sendMovablePositionRequest(clickedPos, piece);
            } else {
                sendMoveRequest(state.selectedFrom, clickedPos);
                state.selectedFrom = null;
                document.querySelectorAll(".board-cell").forEach(c => c.classList.remove("movable-highlight"));
            }
        });
    });
}

export function applyCapturedPieces(captured) {
    if (!captured) return;
    const { owner, piece, count } = captured;
    const kind = Math.abs(piece);
    const capturedPiece = piece * -1;
    const capturedCellId = `capturedCell-${owner}-${kind}`;
    const cell = document.getElementById(capturedCellId);
    if (!cell) return;

    const wrapper = document.createElement("div");
    wrapper.innerHTML = getPieceImage(capturedPiece);

     if (count > 1) {
        const countSpan = document.createElement("div");
        countSpan.className = "piece-count";
        countSpan.textContent = `×${count}`;
        wrapper.appendChild(countSpan);
    }

    cell.appendChild(wrapper);

    cell.onclick = () => {
        if (state.playerId !== state.currentPlayerId) return;
        state.selectedFrom = {
            from: null,
            piece: capturedPiece
        };
        const request = {
            type: "movable_position_request",
            payload: {
                roomId: state.roomId,
                playerId: state.playerId,
                from: null,
                piece: capturedPiece,
                promotion: false
            }
        };
        state.socket.send(JSON.stringify(request));
        console.log("📤 持ち駒からの movable_position_request:", request);
    };
}

export function resetSelectionAndHighlight() {
    state.selectedFrom = null;
    document.querySelectorAll(".board-cell").forEach(c => c.classList.remove("movable-highlight"));
}

export function applyMoveToBoard(from, to, piece, promotion) {
    const [toX, toY] = to;

    if (from === null) {
        // 🟡 打ち駒（持ち駒 → 盤面）
        state.board[toY][toX] = piece;
        return;
    }

    const [fromX, fromY] = from;
    state.board[fromY][fromX] = 0;

    if (promotion) piece = promote(piece); // 成りを反映
    state.board[toY][toX] = piece;
}