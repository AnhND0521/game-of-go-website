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
public class GameState {
    int[] gameBoard;
    String log;
    int blackCaptures;
    int whiteCaptures;
    int lastColor;
    String lastMove;
    int nextColor;
    List<String> chatLog;
}
