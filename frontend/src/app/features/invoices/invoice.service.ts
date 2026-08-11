import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  InvoiceCreateRequest,
  PaymentRecordRequest,
  InvoiceResponse,
  InvoicePage,
  InvoiceStatus
} from './invoice.model';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class InvoiceService {

  private readonly base = `${environment.apiUrl}/invoices`;

  constructor(private http: HttpClient) {}

  listInvoices(
    companyId: string,
    supplierId?: string,
    status?: InvoiceStatus,
    search?: string,
    page = 0,
    size = 20
  ): Observable<InvoicePage> {
    let params = new HttpParams()
      .set('companyId', companyId)
      .set('page', page.toString())
      .set('size', size.toString());

    if (supplierId) params = params.set('supplierId', supplierId);
    if (status) params = params.set('status', status);
    if (search) params = params.set('search', search);

    return this.http.get<ApiResponse<InvoicePage>>(this.base, { params })
      .pipe(map(r => r.data));
  }

  getInvoiceById(id: string): Observable<InvoiceResponse> {
    return this.http.get<ApiResponse<InvoiceResponse>>(`${this.base}/${id}`)
      .pipe(map(r => r.data));
  }

  createInvoice(req: InvoiceCreateRequest): Observable<InvoiceResponse> {
    return this.http.post<ApiResponse<InvoiceResponse>>(this.base, req)
      .pipe(map(r => r.data));
  }

  recordPayment(id: string, req: PaymentRecordRequest): Observable<InvoiceResponse> {
    return this.http.post<ApiResponse<InvoiceResponse>>(`${this.base}/${id}/pay`, req)
      .pipe(map(r => r.data));
  }
}
