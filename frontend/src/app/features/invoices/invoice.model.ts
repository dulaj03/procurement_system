export type InvoiceStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'PARTIALLY_PAID' | 'PAID' | 'OVERDUE' | 'CANCELLED';

export interface InvoiceCreateRequest {
  supplierId: string;
  purchaseOrderId?: string;
  supplierInvoiceNumber?: string;
  invoiceDate: string;
  dueDate?: string;
  totalAmount: number;
  taxAmount?: number;
  currency?: string;
  notes?: string;
}

export interface PaymentRecordRequest {
  amount: number;
  paymentDate: string;
  referenceNumber?: string;
}

export interface InvoiceResponse {
  id: string;
  invoiceNumber: string;
  supplierInvoiceNumber?: string;
  supplierId: string;
  supplierName: string;
  purchaseOrderId?: string;
  poNumber?: string;
  invoiceDate: string;
  dueDate?: string;
  subtotal?: number;
  taxAmount?: number;
  totalAmount: number;
  paidAmount: number;
  outstandingAmount: number;
  currency: string;
  status: InvoiceStatus;
  overdue: boolean;
  paymentDate?: string;
  notes?: string;
  createdAt: string;
}

export interface InvoicePage {
  content: InvoiceResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
