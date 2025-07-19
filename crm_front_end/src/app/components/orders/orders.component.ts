import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { OrderService } from '../../services/order.service';
import { AuthService } from '../../services/auth.service';
import { Order, OrderItem } from '../../models/order.model';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.scss']
})
export class OrdersComponent implements OnInit {
  orders: Order[] = [];
  selectedOrder: Order | null = null;
  showCreateForm = false;
  showEditForm = false;
  loading = false;
  error = '';

  newOrder: Order = {
    customerId: 0,
    customerName: '',
    shippingAddress: '',
    billingAddress: '',
    notes: '',
    items: []
  };

  newItem: OrderItem = {
    productId: '',
    productName: '',
    quantity: 1,
    price: 0
  };

  constructor(
    private orderService: OrderService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }
    
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.orderService.getOrders().subscribe({
      next: (orders) => {
        this.orders = orders;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading orders:', error);
        this.error = 'Failed to load orders';
        this.loading = false;
      }
    });
  }

  createOrder(): void {
    if (this.newOrder.items.length === 0) {
      this.error = 'Please add at least one item to the order';
      return;
    }

    this.loading = true;
    this.orderService.createOrder(this.newOrder).subscribe({
      next: (order) => {
        this.orders.push(order);
        this.resetNewOrder();
        this.showCreateForm = false;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error creating order:', error);
        this.error = 'Failed to create order';
        this.loading = false;
      }
    });
  }

  updateOrder(): void {
    if (!this.selectedOrder?.id) return;

    this.loading = true;
    const updates = {
      status: this.selectedOrder.status,
      shippingAddress: this.selectedOrder.shippingAddress,
      billingAddress: this.selectedOrder.billingAddress,
      notes: this.selectedOrder.notes
    };

    this.orderService.updateOrder(this.selectedOrder.id, updates).subscribe({
      next: (updatedOrder) => {
        const index = this.orders.findIndex(o => o.id === updatedOrder.id);
        if (index !== -1) {
          this.orders[index] = updatedOrder;
        }
        this.selectedOrder = null;
        this.showEditForm = false;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error updating order:', error);
        this.error = 'Failed to update order';
        this.loading = false;
      }
    });
  }

  deleteOrder(id: number): void {
    if (confirm('Are you sure you want to delete this order?')) {
      this.orderService.deleteOrder(id).subscribe({
        next: () => {
          this.orders = this.orders.filter(o => o.id !== id);
        },
        error: (error) => {
          console.error('Error deleting order:', error);
          this.error = 'Failed to delete order';
        }
      });
    }
  }

  addItem(): void {
    if (this.newItem.productId && this.newItem.productName && this.newItem.price > 0) {
      this.newOrder.items.push({ ...this.newItem });
      this.resetNewItem();
    }
  }

  removeItem(index: number): void {
    this.newOrder.items.splice(index, 1);
  }

  resetNewOrder(): void {
    this.newOrder = {
      customerId: 0,
      customerName: '',
      shippingAddress: '',
      billingAddress: '',
      notes: '',
      items: []
    };
  }

  resetNewItem(): void {
    this.newItem = {
      productId: '',
      productName: '',
      quantity: 1,
      price: 0
    };
  }

  calculateTotal(): number {
    return this.newOrder.items.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  }

  selectOrder(order: Order): void {
    this.selectedOrder = { ...order };
    this.showEditForm = true;
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
} 