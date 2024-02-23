package io.github.aylesw.igo.controller;

import io.github.aylesw.igo.dto.GameSetupInfo;
import io.github.aylesw.igo.dto.InviteMessage;
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
        messagingTemplate.convertAndSend("/user/" + message.getTargetPlayer() + "/queue/invitation", message);
    }

    @MessageMapping("/invitation/reply")
    public void replyToInvitation(InviteMessage message) {
        messagingTemplate.convertAndSend("/user/" + message.getSourcePlayer() + "/queue/invitation-reply", message);
        if (message.getReply().equals("ACCEPT")) {
            GameSetupInfo setupInfo = gameService.setupGame(message);
            messagingTemplate.convertAndSend("/user/" + message.getSourcePlayer() + "/queue/game/new", setupInfo);
            messagingTemplate.convertAndSend("/user/" + message.getTargetPlayer() + "/queue/game/new", setupInfo);
        }
    }
}
