import { Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { AccountService } from 'src/app/services/account.service';
import { RxStompService } from 'src/app/services/rx-stomp.service';

@Component({
  selector: 'app-login-page',
  templateUrl: './login-page.component.html',
  styleUrls: ['./login-page.component.css']
})
export class LoginPageComponent implements OnInit {
  errorMessage: string = '';

  constructor(
    private accountService: AccountService,
    private router: Router) {}

  ngOnInit(): void {
    this.accountService.handleLogout();
  }

  onSubmit(form: NgForm) {
    if (form.controls['username']!.hasError('required')) {
      this.errorMessage = 'Username is required.';
      return;
    }

    if (form.controls['password']!.hasError('required')) {
      this.errorMessage = 'Password is required.';
      return;
    }

    this.errorMessage = '';
    console.log('Login form was submitted');

    this.accountService.login(
      form.controls['username']!.value,
      form.controls['password']!.value,
    ).subscribe(
      response => {
        if (response.status === 'OK') {
          console.log(response.data);
          this.accountService.handleLogin(response.data.username, response.data.accessId);
          this.router.navigateByUrl('/play');
        } else {
          this.errorMessage = response.message;
        }
      }
    )
  }
}
