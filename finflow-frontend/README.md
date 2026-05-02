# FinFlow Frontend

Production-style Angular frontend for a microservices backend exposed through `http://localhost:8080`.

## Stack

- Angular 21 standalone app structure
- Angular Material UI
- Reactive Forms
- RxJS
- JWT authentication with interceptor and role guards
- User and admin dashboards

## Project structure

```text
src/app
├── core
│   ├── application.service.ts
│   ├── auth.guard.ts
│   ├── auth.service.ts
│   ├── document.service.ts
│   ├── error.interceptor.ts
│   ├── jwt.interceptor.ts
│   ├── role.guard.ts
│   ├── snackbar.service.ts
│   ├── constants
│   └── models
├── features
│   ├── admin
│   ├── auth
│   └── user
└── shared
    ├── components
    └── material-imports.ts
```

## Features

- Login and signup screens
- JWT storage in `localStorage`
- Automatic `Authorization: Bearer <token>` injection
- Role-based redirects for `ROLE_USER` and `ROLE_ADMIN`
- User draft creation, application listing, submission, and document upload
- Admin application review and approve/reject actions
- Snackbar-based error handling and session expiry redirect
- Responsive Material shell with sidebar and top navbar

## Run locally

1. Make sure the backend API gateway is running at `http://localhost:8080`.
2. Install dependencies:

```bash
npm install
```

3. Start the Angular app:

```bash
npm start
```

4. Open `http://localhost:4200`.

## Build

```bash
npm run build
```

## API assumptions

- Login returns a JWT in one of these fields: `token`, `accessToken`, or `jwt`.
- JWT payload contains either `roles: string[]` or `role: string`.
- Application objects include at least `id`, `amount`, `tenureMonths`, `purpose`, and `status`.

## Backend routes used

- `POST /auth/signup`
- `POST /auth/login`
- `POST /applications/draft`
- `POST /applications/{id}/submit`
- `GET /applications/my`
- `POST /documents/upload`
- `GET /admin/applications`
- `PATCH /applications/{id}/decision`
