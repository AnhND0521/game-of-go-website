import { Component, OnInit, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { GameConfig } from 'src/app/common/game-config';
import { AccountService } from 'src/app/services/account.service';
import { RxStompService } from 'src/app/services/rx-stomp.service';

@Component({
  selector: 'app-play-menu',
  templateUrl: './play-menu.component.html',
  styleUrls: ['./play-menu.component.css']
})
export class PlayMenuComponent implements OnInit {

  onlineList: PlayerStatus[] = [];
  loginName: string = '';
  configValues = {
    boardSize: '19',
    customSize: 10,
    komi: 6.5,
    timeControl: 'None',
    ranked: true
  }

  constructor(
    private stompService: RxStompService, 
    private accountService: AccountService,
    private router: Router) {
  }

  ngOnInit(): void {
    this.loginName = this.accountService.getLoginName();
    
    if (this.loginName !== undefined) {
      this.stompService.watch(`/topic/auth/${this.accountService.getAccessId()}`)
      .subscribe((msg) => {
        console.log(msg.body);
        if (msg.body === 'OK') {
          this.stompService.publish({ destination: '/app/online-list', body: this.loginName });
        } else {
          // window.alert("Your session is no longer valid. Please login again.");
          this.router.navigateByUrl('/login');
        }
      });
    }

    this.stompService.watch(`/user/${this.loginName}/queue/online-list`)
    .subscribe((msg) => {
      console.log(msg.body);
      this.handleOnlineList(msg);
    });

    this.stompService.watch(`/topic/online-list`)
    .subscribe((msg) => {
      console.log(msg.body);
      this.handleOnlineList(msg);
    });

    this.stompService.watch(`/user/${this.loginName}/queue/invitation`)
    .subscribe((msg) => {
      console.log(msg.body);
      let payload = JSON.parse(msg.body);
      let gameConfig = payload.gameConfig;
      let accepted: boolean = window.confirm(
        `You've got a challenge from ${payload.sourcePlayer}. Do you want to accept?\n` +
        `Game details:\n` +
        `\n\tBoard size: ${gameConfig.boardSize}x${gameConfig.boardSize}` +
        `\n\tKomi: ${gameConfig.komi}` +
        `\n\tTime control: ${gameConfig.timeControl}` +
        `\n\tRanked: ${gameConfig.ranked ? "Yes" : "No"}`
      );

      payload.reply = accepted ? "ACCEPT" : "DECLINE";
      this.stompService.publish({
        destination: '/app/invitation/reply',
        body: JSON.stringify(payload)
      });
    });

    this.stompService.watch(`/user/${this.loginName}/queue/invitation-reply`)
    .subscribe((msg) => {
      console.log(msg.body);
      let payload = JSON.parse(msg.body);
      if (payload.reply === 'DECLINE') {
        window.alert(`Player ${payload.targetPlayer} declined your challenge!`);
      }
    });

    this.stompService.watch(`/user/${this.loginName}/queue/game/new`)
    .subscribe((msg) => {
      console.log(msg.body);
      this.router.navigate(['/game', msg.body]);
    });

    this.stompService.publish({ destination: '/app/auth', body: JSON.stringify({
      username: this.loginName,
      accessId: this.accountService.getAccessId()
    }) });
  }

  handleOnlineList(msg: any) {
    this.onlineList = JSON.parse(msg.body);
    this.onlineList = this.onlineList.filter(e => e.username !== this.loginName);
  }

  sendInvitation(username: string) {
    this.stompService.publish({
      destination: '/app/invitation/send',
      body: JSON.stringify({
        sourcePlayer: this.loginName,
        targetPlayer: username,
        gameConfig: this.getGameConfig()
      })
    });
  }

  autoMatching() {
    window.alert('Functionality is under development');
  }

  playWithBot() {
    this.sendInvitation('$BOT');
  }

  getGameConfig(): GameConfig {
    let boardSize = +this.configValues.boardSize;
    if (boardSize === -1) boardSize = this.configValues.customSize;
    return {
      boardSize: boardSize,
      komi: this.configValues.komi,
      timeControl: this.configValues.timeControl,
      ranked: this.configValues.ranked
    };
  }

  getStatusIcon(status: string) {
    switch (status) {
      case 'Available':
        return '🟢';
      case 'In game':
        return '🔴';
      default:
        return undefined;
    }
  }
}

interface PlayerStatus {
  username: string,
  status: string
}