import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, Inject } from '@angular/core';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ApplicationService } from '../../core/application.service';
import { Application, ApplicationDecision } from '../../core/models/application.models';
import { SnackbarService } from '../../core/snackbar.service';
import { DocumentService } from '../../core/document.service';
import { DashboardHeaderComponent } from '../../shared/components/dashboard-header.component';
import { EmptyStateComponent } from '../../shared/components/empty-state.component';
import { LoadingOverlayComponent } from '../../shared/components/loading-overlay.component';
import { MATERIAL_IMPORTS } from '../../shared/material-imports';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-remark-dialog',
  standalone: true,
  imports: [...MATERIAL_IMPORTS, FormsModule],
  template: `
    <h2 mat-dialog-title>Provide Decision Remark</h2>
    <mat-dialog-content>
      <p>Please provide a reason for your {{ data.decision }} decision.</p>
      <mat-form-field appearance="outline" style="width: 100%; margin-top: 10px;">
        <mat-label>Remark</mat-label>
        <textarea matInput [(ngModel)]="remark" rows="3" placeholder="e.g. Income proof is insufficient..."></textarea>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="cancel()">Cancel</button>
      <button mat-flat-button color="primary" (click)="confirm()" [disabled]="!remark.trim()">Confirm</button>
    </mat-dialog-actions>
  `
})
export class RemarkDialogComponent {
  remark = '';
  constructor(
    public dialogRef: MatDialogRef<RemarkDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { decision: string }
  ) {}
  cancel() { this.dialogRef.close(); }
  confirm() { this.dialogRef.close(this.remark); }
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    DashboardHeaderComponent,
    EmptyStateComponent,
    LoadingOverlayComponent,
    ...MATERIAL_IMPORTS,
    FormsModule,
  ],
  template: `
    <app-dashboard-header
      eyebrow="Admin workspace"
      title="Application review queue"
      description="Inspect everything entering the system and apply approval decisions directly against the backend decision endpoint."
    />

    <mat-card class="panel">
      <div class="panel-heading">
        <div>
          <h2>All applications</h2>
          <p>Review submitted work and keep the application lifecycle moving.</p>
        </div>

        <button mat-stroked-button type="button" (click)="loadApplications()" [disabled]="busy">
          Refresh
        </button>
      </div>

      <div class="table-wrapper">
        <app-loading-overlay [active]="busy" />

        @if (applications.length) {
          <table mat-table [dataSource]="applications" class="app-table">
            <ng-container matColumnDef="createdAt">
              <th mat-header-cell *matHeaderCellDef>Created</th>
              <td mat-cell *matCellDef="let application">
                {{ application.createdAt ? (application.createdAt | date: 'medium') : '-' }}
              </td>
            </ng-container>

            <ng-container matColumnDef="userEmail">
              <th mat-header-cell *matHeaderCellDef>User email</th>
              <td mat-cell *matCellDef="let application">{{ application.userEmail || '-' }}</td>
            </ng-container>

            <ng-container matColumnDef="amount">
              <th mat-header-cell *matHeaderCellDef>Amount</th>
              <td mat-cell *matCellDef="let application">{{ application.amount | currency: 'INR' : 'symbol' : '1.0-0' }}</td>
            </ng-container>

            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Status</th>
              <td mat-cell *matCellDef="let application">
                <span class="status-pill" [attr.data-status]="application.status">{{ application.status }}</span>
              </td>
            </ng-container>

            <ng-container matColumnDef="decision">
              <th mat-header-cell *matHeaderCellDef>Decision</th>
              <td mat-cell *matCellDef="let application">
                <div class="action-cell">
                  
                  @if (application.status !== 'SUBMITTED' && application.status !== 'DRAFT') {
                    <button 
                      mat-stroked-button 
                      class="download-btn"
                      (click)="downloadDocs(application.id)"
                      matTooltip="Download Documents for Review"
                    >
                      <mat-icon class="material-symbols-outlined">cloud_download</mat-icon>
                      Docs
                    </button>
                  }

                  @switch (application.status) {
                    @case ('DOCS_UPLOADED') {
                      <button
                        mat-flat-button
                        type="button"
                        (click)="applyDecision(application.id, 'UNDER_VERIFICATION')"
                        [disabled]="actionBusyId === application.id"
                      >
                        Start verification
                      </button>
                    }
                    @case ('UNDER_VERIFICATION') {
                      <button
                        mat-flat-button
                        type="button"
                        (click)="applyDecision(application.id, 'UNDER_REVIEW')"
                        [disabled]="actionBusyId === application.id || !downloadedAppIds.has(application.id)"
                        [matTooltip]="!downloadedAppIds.has(application.id) ? 'Must download document first' : ''"
                      >
                        Finish verification
                      </button>
                    }
                    @case ('UNDER_REVIEW') {
                      <button
                        mat-flat-button
                        type="button"
                        (click)="applyDecision(application.id, 'APPROVED')"
                        [disabled]="actionBusyId === application.id"
                      >
                        Approve
                      </button>
                      <button
                        mat-stroked-button
                        type="button"
                        color="warn"
                        (click)="applyDecision(application.id, 'REJECTED')"
                        [disabled]="actionBusyId === application.id"
                      >
                        Reject
                      </button>
                    }
                    @default {
                      <span class="status-pill" [attr.data-status]="application.status">
                        No action
                      </span>
                    }
                  }
                </div>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
          </table>
        } @else {
          <app-empty-state
            icon="inventory_2"
            title="No applications in queue"
            message="Once users create drafts and submit them, they will appear here for review."
          />
        }
      </div>
    </mat-card>
  `,
  styles: [
    `
      .panel {
        position: relative;
        padding: 1.5rem;
        border-radius: 24px;
        background: rgba(255, 252, 247, 0.94);
        box-shadow: 0 18px 38px rgba(15, 41, 51, 0.08);
        transition: transform 0.3s ease, box-shadow 0.3s ease;
      }

      .panel:hover {
        transform: translateY(-4px);
        box-shadow: 0 24px 48px rgba(15, 41, 51, 0.12);
      }

      .panel-heading {
        display: flex;
        justify-content: space-between;
        gap: 1rem;
        align-items: start;
        margin-bottom: 1rem;
      }

      .panel-heading h2 {
        margin: 0 0 0.25rem;
        color: #0f2933;
      }

      .panel-heading p {
        margin: 0;
        color: #5b707a;
      }

      .table-wrapper {
        position: relative;
        overflow-x: auto;
      }

      .app-table {
        width: 100%;
        min-width: 900px;
        background: transparent;
      }

      .app-table th.mat-header-cell {
        font-size: 0.85rem;
        font-weight: 600;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        color: #5b707a;
        border-bottom: 2px solid rgba(15, 41, 51, 0.08);
        padding: 1rem 1.25rem;
      }

      .app-table td.mat-cell {
        padding: 1.25rem;
        font-size: 0.95rem;
        color: #0f2933;
        border-bottom: 1px solid rgba(15, 41, 51, 0.04);
      }

      .app-table tr.mat-row {
        transition: background-color 0.2s ease;
      }

      .app-table tr.mat-row:hover {
        background-color: rgba(15, 41, 51, 0.04);
      }

      .action-cell {
        display: flex;
        align-items: center;
        gap: 0.75rem;
      }

      .download-btn {
        border-color: #0f2933 !important;
        color: #0f2933 !important;
        font-weight: 600;
        border-radius: 12px;
        line-height: 1;
        display: flex;
        align-items: center;
        gap: 4px;
        padding: 0 12px !important;
        min-width: 90px !important;
        height: 36px !important;
      }

      .download-btn mat-icon {
        font-size: 18px;
        width: 18px;
        height: 18px;
      }

      .status-pill {
        display: inline-flex;
        padding: 0.3rem 0.8rem;
        border-radius: 999px;
        font-size: 0.85rem;
        font-weight: 700;
        background: #e7f0ef;
        color: #124c40;
      }

      .status-pill[data-status='SUBMITTED'] {
        background: #fff0d8;
        color: #8e5a00;
      }

      .status-pill[data-status='DOCS_UPLOADED'],
      .status-pill[data-status='UNDER_VERIFICATION'] {
        background: #e9edff;
        color: #314298;
      }

      .status-pill[data-status='UNDER_REVIEW'] {
        background: #e5f4ff;
        color: #075985;
      }

      .status-pill[data-status='APPROVED'] {
        background: #def5e8;
        color: #0c6b58;
      }

      .status-pill[data-status='REJECTED'] {
        background: #ffe1da;
        color: #a73a19;
      }

      @media (max-width: 760px) {
        .action-cell {
          flex-direction: column;
          align-items: start;
        }
      }
    `,
  ],
})
export class AdminDashboardComponent implements OnInit {
  private readonly applicationService = inject(ApplicationService);
  private readonly documentService = inject(DocumentService);
  private readonly snackbarService = inject(SnackbarService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly displayedColumns = ['createdAt', 'userEmail', 'amount', 'status', 'decision'];
  protected applications: Application[] = [];
  protected busy = false;
  protected actionBusyId: string | null = null;
  protected downloadedAppIds = new Set<string>();

  ngOnInit(): void {
    this.loadApplications();
  }

  protected loadApplications(): void {
    this.busy = true;
    this.applicationService
      .getAllApplications()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => (this.busy = false)),
      )
      .subscribe({
        next: (applications) => {
          this.applications = applications;
        },
        error: (error: any) => {
          this.snackbarService.error(error?.error?.message || 'Unable to load applications.');
        },
      });
  }

  protected downloadDocs(appId: string): void {
    this.actionBusyId = appId;
    this.documentService.getDocumentsForApplication(appId)
      .pipe(finalize(() => this.actionBusyId = null))
      .subscribe({
        next: (docs) => {
          if (docs.length === 0) {
            this.snackbarService.error('No documents found for this application.');
          } else {
            docs.forEach(doc => this.documentService.downloadDocument(doc.id));
            this.downloadedAppIds.add(appId);
            this.snackbarService.success('Document download started.');
          }
        },
        error: () => this.snackbarService.error('Failed to fetch documents.')
      });
  }

  protected applyDecision(id: string, decision: ApplicationDecision): void {
    if (decision === 'APPROVED' || decision === 'REJECTED') {
      const dialogRef = this.dialog.open(RemarkDialogComponent, {
        width: '400px',
        data: { decision }
      });

      dialogRef.afterClosed().subscribe(remark => {
        if (remark) {
          this.executeDecision(id, decision, remark);
        }
      });
    } else {
      this.executeDecision(id, decision, `Application ${decision.toLowerCase()} from admin dashboard.`);
    }
  }

  private executeDecision(id: string, decision: ApplicationDecision, remark: string): void {
    this.actionBusyId = id;
    this.applicationService
      .applyDecision(id, { status: decision, remark })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => (this.actionBusyId = null)),
      )
      .subscribe({
        next: () => {
          this.snackbarService.success(`Application ${decision.toLowerCase()} successfully.`);
          this.loadApplications();
        },
        error: (error: any) => {
          this.snackbarService.error(error?.error?.message || 'Decision update failed.');
        },
      });
  }
}
