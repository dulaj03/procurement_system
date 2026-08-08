import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-companies',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="glass-card p-5 animate-fade-in">
      <h2>Companies & Branches Management</h2>
      <p class="text-muted">Setup multi-company settings, create branches, and link them to users.</p>
    </div>
  `
})
export class CompaniesComponent {}
