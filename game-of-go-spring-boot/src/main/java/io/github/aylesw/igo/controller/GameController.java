package io.github.aylesw.igo.controller;

import io.github.aylesw.igo.dto.*;
import io.github.aylesw.igo.game.GameInfo;
import io.github.aylesw.igo.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GameController {
    private final SimpMessagingTemplate messagingTemplate;
    private final GameService gameService;

    @MessageMapping("/invitation/send")
    public void sendInvitation(InviteMessage message) {
        if (message.getTargetPlayer().equals("$BOT")) {
            message.setReply("ACCEPT");
            messagingTemplate.convertAndSend("/user/" + message.getSourcePlayer() + "/queue/invitation-reply", message);
            gameService.setupGame(message);
            return;
        }
        messagingTemplate.convertAndSend("/user/" + message.getTargetPlayer() + "/queue/invitation", message);
    }

    @MessageMapping("/invitation/reply")
    public void replyToInvitation(InviteMessage message) {
        messagingTemplate.convertAndSend("/user/" + message.getSourcePlayer() + "/queue/invitation-reply", message);
        if (message.getReply().equals("ACCEPT")) {
            gameService.setupGame(message);
        }
    }

    @MessageMapping("/game/info")
    public void getGameInfo(GetGameInfoMessage message) {
        GameInfo info = gameService.getGameInfo(message.getGameId());
        messagingTemplate.convertAndSend("/user/" + message.getUsername() + "/queue/game/info", info);
    }

    @MessageMapping("/game/move")
    public void move(MoveMessage message) {
        gameService.move(message);
    }

    @MessageMapping("/game/interrupt/request")
    public void requestInterrupt(InterruptMessage message) {
        gameService.requestInterrupt(message);
    }

    @MessageMapping("/game/interrupt/reply")
    public void replyToInterrupt(InterruptMessage message) {
        gameService.replyToInterrupt(message);
    }
}
