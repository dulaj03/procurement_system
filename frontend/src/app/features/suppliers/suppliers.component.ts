import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-suppliers',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="glass-card p-5 animate-fade-in">
      <h2>Supplier Management</h2>
      <p class="text-muted">Maintain supplier database, contact profiles, credit limits, and rating scorecards.</p>
    </div>
  `
})
export class SuppliersComponent {}
