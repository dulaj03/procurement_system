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

import { PurchaseRequestService } from './purchase-request.service';
import { ProductService } from '../products/product.service';
import { AuthService } from '../../core/auth/auth.service';
import { PRSummary, PRResponse, PRStatus, PRPriority } from './purchase-request.model';
import { ProductSummary } from '../products/product.model';

@Component({
  selector: 'app-purchase-requests',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule,
    TableModule, ButtonModule, InputTextModule, DropdownModule,
    DialogModule, TagModule, ToastModule, DividerModule,
    InputNumberModule, CalendarModule, TooltipModule
  ],
  providers: [MessageService],
  templateUrl: './purchase-requests.component.html',
  styleUrls: ['./purchase-requests.component.scss']
})
export class PurchaseRequestsComponent implements OnInit {

  prs: PRSummary[] = [];
  selectedPR: PRResponse | null = null;
  products: ProductSummary[] = [];

  totalRecords = 0;
  pageSize = 20;
  currentPage = 0;
  loading = false;
  companyId!: string;

  searchTerm = '';
  statusFilter: PRStatus | undefined;

  showFormDialog = false;
  showDetailDialog = false;
  showRejectDialog = false;
  formLoading = false;
  rejectReason = '';

  prForm!: FormGroup;

  statusOptions = [
    { label: 'All Statuses', value: undefined },
    { label: 'Draft', value: 'DRAFT' },
    { label: 'Submitted', value: 'SUBMITTED' },
    { label: 'Approved', value: 'APPROVED' },
    { label: 'Rejected', value: 'REJECTED' }
  ];

  priorityOptions = [
    { label: 'Low', value: 'LOW' },
    { label: 'Medium', value: 'MEDIUM' },
    { label: 'High', value: 'HIGH' },
    { label: 'Urgent', value: 'URGENT' }
  ];

  branchOptions = [
    { label: 'Main Warehouse (HQ)', value: 'b0196720-80a5-48b2-b13c-0e6e7372d8a1' },
    { label: 'West Coast Logistics Center', value: 'b0296720-80a5-48b2-b13c-0e6e7372d8a2' }
  ];

  constructor(
    private prService: PurchaseRequestService,
    private productService: ProductService,
    public authSvc: AuthService,
    private fb: FormBuilder,
    private msgSvc: MessageService
  ) {}

  ngOnInit(): void {
    this.companyId = this.authSvc.currentUser?.companyId ?? '';
    this.initForm();
    this.loadPRs();
    this.loadProducts();
  }

  initForm(): void {
    this.prForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(255)]],
      description: [''],
      requiredDate: [null],
      priority: ['MEDIUM', Validators.required],
      branchId: [this.branchOptions[0].value, Validators.required],
      items: this.fb.array([this.createItemFormGroup()])
    });
  }

  createItemFormGroup(): FormGroup {
    return this.fb.group({
      productId: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
      unitOfMeasure: ['PCS'],
      estimatedUnitPrice: [0],
      specifications: [''],
      notes: ['']
    });
  }

  get itemsFormArray(): FormArray {
    return this.prForm.get('items') as FormArray;
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
        estimatedUnitPrice: p.unitPrice || 0
      });
    }
  }

  loadPRs(page = this.currentPage): void {
    this.loading = true;
    this.prService.listPRs(this.companyId, undefined, this.statusFilter, this.searchTerm, page, this.pageSize)
      .subscribe({
        next: res => {
          this.prs = res.content;
          this.totalRecords = res.totalElements;
          this.currentPage = res.number;
          this.loading = false;
        },
        error: () => this.loading = false
      });
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
    this.prService.getPRById(id).subscribe({
      next: pr => {
        this.selectedPR = pr;
        this.showDetailDialog = true;
      }
    });
  }

  savePR(): void {
    if (this.prForm.invalid) {
      this.prForm.markAllAsTouched();
      return;
    }
    this.formLoading = true;
    this.prService.createPR(this.prForm.value).subscribe({
      next: () => {
        this.msgSvc.add({ severity: 'success', summary: 'Success', detail: 'Purchase Requisition drafted' });
        this.showFormDialog = false;
        this.formLoading = false;
        this.loadPRs();
      },
      error: err => {
        this.msgSvc.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Creation failed' });
        this.formLoading = false;
      }
    });
  }

  submitForApproval(id: string): void {
    this.prService.submitPR(id).subscribe({
      next: () => {
        this.msgSvc.add({ severity: 'success', summary: 'Submitted', detail: 'PR submitted to manager for approval' });
        if (this.selectedPR && this.selectedPR.id === id) this.selectedPR.status = 'SUBMITTED';
        this.loadPRs();
      }
    });
  }

  approve(id: string): void {
    this.prService.approvePR(id).subscribe({
      next: () => {
        this.msgSvc.add({ severity: 'success', summary: 'Approved', detail: 'Purchase Request Approved' });
        if (this.selectedPR) this.selectedPR.status = 'APPROVED';
        this.loadPRs();
      }
    });
  }

  openRejectModal(): void {
    this.rejectReason = '';
    this.showRejectDialog = true;
  }

  confirmReject(): void {
    if (!this.rejectReason.trim()) return;
    if (!this.selectedPR) return;
    this.prService.rejectPR(this.selectedPR.id, this.rejectReason).subscribe({
      next: () => {
        this.msgSvc.add({ severity: 'info', summary: 'Rejected', detail: 'Purchase Request Rejected' });
        this.showRejectDialog = false;
        this.selectedPR!.status = 'REJECTED';
        this.selectedPR!.rejectionReason = this.rejectReason;
        this.loadPRs();
      }
    });
  }

  getStatusSeverity(status: PRStatus): 'success' | 'warning' | 'danger' | 'info' | 'secondary' {
    switch (status) {
      case 'DRAFT': return 'secondary';
      case 'SUBMITTED': return 'info';
      case 'APPROVED': return 'success';
      case 'REJECTED': return 'danger';
      case 'CONVERTED': return 'warning';
      default: return 'secondary';
    }
  }

  getPrioritySeverity(p: PRPriority): 'success' | 'warning' | 'danger' | 'info' {
    switch (p) {
      case 'LOW': return 'info';
      case 'MEDIUM': return 'success';
      case 'HIGH': return 'warning';
      case 'URGENT': return 'danger';
    }
  }

  calculateTotal(): number {
    return this.itemsFormArray.controls.reduce((acc, ctrl) => {
      const qty = ctrl.get('quantity')?.value || 0;
      const price = ctrl.get('estimatedUnitPrice')?.value || 0;
      return acc + (qty * price);
    }, 0);
  }
}
