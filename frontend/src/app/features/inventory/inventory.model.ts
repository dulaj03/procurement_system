export interface InventoryItem {
  id: string;
  productId: string;
  productName: string;
  sku: string;
  unitOfMeasure: string;
  branchId: string;
  branchName: string;
  quantityOnHand: number;
  quantityReserved: number;
  quantityOnOrder: number;
  availableQuantity: number;
  averageCost?: number;
  reorderLevel?: number;
  lowStock: boolean;
  updatedAt?: string;
}

export interface StockAdjustRequest {
  productId: string;
  branchId: string;
  quantity: number;
  reason: string;
  referenceNumber?: string;
}

export interface StockTransferRequest {
  productId: string;
  fromBranchId: string;
  toBranchId: string;
  quantity: number;
  notes?: string;
}

export interface LowStockAlert {
  productId: string;
  productName: string;
  sku: string;
  branchId: string;
  branchName: string;
  quantityOnHand: number;
  reorderLevel: number;
}

export interface StockMovement {
  id: string;
  productName: string;
  sku: string;
  movementType: string;
  quantity: number;
  fromBranch?: string;
  toBranch?: string;
  referenceNumber?: string;
  notes?: string;
  createdAt: string;
  createdBy?: string;
}

export interface MovementPage {
  content: StockMovement[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
