export type ProductStatus = 'ACTIVE' | 'INACTIVE' | 'DISCONTINUED';

export interface ProductCategory {
  id: string;
  name: string;
  code: string;
  description?: string;
  parentId?: string;
  parentName?: string;
}

export interface CategoryRequest {
  name: string;
  code: string;
  description?: string;
  parentId?: string;
}

export interface ProductSummary {
  id: string;
  name: string;
  sku: string;
  categoryName?: string;
  unitOfMeasure: string;
  unitPrice?: number;
  reorderLevel?: number;
  status: ProductStatus;
  imageUrl?: string;
}

export interface ProductDetail extends ProductSummary {
  barcode?: string;
  description?: string;
  categoryId?: string;
  reorderQuantity?: number;
  companyId?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductRequest {
  name: string;
  sku: string;
  barcode?: string;
  description?: string;
  categoryId?: string;
  unitOfMeasure: string;
  unitPrice?: number;
  reorderLevel?: number;
  reorderQuantity?: number;
  imageUrl?: string;
  status?: ProductStatus;
  companyId: string;
}

export interface ProductPage {
  content: ProductSummary[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
