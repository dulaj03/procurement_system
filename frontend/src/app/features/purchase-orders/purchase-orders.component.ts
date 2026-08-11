import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';

import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { DialogModule } from 'primeng/dialog';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { DividerModule } from 'primeng/divider';
import { InputNumberModule } from 'primeng/inputnumber';
import { CalendarModule } from 'primeng/calendar';
import { TooltipModule } from 'primeng/tooltip';
import { MessageService } from 'primeng/api';

import { PurchaseOrderService } from './purchase-order.service';
import { SupplierService } from '../suppliers/supplier.service';
import { ProductService } from '../products/product.service';
import { AuthService } from '../../core/auth/auth.service';
import { POSummary, POResponse, POStatus } from './purchase-order.model';
import { SupplierSummary } from '../suppliers/supplier.model';
import { ProductSummary } from '../products/product.model';

@Component({
  selector: 'app-purchase-orders',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule,
    TableModule, ButtonModule, InputTextModule, DropdownModule,
    DialogModule, TagModule, ToastModule, DividerModule,
    InputNumberModule, CalendarModule, TooltipModule
  ],
  providers: [MessageService],
  templateUrl: './purchase-orders.component.html',
  styleUrls: ['./purchase-orders.component.scss']
})
export class PurchaseOrdersComponent implements OnInit {

  pos: POSummary[] = [];
  selectedPO: POResponse | null = null;
  suppliers: SupplierSummary[] = [];
  products: ProductSummary[] = [];

  totalRecords = 0;
  pageSize = 20;
  currentPage = 0;
  loading = false;
  companyId!: string;

  searchTerm = '';
  statusFilter: POStatus | undefined;

  showFormDialog = false;
  showDetailDialog = false;
  formLoading = false;

  poForm!: FormGroup;

  statusOptions = [
    { label: 'All Statuses', value: undefined },
    { label: 'Draft', value: 'DRAFT' },
    { label: 'Sent', value: 'SENT' },
    { label: 'Acknowledged', value: 'ACKNOWLEDGED' },
    { label: 'Received', value: 'RECEIVED' }
  ];

  branchOptions = [
    { label: 'Main Warehouse (HQ)', value: 'b0196720-80a5-48b2-b13c-0e6e7372d8a1' },
    { label: 'West Coast Logistics Center', value: 'b0296720-80a5-48b2-b13c-0e6e7372d8a2' }
  ];

  constructor(
    private poService: PurchaseOrderService,
    private supplierService: SupplierService,
    private productService: ProductService,
    private authSvc: AuthService,
    private fb: FormBuilder,
    private msgSvc: MessageService
  ) {}

  ngOnInit(): void {
    this.companyId = this.authSvc.currentUser?.companyId ?? '';
    this.initForm();
    this.loadPOs();
    this.loadSuppliers();
    this.loadProducts();
  }

  initForm(): void {
    this.poForm = this.fb.group({
      supplierId: ['', Validators.required],
      branchId: [this.branchOptions[0].value, Validators.required],
      orderDate: [new Date().toISOString().split('T')[0], Validators.required],
      expectedDeliveryDate: [''],
      deliveryAddress: ['100 Logistics Blvd, Warehouse Bay 4'],
      paymentTerms: ['Net 30'],
      currency: ['USD'],
      notes: [''],
      items: this.fb.array([this.createItemFormGroup()])
    });
  }

  createItemFormGroup(): FormGroup {
    return this.fb.group({
      productId: ['', Validators.required],
      quantityOrdered: [1, [Validators.required, Validators.min(1)]],
      unitPrice: [0, [Validators.required, Validators.min(0)]],
      unitOfMeasure: ['PCS'],
      notes: ['']
    });
  }

  get itemsFormArray(): FormArray {
    return this.poForm.get('items') as FormArray;
  }

  addItemRow(): void {
    this.itemsFormArray.push(this.createItemFormGroup());
  }

  removeItemRow(index: number): void {
    if (this.itemsFormArray.length > 1) {
      this.itemsFormArray.removeAt(index);
    }
  }

  onProductSelect(index: number, productId: string): void {
    const p = this.products.find(x => x.id === productId);
    if (p) {
      const row = this.itemsFormArray.at(index);
      row.patchValue({
        unitOfMeasure: p.unitOfMeasure,
        unitPrice: p.unitPrice || 0
      });
    }
  }

  loadPOs(page = this.currentPage): void {
    this.loading = true;
    this.poService.listPOs(this.companyId, undefined, this.statusFilter, this.searchTerm, page, this.pageSize)
      .subscribe({
        next: res => {
          this.pos = res.content;
          this.totalRecords = res.totalElements;
          this.currentPage = res.number;
          this.loading = false;
        },
        error: () => this.loading = false
      });
  }

  loadSuppliers(): void {
    this.supplierService.list(this.companyId, undefined, 'ACTIVE', 0, 100)
      .subscribe({ next: res => this.suppliers = res.content });
  }

  loadProducts(): void {
    this.productService.listProducts(this.companyId, undefined, 'ACTIVE', undefined, 0, 100)
      .subscribe({ next: res => this.products = res.content });
  }

  openCreate(): void {
    this.initForm();
    this.showFormDialog = true;
  }

  viewDetail(id: string): void {
    this.poService.getPOById(id).subscribe({
      next: po => {
        this.selectedPO = po;
        this.showDetailDialog = true;
      }
    });
  }

  savePO(): void {
    if (this.poForm.invalid) {
      this.poForm.markAllAsTouched();
      return;
    }
    this.formLoading = true;
    this.poService.createPO(this.poForm.value).subscribe({
      next: () => {
        this.msgSvc.add({ severity: 'success', summary: 'Success', detail: 'Purchase Order issued' });
        this.showFormDialog = false;
        this.formLoading = false;
        this.loadPOs();
      },
      error: err => {
        this.msgSvc.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'PO creation failed' });
        this.formLoading = false;
      }
    });
  }

  markStatus(id: string, status: POStatus): void {
    this.poService.updateStatus(id, status).subscribe({
      next: () => {
        this.msgSvc.add({ severity: 'success', summary: 'Status Updated', detail: `PO status updated to ${status}` });
        if (this.selectedPO && this.selectedPO.id === id) this.selectedPO.status = status;
        this.loadPOs();
      }
    });
  }

  getStatusSeverity(status: POStatus): 'success' | 'warning' | 'danger' | 'info' | 'secondary' {
    switch (status) {
      case 'DRAFT': return 'secondary';
      case 'SENT': return 'info';
      case 'ACKNOWLEDGED': return 'warning';
      case 'RECEIVED': return 'success';
      case 'CANCELLED': return 'danger';
      default: return 'secondary';
    }
  }

  calculateTotal(): number {
    return this.itemsFormArray.controls.reduce((acc, ctrl) => {
      const qty = ctrl.get('quantityOrdered')?.value || 0;
      const price = ctrl.get('unitPrice')?.value || 0;
      return acc + (qty * price);
    }, 0);
  }
}
