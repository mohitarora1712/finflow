import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-dashboard-header',
  standalone: true,
  template: `
    <div class="dashboard-header">
      <div>
        <p class="eyebrow">{{ eyebrow }}</p>
        <h1>{{ title }}</h1>
      </div>
      <p class="description">{{ description }}</p>
    </div>
  `,
  styles: [
    `
      .dashboard-header {
        display: flex;
        justify-content: space-between;
        gap: 1rem;
        align-items: end;
        margin-bottom: 1.5rem;
      }

      .eyebrow {
        margin: 0 0 0.35rem;
        color: #0c6b58;
        font-size: 0.9rem;
        font-weight: 700;
        letter-spacing: 0.08em;
        text-transform: uppercase;
      }

      h1 {
        margin: 0;
        font-family: 'Space Grotesk', sans-serif;
        font-size: clamp(1.8rem, 3vw, 2.6rem);
        color: #0f2933;
      }

      .description {
        max-width: 34rem;
        margin: 0;
        color: #4e6470;
      }

      @media (max-width: 900px) {
        .dashboard-header {
          flex-direction: column;
          align-items: start;
        }
      }
    `,
  ],
})
export class DashboardHeaderComponent {
  @Input({ required: true }) eyebrow = '';
  @Input({ required: true }) title = '';
  @Input({ required: true }) description = '';
}
