package io.github.aylesw.igo.dto;

import io.github.aylesw.igo.entity.Account;
import io.github.aylesw.igo.game.GameInstance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionData {
    private String accessId;
    private String sessionId;
    private Account account;
    private String status;
    private Account opponent;
    private GameInstance game;
    private List<String> pendingChallengers;
}
