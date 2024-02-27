import { AfterViewInit, Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';

@Component({
  selector: 'app-game-board',
  templateUrl: './game-board.component.html',
  styleUrls: ['./game-board.component.css']
})
export class GameBoardComponent implements AfterViewInit {
  @Input() boardSize!: number;
  @Input() playerColor!: number;
  @Input() shadowDisabled!: boolean;
  @Output() mouseClick: EventEmitter<string> = new EventEmitter<string>();
  @ViewChild('gameCanvas', { static: true }) gameCanvas!: ElementRef;
  ctx!: CanvasRenderingContext2D;
  fullWidth: number = 600;
  margin: number = 30;
  cellWidth!: number;
  lineWidth!: number;
  stoneRadius!: number;
  lineColor = 'darkred';
  lastShadowCoords: string = '';
  stoneMap: any = {};
  territoryMap: any = {};

  ngAfterViewInit(): void {
    const canvas: HTMLCanvasElement = this.gameCanvas.nativeElement;
    this.ctx = canvas.getContext('2d')!;
    this.cellWidth = (this.fullWidth - 2 * this.margin) / (this.boardSize - 1);
    this.lineWidth = 0.05 * this.cellWidth;
    this.stoneRadius = 0.45 * this.cellWidth;
    this.drawGameBoard();
  }

  drawGameBoard() {
    this.ctx.lineWidth = this.lineWidth;
    this.ctx.strokeStyle = this.lineColor;
    for (let i = 0; i < this.boardSize; i++) {
      let offset = this.margin + i * this.cellWidth;
      this.drawLine(offset, this.margin, offset, this.fullWidth - this.margin);
      this.drawLine(this.margin, offset, this.fullWidth - this.margin, offset);
    }
  }

  drawLine(x1: number, y1: number, x2: number, y2: number) {
    this.ctx.beginPath();
    this.ctx.moveTo(x1, y1);
    this.ctx.lineTo(x2, y2);
    this.ctx.stroke();
  }

  onMouseMove(event: MouseEvent) {
    if (this.shadowDisabled) return;
    let p = this.toCanvasXAndY(event);
    let shadowCoords = this.pointToCoords(p);
    if (shadowCoords === this.lastShadowCoords) return;
    if (this.stoneMap[shadowCoords] !== undefined) return;
    if (this.lastShadowCoords !== '') {
      this.clearBoardAt([this.lastShadowCoords]);
    }
    this.drawStone(this.playerColor, shadowCoords, false, true);
    this.lastShadowCoords = shadowCoords;
  }

  onMouseClick(event: MouseEvent) {
    console.log(`clicked (${event.clientX}, ${event.clientY})`);
    if (this.shadowDisabled) return;
    if (this.playerColor === 0) return;
    let p = this.toCanvasXAndY(event);
    let coords = this.pointToCoords(p);
    if (this.stoneMap[coords] !== undefined) return;
    this.mouseClick.emit(coords);
    console.log(`emitted: ${coords}`);
  }

  onMouseLeave(event: MouseEvent) {
    if (this.lastShadowCoords !== '') {
      this.clearBoardAt([this.lastShadowCoords]);
    }
  }

  toCanvasXAndY(event: MouseEvent): Point {
    const canvas: HTMLCanvasElement = this.gameCanvas.nativeElement;
    const rect = canvas.getBoundingClientRect();
    return {
      x: event.clientX - rect.left,
      y: event.clientY - rect.top
    };
  }

  drawStone(color: number, coords: string, withMarker: boolean = false, isShadow: boolean = false) {
    let p = this.coordsToPoint(coords);
    if (isShadow) this.ctx.globalAlpha = 0.5;
    this.ctx.beginPath();
    this.ctx.arc(p.x, p.y, this.stoneRadius, 0, 2 * Math.PI);
    this.ctx.fillStyle = (color == 1) ? 'black' : 'white';
    this.ctx.fill();
    this.ctx.globalAlpha = 1;
    if (!isShadow) this.stoneMap[coords] = color;

    if (withMarker) {
      this.ctx.beginPath();
      this.ctx.arc(p.x, p.y, 0.35 * this.stoneRadius, 0, 2 * Math.PI);
      this.ctx.fillStyle = (color == 1) ? 'white' : 'black';
      this.ctx.fill();
    }

    this.lastShadowCoords = '';
  }

  clearBoardAt(coordsList: string[]) {
    for (let coords of coordsList) {
      let p = this.coordsToPoint(coords);
      this.ctx.clearRect(p.x - this.cellWidth / 2, p.y - this.cellWidth / 2, this.cellWidth, this.cellWidth);
      if (p.x > this.margin) this.drawLine(p.x - this.cellWidth / 2, p.y, p.x, p.y);
      if (p.x < this.fullWidth - this.margin) this.drawLine(p.x + this.cellWidth / 2, p.y, p.x, p.y);
      if (p.y > this.margin) this.drawLine(p.x, p.y - this.cellWidth / 2, p.x, p.y);
      if (p.y < this.fullWidth - this.margin) this.drawLine(p.x, p.y + this.cellWidth / 2, p.x, p.y);
      this.stoneMap[coords] = undefined;
      this.territoryMap[coords] = undefined;
    }
  }

  reset() {
    console.log('reset game board');
    this.stoneMap = {};
    this.territoryMap = {};
    this.ctx.clearRect(0, 0, this.fullWidth, this.fullWidth);
    this.drawGameBoard();
  }

  setStoneShadowDisabled(disabled: boolean) {
    this.shadowDisabled = disabled;
  }

  drawTerritory(color: number, coordsList: string[]) {
    const width = 0.2 * this.cellWidth;
    this.ctx.fillStyle = (color == 1) ? 'black' : 'white';
    for (let coords of coordsList) {
      let p = this.coordsToPoint(coords);
      this.ctx.fillRect(p.x - width / 2, p.y - width / 2, width, width);
      this.territoryMap[coords] = color;
    }
  }

  clearAllTerritory() {
    for (let coords of Object.keys(this.territoryMap)) {
      this.clearBoardAt([coords]);
      this.territoryMap[coords] = undefined;
    }
  }

  pointToCoords(p: Point): string {
    if (p.x < this.margin) p.x = this.margin;
    if (p.x > this.fullWidth - this.margin) p.x = this.fullWidth - this.margin;
    if (p.y < this.margin) p.y = this.margin;
    if (p.y > this.fullWidth - this.margin) p.y = this.fullWidth - this.margin;

    let col = Math.round((p.x - this.margin) / this.cellWidth);
    let row = this.boardSize - Math.round((p.y - this.margin) / this.cellWidth);

    if (col >= 8) col++;
    let colSymbol = String.fromCharCode(65 + col);

    let coords = `${colSymbol}${row}`;
    return coords;
  }

  coordsToPoint(coords: string): Point {
    let col = coords.charCodeAt(0) - 65;
    if (col >= 9) col--;
    let row = +(coords.substring(1)) - 1;

    let p = {
      x: this.margin + col * this.cellWidth,
      y: this.fullWidth - (this.margin + row * this.cellWidth)
    };
    return p;
  }
}

interface Point {
  x: number;
  y: number;
}
