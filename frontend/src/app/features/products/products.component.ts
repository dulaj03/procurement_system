import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="glass-card p-5 animate-fade-in">
      <h2>Product Catalog</h2>
      <p class="text-muted">Manage items, category hierarchy, unit of measures, and standard pricing.</p>
    </div>
  `
})
export class ProductsComponent {}
