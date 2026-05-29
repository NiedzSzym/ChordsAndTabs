import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, NgIf, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class LoginComponent {
  credentials = { email: '', password: '' };
  errorMessage = '';

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  onSubmit(): void {
    this.authService.login(this.credentials).subscribe({
      next: () => {
        this.router.navigate(['/songs']);
      },
      error: (err: any) => {
        if (err.status === 403 && err.error?.token === 'Please verify your email first') {
          this.errorMessage = 'Account not verified. Please check your email inbox.';
        } else if (err.status === 0) {
          this.errorMessage = 'Connection refused. Please verify the API server status.';
        } else if (err.status === 401 || err.status === 403) {
          this.errorMessage = 'Invalid email or password.';
        } else {
          this.errorMessage = 'An unexpected error occurred. Please try again later.';
        }
      }
    });
  }
}
