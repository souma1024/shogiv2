import { setupWebSocket } from './socketHandler.js';
import { state } from './stateManager.js';

document.addEventListener("DOMContentLoaded", () => {
    const urlParams = new URLSearchParams(window.location.search);
    state.roomId = location.pathname.split("/").pop();
    state.playerId = urlParams.get("playerId");

    setupWebSocket(state.roomId, state.playerId);
});