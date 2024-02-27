package io.github.aylesw.igo.service;

import io.github.aylesw.igo.dto.PlayerStatus;
import io.github.aylesw.igo.dto.SessionData;
import io.github.aylesw.igo.entity.Account;

import java.util.List;

public interface StatusService {
    List<PlayerStatus> getOnlinePlayerList();
    void addOnlinePlayer(Account account, String accessId);
    void removeOnlinePlayer(String username);
    SessionData getSessionData(String username);
    void broadcastOnlinePlayerList();
}
