package io.github.aylesw.igo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "games")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game {
    @Id
    private String id;
    private Long time;
    private Integer boardSize;
    @DBRef
    private Account blackPlayer;
    @DBRef
    private Account whitePlayer;
    private String log;
    private Double blackScore;
    private Double whiteScore;
    private String blackTerritory;
    private String whiteTerritory;
    private String blackEloChange;
    private String whiteEloChange;
}
