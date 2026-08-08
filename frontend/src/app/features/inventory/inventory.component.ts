import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="glass-card p-5 animate-fade-in">
      <h2>Inventory & Stock Levels</h2>
      <p class="text-muted">Track quantities on-hand, reserved, and on-order across branches. Monitor low stock items.</p>
    </div>
  `
})
export class InventoryComponent {}
