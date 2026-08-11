export type PRStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'CANCELLED' | 'CONVERTED';
export type PRPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface PRItemRequest {
  productId: string;
  quantity: number;
  unitOfMeasure?: string;
  estimatedUnitPrice?: number;
  specifications?: string;
  notes?: string;
}

export interface PRCreateRequest {
  title: string;
  description?: string;
  requiredDate?: string;
  priority?: PRPriority;
  branchId: string;
  items: PRItemRequest[];
}

export interface PRItemResponse {
  id: string;
  productId: string;
  productName: string;
  sku: string;
  quantity: number;
  unitOfMeasure?: string;
  estimatedUnitPrice?: number;
  estimatedTotalPrice?: number;
  specifications?: string;
  notes?: string;
}

export interface PRResponse {
  id: string;
  prNumber: string;
  title: string;
  description?: string;
  requiredDate?: string;
  totalAmount?: number;
  status: PRStatus;
  priority: PRPriority;
  rejectionReason?: string;
  requestedById?: string;
  requestedByName?: string;
  approvedById?: string;
  approvedByName?: string;
  approvedAt?: string;
  branchId?: string;
  branchName?: string;
  items: PRItemResponse[];
  createdAt: string;
}

export interface PRSummary {
  id: string;
  prNumber: string;
  title: string;
  requiredDate?: string;
  totalAmount?: number;
  status: PRStatus;
  priority: PRPriority;
  requestedByName?: string;
  branchName?: string;
  itemCount: number;
  createdAt: string;
}

export interface PRPage {
  content: PRSummary[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
