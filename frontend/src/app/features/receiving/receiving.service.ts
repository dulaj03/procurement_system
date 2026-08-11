import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  GRNCreateRequest,
  GRNResponse,
  GRNPage
} from './receiving.model';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class ReceivingService {

  private readonly base = `${environment.apiUrl}/grns`;

  constructor(private http: HttpClient) {}

  listGRNs(companyId: string, search?: string, page = 0, size = 20): Observable<GRNPage> {
    let params = new HttpParams()
      .set('companyId', companyId)
      .set('page', page.toString())
      .set('size', size.toString());

    if (search) params = params.set('search', search);

    return this.http.get<ApiResponse<GRNPage>>(this.base, { params })
      .pipe(map(r => r.data));
  }

  getGRNById(id: string): Observable<GRNResponse> {
    return this.http.get<ApiResponse<GRNResponse>>(`${this.base}/${id}`)
      .pipe(map(r => r.data));
  }

  createAndPostGRN(req: GRNCreateRequest): Observable<GRNResponse> {
    return this.http.post<ApiResponse<GRNResponse>>(this.base, req)
      .pipe(map(r => r.data));
  }
}
