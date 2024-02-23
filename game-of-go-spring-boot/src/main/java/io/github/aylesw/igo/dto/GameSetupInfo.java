package io.github.aylesw.igo.dto;

import io.github.aylesw.igo.game.GameConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSetupInfo {
    PlayerDto blackPlayer;
    PlayerDto whitePlayer;
    GameConfig gameConfig;
}
