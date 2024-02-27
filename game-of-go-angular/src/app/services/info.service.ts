import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Player } from '../common/player';
import { Observable } from 'rxjs';
import appConfig from '../config/app.config';

@Injectable({
  providedIn: 'root'
})
export class InfoService {

  constructor(private httpClient: HttpClient) { }

  getPlayerRankings(): Observable<Player[]> {
    return this.httpClient.get<Player[]>(`${appConfig.apiUrl}/rankings`);
  }
}
