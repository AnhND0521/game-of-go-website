import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import appConfig from '../config/app.config';
import { CookieService } from 'ngx-cookie-service';
import { RxStompService } from './rx-stomp.service';

@Injectable({
  providedIn: 'root'
})
export class AccountService {

  accountUrl: string = `${appConfig.apiUrl}/accounts`

  loggedIn!: BehaviorSubject<boolean>;

  constructor(
    private httpClient: HttpClient, 
    private cookieService: CookieService,
    private stompService: RxStompService) { 
    if (this.getLoginName() && this.getAccessId()) this.loggedIn = new BehaviorSubject(true);
    else this.loggedIn = new BehaviorSubject(false);
  }

  getAccessId(): string {
    return this.cookieService.get('access_id');
  }

  setAccessId(id: string) {
    this.cookieService.set('access_id', id, 365);
  }

  deleteAccessId() {
    this.cookieService.delete('access_id');
  }

  getLoginName(): string {
    return this.cookieService.get('login_name');
  }

  setLoginName(loginName: string) {
    this.cookieService.set('login_name', loginName, 365);
  }

  deleteLoginName() {
    this.cookieService.delete('login_name');
  }

  login(username: string, password: string): Observable<SimpleMessage> {
    return this.httpClient.post<SimpleMessage>(
      `${this.accountUrl}/login`, 
      {
        username: username,
        password: password
      }
    );
  }

  register(username: string, password: string): Observable<SimpleMessage> {
    return this.httpClient.post<SimpleMessage>(
      `${this.accountUrl}/register`,
      {
        username: username,
        password: password
      }
    );
  }

  handleLogin(loginName: string, accessId: string) {
    this.setAccessId(accessId);
    this.setLoginName(loginName);
    this.loggedIn.next(true);
  }

  handleLogout() {
    if (this.getLoginName()) {
      this.stompService.publish({ destination: '/app/end-session', body: this.getLoginName() });
    }
    this.deleteAccessId();
    this.deleteLoginName();
    this.loggedIn.next(false);
  }
}

interface SimpleMessage {
  message: string,
  status: string,
  data: any
}
