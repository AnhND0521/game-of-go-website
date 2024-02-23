package io.github.aylesw.igo.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameResult {
    private int winner;
    private String endingContext;
    private double blackScore;
    private double whiteScore;
    private List<String> blackTerritory;
    private List<String> whiteTerritory;
}
