import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss'
})
export class SidebarComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  loggedUser = signal<string>('');

  ngOnInit(): void {
    this.loggedUser.set(this.authService.getLoggedUser());
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
