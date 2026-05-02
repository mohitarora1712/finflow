import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './constants/api.constants';
import { AuthService } from './auth.service';
import { DemoDataService } from './demo-data.service';
import { Application, CreateDraftRequest, DecisionRequest } from './models/application.models';

@Injectable({ providedIn: 'root' })
export class ApplicationService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly demoDataService = inject(DemoDataService);

  createDraft(payload: CreateDraftRequest): Observable<Application> {
    if (this.authService.isDemoMode) {
      return this.demoDataService.createDraft(payload);
    }
    return this.http.post<Application>(`${API_BASE_URL}/applications/draft`, payload);
  }

  updateDraft(id: string, payload: CreateDraftRequest): Observable<Application> {
    if (this.authService.isDemoMode) {
      return this.demoDataService.updateDraft(id, payload);
    }
    return this.http.put<Application>(`${API_BASE_URL}/applications/draft/${id}`, payload);
  }

  saveDraft(payload: CreateDraftRequest, id: string | null): Observable<Application> {
    return id ? this.updateDraft(id, payload) : this.createDraft(payload);
  }

  deleteDraft(id: string): Observable<void> {
    if (this.authService.isDemoMode) {
      return this.demoDataService.deleteDraft(id);
    }
    return this.http.delete<void>(`${API_BASE_URL}/applications/draft/${id}`);
  }

  submitApplication(id: string): Observable<void> {
    if (this.authService.isDemoMode) {
      return this.demoDataService.submitApplication(id);
    }
    return this.http.post<void>(`${API_BASE_URL}/applications/${id}/submit`, {});
  }

  getMyApplications(): Observable<Application[]> {
    if (this.authService.isDemoMode) {
      return this.demoDataService.getMyApplications();
    }
    return this.http.get<Application[]>(`${API_BASE_URL}/applications/my`);
  }

  getAllApplications(): Observable<Application[]> {
    if (this.authService.isDemoMode) {
      return this.demoDataService.getAllApplications();
    }
    return this.http.get<Application[]>(`${API_BASE_URL}/applications/admin`);
  }

  applyDecision(id: string, payload: DecisionRequest): Observable<void> {
    if (this.authService.isDemoMode) {
      return this.demoDataService.applyDecision(id, payload.status);
    }
    return this.http.patch<void>(`${API_BASE_URL}/admin/applications/${id}/decision`, payload);
  }
}
