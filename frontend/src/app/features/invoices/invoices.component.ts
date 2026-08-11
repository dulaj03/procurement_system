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
import { DividerModule } from 'primeng/divider';
import { InputNumberModule } from 'primeng/inputnumber';
import { TooltipModule } from 'primeng/tooltip';
import { MessageService } from 'primeng/api';

import { InvoiceService } from './invoice.service';
import { SupplierService } from '../suppliers/supplier.service';
import { AuthService } from '../../core/auth/auth.service';
import { InvoiceResponse, InvoiceStatus } from './invoice.model';
import { SupplierSummary } from '../suppliers/supplier.model';

@Component({
  selector: 'app-invoices',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule,
    TableModule, ButtonModule, InputTextModule, DropdownModule,
    DialogModule, TagModule, ToastModule, DividerModule,
    InputNumberModule, TooltipModule
  ],
  providers: [MessageService],
  templateUrl: './invoices.component.html',
  styleUrls: ['./invoices.component.scss']
})
export class InvoicesComponent implements OnInit {

  invoices: InvoiceResponse[] = [];
  selectedInvoice: InvoiceResponse | null = null;
  suppliers: SupplierSummary[] = [];

  totalRecords = 0;
  pageSize = 20;
  currentPage = 0;
  loading = false;
  companyId!: string;

  searchTerm = '';
  statusFilter: InvoiceStatus | undefined;

  showFormDialog = false;
  showPayDialog = false;
  formLoading = false;

  invoiceForm!: FormGroup;
  paymentForm!: FormGroup;

  statusOptions = [
    { label: 'All Statuses', value: undefined },
    { label: 'Approved', value: 'APPROVED' },
    { label: 'Partially Paid', value: 'PARTIALLY_PAID' },
    { label: 'Paid', value: 'PAID' },
    { label: 'Overdue', value: 'OVERDUE' }
  ];

  constructor(
    private invoiceService: InvoiceService,
    private supplierService: SupplierService,
    private authSvc: AuthService,
    private fb: FormBuilder,
    private msgSvc: MessageService
  ) {}

  ngOnInit(): void {
    this.companyId = this.authSvc.currentUser?.companyId ?? '';
    this.initForms();
    this.loadInvoices();
    this.loadSuppliers();
  }

  initForms(): void {
    this.invoiceForm = this.fb.group({
      supplierId: ['', Validators.required],
      supplierInvoiceNumber: [''],
      invoiceDate: [new Date().toISOString().split('T')[0], Validators.required],
      dueDate: [''],
      totalAmount: [0, [Validators.required, Validators.min(0.01)]],
      taxAmount: [0],
      currency: ['USD'],
      notes: ['']
    });

    this.paymentForm = this.fb.group({
      amount: [0, [Validators.required, Validators.min(0.01)]],
      paymentDate: [new Date().toISOString().split('T')[0], Validators.required],
      referenceNumber: ['']
    });
  }

  loadInvoices(page = this.currentPage): void {
    this.loading = true;
    this.invoiceService.listInvoices(this.companyId, undefined, this.statusFilter, this.searchTerm, page, this.pageSize)
      .subscribe({
        next: res => {
          this.invoices = res.content;
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

  openCreate(): void {
    this.initForms();
    this.showFormDialog = true;
  }

  openPayModal(inv: InvoiceResponse): void {
    this.selectedInvoice = inv;
    this.paymentForm.patchValue({
      amount: inv.outstandingAmount,
      paymentDate: new Date().toISOString().split('T')[0],
      referenceNumber: 'PAY-' + SystemRef()
    });
    this.showPayDialog = true;
  }

  saveInvoice(): void {
    if (this.invoiceForm.invalid) {
      this.invoiceForm.markAllAsTouched();
      return;
    }
    this.formLoading = true;
    this.invoiceService.createInvoice(this.invoiceForm.value).subscribe({
      next: () => {
        this.msgSvc.add({ severity: 'success', summary: 'Success', detail: 'Supplier Invoice recorded' });
        this.showFormDialog = false;
        this.formLoading = false;
        this.loadInvoices();
      },
      error: err => {
        this.msgSvc.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Creation failed' });
        this.formLoading = false;
      }
    });
  }

  submitPayment(): void {
    if (this.paymentForm.invalid || !this.selectedInvoice) return;
    this.formLoading = true;
    this.invoiceService.recordPayment(this.selectedInvoice.id, this.paymentForm.value).subscribe({
      next: () => {
        this.msgSvc.add({ severity: 'success', summary: 'Payment Recorded', detail: 'Payment applied to invoice' });
        this.showPayDialog = false;
        this.formLoading = false;
        this.loadInvoices();
      },
      error: err => {
        this.msgSvc.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Payment failed' });
        this.formLoading = false;
      }
    });
  }

  getStatusSeverity(status: InvoiceStatus): 'success' | 'warning' | 'danger' | 'info' | 'secondary' {
    switch (status) {
      case 'DRAFT': return 'secondary';
      case 'APPROVED': return 'info';
      case 'PARTIALLY_PAID': return 'warning';
      case 'PAID': return 'success';
      case 'OVERDUE': return 'danger';
      default: return 'secondary';
    }
  }
}

function SystemRef(): string {
  return Math.floor(1000 + Math.random() * 9000).toString();
}
