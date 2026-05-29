import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../../components/sidebar/sidebar';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent],
  template: `
    <div class="d-flex min-vh-100">
      <app-sidebar></app-sidebar>
      <div class="flex-grow-1" style="overflow-y: auto;">
        <router-outlet></router-outlet>
      </div>
    </div>
  `
})
export class MainLayoutComponent {}
