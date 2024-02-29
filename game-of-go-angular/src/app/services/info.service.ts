import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Player } from '../common/player';
import { Observable, map } from 'rxjs';
import appConfig from '../config/app.config';
import { GameHistory } from '../common/game-history';
import { Statistics } from '../common/statistics';

@Injectable({
  providedIn: 'root'
})
export class InfoService {

  constructor(private httpClient: HttpClient) { }

  getPlayerRankings(): Observable<Player[]> {
    return this.httpClient.get<Player[]>(`${appConfig.apiUrl}/rankings`);
  }

  getStatistics(username: string): Observable<Statistics> {
    return this.httpClient.get<Statistics>(`${appConfig.apiUrl}/players/${username}/statistics`);
  }

  getHistory(username: string): Observable<GameHistory[]> {
    return this.httpClient.get<any[]>(`${appConfig.apiUrl}/players/${username}/history`)
    .pipe(
      map((response: any[]) => response.map(data => {
        const stoneColor = (username === data.blackPlayer.username) ? 1 : 2;
        const opponent = (stoneColor === 1) ? data.whitePlayer : data.blackPlayer;
        const result = (stoneColor === 1 && data.blackScore > data.whiteScore) || (stoneColor === 2 && data.whiteScore > data.blackScore) ? 'victory' :
                       (data.blackScore === data.whiteScore) ? 'draw' : 'defeat';
        const eloChange = (stoneColor === 1) ? data.blackEloChange : data.whiteEloChange;

        const gameHistory: GameHistory = {
          id: data.id,
          time: new Date(data.time * 1000),
          boardSize: data.boardSize,
          stoneColor: stoneColor,
          opponent: opponent,
          result: result,
          blackScore: data.blackScore,
          whiteScore: data.whiteScore,
          eloChange: eloChange
        };
        return gameHistory;
      }))
    );
  }
}
