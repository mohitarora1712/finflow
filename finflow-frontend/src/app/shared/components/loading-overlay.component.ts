import { Component, Input } from '@angular/core';
import { MATERIAL_IMPORTS } from '../material-imports';

@Component({
  selector: 'app-loading-overlay',
  standalone: true,
  imports: [...MATERIAL_IMPORTS],
  template: `
    @if (active) {
      <div class="loading-overlay">
        <mat-progress-spinner diameter="48" mode="indeterminate"></mat-progress-spinner>
      </div>
    }
  `,
  styles: [
    `
      .loading-overlay {
        position: absolute;
        inset: 0;
        display: grid;
        place-items: center;
        background: rgba(255, 253, 250, 0.74);
        backdrop-filter: blur(3px);
        border-radius: 24px;
        z-index: 2;
      }
    `,
  ],
})
export class LoadingOverlayComponent {
  @Input() active = false;
}
