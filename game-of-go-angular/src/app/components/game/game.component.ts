import { AfterViewInit, Component, ElementRef, HostListener, Input, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { GameInfo } from 'src/app/common/game-info';
import { AccountService } from 'src/app/services/account.service';
import { RxStompService } from 'src/app/services/rx-stomp.service';
import { GameBoardComponent } from '../game-board/game-board.component';
import { InfoTableComponent } from '../info-table/info-table.component';
import { LogTableComponent } from '../log-table/log-table.component';
import { GameState } from 'src/app/common/game-state';
import { GameResult } from 'src/app/common/game-result';

const BLACK = 1;
const WHITE = 2;
const oppositeColor = (color: number) => 3 - color;

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.css']
})
export class GameComponent implements OnInit, AfterViewInit {
  @ViewChild(GameBoardComponent) gameBoard!: GameBoardComponent;
  @ViewChild(InfoTableComponent) infoTable!: InfoTableComponent;
  @ViewChild(LogTableComponent) logTable!: LogTableComponent;
  id: string = '';
  username: string = '';
  gameInfo!: GameInfo;
  captures: number[] = [0, 0, 0];
  myColor: number = 0;
  myTurn: boolean = false;
  prompt: string = '';
  lastColor: number = 0;
  lastCoords: string = '';
  gameFinished: boolean = false;

  constructor(
    private route: ActivatedRoute, 
    private router: Router,
    private stompService: RxStompService,
    private accountService: AccountService) {}

  ngOnInit(): void {
    this.username = this.accountService.getLoginName();
    this.route.paramMap.subscribe(params => {
      this.id = params.get('id')!;
      this.setup();
    });
  }

  ngAfterViewInit(): void {
    this.setState();
  }

  setup() {
    this.stompService.watch(`/user/${this.username}/queue/game/info`)
    .subscribe(msg => {
      this.gameInfo = JSON.parse(msg.body);
      console.log(this.gameInfo);
      if (this.gameBoard) this.setState();
    });

    this.stompService.publish({
      destination: '/app/game/info',
      body: JSON.stringify({
        username: this.username,
        gameId: this.id
      })
    });

    this.stompService.watch(`/user/${this.username}/queue/game/move`)
    .subscribe(msg => {
      console.log(msg.body);
      let payload = JSON.parse(msg.body);

      this.logTable.addRow(payload.color, payload.move);
      if (this.lastCoords !== '') {
        this.gameBoard.drawStone(this.lastColor, this.lastCoords, false);
      }

      if (payload.move !== 'PA') {
        this.gameBoard.drawStone(payload.color, payload.move, true);
        this.lastColor = payload.color;
        this.lastCoords = payload.move;
        if (payload.captured) {
          this.gameBoard.clearBoardAt(payload.captured);
          this.captures[payload.color] += payload.captured.length;
        }
        this.myTurn = !this.myTurn;
        this.prompt = (payload.color === BLACK) ? `White's turn` : `Black's turn`;
      } else {
        this.lastColor = payload.color;
        this.lastCoords = '';
        this.myTurn = true;
        this.prompt = (payload.color == BLACK) ? `Black passes. White's turn` : `White passes. Black's turn`;
      }
    });

    this.stompService.watch(`/user/${this.username}/queue/game/move/error`)
    .subscribe(msg => {
      console.log(msg.body);
      this.prompt = msg.body;
    });

    this.stompService.watch(`/user/${this.username}/queue/game/result`)
    .subscribe(msg => {
      console.log(msg.body);

      this.gameFinished = true;
      if (this.lastCoords !== '') {
        this.gameBoard.drawStone(this.lastColor, this.lastCoords, false);
      }
      this.lastColor = 0;
      this.lastCoords = '';
      this.myTurn = false;

      let result: GameResult = JSON.parse(msg.body);
      this.renderResult(result);
    });

    this.stompService.watch(`/user/${this.username}/queue/game/interrupt/request`)
    .subscribe(msg => {
      console.log(msg.body);
      let payload = JSON.parse(msg.body);
      switch (payload.requestType) {
        case 'RESIGN': {
          this.prompt = (payload.color === BLACK) ? `Black resigned. White won!` : `White resigned. Black won!`;
          this.logTable.addRow(payload.color, "RS");
          break;
        }
        case 'DRAW': {
          if (window.confirm(`Your opponent would like to offer a draw. Do you agree?`)) {
            payload.reply = 'ACCEPT';
            this.prompt = `Match drawn by agreement`;
            this.logTable.addRow(this.myColor, 'DR');
          } else {
            payload.reply = 'DECLINE';
          }
          this.stompService.publish({
            destination: '/app/game/interrupt/reply',
            body: JSON.stringify(payload)
          });
          break;
        }
        case 'RESTART': {
          if (window.confirm(`Your opponent would like to restart this game. Do you agree?`)) {
            payload.reply = 'ACCEPT';
          } else {
            payload.reply = 'DECLINE';
          }
          this.stompService.publish({
            destination: '/app/game/interrupt/reply',
            body: JSON.stringify(payload)
          });
          break;
        }
        case 'TIME_OUT': {
          this.prompt = (payload.color === BLACK) ? `Black ran out of time. White won!` : `White ran out of time. Black won!`;
          this.logTable.addRow(payload.color, 'TO');
          break;
        }
        case 'LEAVE': {
          this.prompt = (payload.color === BLACK) ? `Black has left the game. White won!` : `White has left the game. Black won!`;
          this.logTable.addRow(payload.color, "RS");
          break;
        }
      }
    });

    this.stompService.watch(`/user/${this.username}/queue/game/interrupt/reply`)
    .subscribe(msg => {
      console.log(msg.body);
      let payload = JSON.parse(msg.body);
      switch (payload.requestType) {
        case 'DRAW': {
          if (payload.reply === 'ACCEPT') {
            this.prompt = `Match drawn by agreement`;
            this.logTable.addRow(oppositeColor(payload.color), 'DR');
          } else {
            window.alert(`Your opponent has declined your draw request.`);
          }
          break;
        }
        case 'RESTART': {
          if (payload.reply === 'DECLINE') {
            window.alert(`Your opponent has rejected to restart this game.`);
          } else if (payload.reply === 'LEFT') {
            window.alert(`Your opponent has left the game.`);
          }
        }
      }
    });
  }

  setState() {
    const state: GameState = this.gameInfo.gameState!;
    this.myColor = (this.username === this.gameInfo.blackPlayer?.username) ? BLACK : WHITE;
    this.myTurn = (state.nextColor === this.myColor);
    if (state.log!.length === 0)
      this.prompt = `You are ${this.myColor === 1 ? 'black' : 'white'}. Black's turn`;
    else
      this.prompt = state.nextColor === BLACK ? `Black's turn` : `White's turn`;

    this.captures[BLACK] = state.blackCaptures!;
    this.captures[WHITE] = state.whiteCaptures!;

    this.gameBoard.reset();
    for (let i = 0; i < state.gameBoard!.length; i++) {
      let coords = this.indexToCoords(i);
      switch (state.gameBoard![i]) {
        case 1:
          this.gameBoard.drawStone(BLACK, coords);
          break;
        case 2:
          this.gameBoard.drawStone(WHITE, coords);
          break;
      }
    }

    this.logTable.reset();
    if (state.log!.length > 0) {
      let logItems: string[] = state.log!.split(' ');
      logItems.forEach(item => {
        console.log(item);
        let move = item.split('/')[0];
        let color = +move.charAt(0);
        let coords = move.substring(2);
        this.logTable.addRow(color, coords);
      });
    }

    if (state.lastColor && state.lastMove) {
      this.gameBoard.drawStone(state.lastColor, state.lastMove, true);
      this.lastColor = state.lastColor;
      this.lastCoords = state.lastMove;
    } else {
      this.lastColor = 0;
      this.lastCoords = '';
    }

    if (this.gameInfo.gameResult) {
      this.renderResult(this.gameInfo.gameResult!);
    }
  }

  renderResult(result: GameResult) {
    this.gameBoard.drawTerritory(BLACK, result.blackTerritory!);
      this.gameBoard.drawTerritory(WHITE, result.whiteTerritory!);

      switch (result.endingContext) {
        case 'NORMAL': {
          this.prompt = `Black ${result.blackScore} : ${result.whiteScore} White. `;
          if (result.winner! > 0) 
            this.prompt += `${result.winner === BLACK ? 'Black' : 'White'} won!`;
          else
            this.prompt += `Tie game!`;
          break;
        }
        case 'RESIGNED': {
          this.prompt = `${result.winner === BLACK ? 'White' : 'Black'} resigned. ${result.winner === BLACK ? 'Black' : 'White'} won!`;
          break;
        }
        case 'DRAWN': {
          this.prompt = `Match drawn by agreement.`;
          break;
        }
        case 'TIMEOUT': {
          this.prompt = `${result.winner === BLACK ? 'White' : 'Black'} ran out of time. ${result.winner === BLACK ? 'Black' : 'White'} won!`;
          break;
        }
        case 'LEFT': {
          this.prompt = `${result.winner === BLACK ? 'White' : 'Black'} left the game. ${result.winner === BLACK ? 'Black' : 'White'} won!`;
          break;
        }
      }
  }

  indexToCoords(index: number): string {
    const boardRange = this.gameInfo.gameConfig!.boardSize! + 2;
    let row = Math.floor(index / boardRange);
    let col = index % boardRange - 1;

    if (col >= 8) col++;
    let colSymbol = String.fromCharCode(65 + col);

    let coords = `${colSymbol}${row}`;
    return coords;
  }

  move(coords: string) {
    console.log(`move ${coords}`);
    this.stompService.publish({
      destination: '/app/game/move',
      body: JSON.stringify({
        username: this.username,
        color: this.myColor,
        move: coords
      })
    });
  }

  pass() {
    if (this.lastCoords !== '') {
      this.gameBoard.drawStone(this.lastColor, this.lastCoords, false);
    }
    this.lastCoords = '';
    this.lastColor = 0;

    this.stompService.publish({
      destination: '/app/game/move',
      body: JSON.stringify({
        username: this.username,
        color: this.myColor,
        move: 'PA'
      })
    });

    this.myTurn = false;
    this.prompt = this.myColor === BLACK ? `Black passes. White's turn` : `White passes. Black's turn`;
    this.logTable.addRow(this.myColor, 'PA');
  }

  resign() {
    if (window.confirm(`Are you sure you want to resign?`)) {
      this.stompService.publish({
        destination: '/app/game/interrupt/request',
        body: JSON.stringify({
          username: this.username,
          color: this.myColor,
          requestType: 'RESIGN'
        })
      });
    }
  }

  offerDraw() {
    if (window.confirm(`Are you sure you want to offer a draw?`)) {
      this.stompService.publish({
        destination: '/app/game/interrupt/request',
        body: JSON.stringify({
          username: this.username,
          color: this.myColor,
          requestType: 'DRAW'
        })
      });
    }
  }

  offerRestart() {
    if (window.confirm(`Do you want to ask your opponent to restart this game?`)) {
      this.stompService.publish({
        destination: '/app/game/interrupt/request',
        body: JSON.stringify({
          username: this.username,
          color: this.myColor,
          requestType: 'RESTART'
        })
      });
    }
  }

  @HostListener('window:beforeunload', ['$event'])
  beforeUnload($event: any) {
    if (this.gameFinished) return undefined;
    const confirmationMessage = 'Are you sure you want to leave?';
    $event.returnValue = confirmationMessage;
    return confirmationMessage;
  }

  confirmLeaving(): boolean {
    if (this.gameFinished) return true;
    let willLeave = window.confirm(`Are you sure you want to leave this game? This will be counted as your defeat.`);
    if (willLeave) this.leave();
    return willLeave;
  }

  leave() {
    this.stompService.publish({
      destination: '/app/game/interrupt/request',
      body: JSON.stringify({
        username: this.username,
        color: this.myColor,
        requestType: 'LEAVE'
      })
    });
  }
}
