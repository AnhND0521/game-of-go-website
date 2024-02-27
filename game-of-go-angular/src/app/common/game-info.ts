import { GameConfig } from "./game-config";
import { GameResult } from "./game-result";
import { GameState } from "./game-state";
import { Player } from "./player";

export class GameInfo {
    gameId?: string;
    blackPlayer?: Player;
    whitePlayer?: Player;
    gameConfig?: GameConfig;
    gameState?: GameState;
    gameResult?: GameResult;
}
