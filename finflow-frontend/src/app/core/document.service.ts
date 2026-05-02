import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, of } from 'rxjs';
import { API_BASE_URL } from './constants/api.constants';
import { AuthService } from './auth.service';
import { DemoDataService } from './demo-data.service';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly demoDataService = inject(DemoDataService);

  uploadDocument(applicationId: string, type: string, file: File): Observable<void> {
    if (this.authService.isDemoMode) {
      return this.demoDataService.uploadDocument(applicationId, type, file.name);
    }

    const formData = new FormData();
    formData.append('applicationId', applicationId);
    formData.append('type', type);
    formData.append('file', file);

    return this.http.post<void>(`${API_BASE_URL}/documents/upload`, formData);
  }

  getDocumentsForApplication(applicationId: string): Observable<any[]> {
    if (this.authService.isDemoMode) {
      return of([{ id: 'demo-doc-1', docType: 'ID', status: 'UPLOADED' }]);
    }
    return this.http.get<any[]>(`${API_BASE_URL}/documents/application/${applicationId}`);
  }

  downloadDocument(docId: string): void {
    if (this.authService.isDemoMode) {
      alert('Downloading document (Demo Mode)');
      return;
    }

    // Using HttpClient to ensure JWT token is included via interceptor
    this.http.get(`${API_BASE_URL}/documents/${docId}/download`, {
      responseType: 'blob'
    }).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `document_${docId}`; // The browser will try to use the filename from Content-Disposition if available, or this fallback
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.error('Download failed', err);
        // Optionally show a snackbar here
      }
    });
  }
}
