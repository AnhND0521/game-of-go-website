package io.github.aylesw.igo.controller;

import io.github.aylesw.igo.dto.AuthData;
import io.github.aylesw.igo.dto.SessionData;
import io.github.aylesw.igo.entity.Account;
import io.github.aylesw.igo.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Controller
@RequiredArgsConstructor
public class SessionController {
    private final StatusService statusService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();
        System.out.println("Connected id: " + sessionId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();
        System.out.println("Disconnected id: " + sessionId);

        String username = (String) headers.getSessionAttributes().get("username");
        if (username == null) return;
        SessionData data = statusService.getSessionData(username);
        if (data != null) data.setStatus("Offline");
        broadcastOnlineList();
    }

    @MessageMapping("/auth")
    public void authenticate(AuthData authData, SimpMessageHeaderAccessor headerAccessor) {
        SessionData sessionData = statusService.getSessionData(authData.getUsername());
        if (sessionData == null || !sessionData.getAccessId().equals(authData.getAccessId())) {
            messagingTemplate.convertAndSend("/topic/auth/" + authData.getAccessId(), "INVALID");
            return;
        }
        sessionData.setSessionId(headerAccessor.getSessionId());
        sessionData.setStatus("Available");
        messagingTemplate.convertAndSend("/topic/auth/" + authData.getAccessId(), "OK");
        headerAccessor.getSessionAttributes().put("username", authData.getUsername());
        broadcastOnlineList();
    }

    @MessageMapping("/online-list")
    public void getOnlineList(String username) {
        messagingTemplate.convertAndSend("/user/" + username + "/queue/online-list", statusService.getOnlinePlayerList());
    }

    @MessageMapping("/end-session")
    public void endSession(String username) {
        statusService.removeOnlinePlayer(username);
        broadcastOnlineList();
    }

    private void broadcastOnlineList() {
        messagingTemplate.convertAndSend("/topic/online-list", statusService.getOnlinePlayerList());
    }
}
