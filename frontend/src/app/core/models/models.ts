// ──────────────────────────────────────────────────────────
//  Core Models — TypeScript interfaces matching backend DTOs
// ──────────────────────────────────────────────────────────

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  errors?: Record<string, string>;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

// ── Auth ─────────────────────────────────────────────────────

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone?: string;
  employeeCode?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserInfo;
}

export interface UserInfo {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  roles: string[];
}

// ── Company ───────────────────────────────────────────────────

export interface Company {
  id: string;
  name: string;
  code: string;
  registrationNumber?: string;
  taxNumber?: string;
  address?: string;
  city?: string;
  country?: string;
  email?: string;
  phone?: string;
  logoUrl?: string;
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
  createdAt: string;
}

export interface Branch {
  id: string;
  name: string;
  code: string;
  address?: string;
  city?: string;
  country?: string;
  phone?: string;
  email?: string;
  status: 'ACTIVE' | 'INACTIVE';
  company: Company;
}

// ── User ──────────────────────────────────────────────────────

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  employeeCode?: string;
  status: 'ACTIVE' | 'INACTIVE' | 'LOCKED';
  roles: Role[];
  company?: Company;
  branch?: Branch;
  createdAt: string;
}

export interface Role {
  id: string;
  name: string;
  description?: string;
  permissions: Permission[];
}

export interface Permission {
  id: string;
  name: string;
  description?: string;
  module: string;
}

// ── Supplier ──────────────────────────────────────────────────

export interface Supplier {
  id: string;
  name: string;
  code: string;
  email?: string;
  phone?: string;
  address?: string;
  city?: string;
  country?: string;
  taxNumber?: string;
  registrationNumber?: string;
  website?: string;
  paymentTerms?: string;
  creditLimit?: number;
  rating?: number;
  status: 'ACTIVE' | 'INACTIVE' | 'BLACKLISTED';
  contacts?: SupplierContact[];
  createdAt: string;
}

export interface SupplierContact {
  id: string;
  name: string;
  designation?: string;
  email?: string;
  phone?: string;
  primary: boolean;
}

// ── Product ───────────────────────────────────────────────────

export interface Product {
  id: string;
  name: string;
  sku: string;
  barcode?: string;
  description?: string;
  category?: ProductCategory;
  unitOfMeasure: string;
  unitPrice?: number;
  reorderLevel?: number;
  reorderQuantity?: number;
  imageUrl?: string;
  status: 'ACTIVE' | 'INACTIVE' | 'DISCONTINUED';
  createdAt: string;
}

export interface ProductCategory {
  id: string;
  name: string;
  code: string;
  description?: string;
  parent?: ProductCategory;
}

// ── Inventory ─────────────────────────────────────────────────

export interface Inventory {
  id: string;
  product: Product;
  branch: Branch;
  quantityOnHand: number;
  quantityReserved: number;
  quantityOnOrder: number;
  availableQuantity: number;
  averageCost?: number;
  lowStock: boolean;
  updatedAt: string;
}

// ── Purchase Request ──────────────────────────────────────────

export type PRStatus = 'DRAFT' | 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED' | 'CANCELLED' | 'CONVERTED_TO_PO';
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface PurchaseRequest {
  id: string;
  prNumber: string;
  title: string;
  description?: string;
  requiredDate?: string;
  totalAmount?: number;
  status: PRStatus;
  priority: Priority;
  rejectionReason?: string;
  requestedBy: User;
  approvedBy?: User;
  approvedAt?: string;
  branch: Branch;
  items: PurchaseRequestItem[];
  createdAt: string;
}

export interface PurchaseRequestItem {
  id: string;
  product: Product;
  quantity: number;
  unitOfMeasure?: string;
  estimatedUnitPrice?: number;
  estimatedTotalPrice?: number;
  specifications?: string;
  notes?: string;
}

// ── Purchase Order ────────────────────────────────────────────

export type POStatus = 'DRAFT' | 'SENT' | 'ACKNOWLEDGED' | 'PARTIALLY_RECEIVED' | 'FULLY_RECEIVED' | 'CANCELLED' | 'CLOSED';

export interface PurchaseOrder {
  id: string;
  poNumber: string;
  supplier: Supplier;
  branch: Branch;
  orderDate: string;
  expectedDeliveryDate?: string;
  subtotal?: number;
  taxAmount: number;
  discountAmount: number;
  totalAmount?: number;
  currency: string;
  paymentTerms?: string;
  status: POStatus;
  items: PurchaseOrderItem[];
  createdAt: string;
}

export interface PurchaseOrderItem {
  id: string;
  product: Product;
  quantityOrdered: number;
  quantityReceived: number;
  unitPrice: number;
  discountPercent: number;
  taxPercent: number;
  totalPrice?: number;
  unitOfMeasure?: string;
}

// ── Invoice ───────────────────────────────────────────────────

export type InvoiceStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'PARTIALLY_PAID' | 'PAID' | 'OVERDUE' | 'CANCELLED';

export interface Invoice {
  id: string;
  invoiceNumber: string;
  supplierInvoiceNumber?: string;
  supplier: Supplier;
  purchaseOrder?: PurchaseOrder;
  invoiceDate: string;
  dueDate?: string;
  subtotal?: number;
  taxAmount: number;
  totalAmount: number;
  paidAmount: number;
  outstandingAmount: number;
  currency: string;
  status: InvoiceStatus;
  paymentDate?: string;
  overdue: boolean;
  createdAt: string;
}

// ── Dashboard ─────────────────────────────────────────────────

export interface DashboardStats {
  totalPurchaseRequests: number;
  pendingApprovals: number;
  openPurchaseOrders: number;
  lowStockItems: number;
  totalInventoryValue: number;
  overdueInvoices: number;
  monthlyPurchaseAmount: number;
  topSuppliers: TopSupplier[];
  recentPRs: PurchaseRequest[];
}

export interface TopSupplier {
  supplier: Supplier;
  totalOrderAmount: number;
  orderCount: number;
}

// ── Audit ─────────────────────────────────────────────────────

export interface AuditLog {
  id: string;
  entityType: string;
  entityId: string;
  action: string;
  performedBy: string;
  performedAt: string;
  oldValues?: string;
  newValues?: string;
  notes?: string;
}
