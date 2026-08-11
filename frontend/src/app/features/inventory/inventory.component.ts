import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { DialogModule } from 'primeng/dialog';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { TabViewModule } from 'primeng/tabview';
import { InputNumberModule } from 'primeng/inputnumber';
import { TooltipModule } from 'primeng/tooltip';
import { MessageService } from 'primeng/api';

import { InventoryService } from './inventory.service';
import { ProductService } from '../products/product.service';
import { AuthService } from '../../core/auth/auth.service';
import { InventoryItem, StockMovement, LowStockAlert } from './inventory.model';
import { ProductSummary } from '../products/product.model';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule,
    TableModule, ButtonModule, InputTextModule, DropdownModule,
    DialogModule, TagModule, ToastModule, TabViewModule,
    InputNumberModule, TooltipModule
  ],
  providers: [MessageService],
  templateUrl: './inventory.component.html',
  styleUrls: ['./inventory.component.scss']
})
export class InventoryComponent implements OnInit {

  inventory: InventoryItem[] = [];
  movements: StockMovement[] = [];
  lowStockAlerts: LowStockAlert[] = [];
  products: ProductSummary[] = [];

  loading = false;
  movementLoading = false;
  companyId!: string;

  filterLowStockOnly = false;
  searchTerm = '';

  showAdjustDialog = false;
  showTransferDialog = false;
  formLoading = false;

  adjustForm!: FormGroup;
  transferForm!: FormGroup;

  // Mock branches for selector (in real flow fetched from BranchService)
  branchOptions = [
    { label: 'Main Warehouse (HQ)', value: 'b0196720-80a5-48b2-b13c-0e6e7372d8a1' },
    { label: 'West Coast Logistics Center', value: 'b0296720-80a5-48b2-b13c-0e6e7372d8a2' },
    { label: 'East Coast Distribution Center', value: 'b0396720-80a5-48b2-b13c-0e6e7372d8a3' }
  ];

  constructor(
    private inventoryService: InventoryService,
    private productService: ProductService,
    private authSvc: AuthService,
    private fb: FormBuilder,
    private msgSvc: MessageService
  ) {}

  ngOnInit(): void {
    this.companyId = this.authSvc.currentUser?.companyId ?? '';
    this.initForms();
    this.loadInventory();
    this.loadMovements();
    this.loadProducts();
  }

  initForms(): void {
    this.adjustForm = this.fb.group({
      productId: ['', Validators.required],
      branchId: ['', Validators.required],
      quantity: [0, [Validators.required]],
      reason: ['', Validators.required],
      referenceNumber: ['']
    });

    this.transferForm = this.fb.group({
      productId: ['', Validators.required],
      fromBranchId: ['', Validators.required],
      toBranchId: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
      notes: ['']
    });
  }

  loadInventory(): void {
    this.loading = true;
    this.inventoryService.getInventory(this.companyId, undefined, this.filterLowStockOnly)
      .subscribe({
        next: items => {
          this.inventory = items;
          this.loading = false;
        },
        error: () => {
          this.msgSvc.add({ severity: 'error', summary: 'Error', detail: 'Failed to load stock levels' });
          this.loading = false;
        }
      });
  }

  loadMovements(): void {
    this.movementLoading = true;
    this.inventoryService.getMovements(undefined, undefined, 0, 50)
      .subscribe({
        next: res => {
          this.movements = res.content;
          this.movementLoading = false;
        },
        error: () => this.movementLoading = false
      });
  }

  loadProducts(): void {
    this.productService.listProducts(this.companyId, undefined, 'ACTIVE', undefined, 0, 100)
      .subscribe({ next: res => this.products = res.content });
  }

  openAdjustModal(item?: InventoryItem): void {
    this.adjustForm.reset({
      productId: item?.productId ?? '',
      branchId: item?.branchId ?? this.branchOptions[0].value,
      quantity: 0,
      reason: 'Physical Stock Count Audit'
    });
    this.showAdjustDialog = true;
  }

  openTransferModal(item?: InventoryItem): void {
    this.transferForm.reset({
      productId: item?.productId ?? '',
      fromBranchId: item?.branchId ?? this.branchOptions[0].value,
      toBranchId: '',
      quantity: 1,
      notes: 'Internal Branch Rebalancing'
    });
    this.showTransferDialog = true;
  }

  submitAdjust(): void {
    if (this.adjustForm.invalid) {
      this.adjustForm.markAllAsTouched();
      return;
    }
    this.formLoading = true;
    this.inventoryService.adjustStock(this.adjustForm.value).subscribe({
      next: () => {
        this.msgSvc.add({ severity: 'success', summary: 'Success', detail: 'Stock quantity adjusted' });
        this.showAdjustDialog = false;
        this.formLoading = false;
        this.loadInventory();
        this.loadMovements();
      },
      error: err => {
        this.msgSvc.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Adjustment failed' });
        this.formLoading = false;
      }
    });
  }

  submitTransfer(): void {
    if (this.transferForm.invalid) {
      this.transferForm.markAllAsTouched();
      return;
    }
    if (this.transferForm.value.fromBranchId === this.transferForm.value.toBranchId) {
      this.msgSvc.add({ severity: 'warn', summary: 'Warning', detail: 'Source and Destination branches must be different' });
      return;
    }
    this.formLoading = true;
    this.inventoryService.transferStock(this.transferForm.value).subscribe({
      next: () => {
        this.msgSvc.add({ severity: 'success', summary: 'Success', detail: 'Stock transfer logged' });
        this.showTransferDialog = false;
        this.formLoading = false;
        this.loadInventory();
        this.loadMovements();
      },
      error: err => {
        this.msgSvc.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Transfer failed' });
        this.formLoading = false;
      }
    });
  }

  getFilteredInventory(): InventoryItem[] {
    if (!this.searchTerm) return this.inventory;
    const term = this.searchTerm.toLowerCase();
    return this.inventory.filter(i =>
      i.productName.toLowerCase().includes(term) ||
      i.sku.toLowerCase().includes(term) ||
      i.branchName.toLowerCase().includes(term)
    );
  }

  getMovementBadgeClass(type: string): 'success' | 'warning' | 'danger' | 'info' {
    if (type.includes('RECEIPT') || type.includes('IN')) return 'success';
    if (type.includes('TRANSFER')) return 'info';
    if (type.includes('OUT') || type.includes('ISSUE')) return 'warning';
    return 'info';
  }
}
