package io.github.aylesw.igo.game;

import io.github.aylesw.igo.entity.Game;

import java.util.List;

public interface GameInstance {
    String getId();
    void printBoard();
    boolean play(int color, String coords);
    String generateMove(int color);
    int pass(int color);
    void resign(int color);
    void acceptDraw(int color);
    void timeout(int color);
    void leave(int color);
    List<String> getCaptured();
    void calculateScore();
    GameResult getResult();
    void reset();
    Game toEntity();
    GameInfo getInfo();
    GameState getState();
    void addChat(String chatMessage);
}
