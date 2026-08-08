import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { UserInfo } from '../../core/models/models';

interface MenuItem {
  label: string;
  icon: string;
  link: string;
  roles?: string[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit {
  currentUser: UserInfo | null = null;

  get displayRole(): string {
    if (!this.currentUser || !this.currentUser.roles || this.currentUser.roles.length === 0) {
      return '';
    }
    return this.currentUser.roles[0].replace('ROLE_', '');
  }

  menuItems: MenuItem[] = [
    { label: 'Dashboard', icon: 'pi pi-chart-bar', link: '/dashboard' },
    { label: 'Companies', icon: 'pi pi-building', link: '/companies', roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN'] },
    { label: 'Users', icon: 'pi pi-users', link: '/users', roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN'] },
    { label: 'Suppliers', icon: 'pi pi-truck', link: '/suppliers' },
    { label: 'Products', icon: 'pi pi-box', link: '/products' },
    { label: 'Inventory', icon: 'pi pi-database', link: '/inventory' },
    { label: 'Purchase Requests', icon: 'pi pi-file', link: '/purchase-requests' },
    { label: 'Purchase Orders', icon: 'pi pi-shopping-bag', link: '/purchase-orders' },
    { label: 'Stock Receiving', icon: 'pi pi-download', link: '/receiving' },
    { label: 'Invoices', icon: 'pi pi-file-pdf', link: '/invoices' },
    { label: 'Reports', icon: 'pi pi-sliders-h', link: '/reports' }
  ];

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
  }

  hasAccess(item: MenuItem): boolean {
    if (!item.roles) return true;
    return item.roles.some(role => this.authService.hasRole(role));
  }

  logout(): void {
    this.authService.logout();
  }
}
