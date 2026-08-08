import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-unauthorized',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="unauthorized-container animate-fade-in">
      <div class="glass-card error-card">
        <i class="pi pi-lock-open lock-icon"></i>
        <h1>403 - Access Denied</h1>
        <p>You do not have permission to view this resource. Please contact your system administrator.</p>
        <button routerLink="/dashboard" class="p-button p-button-primary hover-lift">
          <i class="pi pi-home mr-2"></i> Go to Dashboard
        </button>
      </div>
    </div>
  `,
  styles: [`
    .unauthorized-container {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      width: 100vw;
      background-color: var(--bg-primary);
      padding: 24px;

      .error-card {
        max-width: 480px;
        width: 100%;
        padding: 40px;
        text-align: center;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 20px;

        .lock-icon {
          font-size: 3rem;
          color: var(--danger-color);
          background-color: rgba(239, 68, 68, 0.08);
          padding: 20px;
          border-radius: 50%;
        }

        h1 {
          font-size: 1.8rem;
          font-weight: 700;
          color: var(--text-color);
        }

        p {
          color: var(--text-muted);
          line-height: 1.5;
        }

        button {
          margin-top: 10px;
          font-family: var(--font-family);
          padding: 12px 24px;
          font-weight: 600;
        }
      }
    }
  `]
})
export class UnauthorizedComponent {}
