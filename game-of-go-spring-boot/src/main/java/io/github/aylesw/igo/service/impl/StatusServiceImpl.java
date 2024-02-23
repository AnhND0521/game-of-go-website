package io.github.aylesw.igo.service.impl;

import io.github.aylesw.igo.dto.PlayerStatus;
import io.github.aylesw.igo.dto.SessionData;
import io.github.aylesw.igo.entity.Account;
import io.github.aylesw.igo.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class StatusServiceImpl implements StatusService {
    private Map<String, SessionData> sessionData = new TreeMap<>();

    @Override
    public List<PlayerStatus> getOnlinePlayerList() {
        return sessionData.entrySet().stream()
                .filter(e -> !e.getValue().getStatus().equals("Offline"))
                .map(e -> PlayerStatus.builder()
                        .username(e.getKey())
                        .status(e.getValue().getStatus())
                        .build()
                ).toList();
    }

    @Override
    public void addOnlinePlayer(Account account, String accessId) {
        SessionData data = SessionData.builder()
                .accessId(accessId)
                .account(account)
                .status("Available")
                .build();
        sessionData.put(account.getUsername(), data);
    }

    @Override
    public void removeOnlinePlayer(String username) {
        sessionData.remove(username);
    }

    @Override
    public SessionData getSessionData(String username) {
        return sessionData.get(username);
    }
}