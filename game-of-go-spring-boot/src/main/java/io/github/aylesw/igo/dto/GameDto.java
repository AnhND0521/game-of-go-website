package io.github.aylesw.igo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameDto {
    private String id;
    private Long time;
    private Integer boardSize;
    private PlayerDto blackPlayer;
    private PlayerDto whitePlayer;
    private Double blackScore;
    private Double whiteScore;
    private Integer blackEloChange;
    private Integer whiteEloChange;
}
