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

import { ReceivingService } from './receiving.service';
import { PurchaseOrderService } from '../purchase-orders/purchase-order.service';
import { AuthService } from '../../core/auth/auth.service';
import { GRNResponse } from './receiving.model';
import { POSummary, POResponse } from '../purchase-orders/purchase-order.model';

@Component({
  selector: 'app-receiving',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule,
    TableModule, ButtonModule, InputTextModule, DropdownModule,
    DialogModule, TagModule, ToastModule, DividerModule,
    InputNumberModule, CalendarModule, TooltipModule
  ],
  providers: [MessageService],
  templateUrl: './receiving.component.html',
  styleUrls: ['./receiving.component.scss']
})
export class ReceivingComponent implements OnInit {

  grns: GRNResponse[] = [];
  openPOs: POSummary[] = [];
  selectedGRN: GRNResponse | null = null;
  selectedPOForReceiving: POResponse | null = null;

  totalRecords = 0;
  pageSize = 20;
  currentPage = 0;
  loading = false;
  companyId!: string;

  searchTerm = '';
  showReceiveDialog = false;
  showDetailDialog = false;
  formLoading = false;

  grnForm!: FormGroup;

  branchOptions = [
    { label: 'Main Warehouse (HQ)', value: 'b0196720-80a5-48b2-b13c-0e6e7372d8a1' },
    { label: 'West Coast Logistics Center', value: 'b0296720-80a5-48b2-b13c-0e6e7372d8a2' }
  ];

  constructor(
    private receivingService: ReceivingService,
    private poService: PurchaseOrderService,
    private authSvc: AuthService,
    private fb: FormBuilder,
    private msgSvc: MessageService
  ) {}

  ngOnInit(): void {
    this.companyId = this.authSvc.currentUser?.companyId ?? '';
    this.initForm();
    this.loadGRNs();
    this.loadOpenPOs();
  }

  initForm(): void {
    this.grnForm = this.fb.group({
      purchaseOrderId: ['', Validators.required],
      branchId: [this.branchOptions[0].value, Validators.required],
      receiptDate: [new Date().toISOString().split('T')[0], Validators.required],
      supplierInvoiceNumber: [''],
      deliveryNoteNumber: [''],
      notes: [''],
      items: this.fb.array([])
    });
  }

  get itemsFormArray(): FormArray {
    return this.grnForm.get('items') as FormArray;
  }

  loadGRNs(page = this.currentPage): void {
    this.loading = true;
    this.receivingService.listGRNs(this.companyId, this.searchTerm, page, this.pageSize)
      .subscribe({
        next: res => {
          this.grns = res.content;
          this.totalRecords = res.totalElements;
          this.currentPage = res.number;
          this.loading = false;
        },
        error: () => this.loading = false
      });
  }

  loadOpenPOs(): void {
    this.poService.listPOs(this.companyId, undefined, 'SENT', undefined, 0, 100)
      .subscribe({ next: res => this.openPOs = res.content });
  }

  onPOSelected(poId: string): void {
    if (!poId) return;
    this.poService.getPOById(poId).subscribe({
      next: po => {
        this.selectedPOForReceiving = po;
        this.itemsFormArray.clear();

        po.items.forEach(item => {
          const remaining = item.quantityOrdered - item.quantityReceived;
          this.itemsFormArray.push(this.fb.group({
            poItemId: [item.id, Validators.required],
            productId: [item.productId, Validators.required],
            productName: [item.productName],
            sku: [item.sku],
            quantityReceived: [remaining > 0 ? remaining : 0, [Validators.required, Validators.min(0)]],
            quantityAccepted: [remaining > 0 ? remaining : 0, [Validators.required, Validators.min(0)]],
            quantityRejected: [0, [Validators.min(0)]],
            unitCost: [item.unitPrice],
            rejectionReason: [''],
            batchNumber: [''],
            notes: ['']
          }));
        });
      }
    });
  }

  openReceiveDialog(): void {
    this.initForm();
    this.selectedPOForReceiving = null;
    this.showReceiveDialog = true;
  }

  viewDetail(id: string): void {
    this.receivingService.getGRNById(id).subscribe({
      next: grn => {
        this.selectedGRN = grn;
        this.showDetailDialog = true;
      }
    });
  }

  submitGRN(): void {
    if (this.grnForm.invalid || this.itemsFormArray.length === 0) {
      this.grnForm.markAllAsTouched();
      return;
    }
    this.formLoading = true;
    this.receivingService.createAndPostGRN(this.grnForm.value).subscribe({
      next: () => {
        this.msgSvc.add({ severity: 'success', summary: 'Success', detail: 'Goods received note posted. Inventory stock updated!' });
        this.showReceiveDialog = false;
        this.formLoading = false;
        this.loadGRNs();
        this.loadOpenPOs();
      },
      error: err => {
        this.msgSvc.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'GRN posting failed' });
        this.formLoading = false;
      }
    });
  }
}
