package io.github.aylesw.igo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Statistics {
    private int totalGames;
    private int wins;
    private int losses;
    private int draws;
    private double winningRate;
    private int elo;
    private String rankType;
    private long ranking;
}
