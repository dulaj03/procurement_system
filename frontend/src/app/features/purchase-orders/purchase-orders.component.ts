import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-purchase-orders',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="glass-card p-5 animate-fade-in">
      <h2>Purchase Orders (PO)</h2>
      <p class="text-muted">Generate official POs from approved PRs, dispatch to suppliers, and track expected delivery dates.</p>
    </div>
  `
})
export class PurchaseOrdersComponent {}
