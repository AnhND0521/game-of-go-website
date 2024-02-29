package io.github.aylesw.igo.service.impl;

import io.github.aylesw.igo.dto.*;
import io.github.aylesw.igo.entity.Account;
import io.github.aylesw.igo.entity.Game;
import io.github.aylesw.igo.game.GameInfo;
import io.github.aylesw.igo.game.GameInstance;
import io.github.aylesw.igo.game.SimpleGameInstance;
import io.github.aylesw.igo.repository.AccountRepository;
import io.github.aylesw.igo.repository.GameRepository;
import io.github.aylesw.igo.service.GameService;
import io.github.aylesw.igo.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static io.github.aylesw.igo.game.GameConstants.*;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {
    private final AccountRepository accountRepository;
    private final GameRepository gameRepository;
    private final StatusService statusService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ModelMapper mapper;
    private final Map<String, GameInstance> games = new HashMap<>();

    private boolean isBot(Account player) {
        return player.getUsername().equals("$BOT");
    }

    @Override
    public void setupGame(InviteMessage message) {
        Account player1 = accountRepository.findByUsername(message.getSourcePlayer());
        Account player2 = accountRepository.findByUsername(message.getTargetPlayer());
        Account blackPlayer, whitePlayer;

        Random random = new Random();
        int randomResult = random.nextInt(2);
        if (randomResult == 0) {
            blackPlayer = player1;
            whitePlayer = player2;
        } else {
            blackPlayer = player2;
            whitePlayer = player1;
        }

        GameInstance game = new SimpleGameInstance(blackPlayer, whitePlayer, message.getGameConfig());
        games.put(game.getId(), game);

        SessionData data1 = statusService.getSessionData(player1.getUsername());
        data1.setOpponent(player2);
        data1.setGame(game);
        data1.setStatus("In game");
        statusService.broadcastOnlinePlayerList();
        messagingTemplate.convertAndSend("/user/" + player1.getUsername() + "/queue/game/new", game.getId());

        if (!isBot(player2)) {
            SessionData data2 = statusService.getSessionData(player2.getUsername());
            data2.setOpponent(player1);
            data2.setGame(game);
            data2.setStatus("In game");
            statusService.broadcastOnlinePlayerList();
            messagingTemplate.convertAndSend("/user/" + player2.getUsername() + "/queue/game/new", game.getId());
        } else {
            if (player2.equals(blackPlayer)) {
                sendBotMove(game, BLACK, player1.getUsername());
            }
        }
    }

    @Override
    public GameInfo getGameInfo(String gameId) {
        var game = games.get(gameId);
        if (game == null) return null;
        return game.getInfo();
    }

    @Override
    public void move(MoveMessage message) {
        var data = statusService.getSessionData(message.getUsername());
        var game = data.getGame();
        var opponent = data.getOpponent();

        if (!message.getMove().equals("PA")) {
            if (game.play(message.getColor(), message.getMove())) {
                var m = MoveResultMessage.builder()
                        .color(message.getColor())
                        .move(message.getMove())
                        .captured(game.getCaptured())
                        .build();
                messagingTemplate.convertAndSend("/user/" + message.getUsername() + "/queue/game/move", m);
                if (!isBot(opponent)) {
                    messagingTemplate.convertAndSend("/user/" + opponent.getUsername() + "/queue/game/move", m);
                } else {
                    sendBotMove(game, oppositeColor(message.getColor()), message.getUsername());
                }
            } else {
                messagingTemplate.convertAndSend("/user/" + message.getUsername() + "/queue/game/move/error", "Invalid move");
            }
        } else {
            if (game.pass(message.getColor()) == 2) {
                game.calculateScore();
                endGame(game);
            } else {
                if (isBot(opponent)) {
                    game.calculateScore();
                    var currentResult = game.getResult();
                    if ((message.getColor() == BLACK && currentResult.getBlackScore() < currentResult.getWhiteScore())
                            || (message.getColor() == WHITE && currentResult.getBlackScore() > currentResult.getWhiteScore())) {
                        game.pass(oppositeColor(message.getColor()));
                        var m = MoveResultMessage.builder()
                                .color(oppositeColor(message.getColor()))
                                .move("PA")
                                .build();
                        messagingTemplate.convertAndSend("/user/" + message.getUsername() + "/queue/game/move", m);
                        endGame(game);
                    } else {
                        sendBotMove(game, oppositeColor(message.getColor()), message.getUsername());
                    }
                } else {
                    var m = MoveResultMessage.builder()
                            .color(message.getColor())
                            .move(message.getMove())
                            .build();
                    messagingTemplate.convertAndSend("/user/" + opponent.getUsername() + "/queue/game/move", m);
                }
            }
        }
    }

    @Override
    public void requestInterrupt(InterruptMessage message) {
        var data = statusService.getSessionData(message.getUsername());
        var game = data.getGame();
        var opponent = data.getOpponent();

        if (message.getRequestType().equals("RESIGN")) {
            if (!isBot(data.getOpponent())) {
                messagingTemplate.convertAndSend("/user/" + opponent.getUsername() + "/queue/game/interrupt/request", message);
            }
            game.resign(message.getColor());
            endGame(game);
            return;
        }

        if (message.getRequestType().equals("DRAW")) {
            if (isBot(opponent)) {
                message.setReply("ACCEPT");
                messagingTemplate.convertAndSend("/user/" + message.getUsername() + "/queue/game/interrupt/reply", message);
                game.acceptDraw(oppositeColor(message.getColor()));
                game.getInfo().getGameConfig().setRanked(false);
                endGame(game);
            } else {
                messagingTemplate.convertAndSend("/user/" + opponent.getUsername() + "/queue/game/interrupt/request", message);
            }
            return;
        }

        if (message.getRequestType().equals("RESTART")) {
            if (isBot(opponent)) {
                game.reset();
                messagingTemplate.convertAndSend("/user/" + message.getUsername() + "/queue/game/info", game.getInfo());
                if (opponent.getUsername().equals(game.getInfo().getBlackPlayer().getUsername())) {
                    sendBotMove(game, BLACK, message.getUsername());
                }
            } else {
                var opponentData = statusService.getSessionData(opponent.getUsername());
                if (opponentData == null || opponentData.getGame() != game) {
                    message.setReply("LEFT");
                    messagingTemplate.convertAndSend("/user/" + message.getUsername() + "/queue/game/interrupt/reply", message);
                } else {
                    messagingTemplate.convertAndSend("/user/" + opponent.getUsername() + "/queue/game/interrupt/request", message);
                }
            }
            return;
        }

        if (message.getRequestType().equals("LEAVE")) {
            if (!isBot(data.getOpponent())) {
                messagingTemplate.convertAndSend("/user/" + opponent.getUsername() + "/queue/game/interrupt/request", message);
            }
            game.leave(message.getColor());
            endGame(game);
            return;
        }
    }

    @Override
    public void replyToInterrupt(InterruptMessage message) {
        var data = statusService.getSessionData(message.getUsername());
        var game = data.getGame();
        var self = data.getOpponent();

        messagingTemplate.convertAndSend("/user/" + message.getUsername() + "/queue/game/interrupt/reply", message);

        if (message.getRequestType().equals("DRAW")) {
            if (message.getReply().equals("ACCEPT")) {
                game.acceptDraw(oppositeColor(message.getColor()));
                game.getInfo().getGameConfig().setRanked(false);
                endGame(game);
            }
            return;
        }

        if (message.getRequestType().equals("RESTART")) {
            if (message.getReply().equals("ACCEPT")) {
                game.reset();
                messagingTemplate.convertAndSend("/user/" + message.getUsername() + "/queue/game/info", game.getInfo());
                messagingTemplate.convertAndSend("/user/" + self.getUsername() + "/queue/game/info", game.getInfo());
            }
            return;
        }
    }

    @Override
    public void handlePlayerLeave(String username) {
        var data = statusService.getSessionData(username);
        if (data != null && data.getGame() != null) {
            var game = data.getGame();
            int color = username.equals(data.getGame().getInfo().getBlackPlayer().getUsername()) ? BLACK : WHITE;
            var message = InterruptMessage.builder()
                    .username(username)
                    .color(color)
                    .requestType("LEAVE")
                    .build();
            messagingTemplate.convertAndSend("/user/" + data.getOpponent().getUsername() + "/queue/game/interrupt/request", message);
            game.leave(color);
            endGame(game);
        }
    }

    private void sendBotMove(GameInstance game, int color, String targetUsername) {
        String move = game.generateMove(color);
        var m = MoveResultMessage.builder()
                .color(color)
                .move(move)
                .captured(game.getCaptured())
                .build();
        messagingTemplate.convertAndSend("/user/" + targetUsername + "/queue/game/move", m);

        if (move.equals("PA") && game.pass(color) == 2) {
            game.calculateScore();
            endGame(game);
        }
    }

    private void endGame(GameInstance game) {
        var result = game.getResult();
        var gameEntity = game.toEntity();
        if (game.getInfo().getGameConfig().isRanked()) {
            updatePlayerRankings(gameEntity);
            result.setBlackEloChange(gameEntity.getBlackEloChange());
            result.setWhiteEloChange(gameEntity.getWhiteEloChange());
        }
        gameRepository.save(gameEntity);

        var blackPlayer = gameEntity.getBlackPlayer();
        var whitePlayer = gameEntity.getWhitePlayer();
        if (!isBot(blackPlayer)) {
            messagingTemplate.convertAndSend("/user/" + blackPlayer.getUsername() + "/queue/game/result", result);
            var data = statusService.getSessionData(blackPlayer.getUsername());
            if (data != null) {
                if (data.getStatus().equals("In game")) data.setStatus("Available");
                data.setGame(null);
                data.setOpponent(null);
            }
        }
        if (!isBot(whitePlayer)) {
            messagingTemplate.convertAndSend("/user/" + whitePlayer.getUsername() + "/queue/game/result", result);
            var data = statusService.getSessionData(whitePlayer.getUsername());
            if (data != null) {
                if (data.getStatus().equals("In game")) data.setStatus("Available");
                data.setGame(null);
                data.setOpponent(null);
            }
        }
        statusService.broadcastOnlinePlayerList();
    }

    private void updatePlayerRankings(Game game) {
        Account blackPlayer = game.getBlackPlayer();
        Account whitePlayer = game.getWhitePlayer();
        double result1 = (game.getBlackScore() > game.getWhiteScore()) ? 1 :
                (game.getBlackScore().equals(game.getWhiteScore())) ? 0.5 : 0;
        double result2 = 1 - result1;
        int change1 = calculateEloChange(result1, blackPlayer.getElo(), whitePlayer.getElo());
        int change2 = calculateEloChange(result2, whitePlayer.getElo(), blackPlayer.getElo());
        blackPlayer.setElo(blackPlayer.getElo() + change1);
        blackPlayer.setRankType(calculateRankType(blackPlayer.getElo()));
        whitePlayer.setElo(whitePlayer.getElo() + change2);
        whitePlayer.setRankType(calculateRankType(whitePlayer.getElo()));
        game.setBlackEloChange(change1);
        game.setWhiteEloChange(change2);
        accountRepository.save(blackPlayer);
        accountRepository.save(whitePlayer);
    }

    private int calculateEloChange(double result, int selfElo, int opponentElo) {
        double winningChance = 1.0 / (1.0 + Math.exp((opponentElo - selfElo) / 110.0));
        double K = 1561.0 / 13.0 - 53.0 / 1300.0 * selfElo;
        if (K < 10) K = 10;
        int change = (int) Math.round(K * (result - winningChance));
        System.out.printf("new elo: %d + %.1f * (%.1f - %.3f) = %d\n", selfElo, K, result, winningChance, selfElo + change);
        return change;
    }

    private String calculateRankType(int elo) {
        if (elo < 100) {
            return "Unranked";
        }
        if (elo < 2100) {
            int kyu = (2099 - elo) / 100 + 1;
            return kyu + "K";
        }
        if (elo < 2700) {
            int dan = (elo - 2000) / 100;
            return dan + "D";
        }
        if (elo < 2940) {
            int dan = (elo - 2700) / 30 + 1;
            return dan + "P";
        }
        return "9P";
    }
}
