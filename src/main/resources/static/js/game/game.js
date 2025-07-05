import { setupWebSocket } from './socketHandler.js';
import { state } from './stateManager.js';

document.addEventListener("DOMContentLoaded", () => {
    const urlParams = new URLSearchParams(window.location.search);
    const roomId = location.pathname.split("/").pop();
    const playerId = urlParams.get("playerId");

    if (!roomId || !playerId) {
        alert("不正なアクセスです。URLにroomIdまたはplayerIdがありません。");
        return;
    }

    state.roomId = roomId;
    state.playerId = playerId;


    setupWebSocket(state.roomId, state.playerId);
});