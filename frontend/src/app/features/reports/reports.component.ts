import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="glass-card p-5 animate-fade-in">
      <h2>Analytical Reports & Exports</h2>
      <p class="text-muted">Export data dumps, build custom reports, view charts, and download PDF sheets.</p>
    </div>
  `
})
export class ReportsComponent {}
