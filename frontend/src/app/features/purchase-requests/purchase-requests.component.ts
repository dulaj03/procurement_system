import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-purchase-requests',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="glass-card p-5 animate-fade-in">
      <h2>Purchase Requests (PR)</h2>
      <p class="text-muted">Initiate items requisition, track status, review approvals queue, and reject/approve requests.</p>
    </div>
  `
})
export class PurchaseRequestsComponent {}
