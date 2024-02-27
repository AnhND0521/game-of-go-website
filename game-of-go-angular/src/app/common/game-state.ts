import { GameResult } from "./game-result";

export class GameState {
    gameBoard?: number[];
    log?: string;
    blackCaptures?: number;
    whiteCaptures?: number;
    lastColor?: number;
    lastMove?: string;
    nextColor?: number;
    result?: GameResult;
    chatLog?: string[];
}
