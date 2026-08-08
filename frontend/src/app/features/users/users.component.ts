import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="glass-card p-5 animate-fade-in">
      <h2>Employee & Role Management</h2>
      <p class="text-muted">Invite employees, assign RBAC roles, and set department scopes.</p>
    </div>
  `
})
export class UsersComponent {}
