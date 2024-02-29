import { Component } from '@angular/core';

@Component({
  selector: 'app-log-table',
  templateUrl: './log-table.component.html',
  styleUrls: ['./log-table.component.css']
})
export class LogTableComponent {
  logItems: LogItem[] = [];
  count: number = 0;

  addRow(color: number, coords: string) {
    let colorString = (color == 1) ? 'BLACK' : 'WHITE';
    let action: string;

    switch (coords) {
      case 'PA':
        action = 'passes';
        break;
      case 'RS':
        action = 'resigns';
        break;
      case 'DR':
        action = 'accepts draw request';
        break;
      case 'TO':
        action = 'runs out of time';
        break;
      case 'LV':
        action = 'leaves the game';
        break;
      default:
        action = 'plays ' + coords;
        break;
    }

    this.logItems.push({
      no: ++this.count,
      color: colorString,
      action: action
    });
  }

  removeLastRow() {
    this.logItems.pop();
    this.count--;
  }

  reset() {
    this.logItems = [];
    this.count = 0;
  }
}

interface LogItem {
  no: number,
  color: string;
  action: string;
}
