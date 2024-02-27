import { Component, OnInit } from '@angular/core';
import { Player } from 'src/app/common/player';
import { InfoService } from 'src/app/services/info.service';

@Component({
  selector: 'app-rankings',
  templateUrl: './rankings.component.html',
  styleUrls: ['./rankings.component.css']
})
export class RankingsComponent implements OnInit {
  rankings: Player[] = [];

  constructor(private infoService: InfoService) {}

  ngOnInit(): void {
    this.infoService.getPlayerRankings().subscribe(
      data => {
        console.log(data);
        this.rankings = data;
      }
    ); 
  }
}
