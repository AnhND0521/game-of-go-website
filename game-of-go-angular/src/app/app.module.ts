import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { PlayMenuComponent } from './components/play-menu/play-menu.component';
import { LoginPageComponent } from './components/login-page/login-page.component';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { RegisterPageComponent } from './components/register-page/register-page.component';
import { SocketIoConfig, SocketIoModule } from 'ngx-socket-io';
import { RxStompService } from './services/rx-stomp.service';
import { rxStompServiceFactory } from './config/rx-stomp-service-factory';
import { NavBarComponent } from './components/nav-bar/nav-bar.component';
import { GameComponent } from './components/game/game.component';
import { GameBoardComponent } from './components/game-board/game-board.component';
import { InfoTableComponent } from './components/info-table/info-table.component';
import { LogTableComponent } from './components/log-table/log-table.component';
import { ChatBoxComponent } from './components/chat-box/chat-box.component';
import { RankingsComponent } from './components/rankings/rankings.component';
import { HistoryComponent } from './components/history/history.component';

@NgModule({
  declarations: [
    AppComponent,
    PlayMenuComponent,
    LoginPageComponent,
    RegisterPageComponent,
    NavBarComponent,
    GameComponent,
    GameBoardComponent,
    InfoTableComponent,
    LogTableComponent,
    ChatBoxComponent,
    RankingsComponent,
    HistoryComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule
  ],
  providers: [{
    provide: RxStompService,
    useFactory: rxStompServiceFactory
  }],
  bootstrap: [AppComponent]
})
export class AppModule { }
