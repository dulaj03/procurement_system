import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  PRCreateRequest,
  PRResponse,
  PRPage,
  PRStatus
} from './purchase-request.model';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class PurchaseRequestService {

  private readonly base = `${environment.apiUrl}/purchase-requests`;

  constructor(private http: HttpClient) {}

  listPRs(
    companyId: string,
    branchId?: string,
    status?: PRStatus,
    search?: string,
    page = 0,
    size = 20
  ): Observable<PRPage> {
    let params = new HttpParams()
      .set('companyId', companyId)
      .set('page', page.toString())
      .set('size', size.toString());

    if (branchId) params = params.set('branchId', branchId);
    if (status) params = params.set('status', status);
    if (search) params = params.set('search', search);

    return this.http.get<ApiResponse<PRPage>>(this.base, { params })
      .pipe(map(r => r.data));
  }

  getPRById(id: string): Observable<PRResponse> {
    return this.http.get<ApiResponse<PRResponse>>(`${this.base}/${id}`)
      .pipe(map(r => r.data));
  }

  createPR(req: PRCreateRequest): Observable<PRResponse> {
    return this.http.post<ApiResponse<PRResponse>>(this.base, req)
      .pipe(map(r => r.data));
  }

  submitPR(id: string): Observable<PRResponse> {
    return this.http.patch<ApiResponse<PRResponse>>(`${this.base}/${id}/submit`, {})
      .pipe(map(r => r.data));
  }

  approvePR(id: string): Observable<PRResponse> {
    return this.http.patch<ApiResponse<PRResponse>>(`${this.base}/${id}/approve`, {})
      .pipe(map(r => r.data));
  }

  rejectPR(id: string, reason: string): Observable<PRResponse> {
    return this.http.patch<ApiResponse<PRResponse>>(`${this.base}/${id}/reject`, { reason })
      .pipe(map(r => r.data));
  }
}
