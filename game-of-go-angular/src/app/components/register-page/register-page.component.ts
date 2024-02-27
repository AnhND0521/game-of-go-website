import { Component } from '@angular/core';
import { NgForm } from '@angular/forms';
import { AccountService } from 'src/app/services/account.service';

@Component({
  selector: 'app-register-page',
  templateUrl: './register-page.component.html',
  styleUrls: ['./register-page.component.css']
})
export class RegisterPageComponent {
  errorMessage: string = '';

  constructor(private accountService: AccountService) {}

  onSubmit(form: NgForm) {
    let username: string = form.controls['username']!.value;
    let password: string = form.controls['password']!.value;
    let confirmPassword: string = form.controls['confirm-password']!.value;

    if (form.controls['username']!.hasError('required')) {
      this.errorMessage = 'Username is required.';
      return;
    }

    const pattern: RegExp = /^[A-Za-z0-9_]+$/;
    if (!pattern.test(username)) {
      this.errorMessage = 'Username should just contain letters, numbers or underscores.';
      return;
    }

    if (form.controls['password']!.hasError('required')) {
      this.errorMessage = 'Password is required.';
      return;
    }

    if (password.length < 8) {
      this.errorMessage = 'Password must have at least 8 characters.';
      return;
    }

    if (confirmPassword !== password) {
      this.errorMessage = 'Confirm password does not match.';
      return;
    }

    this.errorMessage = '';
    console.log('Register form was submitted');

    this.accountService.register(username, password).subscribe(
      response => this.errorMessage = response.message
    )
  }
}
