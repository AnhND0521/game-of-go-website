package io.github.aylesw.igo.game;

import io.github.aylesw.igo.dto.PlayerDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameInfo {
    String gameId;
    PlayerDto blackPlayer;
    PlayerDto whitePlayer;
    GameConfig gameConfig;
    GameState gameState;
    GameResult gameResult;
}
