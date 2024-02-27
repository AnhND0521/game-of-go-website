import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InfoTableComponent } from './info-table.component';

describe('InfoTableComponent', () => {
  let component: InfoTableComponent;
  let fixture: ComponentFixture<InfoTableComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [InfoTableComponent]
    });
    fixture = TestBed.createComponent(InfoTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
