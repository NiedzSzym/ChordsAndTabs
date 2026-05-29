import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class RegisterComponent {
  credentials = { email: '', password: '', nickname: '' };

  errorMessage = signal('');
  successMessage = signal('');
  isLoading = signal(false);

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  onSubmit(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    this.authService.register(this.credentials).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.successMessage.set('Account created successfully! Redirecting to login...');
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (err: any) => {
        this.isLoading.set(false);
        if (err.status === 400 || err.status === 409) {
          this.errorMessage.set('Registration failed. Email might be already in use or data is invalid.');
        } else {
          this.errorMessage.set('An unexpected server error occurred.');
        }
        console.error('Registration error details:', err);
      }
    });
  }
}
