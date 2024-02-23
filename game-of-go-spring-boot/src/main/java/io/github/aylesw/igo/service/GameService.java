package io.github.aylesw.igo.service;

import io.github.aylesw.igo.dto.GameSetupInfo;
import io.github.aylesw.igo.dto.InviteMessage;

public interface GameService {
    GameSetupInfo setupGame(InviteMessage message);
}
