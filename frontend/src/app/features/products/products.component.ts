import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';

import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { DialogModule } from 'primeng/dialog';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DividerModule } from 'primeng/divider';
import { TooltipModule } from 'primeng/tooltip';
import { InputNumberModule } from 'primeng/inputnumber';
import { MessageService, ConfirmationService } from 'primeng/api';

import { ProductService } from './product.service';
import { AuthService } from '../../core/auth/auth.service';
import { ProductSummary, ProductDetail, ProductCategory, ProductStatus, ProductPage } from './product.model';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule,
    TableModule, ButtonModule, InputTextModule, DropdownModule,
    DialogModule, TagModule, ToastModule, ConfirmDialogModule,
    DividerModule, TooltipModule, InputNumberModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './products.component.html',
  styleUrls: ['./products.component.scss']
})
export class ProductsComponent implements OnInit, OnDestroy {

  products: ProductSummary[] = [];
  categories: ProductCategory[] = [];
  selectedProduct: ProductDetail | null = null;

  totalRecords = 0;
  pageSize = 20;
  currentPage = 0;
  loading = false;

  searchTerm = '';
  statusFilter: ProductStatus | undefined;
  categoryFilter: string | undefined;
  companyId!: string;

  showFormDialog = false;
  showCategoryDialog = false;
  showDetailDialog = false;
  isEditMode = false;
  formLoading = false;

  productForm!: FormGroup;
  categoryForm!: FormGroup;

  statusOptions = [
    { label: 'All Statuses', value: undefined },
    { label: 'Active', value: 'ACTIVE' },
    { label: 'Inactive', value: 'INACTIVE' },
    { label: 'Discontinued', value: 'DISCONTINUED' }
  ];

  uomOptions = [
    { label: 'Pieces (PCS)', value: 'PCS' },
    { label: 'Kilograms (KG)', value: 'KG' },
    { label: 'Liters (L)', value: 'L' },
    { label: 'Meters (M)', value: 'M' },
    { label: 'Boxes (BOX)', value: 'BOX' },
    { label: 'Packs (PACK)', value: 'PACK' },
    { label: 'Hours (HRS)', value: 'HRS' }
  ];

  private search$ = new Subject<string>();
  private destroy$ = new Subject<void>();

  constructor(
    private productService: ProductService,
    private authSvc: AuthService,
    private fb: FormBuilder,
    private msgSvc: MessageService,
    private confirmSvc: ConfirmationService
  ) {}

  ngOnInit(): void {
    this.companyId = this.authSvc.currentUser?.companyId ?? '';
    this.initForms();
    this.loadCategories();
    this.loadProducts();

    this.search$.pipe(
      debounceTime(350),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(() => this.loadProducts(0));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  initForms(): void {
    this.productForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      sku: ['', [Validators.required, Validators.maxLength(100)]],
      barcode: [''],
      description: [''],
      categoryId: [null],
      unitOfMeasure: ['PCS', Validators.required],
      unitPrice: [0.00, [Validators.required, Validators.min(0)]],
      reorderLevel: [10, [Validators.min(0)]],
      reorderQuantity: [50, [Validators.min(0)]],
      imageUrl: [''],
      status: ['ACTIVE']
    });

    this.categoryForm = this.fb.group({
      name: ['', Validators.required],
      code: ['', Validators.required],
      description: [''],
      parentId: [null]
    });
  }

  loadCategories(): void {
    this.productService.getCategories().subscribe({
      next: cats => this.categories = cats,
      error: () => this.msgSvc.add({ severity: 'error', summary: 'Error', detail: 'Failed to load categories' })
    });
  }

  loadProducts(page = this.currentPage): void {
    this.loading = true;
    this.productService.listProducts(
      this.companyId,
      this.searchTerm || undefined,
      this.statusFilter,
      this.categoryFilter,
      page,
      this.pageSize
    ).subscribe({
      next: (res: ProductPage) => {
        this.products = res.content;
        this.totalRecords = res.totalElements;
        this.currentPage = res.number;
        this.loading = false;
      },
      error: () => {
        this.msgSvc.add({ severity: 'error', summary: 'Error', detail: 'Failed to load product catalog' });
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    this.search$.next(this.searchTerm);
  }

  onFilterChange(): void {
    this.loadProducts(0);
  }

  onPageChange(event: any): void {
    this.loadProducts(event.first / event.rows);
  }

  openCreate(): void {
    this.isEditMode = false;
    this.productForm.reset({
      unitOfMeasure: 'PCS',
      unitPrice: 0.00,
      reorderLevel: 10,
      reorderQuantity: 50,
      status: 'ACTIVE'
    });
    this.showFormDialog = true;
  }

  openEdit(id: string): void {
    this.productService.getProductById(id).subscribe({
      next: p => {
        this.isEditMode = true;
        this.selectedProduct = p;
        this.productForm.patchValue({
          name: p.name,
          sku: p.sku,
          barcode: p.barcode,
          description: p.description,
          categoryId: p.categoryId,
          unitOfMeasure: p.unitOfMeasure,
          unitPrice: p.unitPrice,
          reorderLevel: p.reorderLevel,
          reorderQuantity: p.reorderQuantity,
          imageUrl: p.imageUrl,
          status: p.status
        });
        this.showFormDialog = true;
      },
      error: () => this.msgSvc.add({ severity: 'error', summary: 'Error', detail: 'Failed to load product details' })
    });
  }

  viewDetail(id: string): void {
    this.productService.getProductById(id).subscribe({
      next: p => {
        this.selectedProduct = p;
        this.showDetailDialog = true;
      }
    });
  }

  saveProduct(): void {
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }
    const payload = { ...this.productForm.value, companyId: this.companyId };
    this.formLoading = true;

    const req$ = this.isEditMode && this.selectedProduct
      ? this.productService.updateProduct(this.selectedProduct.id, payload)
      : this.productService.createProduct(payload);

    req$.subscribe({
      next: () => {
        this.msgSvc.add({ severity: 'success', summary: 'Success', detail: this.isEditMode ? 'Product updated' : 'Product created' });
        this.showFormDialog = false;
        this.formLoading = false;
        this.loadProducts();
      },
      error: err => {
        this.msgSvc.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Operation failed' });
        this.formLoading = false;
      }
    });
  }

  saveCategory(): void {
    if (this.categoryForm.invalid) {
      this.categoryForm.markAllAsTouched();
      return;
    }
    this.productService.createCategory(this.categoryForm.value).subscribe({
      next: () => {
        this.msgSvc.add({ severity: 'success', summary: 'Success', detail: 'Category added' });
        this.showCategoryDialog = false;
        this.categoryForm.reset();
        this.loadCategories();
      },
      error: err => this.msgSvc.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Failed to add category' })
    });
  }

  confirmDelete(p: ProductSummary): void {
    this.confirmSvc.confirm({
      message: `Delete product <strong>${p.name}</strong> (${p.sku})?`,
      header: 'Confirm Delete',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.productService.deleteProduct(p.id).subscribe({
          next: () => {
            this.msgSvc.add({ severity: 'success', summary: 'Success', detail: 'Product deleted' });
            this.loadProducts();
          }
        });
      }
    });
  }

  getStatusSeverity(status: ProductStatus): 'success' | 'warning' | 'danger' {
    switch (status) {
      case 'ACTIVE': return 'success';
      case 'INACTIVE': return 'warning';
      case 'DISCONTINUED': return 'danger';
    }
  }

  isInvalid(field: string): boolean {
    const c = this.productForm.get(field);
    return !!(c?.invalid && c?.touched);
  }
}
