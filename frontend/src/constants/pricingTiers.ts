/**
 * Pricing Tier Constants
 * Defines the three-tier pricing model for the EV Fleet Management System
 */

export interface PricingTierFeature {
  name: string;
  included: boolean;
  description?: string;
}

export interface PricingTierInfo {
  tierName: string;
  displayName: string;
  pricePerVehiclePerMonth: number;
  description: string;
  features: string[];
  recommended: boolean;
  billingCycles: string[];
  color: string;
  icon?: string;
}

/**
 * Pricing Tier enum values
 */
export enum PricingTierEnum {
  BASIC = 'BASIC',
  EV_PREMIUM = 'EV_PREMIUM',
  ENTERPRISE = 'ENTERPRISE'
}

/**
 * Billing Cycle options
 */
export enum BillingCycle {
  MONTHLY = 'MONTHLY',
  QUARTERLY = 'QUARTERLY',
  ANNUAL = 'ANNUAL'
}

/**
 * Billing cycle display names
 */
export const BILLING_CYCLE_LABELS: Record<BillingCycle, string> = {
  [BillingCycle.MONTHLY]: 'Monthly',
  [BillingCycle.QUARTERLY]: 'Quarterly (5% off)',
  [BillingCycle.ANNUAL]: 'Annual (10% off)'
};

/**
 * Pricing tier information
 */
export const PRICING_TIERS: Record<PricingTierEnum, PricingTierInfo> = {
  [PricingTierEnum.BASIC]: {
    tierName: 'BASIC',
    displayName: 'Basic',
    pricePerVehiclePerMonth: 299,
    description: 'General fleet management - Perfect for all vehicle types',
    features: [
      'Real-time GPS tracking',
      'Fleet monitoring dashboard',
      'Driver management',
      'Trip history and reports',
      'Basic analytics',
      'Email support'
    ],
    recommended: false,
    billingCycles: ['MONTHLY', 'QUARTERLY', 'ANNUAL'],
    color: '#2196F3'
  },
  [PricingTierEnum.EV_PREMIUM]: {
    tierName: 'EV_PREMIUM',
    displayName: 'EV Premium',
    pricePerVehiclePerMonth: 699,
    description: 'All features + EV-specific optimization',
    features: [
      'Everything in Basic',
      'Advanced battery health monitoring',
      'Smart charging optimization',
      'Charging station management',
      'Predictive maintenance',
      'Advanced EV analytics',
      'Carbon footprint tracking',
      'Priority support'
    ],
    recommended: true,
    billingCycles: ['MONTHLY', 'QUARTERLY', 'ANNUAL'],
    color: '#4CAF50'
  },
  [PricingTierEnum.ENTERPRISE]: {
    tierName: 'ENTERPRISE',
    displayName: 'Enterprise',
    pricePerVehiclePerMonth: 999,
    description: 'Multi-depot, custom integrations, dedicated support',
    features: [
      'Everything in EV Premium',
      'Multi-depot management',
      'Custom API integrations',
      'White-label options',
      'Advanced role-based access control',
      'Custom reports and dashboards',
      'Dedicated account manager',
      '24/7 priority support',
      'SLA guarantee (99.9% uptime)'
    ],
    recommended: false,
    billingCycles: ['MONTHLY', 'QUARTERLY', 'ANNUAL'],
    color: '#FF9800'
  }
};

/**
 * Get discount percentage for billing cycle
 */
export const getDiscountPercentage = (billingCycle: BillingCycle): number => {
  switch (billingCycle) {
    case BillingCycle.QUARTERLY:
      return 5;
    case BillingCycle.ANNUAL:
      return 10;
    default:
      return 0;
  }
};

/**
 * Get months for billing cycle
 */
export const getBillingCycleMonths = (billingCycle: BillingCycle): number => {
  switch (billingCycle) {
    case BillingCycle.QUARTERLY:
      return 3;
    case BillingCycle.ANNUAL:
      return 12;
    default:
      return 1;
  }
};

/**
 * Format price in INR
 */
export const formatPrice = (amount: number): string => {
  return `₹${amount.toLocaleString('en-IN')}`;
};
