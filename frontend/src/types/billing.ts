export enum SubscriptionTier {
  FREE = 'FREE',
  STARTER = 'STARTER',
  PROFESSIONAL = 'PROFESSIONAL',
  ENTERPRISE = 'ENTERPRISE',
}

export enum SubscriptionStatus {
  ACTIVE = 'ACTIVE',
  PAST_DUE = 'PAST_DUE',
  CANCELLED = 'CANCELLED',
  EXPIRED = 'EXPIRED',
  TRIAL = 'TRIAL',
}

export enum BillingCycle {
  MONTHLY = 'MONTHLY',
  QUARTERLY = 'QUARTERLY',
  ANNUAL = 'ANNUAL',
}

export interface Subscription {
  id: string;
  fleetId: string;
  tier: SubscriptionTier;
  status: SubscriptionStatus;
  billingCycle: BillingCycle;
  currentPeriodStart: string;
  currentPeriodEnd: string;
  trialEnd?: string;
  cancelAtPeriodEnd: boolean;
  cancelledAt?: string;
  vehicleLimit: number;
  currentVehicleCount: number;
  features: string[];
  price: number;
  currency: string;
  nextBillingDate?: string;
  nextBillingAmount?: number;
  createdAt: string;
  updatedAt: string;
}

export interface Invoice {
  id: string;
  fleetId: string;
  subscriptionId: string;
  invoiceNumber: string;
  status: 'DRAFT' | 'OPEN' | 'PAID' | 'VOID' | 'UNCOLLECTIBLE';
  amount: number;
  amountPaid: number;
  amountDue: number;
  currency: string;
  description: string;
  billingPeriod: {
    start: string;
    end: string;
  };
  lineItems: {
    description: string;
    quantity: number;
    unitPrice: number;
    amount: number;
  }[];
  tax?: number;
  discount?: number;
  subtotal: number;
  total: number;
  dueDate: string;
  paidAt?: string;
  invoiceUrl?: string;
  receiptUrl?: string;
  createdAt: string;
}

export interface Payment {
  id: string;
  fleetId: string;
  invoiceId: string;
  amount: number;
  currency: string;
  status: 'PENDING' | 'SUCCEEDED' | 'FAILED' | 'REFUNDED';
  paymentMethod: 'CARD' | 'BANK_TRANSFER' | 'PAYPAL' | 'OTHER';
  last4?: string; // last 4 digits of card
  brand?: string; // visa, mastercard, etc.
  failureReason?: string;
  refundedAmount?: number;
  createdAt: string;
  updatedAt: string;
}

export interface PricingPlan {
  tier: SubscriptionTier;
  name: string;
  description: string;
  monthlyPrice: number;
  annualPrice: number;
  vehicleLimit: number;
  features: {
    name: string;
    included: boolean;
    limit?: number;
  }[];
  popular?: boolean;
  recommended?: boolean;
}

export interface UsageMetrics {
  fleetId: string;
  period: string;
  vehicles: number;
  trips: number;
  drivers: number;
  chargingSessions: number;
  apiCalls: number;
  storage: number; // in GB
  dataTransfer: number; // in GB
}

export interface BillingAddress {
  fleetId: string;
  companyName: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  taxId?: string;
}

export interface PaymentMethod {
  id: string;
  fleetId: string;
  type: 'CARD' | 'BANK_ACCOUNT';
  isDefault: boolean;
  card?: {
    brand: string;
    last4: string;
    expMonth: number;
    expYear: number;
  };
  bankAccount?: {
    bankName: string;
    last4: string;
    accountType: string;
  };
  createdAt: string;
}
