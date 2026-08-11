import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  InventoryItem,
  StockAdjustRequest,
  StockTransferRequest,
  LowStockAlert,
  MovementPage
} from './inventory.model';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class InventoryService {

  private readonly base = `${environment.apiUrl}/inventory`;

  constructor(private http: HttpClient) {}

  getInventory(companyId: string, branchId?: string, lowStockOnly = false): Observable<InventoryItem[]> {
    let params = new HttpParams()
      .set('companyId', companyId)
      .set('lowStockOnly', lowStockOnly.toString());

    if (branchId) params = params.set('branchId', branchId);

    return this.http.get<ApiResponse<InventoryItem[]>>(this.base, { params })
      .pipe(map(r => r.data));
  }

  getLowStockAlerts(companyId: string): Observable<LowStockAlert[]> {
    const params = new HttpParams().set('companyId', companyId);
    return this.http.get<ApiResponse<LowStockAlert[]>>(`${this.base}/low-stock`, { params })
      .pipe(map(r => r.data));
  }

  adjustStock(req: StockAdjustRequest): Observable<InventoryItem> {
    return this.http.post<ApiResponse<InventoryItem>>(`${this.base}/adjust`, req)
      .pipe(map(r => r.data));
  }

  transferStock(req: StockTransferRequest): Observable<void> {
    return this.http.post<ApiResponse<void>>(`${this.base}/transfer`, req)
      .pipe(map(() => void 0));
  }

  getMovements(productId?: string, branchId?: string, page = 0, size = 50): Observable<MovementPage> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (productId) params = params.set('productId', productId);
    if (branchId) params = params.set('branchId', branchId);

    return this.http.get<ApiResponse<MovementPage>>(`${this.base}/movements`, { params })
      .pipe(map(r => r.data));
  }
}
