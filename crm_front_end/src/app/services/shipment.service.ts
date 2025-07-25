import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Shipment, ShipmentUpdateRequest } from '../models/shipment.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class ShipmentService {
  private readonly API_URL = 'http://localhost:8089';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getHeaders(): HttpHeaders {
    // Remove auth headers for now to prevent issues (similar to file service fix)
    return new HttpHeaders({
      'Content-Type': 'application/json'
    });
  }

  getShipments(): Observable<Shipment[]> {
    return this.http.get<{shipments: Shipment[]}>(`${this.API_URL}/ext/shipment`, { headers: this.getHeaders() })
      .pipe(map(response => response.shipments));
  }

  getShipmentByOrderId(orderId: number): Observable<Shipment> {
    return this.http.get<Shipment>(`${this.API_URL}/ext/shipment?orderId=${orderId}`, { headers: this.getHeaders() });
  }

  getShipmentByTrackingNumber(trackingNumber: string): Observable<Shipment> {
    return this.http.get<Shipment>(`${this.API_URL}/ext/shipment?trackingNumber=${trackingNumber}`, { headers: this.getHeaders() });
  }

  createShipment(shipment: Shipment): Observable<Shipment> {
    return this.http.post<Shipment>(`${this.API_URL}/ext/shipment`, shipment, { headers: this.getHeaders() });
  }

  updateShipment(id: number, updates: ShipmentUpdateRequest): Observable<Shipment> {
    return this.http.put<Shipment>(`${this.API_URL}/ext/shipment?id=${id}`, updates, { headers: this.getHeaders() });
  }

  deleteShipment(id: number): Observable<any> {
    return this.http.delete(`${this.API_URL}/ext/shipment?id=${id}`, { headers: this.getHeaders() });
  }
} 