package io.github.aylesw.igo.game;

import io.github.aylesw.igo.entity.Game;

import java.util.List;

public interface GameInstance {
    void printBoard();
    boolean play(String coords, int color);
    String generateMove(int color);
    int pass(int color);
    void resign(int color);
    void acceptDraw(int color);
    void timeout(int color);
    List<String> getCaptured();
    void calculateScore();
    GameResult getResult();
    void reset();
    Game toEntity();
}
