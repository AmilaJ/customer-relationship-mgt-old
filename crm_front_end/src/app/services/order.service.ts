import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order, OrderUpdateRequest } from '../models/order.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly API_URL = 'http://localhost:8089';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getHeaders(): HttpHeaders {
    const token = this.authService.getAuthToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    });
  }

  getOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.API_URL}/orders`, { headers: this.getHeaders() });
  }

  getOrderById(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.API_URL}/orders?id=${id}`, { headers: this.getHeaders() });
  }

  getOrderByNumber(orderNumber: string): Observable<Order> {
    return this.http.get<Order>(`${this.API_URL}/orders?orderNumber=${orderNumber}`, { headers: this.getHeaders() });
  }

  createOrder(order: Order): Observable<Order> {
    return this.http.post<Order>(`${this.API_URL}/orders`, order, { headers: this.getHeaders() });
  }

  updateOrder(id: number, updates: OrderUpdateRequest): Observable<Order> {
    return this.http.put<Order>(`${this.API_URL}/orders?id=${id}`, updates, { headers: this.getHeaders() });
  }

  deleteOrder(id: number): Observable<any> {
    return this.http.delete(`${this.API_URL}/orders?id=${id}`, { headers: this.getHeaders() });
  }
} 