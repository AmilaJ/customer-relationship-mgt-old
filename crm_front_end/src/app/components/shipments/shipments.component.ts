import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ShipmentService } from '../../services/shipment.service';
import { OrderService } from '../../services/order.service';
import { AuthService } from '../../services/auth.service';
import { Shipment } from '../../models/shipment.model';
import { Order } from '../../models/order.model';

@Component({
  selector: 'app-shipments',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './shipments.component.html',
  styleUrls: ['./shipments.component.scss']
})
export class ShipmentsComponent implements OnInit {
  shipments: Shipment[] = [];
  orders: Order[] = [];
  selectedShipment: Shipment | null = null;
  showCreateForm = false;
  showEditForm = false;
  loading = false;
  error = '';

  newShipment: Shipment = {
    orderId: 0,
    carrier: '',
    shippingAddress: '',
    trackingNumber: '',
    status: 'PENDING'
  };

  constructor(
    private shipmentService: ShipmentService,
    private orderService: OrderService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }
    
    this.loadShipments();
    this.loadOrders();
  }

  loadShipments(): void {
    this.loading = true;
    this.shipmentService.getShipments().subscribe({
      next: (shipments) => {
        this.shipments = shipments;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading shipments:', error);
        this.error = 'Failed to load shipments';
        this.loading = false;
      }
    });
  }

  loadOrders(): void {
    this.orderService.getOrders().subscribe({
      next: (orders) => {
        this.orders = orders;
      },
      error: (error) => {
        console.error('Error loading orders:', error);
      }
    });
  }

  createShipment(): void {
    this.loading = true;
    this.shipmentService.createShipment(this.newShipment).subscribe({
      next: (shipment) => {
        this.shipments.push(shipment);
        this.resetNewShipment();
        this.showCreateForm = false;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error creating shipment:', error);
        this.error = 'Failed to create shipment';
        this.loading = false;
      }
    });
  }

  updateShipment(): void {
    if (!this.selectedShipment?.id) return;

    this.loading = true;
    const updates = {
      status: this.selectedShipment.status,
      trackingNumber: this.selectedShipment.trackingNumber,
      carrier: this.selectedShipment.carrier
    };

    this.shipmentService.updateShipment(this.selectedShipment.id, updates).subscribe({
      next: (updatedShipment) => {
        const index = this.shipments.findIndex(s => s.id === updatedShipment.id);
        if (index !== -1) {
          this.shipments[index] = updatedShipment;
        }
        this.selectedShipment = null;
        this.showEditForm = false;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error updating shipment:', error);
        this.error = 'Failed to update shipment';
        this.loading = false;
      }
    });
  }

  deleteShipment(id: number): void {
    if (confirm('Are you sure you want to delete this shipment?')) {
      this.shipmentService.deleteShipment(id).subscribe({
        next: () => {
          this.shipments = this.shipments.filter(s => s.id !== id);
        },
        error: (error) => {
          console.error('Error deleting shipment:', error);
          this.error = 'Failed to delete shipment';
        }
      });
    }
  }

  resetNewShipment(): void {
    this.newShipment = {
      orderId: 0,
      carrier: '',
      shippingAddress: '',
      trackingNumber: '',
      status: 'PENDING'
    };
  }

  selectShipment(shipment: Shipment): void {
    this.selectedShipment = { ...shipment };
    this.showEditForm = true;
  }

  getOrderNumber(orderId: number): string {
    const order = this.orders.find(o => o.id === orderId);
    return order?.orderNumber || `Order #${orderId}`;
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
} 