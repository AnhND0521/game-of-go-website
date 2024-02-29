import { Player } from "./player";

export class GameHistory {
    id?: string;
    time?: Date;
    boardSize?: number;
    stoneColor?: number;
    opponent?: Player;
    result?: string;
    blackScore?: number;
    whiteScore?: number;
    eloChange?: number;
}
