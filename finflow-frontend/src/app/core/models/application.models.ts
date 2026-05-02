export type ApplicationStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'DOCS_UPLOADED'
  | 'UNDER_VERIFICATION'
  | 'UNDER_REVIEW'
  | 'APPROVED'
  | 'REJECTED';
export type ApplicationDecision = 'UNDER_VERIFICATION' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED';

export interface Application {
  id: string;
  userEmail?: string;
  amount: number;
  tenureMonths: number;
  purpose: string;
  
  fullName?: string;
  phone?: string;
  employmentType?: string;
  companyName?: string;
  income?: number;
  remark?: string;

  status: ApplicationStatus;
  createdAt?: string;
}

export interface CreateDraftRequest {
  amount: number;
  tenureMonths: number;
  purpose: string;

  fullName?: string;
  phone?: string;
  employmentType?: string;
  companyName?: string;
  income?: number;
}

export interface DecisionRequest {
  status: ApplicationDecision;
  remark?: string;
}
