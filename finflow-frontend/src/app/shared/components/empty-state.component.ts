import { Component, Input } from '@angular/core';
import { MATERIAL_IMPORTS } from '../material-imports';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [...MATERIAL_IMPORTS],
  template: `
    <div class="empty-state">
      <mat-icon>{{ icon }}</mat-icon>
      <h3>{{ title }}</h3>
      <p>{{ message }}</p>
    </div>
  `,
  styles: [
    `
      .empty-state {
        display: grid;
        place-items: center;
        gap: 0.5rem;
        padding: 2rem;
        text-align: center;
        border: 1px dashed rgba(15, 41, 51, 0.18);
        border-radius: 20px;
        color: #4e6470;
      }

      h3,
      p {
        margin: 0;
      }

      mat-icon {
        width: 36px;
        height: 36px;
        font-size: 36px;
        color: #0c6b58;
      }
    `,
  ],
})
export class EmptyStateComponent {
  @Input() icon = 'hourglass_empty';
  @Input() title = 'Nothing here yet';
  @Input() message = 'New records will appear here once you start using the system.';
}
