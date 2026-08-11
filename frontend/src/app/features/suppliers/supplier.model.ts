export type SupplierStatus = 'ACTIVE' | 'INACTIVE' | 'BLACKLISTED';

export interface SupplierContact {
  id?: string;
  name: string;
  designation?: string;
  email?: string;
  phone?: string;
  primary: boolean;
}

export interface SupplierSummary {
  id: string;
  name: string;
  code: string;
  email?: string;
  phone?: string;
  city?: string;
  country?: string;
  rating?: number;
  status: SupplierStatus;
  contactCount: number;
}

export interface SupplierDetail extends SupplierSummary {
  address?: string;
  taxNumber?: string;
  registrationNumber?: string;
  website?: string;
  paymentTerms?: string;
  creditLimit?: number;
  companyId?: string;
  contacts: SupplierContact[];
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
}

export interface SupplierRequest {
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
  status?: SupplierStatus;
  companyId: string;
  contacts?: SupplierContact[];
}

export interface SupplierPage {
  content: SupplierSummary[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
