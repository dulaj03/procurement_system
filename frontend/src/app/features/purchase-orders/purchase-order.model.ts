export type POStatus = 'DRAFT' | 'SENT' | 'ACKNOWLEDGED' | 'PARTIALLY_RECEIVED' | 'RECEIVED' | 'CANCELLED' | 'CLOSED';

export interface POItemRequest {
  productId: string;
  quantityOrdered: number;
  unitPrice: number;
  discountPercent?: number;
  taxPercent?: number;
  unitOfMeasure?: string;
  notes?: string;
}

export interface POCreateRequest {
  purchaseRequestId?: string;
  supplierId: string;
  branchId: string;
  orderDate: string;
  expectedDeliveryDate?: string;
  deliveryAddress?: string;
  paymentTerms?: string;
  currency?: string;
  notes?: string;
  items: POItemRequest[];
}

export interface POItemResponse {
  id: string;
  productId: string;
  productName: string;
  sku: string;
  quantityOrdered: number;
  quantityReceived: number;
  unitPrice: number;
  discountPercent?: number;
  taxPercent?: number;
  totalPrice: number;
  unitOfMeasure?: string;
}

export interface POResponse {
  id: string;
  poNumber: string;
  purchaseRequestId?: string;
  prNumber?: string;
  supplierId: string;
  supplierName: string;
  branchId: string;
  branchName: string;
  orderDate: string;
  expectedDeliveryDate?: string;
  deliveryAddress?: string;
  subtotal?: number;
  taxAmount?: number;
  discountAmount?: number;
  totalAmount?: number;
  currency: string;
  paymentTerms?: string;
  status: POStatus;
  items: POItemResponse[];
  createdAt: string;
}

export interface POSummary {
  id: string;
  poNumber: string;
  supplierName: string;
  branchName: string;
  orderDate: string;
  totalAmount?: number;
  currency: string;
  status: POStatus;
  itemCount: number;
  createdAt: string;
}

export interface POPage {
  content: POSummary[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
