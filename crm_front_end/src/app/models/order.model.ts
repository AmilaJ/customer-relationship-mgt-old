export interface OrderItem {
  id?: number;
  orderId?: number;
  productId: string;
  productName: string;
  description?: string;
  quantity: number;
  price: number;
  totalPrice?: number;
}

export interface Order {
  id?: number;
  orderNumber?: string;
  customerId: number;
  customerName: string;
  status?: string;
  totalAmount?: number;
  currency?: string;
  shippingAddress: string;
  billingAddress: string;
  orderDate?: string;
  expectedDeliveryDate?: string;
  actualDeliveryDate?: string;
  notes?: string;
  items: OrderItem[];
  createdAt?: string;
  updatedAt?: string;
}

export interface OrderUpdateRequest {
  status?: string;
  shippingAddress?: string;
  billingAddress?: string;
  notes?: string;
  expectedDeliveryDate?: string;
} 