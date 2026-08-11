import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  POCreateRequest,
  POResponse,
  POPage,
  POStatus
} from './purchase-order.model';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class PurchaseOrderService {

  private readonly base = `${environment.apiUrl}/purchase-orders`;

  constructor(private http: HttpClient) {}

  listPOs(
    companyId: string,
    supplierId?: string,
    status?: POStatus,
    search?: string,
    page = 0,
    size = 20
  ): Observable<POPage> {
    let params = new HttpParams()
      .set('companyId', companyId)
      .set('page', page.toString())
      .set('size', size.toString());

    if (supplierId) params = params.set('supplierId', supplierId);
    if (status) params = params.set('status', status);
    if (search) params = params.set('search', search);

    return this.http.get<ApiResponse<POPage>>(this.base, { params })
      .pipe(map(r => r.data));
  }

  getPOById(id: string): Observable<POResponse> {
    return this.http.get<ApiResponse<POResponse>>(`${this.base}/${id}`)
      .pipe(map(r => r.data));
  }

  createPO(req: POCreateRequest): Observable<POResponse> {
    return this.http.post<ApiResponse<POResponse>>(this.base, req)
      .pipe(map(r => r.data));
  }

  updateStatus(id: string, status: POStatus): Observable<POResponse> {
    const params = new HttpParams().set('status', status);
    return this.http.patch<ApiResponse<POResponse>>(`${this.base}/${id}/status`, {}, { params })
      .pipe(map(r => r.data));
  }
}
