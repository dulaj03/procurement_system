import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  ProductCategory,
  CategoryRequest,
  ProductDetail,
  ProductPage,
  ProductRequest,
  ProductStatus
} from './product.model';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class ProductService {

  private readonly productBase = `${environment.apiUrl}/products`;
  private readonly categoryBase = `${environment.apiUrl}/categories`;

  constructor(private http: HttpClient) {}

  // Categories
  getCategories(): Observable<ProductCategory[]> {
    return this.http.get<ApiResponse<ProductCategory[]>>(this.categoryBase)
      .pipe(map(r => r.data));
  }

  createCategory(req: CategoryRequest): Observable<ProductCategory> {
    return this.http.post<ApiResponse<ProductCategory>>(this.categoryBase, req)
      .pipe(map(r => r.data));
  }

  deleteCategory(id: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.categoryBase}/${id}`)
      .pipe(map(() => void 0));
  }

  // Products
  listProducts(
    companyId: string,
    search?: string,
    status?: ProductStatus,
    categoryId?: string,
    page = 0,
    size = 20
  ): Observable<ProductPage> {
    let params = new HttpParams()
      .set('companyId', companyId)
      .set('page', page.toString())
      .set('size', size.toString());

    if (search) params = params.set('search', search);
    if (status) params = params.set('status', status);
    if (categoryId) params = params.set('categoryId', categoryId);

    return this.http.get<ApiResponse<ProductPage>>(this.productBase, { params })
      .pipe(map(r => r.data));
  }

  getProductById(id: string): Observable<ProductDetail> {
    return this.http.get<ApiResponse<ProductDetail>>(`${this.productBase}/${id}`)
      .pipe(map(r => r.data));
  }

  createProduct(req: ProductRequest): Observable<ProductDetail> {
    return this.http.post<ApiResponse<ProductDetail>>(this.productBase, req)
      .pipe(map(r => r.data));
  }

  updateProduct(id: string, req: ProductRequest): Observable<ProductDetail> {
    return this.http.put<ApiResponse<ProductDetail>>(`${this.productBase}/${id}`, req)
      .pipe(map(r => r.data));
  }

  deleteProduct(id: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.productBase}/${id}`)
      .pipe(map(() => void 0));
  }
}
