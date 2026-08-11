export type GRNStatus = 'DRAFT' | 'CONFIRMED' | 'POSTED';

export interface GRNItemRequest {
  poItemId: string;
  productId: string;
  quantityReceived: number;
  quantityAccepted: number;
  quantityRejected?: number;
  unitCost?: number;
  rejectionReason?: string;
  batchNumber?: string;
  expiryDate?: string;
  notes?: string;
}

export interface GRNCreateRequest {
  purchaseOrderId: string;
  branchId: string;
  receiptDate: string;
  supplierInvoiceNumber?: string;
  deliveryNoteNumber?: string;
  notes?: string;
  items: GRNItemRequest[];
}

export interface GRNItemResponse {
  id: string;
  poItemId: string;
  productId: string;
  productName: string;
  sku: string;
  quantityReceived: number;
  quantityAccepted: number;
  quantityRejected?: number;
  unitCost?: number;
  rejectionReason?: string;
  batchNumber?: string;
  expiryDate?: string;
}

export interface GRNResponse {
  id: string;
  grnNumber: string;
  purchaseOrderId: string;
  poNumber: string;
  supplierName: string;
  branchId: string;
  branchName: string;
  receiptDate: string;
  supplierInvoiceNumber?: string;
  deliveryNoteNumber?: string;
  status: GRNStatus;
  receivedByName?: string;
  items: GRNItemResponse[];
  createdAt: string;
}

export interface GRNPage {
  content: GRNResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
