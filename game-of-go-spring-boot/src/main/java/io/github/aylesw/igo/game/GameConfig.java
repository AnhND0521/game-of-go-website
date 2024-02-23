package io.github.aylesw.igo.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameConfig {
    private int boardSize;
    private double komi;
    private String timeControl;
    private boolean ranked;
}
