export interface Shipment {
  id?: number;
  orderId: number;
  trackingNumber?: string;
  carrier: string;
  status?: string;
  shippingAddress: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ShipmentUpdateRequest {
  status?: string;
  trackingNumber?: string;
  carrier?: string;
} 