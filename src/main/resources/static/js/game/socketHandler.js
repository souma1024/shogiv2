import { state } from './stateManager.js';
import { drawBoard, applyMoveToBoard, resetSelectionAndHighlight, applyCapturedPieces, initBoardClickHandlers,  highlightMovableCells, drawCell } from './boardRenderer.js';
import { getPieceImage, isPromoted } from './pieceUtils.js';

export function setupWebSocket(roomId, playerId) {
    state.socket = new WebSocket(`ws://${location.host}/ws/shogi?roomId=${roomId}&playerId=${playerId}`);

    state.socket.onopen = () => {
        console.log("✅ WebSocket 接続");
        send({
            type: "start_game_request",
            payload: { roomId, playerId }
        });
    };

    state.socket.onmessage = (event) => {
        const msg = JSON.parse(event.data);
        console.log("📥 メッセージ受信:", msg);

        switch (msg.type) {
            case "start_game_response":
                state.currentPlayerId = msg.payload.senteId;
                state.isSente = msg.payload.senteId === playerId;
                state.board = msg.payload.board;
                drawBoard(state.board, state.isSente);
                initBoardClickHandlers();
                break;
            case "movable_position_response":
                highlightMovableCells(msg.payload.movable);
                break;
            case "move_response":
                handleMoveResponse(msg.payload);
                applyCapturedPieces(msg.payload.captured);
                state.currentPlayerId = msg.payload.nextPlayerId;
                break;
        }
    };
}

function send(msg) {
    state.socket.send(JSON.stringify(msg));
}

function handleMoveResponse(res) {
    if (!res.success) {
        resetSelectionAndHighlight();
        return;
    }

    // update state
    applyMoveToBoard(res.from, res.to, res.piece, res.promotion);
    drawCell(res);
    resetSelectionAndHighlight();
}

export function sendMovablePositionRequest(from, piece) {
    const promotion = Math.abs(piece) >= 100;
    const msg = {
        type: "movable_position_request",
        payload: {
            roomId: state.roomId,
            playerId: state.playerId,
            from,
            piece,
            promotion
        }
    };
    send(msg);
    console.log("📤 movable_position_request:", msg);
}

export function sendMoveRequest(fromObj, to) {
    const from = fromObj;
    
    if (JSON.stringify(from.from) === JSON.stringify(to)) return;

    if (from.from === null) {
        const piece = fromObj.piece;
        const promotion = Math.abs(piece) >= 100;
        const msg = {
            type: "move_request",
            payload: {
                roomId: state.roomId,
                playerId: state.playerId,
                from: from.from,
                to,
                piece,
                promotion
            }
        };
        send(msg);
        console.log("📤 move_request:", msg);
    } else {
        const piece = state.board[from.from[1]][from.from[0]];
        const promotion = isPromoted(piece);

        const moveMsg = {
            type: "move_request",
            payload: {
                roomId: state.roomId,
                playerId: state.playerId,
                from: from.from,
                to,
                piece,
                promotion
            }
        };

        send(moveMsg);
        console.log("📤 move_request:", moveMsg);
    }
}
