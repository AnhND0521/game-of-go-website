import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AccountService } from 'src/app/services/account.service';

@Component({
  selector: 'app-nav-bar',
  templateUrl: './nav-bar.component.html',
  styleUrls: ['./nav-bar.component.css']
})
export class NavBarComponent implements OnInit {
  loggedIn: boolean = false;
  loginName: string = '';

  constructor(private accountService: AccountService, private router: Router) {}

  ngOnInit(): void {
    this.accountService.loggedIn.subscribe(value => {
      this.loggedIn = value;
      if (value) {
        this.loginName = this.accountService.getLoginName();
      }
    });
  }

  login() {
    this.router.navigateByUrl('/login');
  }

  logout() {
    this.router.navigateByUrl('/login');
  }
}
