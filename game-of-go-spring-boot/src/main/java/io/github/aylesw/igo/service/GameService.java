package io.github.aylesw.igo.service;

import io.github.aylesw.igo.dto.*;
import io.github.aylesw.igo.game.GameInfo;

import java.util.List;

public interface GameService {
    void setupGame(InviteMessage message);
    GameInfo getGameInfo(String gameId);
    void move(MoveMessage message);
    void requestInterrupt(InterruptMessage message);
    void replyToInterrupt(InterruptMessage message);
    void handlePlayerLeave(String username);
}
