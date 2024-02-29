import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { GameHistory } from 'src/app/common/game-history';
import { Statistics } from 'src/app/common/statistics';
import { AccountService } from 'src/app/services/account.service';
import { InfoService } from 'src/app/services/info.service';

@Component({
  selector: 'app-history',
  templateUrl: './history.component.html',
  styleUrls: ['./history.component.css']
})
export class HistoryComponent implements OnInit {
  username: string = '';
  statistics!: Statistics;
  historyItems: GameHistory[] = [];

  constructor(
    private accountService: AccountService,
    private infoService: InfoService,
    private router: Router) {}

  ngOnInit(): void {
    this.username = this.accountService.getLoginName();
    if (!this.username) {
      this.router.navigateByUrl('/login');
      return;
    }

    this.infoService.getStatistics(this.username).subscribe(
      data => {
        console.log(data);
        this.statistics = data;
      }
    )

    this.infoService.getHistory(this.username).subscribe(
      data => {
        console.log(data);
        this.historyItems = data;
      }
    );
  }
}
