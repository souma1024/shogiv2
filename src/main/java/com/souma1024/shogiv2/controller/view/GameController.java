package com.souma1024.shogiv2.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.souma1024.shogiv2.domain.model.Player;
import com.souma1024.shogiv2.service.RoomSessionManager;

@Controller
public class GameController {
    
    @GetMapping("/games/{roomId}")
    public String showGamePage(@PathVariable String roomId, @RequestParam String playerId, Model model) {
        RoomSessionManager roomManager = RoomSessionManager.getInstance();
        Player player = roomManager.getPlayerById(roomId, playerId);

        if (player == null) {
            return "error"; // 不正なアクセスの場合
        }

        Player[] players = roomManager.getPlayers(roomId);
        if (players == null || players[0] == null || players[1] == null) {
            return "error"; // 両プレイヤー揃っていない
        }

        String senteId = players[0].getId();
        String goteId = players[1].getId();

        model.addAttribute("roomId", roomId);
        model.addAttribute("playerId", playerId);
        model.addAttribute("senteId", senteId);
        model.addAttribute("goteId", goteId);

        return "game"; // resources/templates/game.html
    }
}
