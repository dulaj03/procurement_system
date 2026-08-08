import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-invoices',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="glass-card p-5 animate-fade-in">
      <h2>Invoice & Payments Management</h2>
      <p class="text-muted">Manage supplier bills, record payments made, match invoices to POs, and review outstanding accounts.</p>
    </div>
  `
})
export class InvoicesComponent {}
