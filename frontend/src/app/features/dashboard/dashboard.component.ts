import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TableModule } from 'primeng/table';

interface MockPR {
  prNumber: string;
  title: string;
  priority: string;
  status: string;
  totalAmount: number;
}

interface MockStockAlert {
  productName: string;
  sku: string;
  quantity: number;
  unit: string;
  minLevel: number;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, TableModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  recentRequests: MockPR[] = [
    { prNumber: 'PR-2026-001', title: 'Office Laptops Upgrade', priority: 'HIGH', status: 'UNDER_REVIEW', totalAmount: 4500.00 },
    { prNumber: 'PR-2026-002', title: 'Server Room AC Repair', priority: 'URGENT', status: 'APPROVED', totalAmount: 1200.00 },
    { prNumber: 'PR-2026-003', title: 'Ergonomic Desk Chairs', priority: 'MEDIUM', status: 'DRAFT', totalAmount: 2500.00 },
    { prNumber: 'PR-2026-004', title: 'Printer Paper Reorder', priority: 'LOW', status: 'CONVERTED_TO_PO', totalAmount: 320.00 }
  ];

  lowStockItems: MockStockAlert[] = [
    { productName: 'Cat6 Ethernet Cables 30m', sku: 'CAB-CAT6-30', quantity: 2, unit: 'PCS', minLevel: 10 },
    { productName: 'Dell 24" Monitor P2422H', sku: 'MON-DELL-24', quantity: 1, unit: 'PCS', minLevel: 5 },
    { productName: 'Wireless Mouse Logitech M185', sku: 'MOU-LOGI-185', quantity: 3, unit: 'PCS', minLevel: 12 }
  ];

  ngOnInit(): void {}

  onNewRequest(): void {
    // Navigate to PR creation or open modal in Phase 4
    console.log('Open Purchase Request Draft');
  }
}
