import { Component, Input } from '@angular/core';
import { GameConfig } from 'src/app/common/game-config';

@Component({
  selector: 'app-info-table',
  templateUrl: './info-table.component.html',
  styleUrls: ['./info-table.component.css']
})
export class InfoTableComponent {
  @Input() gameConfig!: GameConfig;
  @Input() blackName!: string;
  @Input() whiteName!: string;
  @Input() blackCaptures!: number;
  @Input() whiteCaptures!: number;
}
