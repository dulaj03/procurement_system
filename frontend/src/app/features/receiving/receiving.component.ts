import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-receiving',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="glass-card p-5 animate-fade-in">
      <h2>Goods Receiving (GRN)</h2>
      <p class="text-muted">Record physical item deliveries, log accepted vs rejected quantities, and post stocks directly to warehouse.</p>
    </div>
  `
})
export class ReceivingComponent {}
