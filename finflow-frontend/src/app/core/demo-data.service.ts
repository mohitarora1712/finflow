import { Injectable } from '@angular/core';
import { Observable, delay, of } from 'rxjs';
import { Application, ApplicationDecision, ApplicationStatus, CreateDraftRequest } from './models/application.models';

@Injectable({ providedIn: 'root' })
export class DemoDataService {
  private readonly adminSeed: Application[] = [
    {
      id: 'app-demo-001',
      userEmail: 'olivia@finflow.dev',
      amount: 25000,
      tenureMonths: 24,
      purpose: 'Working capital for inventory expansion.',
      status: 'SUBMITTED',
      createdAt: new Date('2026-03-29T10:15:00Z').toISOString(),
    },
    {
      id: 'app-demo-002',
      userEmail: 'mason@finflow.dev',
      amount: 18000,
      tenureMonths: 18,
      purpose: 'Office renovation and equipment refresh.',
      status: 'APPROVED',
      createdAt: new Date('2026-03-24T08:30:00Z').toISOString(),
    },
    {
      id: 'app-demo-003',
      userEmail: 'ava@finflow.dev',
      amount: 9000,
      tenureMonths: 12,
      purpose: 'Short-term liquidity for vendor payments.',
      status: 'REJECTED',
      createdAt: new Date('2026-03-20T13:10:00Z').toISOString(),
    },
  ];

  private userApplications: Application[] = [
    {
      id: 'app-user-001',
      userEmail: 'demo.user@finflow.dev',
      amount: 12000,
      tenureMonths: 12,
      purpose: 'Purchase equipment for a new service line.',
      status: 'DRAFT',
      createdAt: new Date('2026-04-01T09:45:00Z').toISOString(),
    },
    {
      id: 'app-user-002',
      userEmail: 'demo.user@finflow.dev',
      amount: 46000,
      tenureMonths: 36,
      purpose: 'Warehouse lease and distribution ramp-up.',
      status: 'SUBMITTED',
      createdAt: new Date('2026-03-27T15:20:00Z').toISOString(),
    },
  ];

  private adminApplications: Application[] = [...this.adminSeed, ...this.userApplications];

  getMyApplications(): Observable<Application[]> {
    return of(this.clone(this.userApplications)).pipe(delay(250));
  }

  getAllApplications(): Observable<Application[]> {
    return of(this.clone(this.adminApplications)).pipe(delay(250));
  }

  createDraft(payload: CreateDraftRequest): Observable<Application> {
    const application: Application = {
      id: `app-user-${crypto.randomUUID().slice(0, 8)}`,
      userEmail: 'demo.user@finflow.dev',
      status: 'DRAFT',
      createdAt: new Date().toISOString(),
      ...payload,
    };

    this.userApplications = [application, ...this.userApplications];
    this.adminApplications = [application, ...this.adminApplications];

    return of({ ...application }).pipe(delay(200));
  }

  updateDraft(id: string, payload: CreateDraftRequest): Observable<Application> {
    this.userApplications = this.userApplications.map((app) =>
      app.id === id ? { ...app, ...payload } : app
    );
    this.adminApplications = this.adminApplications.map((app) =>
      app.id === id ? { ...app, ...payload } : app
    );
    const updated = this.userApplications.find(a => a.id === id)!;
    return of({ ...updated }).pipe(delay(200));
  }

  deleteDraft(id: string): Observable<void> {
    this.userApplications = this.userApplications.filter(a => a.id !== id);
    this.adminApplications = this.adminApplications.filter(a => a.id !== id);
    return of(void 0).pipe(delay(200));
  }

  submitApplication(id: string): Observable<void> {
    this.updateStatus(id, 'SUBMITTED');
    return of(void 0).pipe(delay(200));
  }

  applyDecision(id: string, decision: ApplicationDecision): Observable<void> {
    this.updateStatus(id, decision);
    return of(void 0).pipe(delay(200));
  }

  uploadDocument(_applicationId: string, _type: string, _fileName: string): Observable<void> {
    return of(void 0).pipe(delay(250));
  }

  private updateStatus(id: string, status: ApplicationStatus): void {
    this.userApplications = this.userApplications.map((application) =>
      application.id === id ? { ...application, status } : application,
    );
    this.adminApplications = this.adminApplications.map((application) =>
      application.id === id ? { ...application, status } : application,
    );
  }

  private clone(applications: Application[]): Application[] {
    return applications.map((application) => ({ ...application }));
  }
}
