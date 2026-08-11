import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  SupplierDetail,
  SupplierPage,
  SupplierRequest,
  SupplierStatus,
  SupplierContact
} from './supplier.model';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class SupplierService {

  private readonly base = `${environment.apiUrl}/suppliers`;

  constructor(private http: HttpClient) {}

  list(companyId: string, search?: string, status?: SupplierStatus, page = 0, size = 20): Observable<SupplierPage> {
    let params = new HttpParams()
      .set('companyId', companyId)
      .set('page', page.toString())
      .set('size', size.toString());
    if (search) params = params.set('search', search);
    if (status) params = params.set('status', status);

    return this.http.get<ApiResponse<SupplierPage>>(this.base, { params })
      .pipe(map(r => r.data));
  }

  getById(id: string): Observable<SupplierDetail> {
    return this.http.get<ApiResponse<SupplierDetail>>(`${this.base}/${id}`)
      .pipe(map(r => r.data));
  }

  create(request: SupplierRequest): Observable<SupplierDetail> {
    return this.http.post<ApiResponse<SupplierDetail>>(this.base, request)
      .pipe(map(r => r.data));
  }

  update(id: string, request: SupplierRequest): Observable<SupplierDetail> {
    return this.http.put<ApiResponse<SupplierDetail>>(`${this.base}/${id}`, request)
      .pipe(map(r => r.data));
  }

  delete(id: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.base}/${id}`)
      .pipe(map(() => void 0));
  }

  rate(id: string, rating: number): Observable<SupplierDetail> {
    return this.http.patch<ApiResponse<SupplierDetail>>(`${this.base}/${id}/rate`, { rating })
      .pipe(map(r => r.data));
  }

  addContact(supplierId: string, contact: SupplierContact): Observable<SupplierDetail> {
    return this.http.post<ApiResponse<SupplierDetail>>(`${this.base}/${supplierId}/contacts`, contact)
      .pipe(map(r => r.data));
  }

  deleteContact(supplierId: string, contactId: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.base}/${supplierId}/contacts/${contactId}`)
      .pipe(map(() => void 0));
  }
}
