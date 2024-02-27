package io.github.aylesw.igo.game;

import io.github.aylesw.igo.dto.PlayerDto;
import io.github.aylesw.igo.entity.Account;
import io.github.aylesw.igo.entity.Game;
import org.modelmapper.ModelMapper;

import java.time.Instant;
import java.util.*;

import static io.github.aylesw.igo.game.GameConstants.*;

public class SimpleGameInstance implements GameInstance {
    private String id;
    private long timestamp;
    private Account blackPlayer;
    private Account whitePlayer;
    private GameConfig config;
    private int[] board;
    private int boardRange;
    private List<Integer> liberties;
    private List<Integer> block;
    private List<Integer> captured;
    private List<Integer> lastCaptured;
    private int lastColor;
    private int lastMove;
    private int nextColor;
    private int blackCaptures;
    private int whiteCaptures;
    private int consecutivePass;
    private StringBuilder log;
    private double blackScore;
    private double whiteScore;
    private List<String> blackTerritory;
    private List<String> whiteTerritory;
    private String endingContext;
    private List<String> chatLog;

    public SimpleGameInstance(Account blackPlayer, Account whitePlayer, GameConfig config) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = Instant.now().getEpochSecond();
        this.blackPlayer = blackPlayer;
        this.whitePlayer = whitePlayer;
        this.config = config;
        this.boardRange = config.getBoardSize() + 2;
        this.liberties = new ArrayList<>();
        this.block = new ArrayList<>();
        this.captured = new ArrayList<>();
        this.lastCaptured = new ArrayList<>();
        this.lastColor = 0;
        this.lastMove = -1;
        this.nextColor = BLACK;
        this.consecutivePass = 0;
        this.log = new StringBuilder();
        this.blackScore = 0;
        this.whiteScore = 0;
        this.blackTerritory = new ArrayList<>();
        this.whiteTerritory = new ArrayList<>();
        this.endingContext = "NORMAL";
        this.chatLog = new ArrayList<>();
        initializeBoard();
    }

    private void initializeBoard() {
        this.board = new int[boardRange * boardRange];
        for (int i = 0; i < boardRange * boardRange; i++) {
            int row = i / boardRange;
            int col = i % boardRange;
            if (row < 1 || row > config.getBoardSize() || col < 1 || col > config.getBoardSize()) {
                board[i] = OFF_BOARD;
            } else {
                board[i] = EMPTY;
            }
        }
    }

    private void count(int pos, int color) {
        int piece = board[pos];

        if (piece == OFF_BOARD) return;

        if (piece > 0 && (piece & color) > 0 && (piece & MARKER) == 0) {
            block.add(pos);
            board[pos] |= MARKER;
            count(pos - boardRange, color);
            count(pos - 1, color);
            count(pos + boardRange, color);
            count(pos + 1, color);
        } else if (piece == EMPTY) {
            board[pos] |= LIBERTY;
            liberties.add(pos);
        }
    }

    private void clearBlock() {
        for (int captured : block) {
            board[captured] = EMPTY;
        }
    }

    private void clearGroups() {
        block.clear();
        liberties.clear();
    }

    private void restoreBoard() {
        clearGroups();
        for (int pos = 0; pos < boardRange * boardRange; pos++) {
            if (board[pos] != OFF_BOARD)
                board[pos] &= 3;
        }
    }

    private void clearBoard() {
        clearGroups();
        for (int pos = 0; pos < boardRange * boardRange; pos++) {
            if (board[pos] != OFF_BOARD)
                board[pos] = EMPTY;
        }
    }

    private void capture(int color) {
        captured.clear();

        for (int pos = 0; pos < boardRange * boardRange; pos++) {
            int piece = board[pos];

            if (piece == OFF_BOARD) continue;

            if ((piece & color) > 0) {
                count(pos, color);
                if (liberties.size() == 0) {
                    captured.addAll(block);
                    clearBlock();
                }
                restoreBoard();
            }
        }
    }

    boolean canMove(int pos, int color) {
        // check if the move can capture any opponent stones
        if (board[pos] != EMPTY) return false;

        board[pos] = color;
        int[] directions = {-1, 1, -boardRange, boardRange};
        boolean capturable = false;

        for (int dir : directions) {
            if (board[pos + dir] != oppositeColor(color)) continue;
            count(pos + dir, oppositeColor(color));
            if (liberties.size() == 0) {
                restoreBoard();
                capturable = true;
                break;
            }
            restoreBoard();
        }

        // check if the position has any liberties
        boolean hasLiberties = true;
        count(pos, color);
        if (liberties.size() == 0) {
            hasLiberties = false;
        }
        restoreBoard();

        if (!capturable && !hasLiberties) {
            board[pos] = EMPTY;
            return false;
        }

        // check if the move breaks Ko rule
        board[pos] = color;
        capture(oppositeColor(color));
        if (captured.size() == 1 && lastCaptured.size() == 1
                && captured.get(0) == lastMove && pos == lastCaptured.get(0)) {
            board[captured.get(0)] = oppositeColor(color);
            board[pos] = EMPTY;
            return false;
        }
        for (int c : captured) {
            board[c] = oppositeColor(color);
        }
        board[pos] = EMPTY;
        return true;
    }

    private int evaluate(int color) {
        int bestCount = 0;
        int bestLiberty = liberties.get(0);

        List<Integer> libertiesCopy = new ArrayList<>(liberties);
        for (int liberty : libertiesCopy) {
            if (libertiesIfPut(liberty, color) > bestCount) {
                bestLiberty = liberty;
                bestCount = liberties.size();
            }
        }

        return bestLiberty;
    }

    private int libertiesIfPut(int pos, int color) {
        board[pos] = color;
        count(pos, color);
        int n = liberties.size();
        restoreBoard();
        board[pos] = EMPTY;
        return n;
    }

    private void determineTerritory(int pos) {
        Set<Integer> black = new HashSet<>();
        Set<Integer> white = new HashSet<>();
        Set<Integer> side = new HashSet<>();
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();

        queue.offer(pos);

        while (!queue.isEmpty()) {
            int p = queue.poll();

            if (board[p] == OFF_BOARD) {
                if (p / boardRange == 0) side.add(1);
                if (p / boardRange == config.getBoardSize() + 1) side.add(2);
                if (p % boardRange == 0) side.add(3);
                if (p % boardRange == config.getBoardSize() + 1) side.add(4);
            } else if (board[p] == BLACK)
                black.add(p);
            else if (board[p] == WHITE)
                white.add(p);
            else {
                if (visited.contains(p)) continue;
                visited.add(p);

                queue.offer(p + 1);
                queue.offer(p - 1);
                queue.offer(p + boardRange);
                queue.offer(p - boardRange);
            }
        }

        int marker;
        if (side.size() == 4 || black.size() == white.size()) {
            marker = MARKER;
        } else if (black.size() > white.size()) {
            marker = MARKER | BLACK;
        } else {
            marker = MARKER | WHITE;
        }

        for (int p : visited) {
            board[p] = marker;
        }
    }

    private boolean isFinished() {
        return nextColor == 0;
    }

    private String toCoords(int pos) {
        if (pos < 0 || pos >= boardRange * boardRange) return null;

        int row = pos / boardRange;
        int col = pos % boardRange;

        char colSymbol = (char) (col - 1 + 'A');
        if (colSymbol >= 'I') colSymbol++;

        return "" + colSymbol + row;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void printBoard() {
        String symbols = ".#o .bw +    ";
        System.out.println();
        for (int row = 0; row < boardRange; row++) {
            for (int col = 0; col < boardRange; col++) {
                int pos = row * boardRange + col;
                System.out.print(symbols.charAt(board[pos]) + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    @Override
    public boolean play(int color, String coords) {
        char colSymbol = coords.charAt(0);
        int col = colSymbol - 'A' + 1;
        if (colSymbol >= 'J') col--;
        int row = Integer.parseInt(coords.substring(1));
        int pos = row * boardRange + col;

        if (canMove(pos, color)) {
            consecutivePass = 0;
            board[pos] = color;
            capture(oppositeColor(color));
            printBoard();
            lastColor = color;
            lastMove = pos;
            lastCaptured.clear();
            lastCaptured.addAll(captured);
            if (color == BLACK)
                blackCaptures += lastCaptured.size();
            else
                whiteCaptures += lastCaptured.size();

            log.append(color).append('+').append(toCoords(lastMove));
            if (lastCaptured.size() > 0) {
                log.append('/').append(oppositeColor(color)).append('-');
                for (int i = 0; i < lastCaptured.size(); i++) {
                    if (i > 0) log.append(',');
                    log.append(toCoords(lastCaptured.get(i)));
                }
            }
            log.append(' ');
            nextColor = oppositeColor(color);
            return true;
        }
        return false;
    }

    private String randomMove(int color) {
        Random random = new Random();
        int pos;
        String coords;
        int maxAttempts = 50;
        int count = 0;

        do {
            pos = random.nextInt(boardRange * boardRange);
            coords = toCoords(pos);
            count++;
        } while (count < maxAttempts && !play(color, coords));

        if (count == maxAttempts) {
            return "PA";
        }

        return coords;
    }

    @Override
    public String generateMove(int color) {
        if (lastMove == -1) {
            return randomMove(color);
        }

        int bestMove = 0;
        int capture = 0;
        int save = 0;
        int defend = 0;
        int surround = 0;
        int pattern = 0;

        List<Integer> candidates = new ArrayList<>();
        Random random = new Random();

        // capture opponent's group
        for (int pos = 0; pos < boardRange * boardRange; pos++) {
            int piece = board[pos];
            if ((piece & (oppositeColor(color))) > 0) {
                count(pos, oppositeColor(color));
                if (liberties.size() == 1) {
                    int targetPos = liberties.get(0);
                    restoreBoard();
                    if (canMove(targetPos, color)) {
                        bestMove = targetPos;
                        candidates.add(targetPos);
                        break;
                    }
                }
                restoreBoard();
            }
        }
        if (candidates.size() > 0) {
            capture = candidates.get(random.nextInt(candidates.size()));
            candidates.clear();
        }

        // save own group
        for (int pos = 0; pos < boardRange * boardRange; pos++) {
            int piece = board[pos];
            if ((piece & color) > 0) {
                count(pos, color);
                if (liberties.size() == 1) {
                    int targetPos = liberties.get(0);
                    restoreBoard();
                    if (canMove(targetPos, color) && libertiesIfPut(targetPos, color) > 1) {
                        bestMove = targetPos;
                        candidates.add(targetPos);
                        break;
                    }
                }
                restoreBoard();
            }
        }
        if (candidates.size() > 0) {
            save = candidates.get(random.nextInt(candidates.size()));
            candidates.clear();
        }

        // defend own group
        for (int pos = 0; pos < boardRange * boardRange; pos++) {
            int piece = board[pos];
            if ((piece & color) > 0) {
                count(pos, color);
                if (liberties.size() == 2) {
                    int bestLiberty = evaluate(color);
                    restoreBoard();
                    if (canMove(bestLiberty, color) && libertiesIfPut(bestLiberty, color) > 1) {
                        bestMove = bestLiberty;
                        candidates.add(bestLiberty);
                        break;
                    }
                }
                restoreBoard();
            }
        }

        if (candidates.size() > 0) {
            defend = candidates.get(random.nextInt(candidates.size()));
            candidates.clear();
        }

        // surround opponent's group
        for (int pos = 0; pos < boardRange * boardRange; pos++) {
            int piece = board[pos];
            if ((piece & (oppositeColor(color))) > 0) {
                count(pos, oppositeColor(color));
                if (liberties.size() > 1) {
                    int bestLiberty = evaluate(oppositeColor(color));
                    restoreBoard();
                    if (canMove(bestLiberty, color) && libertiesIfPut(bestLiberty, color) > 1) {
                        bestMove = bestLiberty;
                        candidates.add(bestLiberty);
                        break;
                    }
                }
                restoreBoard();
            }
        }
        if (candidates.size() > 0) {
            surround = candidates.get(random.nextInt(candidates.size()));
            candidates.clear();
        }

        // pattern matching
        int targetOne, targetTwo;
        for (int pos = 0; pos < boardRange * boardRange; pos++) {
            int piece = board[pos];
            if (piece == OFF_BOARD) continue;
            if ((piece & (oppositeColor(color))) > 0) {
                targetOne = pos - boardRange + 1;
                targetTwo = pos - boardRange - 1;
                if ((board[targetOne] & color) > 0 && (board[targetTwo] & color) > 0 && canMove(pos - boardRange, color)) {
                    bestMove = pos - boardRange;
                    candidates.add(bestMove);
                }

                targetOne = pos + 1;
                targetTwo = pos - boardRange - 1;
                if ((board[targetOne] & color) > 0 && (board[targetTwo] & color) > 0 && canMove(pos - boardRange, color)) {
                    bestMove = pos - boardRange;
                    candidates.add(bestMove);
                }

                targetOne = pos + 1;
                targetTwo = pos - 1;
                if ((board[targetOne] & color) > 0 && (board[targetTwo] & color) > 0 && canMove(pos + boardRange, color)) {
                    bestMove = pos + boardRange;
                    candidates.add(bestMove);
                }

                targetOne = pos - boardRange + 2;
                targetTwo = pos - boardRange - 1;
                if ((board[targetOne] & color) > 0 && (board[targetTwo] & color) > 0 && canMove(pos - boardRange, color)) {
                    bestMove = pos - boardRange;
                    candidates.add(bestMove);
                }

                targetOne = pos - boardRange + 2;
                targetTwo = pos - boardRange - 2;
                if ((board[targetOne] & color) > 0 && (board[targetTwo] & color) > 0 && canMove(pos - boardRange, color)) {
                    bestMove = pos - boardRange;
                    candidates.add(bestMove);
                }

                targetOne = pos - 1;
                targetTwo = pos + boardRange - 2;
                if ((board[targetOne] & color) > 0 && (board[targetTwo] & color) > 0 && canMove(pos + boardRange, color)) {
                    bestMove = pos + boardRange;
                    candidates.add(bestMove);
                }

                targetOne = pos - boardRange;
                targetTwo = pos - boardRange - 2;
                if ((board[targetOne] & color) > 0 && (board[targetTwo] & color) > 0 && canMove(pos - 1, color)) {
                    bestMove = pos - 1;
                    candidates.add(bestMove);
                }

                if (candidates.size() > 0) {
                    pattern = candidates.get(random.nextInt(candidates.size()));
                    candidates.clear();
                }
            }
        }

        if (bestMove > 0) {
            System.out.println("capture move: " + toCoords(capture));
            System.out.println("save move: " + toCoords(save));
            System.out.println("defend move: " + toCoords(defend));
            System.out.println("surround move: " + toCoords(surround));
            System.out.println("pattern move: " + toCoords(pattern));

            if (save > 0) bestMove = save;
            else if (capture > 0) bestMove = capture;
            else {
                candidates.clear();
                if (defend > 0) candidates.add(defend);
                if (surround > 0) candidates.add(surround);
                if (pattern > 0) candidates.add(pattern);

                bestMove = candidates.get(random.nextInt(candidates.size()));
            }

            String coords = toCoords(bestMove);
            System.out.println("chosen move: " + coords);
            play(color, coords);
            return coords;
        } else {
            int rd = random.nextInt(4);
            String move = (rd == 0) ? "PA" : randomMove(color);
            System.out.println("move: " + move);
            return move;
        }
    }

    @Override
    public int pass(int color) {
        consecutivePass++;
        log.append(color).append("=PA ");
        lastColor = color;
        lastMove = -1;
        if (consecutivePass == 2) nextColor = 0;
        else nextColor = oppositeColor(color);
        return consecutivePass;
    }

    @Override
    public void resign(int color) {
        calculateScore();
        if (color == BLACK)
            blackScore = -1;
        else
            whiteScore = -1;
        log.append(color).append("=RS ");
        lastColor = color;
        lastMove = -1;
        endingContext = "RESIGNED";
        nextColor = 0;
    }

    @Override
    public void acceptDraw(int color) {
        calculateScore();
        blackScore = whiteScore = -1;
        log.append(color).append("=DR ");
        lastColor = color;
        lastMove = -1;
        endingContext = "DRAWN";
        nextColor = 0;
    }

    @Override
    public void timeout(int color) {
        calculateScore();
        if (color == BLACK)
            blackScore = -1;
        else
            whiteScore = -1;
        log.append(color).append("=TO ");
        lastColor = color;
        lastMove = -1;
        endingContext = "TIMEOUT";
        nextColor = 0;
    }

    @Override
    public void leave(int color) {
        calculateScore();
        if (color == BLACK)
            blackScore = -1;
        else
            whiteScore = -1;
        log.append(color).append("=LV ");
        lastColor = color;
        lastMove = -1;
        endingContext = "LEFT";
        nextColor = 0;
    }

    @Override
    public List<String> getCaptured() {
        return captured.stream().map(this::toCoords).toList();
    }

    @Override
    public void calculateScore() {
        for (int pos = 0; pos < boardRange * boardRange; pos++) {
            if (board[pos] == EMPTY) {
                determineTerritory(pos);
            }
        }

        printBoard();

        blackScore = 0.0;
        whiteScore = config.getKomi();
        blackTerritory.clear();
        whiteTerritory.clear();

        for (int pos = 0; pos < boardRange * boardRange; pos++) {
            if (board[pos] == (MARKER | BLACK)) {
                blackScore += 1;
                blackTerritory.add(toCoords(pos));
                board[pos] = EMPTY;
            } else if (board[pos] == (MARKER | WHITE)) {
                whiteScore += 1;
                whiteTerritory.add(toCoords(pos));
                board[pos] = EMPTY;
            } else if (board[pos] == MARKER) {
                board[pos] = EMPTY;
            }
        }

        blackScore += blackCaptures;
        whiteScore += whiteCaptures;
    }

    @Override
    public GameResult getResult() {
        int winner = 0;
        if (blackScore > whiteScore) winner = BLACK;
        else if (blackScore < whiteScore) winner = WHITE;
        return GameResult.builder()
                .winner(winner)
                .endingContext(endingContext)
                .blackScore(blackScore)
                .whiteScore(whiteScore)
                .blackTerritory(blackTerritory)
                .whiteTerritory(whiteTerritory)
                .build();
    }

    @Override
    public void reset() {
        timestamp = Instant.now().getEpochSecond();
        log = new StringBuilder();
        blackCaptures = whiteCaptures = 0;
        lastColor = 0;
        lastMove = -1;
        nextColor = BLACK;
        chatLog.clear();
        clearBoard();
    }

    @Override
    public Game toEntity() {
        return Game.builder()
                .id(id)
                .time(timestamp)
                .boardSize(config.getBoardSize())
                .blackPlayer(blackPlayer)
                .whitePlayer(whitePlayer)
                .log(log.toString())
                .blackScore(blackScore)
                .whiteScore(whiteScore)
                .blackTerritory(String.join(" ", blackTerritory))
                .whiteTerritory(String.join(" ", whiteTerritory))
                .build();
    }

    @Override
    public GameInfo getInfo() {
        ModelMapper mapper = new ModelMapper();
        return GameInfo.builder()
                .gameId(id)
                .blackPlayer(mapper.map(blackPlayer, PlayerDto.class))
                .whitePlayer(mapper.map(whitePlayer, PlayerDto.class))
                .gameConfig(config)
                .gameState(getState())
                .gameResult(isFinished() ? getResult() : null)
                .build();
    }

    @Override
    public GameState getState() {
        return GameState.builder()
                .gameBoard(board)
                .blackCaptures(blackCaptures)
                .whiteCaptures(whiteCaptures)
                .log(log.toString().trim())
                .lastColor(lastColor)
                .lastMove(toCoords(lastMove))
                .nextColor(nextColor)
                .chatLog(chatLog)
                .build();
    }

    @Override
    public void addChat(String chatMessage) {
        chatLog.add(chatMessage);
    }
}
