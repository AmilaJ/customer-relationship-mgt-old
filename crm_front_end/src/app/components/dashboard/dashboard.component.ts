import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { OrderService } from '../../services/order.service';
import { ShipmentService } from '../../services/shipment.service';
import { FileService } from '../../services/file.service';
import { Order } from '../../models/order.model';
import { Shipment } from '../../models/shipment.model';
import { FileInfo } from '../../models/file.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  currentUser: any;
  orders: Order[] = [];
  shipments: Shipment[] = [];
  files: FileInfo[] = [];
  
  stats = {
    totalOrders: 0,
    pendingOrders: 0,
    shippedOrders: 0,
    totalShipments: 0,
    totalFiles: 0,
    totalRevenue: 0
  };

  loading = true;

  constructor(
    private authService: AuthService,
    private orderService: OrderService,
    private shipmentService: ShipmentService,
    private fileService: FileService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }
    
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading = true;

    // Load orders
    this.orderService.getOrders().subscribe({
      next: (orders) => {
        this.orders = orders;
        this.calculateOrderStats();
      },
      error: (error) => console.error('Error loading orders:', error)
    });

    // Load shipments
    this.shipmentService.getShipments().subscribe({
      next: (shipments) => {
        this.shipments = shipments;
        this.stats.totalShipments = shipments.length;
      },
      error: (error) => console.error('Error loading shipments:', error)
    });

    // Load files
    this.fileService.getFiles().subscribe({
      next: (files) => {
        this.files = files;
        this.stats.totalFiles = files.length;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading files:', error);
        this.loading = false;
      }
    });
  }

  calculateOrderStats(): void {
    this.stats.totalOrders = this.orders.length;
    this.stats.pendingOrders = this.orders.filter(o => o.status === 'PENDING').length;
    this.stats.shippedOrders = this.orders.filter(o => o.status === 'SHIPPED').length;
    this.stats.totalRevenue = this.orders.reduce((sum, order) => sum + (order.totalAmount || 0), 0);
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  getRecentOrders(): Order[] {
    return this.orders.slice(0, 5);
  }

  getRecentShipments(): Shipment[] {
    return this.shipments.slice(0, 5);
  }
} 