import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ApplicationService } from '../../core/application.service';
import { DocumentService } from '../../core/document.service';
import { Application } from '../../core/models/application.models';
import { SnackbarService } from '../../core/snackbar.service';
import { DashboardHeaderComponent } from '../../shared/components/dashboard-header.component';
import { EmptyStateComponent } from '../../shared/components/empty-state.component';
import { LoadingOverlayComponent } from '../../shared/components/loading-overlay.component';
import { MATERIAL_IMPORTS } from '../../shared/material-imports';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    DecimalPipe,
    DashboardHeaderComponent,
    EmptyStateComponent,
    LoadingOverlayComponent,
    ...MATERIAL_IMPORTS,
  ],
  template: `
    <app-dashboard-header
      eyebrow="User workspace"
      title="Application dashboard"
      description="Create a draft, submit it when ready, and attach supporting documents without leaving the page."
    />

    <section class="dashboard-grid">
      <mat-card class="panel panel-form">
        <div class="panel-heading">
          <div>
            <h2 id="form-title">{{ editingAppId ? 'Edit draft' : 'Create draft' }}</h2>
            <p>{{ editingAppId ? 'Update your saved draft details.' : 'Start a new application with the core loan details.' }}</p>
          </div>
          <mat-chip>{{ editingAppId ? 'Editing ID: ' + editingAppId.substring(0,8) : 'New Draft' }}</mat-chip>
        </div>

        <form [formGroup]="draftForm" (ngSubmit)="createDraft()" class="form-grid">
          <mat-form-field appearance="outline">
            <mat-label>Amount</mat-label>
            <input matInput formControlName="amount" type="number" min="1" />
            @if (draftForm.controls.amount.touched && draftForm.controls.amount.invalid) {
              <mat-error>Amount must be greater than zero.</mat-error>
            }
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Tenure (months)</mat-label>
            <input matInput formControlName="tenureMonths" type="number" min="1" />
            @if (draftForm.controls.tenureMonths.touched && draftForm.controls.tenureMonths.invalid) {
              <mat-error>Tenure must be at least 1 month.</mat-error>
            }
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-span">
            <mat-label>Purpose</mat-label>
            <textarea matInput rows="2" formControlName="purpose"></textarea>
            @if (draftForm.controls.purpose.touched && draftForm.controls.purpose.invalid) {
              <mat-error>Purpose is required.</mat-error>
            }
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Full Name</mat-label>
            <input matInput formControlName="fullName" />
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Phone</mat-label>
            <input matInput formControlName="phone" />
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Employment Type</mat-label>
            <input matInput formControlName="employmentType" />
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Company Name</mat-label>
            <input matInput formControlName="companyName" />
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-span">
            <mat-label>Monthly Income</mat-label>
            <input matInput formControlName="income" type="number" min="0" />
          </mat-form-field>

          <div class="form-actions full-span">
            <button mat-flat-button type="submit" [disabled]="draftBusy || draftForm.invalid">
              {{ editingAppId ? 'Update draft' : 'Create draft' }}
            </button>
            @if (editingAppId) {
              <button mat-stroked-button type="button" (click)="cancelEdit()">
                Cancel
              </button>
            }
          </div>
        </form>
      </mat-card>

      <mat-card class="panel panel-form">
        <div class="panel-heading">
          <div>
            <h2>Upload documents</h2>
            <p>Attach files to an existing application using the gateway upload endpoint.</p>
          </div>
          <mat-chip>Multipart</mat-chip>
        </div>

        <form [formGroup]="documentForm" (ngSubmit)="uploadDocument()" class="form-grid">
          <mat-form-field appearance="outline">
            <mat-label>Application</mat-label>
            <mat-select formControlName="applicationId">
              @for (application of applications; track application.id) {
                <mat-option [value]="application.id" [disabled]="application.status !== 'SUBMITTED'">
                  {{ application.id }} - {{ application.status }}
                </mat-option>
              }
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Document type</mat-label>
            <mat-select formControlName="type">
              @for (type of documentTypes; track type) {
                <mat-option [value]="type">{{ type }}</mat-option>
              }
            </mat-select>
          </mat-form-field>

          <div class="file-field full-span">
            <label for="document-upload">Choose file</label>
            <input id="document-upload" type="file" (change)="onFileSelected($event)" />
            <p>{{ selectedFileName || 'No file selected yet.' }}</p>
          </div>

          <button
            mat-flat-button
            type="submit"
            [disabled]="documentBusy || documentForm.invalid || !selectedFile"
          >
            Upload document
          </button>
        </form>
      </mat-card>
    </section>

    <section class="table-section">
      <mat-card class="panel panel-table">
        <div class="panel-heading table-toolbar">
          <div>
            <h2>My applications</h2>
            <p>Monitor status changes and submit any draft when it is complete.</p>
          </div>

          <button mat-stroked-button type="button" (click)="loadApplications()" [disabled]="tableBusy">
            Refresh
          </button>
        </div>

        <div class="table-wrapper">
          <app-loading-overlay [active]="tableBusy" />

          @if (applications.length) {
            <table mat-table [dataSource]="applications" class="app-table">
              <ng-container matColumnDef="createdAt">
                <th mat-header-cell *matHeaderCellDef>Created</th>
                <td mat-cell *matCellDef="let application">
                  {{ application.createdAt ? (application.createdAt | date: 'mediumDate') : '-' }}
                </td>
              </ng-container>

              <ng-container matColumnDef="amount">
                <th mat-header-cell *matHeaderCellDef>Amount</th>
                <td mat-cell *matCellDef="let application">{{ application.amount | currency: 'INR' : 'symbol' : '1.0-0' }}</td>
              </ng-container>

              <ng-container matColumnDef="tenureMonths">
                <th mat-header-cell *matHeaderCellDef>Tenure</th>
                <td mat-cell *matCellDef="let application">{{ application.tenureMonths | number }} months</td>
              </ng-container>

              <ng-container matColumnDef="status">
                <th mat-header-cell *matHeaderCellDef>Status & Remarks</th>
                <td mat-cell *matCellDef="let application">
                  <div class="status-cell">
                    <span class="status-pill" [attr.data-status]="application.status">{{ application.status }}</span>
                    @if (application.remark) {
                      <p class="app-remark"><strong>Admin:</strong> {{ application.remark }}</p>
                    }
                  </div>
                </td>
              </ng-container>

              <ng-container matColumnDef="actions">
                <th mat-header-cell *matHeaderCellDef>Actions</th>
                <td mat-cell *matCellDef="let application">
                  <div class="action-buttons">
                    @if (application.status === 'DRAFT') {
                      <button
                        mat-flat-button
                        color="primary"
                        type="button"
                        (click)="submitApplication(application.id)"
                        [disabled]="submitBusyId === application.id"
                      >
                        Submit
                      </button>
                      <button
                        mat-icon-button
                        color="accent"
                        matTooltip="Edit Draft"
                        (click)="editDraft(application)"
                      >
                        <mat-icon class="material-symbols-outlined">edit</mat-icon>
                      </button>
                      <button
                        mat-icon-button
                        color="warn"
                        matTooltip="Delete Draft"
                        (click)="deleteDraft(application.id)"
                      >
                        <mat-icon class="material-symbols-outlined">delete</mat-icon>
                      </button>
                    }
                  </div>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
            </table>
          } @else {
            <app-empty-state
              icon="fact_check"
              title="No applications yet"
              message="Create your first draft above and it will appear here immediately."
            />
          }
        </div>
      </mat-card>
    </section>
  `,
  styles: [
    `
      .dashboard-grid {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 1rem;
        margin-bottom: 1rem;
      }

      .table-section {
        position: relative;
      }

      .table-wrapper {
        position: relative;
        overflow-x: auto;
      }

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

      .panel-form {
        min-height: 100%;
      }

      .panel-heading {
        display: flex;
        justify-content: space-between;
        gap: 1rem;
        align-items: start;
        margin-bottom: 1rem;
      }

      .panel-heading h2,
      .table-toolbar h2 {
        margin: 0 0 0.25rem;
        color: #0f2933;
      }

      .panel-heading p {
        margin: 0;
        color: #5b707a;
      }

      .form-grid {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 1rem;
      }

      .form-actions {
        display: flex;
        gap: 0.75rem;
        margin-top: 0.5rem;
      }

      .action-buttons {
        display: flex;
        gap: 0.4rem;
        align-items: center;
      }

      .file-field {
        display: grid;
        gap: 0.4rem;
        padding: 0.85rem 1rem;
        border: 1px dashed rgba(15, 41, 51, 0.2);
        border-radius: 18px;
        background: #fffcf8;
      }

      .file-field label {
        font-weight: 700;
        color: #0f2933;
      }

      .file-field p {
        margin: 0;
        color: #5b707a;
      }

      .app-table {
        width: 100%;
        min-width: 800px;
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

      .status-cell {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        gap: 0.4rem;
      }

      .app-remark {
        margin: 0;
        font-size: 0.8rem;
        color: #5b707a;
        max-width: 200px;
        line-height: 1.3;
      }

      @media (max-width: 1100px) {
        .dashboard-grid {
          grid-template-columns: 1fr;
        }
      }

      @media (max-width: 700px) {
        .form-grid {
          grid-template-columns: 1fr;
        }
      }
    `,
  ],
})
export class UserDashboardComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly applicationService = inject(ApplicationService);
  private readonly documentService = inject(DocumentService);
  private readonly snackbarService = inject(SnackbarService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly displayedColumns = ['createdAt', 'amount', 'tenureMonths', 'status', 'actions'];
  protected readonly documentTypes = ['ID', 'INCOME_PROOF', 'BANK_STATEMENT', 'OTHER'];

  protected readonly draftForm = this.fb.nonNullable.group({
    amount: [1000, [Validators.required, Validators.min(1)]],
    tenureMonths: [12, [Validators.required, Validators.min(1)]],
    purpose: ['', [Validators.required, Validators.maxLength(500)]],
    fullName: [''],
    phone: [''],
    employmentType: [''],
    companyName: [''],
    income: [0]
  });

  protected readonly documentForm = this.fb.nonNullable.group({
    applicationId: ['', Validators.required],
    type: ['ID', Validators.required],
  });

  protected applications: Application[] = [];
  protected draftBusy = false;
  protected documentBusy = false;
  protected tableBusy = false;
  protected submitBusyId: string | null = null;
  protected selectedFile: File | null = null;
  protected selectedFileName = '';
  protected editingAppId: string | null = null;

  ngOnInit(): void {
    this.loadApplications();
  }

  protected loadApplications(): void {
    this.tableBusy = true;
    this.applicationService
      .getMyApplications()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => (this.tableBusy = false)),
      )
      .subscribe({
        next: (applications) => {
          this.applications = applications;

          const firstSubmittedApplication = applications.find((application) => application.status === 'SUBMITTED');

          if (!this.documentForm.controls.applicationId.value && firstSubmittedApplication) {
            this.documentForm.patchValue({ applicationId: firstSubmittedApplication.id });
          }
        },
        error: (error: any) => {
          this.snackbarService.error(error?.error?.message || 'Unable to load your applications.');
        },
      });
  }

  protected createDraft(): void {
    if (this.draftForm.invalid || this.draftBusy) {
      this.draftForm.markAllAsTouched();
      return;
    }

    this.draftBusy = true;
    const request = this.applicationService
      .saveDraft(this.draftForm.getRawValue(), this.editingAppId);

    request
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => (this.draftBusy = false)),
      )
      .subscribe({
        next: () => {
          this.snackbarService.success(this.editingAppId ? 'Draft updated.' : 'Draft created.');
          this.cancelEdit();
          this.loadApplications();
        },
        error: (error: any) => {
          this.snackbarService.error(error?.error?.message || 'Save failed.');
        },
      });
  }

  protected editDraft(app: Application): void {
    this.editingAppId = app.id;
    this.draftForm.patchValue({
      amount: app.amount,
      tenureMonths: app.tenureMonths,
      purpose: app.purpose,
      fullName: app.fullName || '',
      phone: app.phone || '',
      employmentType: app.employmentType || '',
      companyName: app.companyName || '',
      income: app.income || 0
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  protected cancelEdit(): void {
    this.editingAppId = null;
    this.draftForm.reset({ amount: 1000, tenureMonths: 12, purpose: '', fullName: '', phone: '', employmentType: '', companyName: '', income: 0 });
  }

  protected deleteDraft(id: string): void {
    if (!confirm('Are you sure you want to delete this draft?')) return;

    this.tableBusy = true;
    this.applicationService.deleteDraft(id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.tableBusy = false)
      )
      .subscribe({
        next: () => {
          this.snackbarService.success('Draft deleted.');
          this.loadApplications();
          if (this.editingAppId === id) this.cancelEdit();
        },
        error: (err: any) => this.snackbarService.error(err?.error?.message || 'Delete failed.')
      });
  }

  protected submitApplication(id: string): void {
    this.submitBusyId = id;
    this.applicationService
      .submitApplication(id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => (this.submitBusyId = null)),
      )
      .subscribe({
        next: () => {
          this.snackbarService.success('Application submitted successfully.');
          this.loadApplications();
        },
        error: (error: any) => {
          this.snackbarService.error(error?.error?.message || 'Unable to submit the application.');
        },
      });
  }

  protected onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
    this.selectedFileName = this.selectedFile?.name ?? '';
  }

  protected uploadDocument(): void {
    if (this.documentForm.invalid || !this.selectedFile || this.documentBusy) {
      this.documentForm.markAllAsTouched();
      return;
    }

    this.documentBusy = true;
    const { applicationId, type } = this.documentForm.getRawValue();

    this.documentService
      .uploadDocument(applicationId, type, this.selectedFile)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => (this.documentBusy = false)),
      )
      .subscribe({
        next: () => {
          this.snackbarService.success('Document uploaded successfully.');
          this.selectedFile = null;
          this.selectedFileName = '';
          this.loadApplications();
        },
        error: (error: any) => {
          this.snackbarService.error(error?.error?.message || 'Document upload failed.');
        },
      });
  }
}
