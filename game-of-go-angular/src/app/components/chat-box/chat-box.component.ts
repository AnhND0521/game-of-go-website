import { Component, OnInit } from '@angular/core';
import { AccountService } from 'src/app/services/account.service';
import { RxStompService } from 'src/app/services/rx-stomp.service';

@Component({
  selector: 'app-chat-box',
  templateUrl: './chat-box.component.html',
  styleUrls: ['./chat-box.component.css']
})
export class ChatBoxComponent implements OnInit {
  messages: string[] = [];
  inputValue: string = '';

  constructor(private stompService: RxStompService, private accountService: AccountService) {}

  ngOnInit(): void {
    const username = this.accountService.getLoginName();
    this.stompService.watch(`/user/${username}/queue/game/chat`)
    .subscribe(msg => {
      // add message to array
    });
  }

  sendMessage() {
    this.stompService.publish({
      destination: '/app/game/chat',
      body: this.inputValue
    });
    this.inputValue = '';
  }
}
