import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule }  from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';

// PrimeNG
import { TableModule }        from 'primeng/table';
import { ButtonModule }       from 'primeng/button';
import { InputTextModule }    from 'primeng/inputtext';
import { DropdownModule }     from 'primeng/dropdown';
import { DialogModule }       from 'primeng/dialog';
import { TagModule }          from 'primeng/tag';
import { ToastModule }        from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { RatingModule }       from 'primeng/rating';
import { DividerModule }      from 'primeng/divider';
import { ChipModule }         from 'primeng/chip';
import { TooltipModule }      from 'primeng/tooltip';
import { InputNumberModule }  from 'primeng/inputnumber';
import { CheckboxModule }     from 'primeng/checkbox';
import { MessageService }     from 'primeng/api';
import { ConfirmationService } from 'primeng/api';

import { SupplierService } from './supplier.service';
import { AuthService }     from '../../core/auth/auth.service';
import { SupplierSummary, SupplierDetail, SupplierStatus, SupplierPage } from './supplier.model';

@Component({
  selector: 'app-suppliers',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule,
    TableModule, ButtonModule, InputTextModule, DropdownModule,
    DialogModule, TagModule, ToastModule, ConfirmDialogModule,
    RatingModule, DividerModule, ChipModule, TooltipModule,
    InputNumberModule, CheckboxModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './suppliers.component.html',
  styleUrls: ['./suppliers.component.scss']
})
export class SuppliersComponent implements OnInit, OnDestroy {

  suppliers: SupplierSummary[] = [];
  selectedSupplier: SupplierDetail | null = null;

  // Pagination
  totalRecords = 0;
  pageSize     = 20;
  currentPage  = 0;
  loading      = false;

  // Filters
  searchTerm   = '';
  statusFilter: SupplierStatus | undefined;
  companyId!: string;

  // Dialog states
  showFormDialog   = false;
  showDetailDialog = false;
  isEditMode       = false;
  formLoading      = false;

  supplierForm!: FormGroup;

  statusOptions = [
    { label: 'All Status',   value: undefined },
    { label: 'Active',       value: 'ACTIVE' as SupplierStatus },
    { label: 'Inactive',     value: 'INACTIVE' as SupplierStatus },
    { label: 'Blacklisted',  value: 'BLACKLISTED' as SupplierStatus }
  ];

  private search$ = new Subject<string>();
  private destroy$ = new Subject<void>();

  constructor(
    private supplierSvc: SupplierService,
    private authSvc:     AuthService,
    private fb:          FormBuilder,
    private msgSvc:      MessageService,
    private confirmSvc:  ConfirmationService
  ) {}

  ngOnInit(): void {
    this.companyId = this.authSvc.currentUser?.companyId ?? '';
    this.buildForm();
    this.loadSuppliers();

    this.search$.pipe(
      debounceTime(350),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(() => this.loadSuppliers(0));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ── DATA ──────────────────────────────────────────────────────

  loadSuppliers(page = this.currentPage): void {
    this.loading = true;
    this.supplierSvc.list(this.companyId, this.searchTerm || undefined, this.statusFilter, page, this.pageSize)
      .subscribe({
        next: (res: SupplierPage) => {
          this.suppliers     = res.content;
          this.totalRecords  = res.totalElements;
          this.currentPage   = res.number;
          this.loading       = false;
        },
        error: () => {
          this.showError('Failed to load suppliers');
          this.loading = false;
        }
      });
  }

  onPageChange(event: any): void {
    this.loadSuppliers(event.first / event.rows);
  }

  onSearch(): void {
    this.search$.next(this.searchTerm);
  }

  onStatusFilter(): void {
    this.loadSuppliers(0);
  }

  // ── VIEW DETAIL ───────────────────────────────────────────────

  viewDetail(id: string): void {
    this.supplierSvc.getById(id).subscribe({
      next: s => {
        this.selectedSupplier = s;
        this.showDetailDialog = true;
      },
      error: () => this.showError('Could not load supplier details')
    });
  }

  // ── FORM ──────────────────────────────────────────────────────

  buildForm(supplier?: SupplierDetail): void {
    this.supplierForm = this.fb.group({
      name:               [supplier?.name ?? '',        [Validators.required, Validators.maxLength(255)]],
      code:               [supplier?.code ?? '',        [Validators.required, Validators.maxLength(50)]],
      email:              [supplier?.email ?? '',       [Validators.email]],
      phone:              [supplier?.phone ?? ''],
      address:            [supplier?.address ?? ''],
      city:               [supplier?.city ?? ''],
      country:            [supplier?.country ?? ''],
      taxNumber:          [supplier?.taxNumber ?? ''],
      registrationNumber: [supplier?.registrationNumber ?? ''],
      website:            [supplier?.website ?? ''],
      paymentTerms:       [supplier?.paymentTerms ?? ''],
      creditLimit:        [supplier?.creditLimit ?? null],
      status:             [supplier?.status ?? 'ACTIVE'],
      contacts:           this.fb.array(
        (supplier?.contacts ?? []).map(c => this.buildContactGroup(c))
      )
    });
  }

  buildContactGroup(c?: any): FormGroup {
    return this.fb.group({
      id:          [c?.id ?? null],
      name:        [c?.name ?? '', Validators.required],
      designation: [c?.designation ?? ''],
      email:       [c?.email ?? '', Validators.email],
      phone:       [c?.phone ?? ''],
      primary:     [c?.primary ?? false]
    });
  }

  get contacts(): FormArray {
    return this.supplierForm.get('contacts') as FormArray;
  }

  addContactRow(): void {
    this.contacts.push(this.buildContactGroup());
  }

  removeContactRow(index: number): void {
    this.contacts.removeAt(index);
  }

  openCreate(): void {
    this.isEditMode = false;
    this.buildForm();
    this.showFormDialog = true;
  }

  openEdit(id: string): void {
    this.supplierSvc.getById(id).subscribe({
      next: s => {
        this.isEditMode = true;
        this.selectedSupplier = s;
        this.buildForm(s);
        this.showFormDialog = true;
      },
      error: () => this.showError('Could not load supplier for editing')
    });
  }

  submitForm(): void {
    if (this.supplierForm.invalid) {
      this.supplierForm.markAllAsTouched();
      return;
    }

    const payload = { ...this.supplierForm.value, companyId: this.companyId };
    this.formLoading = true;

    const req$ = this.isEditMode
      ? this.supplierSvc.update(this.selectedSupplier!.id, payload)
      : this.supplierSvc.create(payload);

    req$.subscribe({
      next: () => {
        this.showSuccess(this.isEditMode ? 'Supplier updated' : 'Supplier created');
        this.showFormDialog = false;
        this.formLoading    = false;
        this.loadSuppliers();
      },
      error: err => {
        this.showError(err?.error?.message ?? 'Operation failed');
        this.formLoading = false;
      }
    });
  }

  // ── RATING ────────────────────────────────────────────────────

  onRate(id: string, rating: number): void {
    this.supplierSvc.rate(id, rating).subscribe({
      next: () => {
        this.loadSuppliers();
        this.showSuccess('Rating saved');
      },
      error: () => this.showError('Rating update failed')
    });
  }

  // ── DELETE ────────────────────────────────────────────────────

  confirmDelete(supplier: SupplierSummary): void {
    this.confirmSvc.confirm({
      message: `Delete supplier <strong>${supplier.name}</strong>? This cannot be undone.`,
      header:  'Confirm Delete',
      icon:    'pi pi-exclamation-triangle',
      accept: () => {
        this.supplierSvc.delete(supplier.id).subscribe({
          next:  () => { this.showSuccess('Supplier deleted'); this.loadSuppliers(); },
          error: () => this.showError('Delete failed')
        });
      }
    });
  }

  // ── HELPERS ───────────────────────────────────────────────────

  getStatusSeverity(status: SupplierStatus): 'success' | 'warning' | 'danger' | 'info' {
    const map: Record<SupplierStatus, 'success' | 'warning' | 'danger' | 'info'> = {
      ACTIVE: 'success', INACTIVE: 'warning', BLACKLISTED: 'danger'
    };
    return map[status];
  }

  isInvalid(field: string): boolean {
    const c = this.supplierForm.get(field);
    return !!(c?.invalid && c?.touched);
  }

  private showSuccess(msg: string) {
    this.msgSvc.add({ severity: 'success', summary: 'Success', detail: msg, life: 3000 });
  }

  private showError(msg: string) {
    this.msgSvc.add({ severity: 'error', summary: 'Error', detail: msg, life: 4000 });
  }
}
