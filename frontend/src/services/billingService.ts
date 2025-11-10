import { apiClient } from './api';
import { Subscription, Invoice, Payment, PricingPlan, BillingAddress, PaymentMethod } from '../types';

/**
 * Billing Service
 * Handles all billing-related API calls
 * API Base: /api/v1/billing
 */
export const billingService = {
  /**
   * Get current subscription
   */
  getSubscription: async (): Promise<Subscription> => {
    return apiClient.get('/v1/billing/subscription');
  },

  /**
   * Update subscription tier and cycle
   */
  updateSubscription: async (tier: string, cycle: string): Promise<Subscription> => {
    return apiClient.post('/v1/billing/subscription/update', { tier, cycle });
  },

  /**
   * Cancel subscription
   */
  cancelSubscription: async (): Promise<void> => {
    return apiClient.post('/v1/billing/subscription/cancel', {});
  },

  /**
   * Get all invoices
   */
  getInvoices: async (params?: any): Promise<Invoice[]> => {
    return apiClient.get('/v1/billing/invoices', params);
  },

  /**
   * Get invoice by ID
   */
  getInvoiceById: async (invoiceId: number): Promise<Invoice> => {
    return apiClient.get(`/v1/billing/invoices/${invoiceId}`);
  },

  /**
   * Create invoice
   */
  createInvoice: async (data: Partial<Invoice>): Promise<Invoice> => {
    return apiClient.post('/v1/billing/invoices', data);
  },

  /**
   * Download invoice PDF
   */
  downloadInvoice: async (invoiceId: number): Promise<Blob> => {
    return apiClient.get(`/v1/billing/invoices/${invoiceId}/download`);
  },

  /**
   * Get payment history
   */
  getPaymentHistory: async (params?: any): Promise<Payment[]> => {
    return apiClient.get('/v1/billing/payments', params);
  },

  /**
   * Get payment by ID
   */
  getPaymentById: async (paymentId: number): Promise<Payment> => {
    return apiClient.get(`/v1/billing/payments/${paymentId}`);
  },

  /**
   * Process payment
   */
  processPayment: async (data: {
    amount: number;
    paymentMethodId: number;
    invoiceId?: number;
  }): Promise<Payment> => {
    return apiClient.post('/v1/billing/payments/process', data);
  },

  /**
   * Get pricing plans
   */
  getPricingPlans: async (): Promise<PricingPlan[]> => {
    return apiClient.get('/v1/billing/pricing-plans');
  },

  /**
   * Get all pricing tiers
   */
  getPricingTiers: async (): Promise<any[]> => {
    return apiClient.get('/billing/pricing/tiers');
  },

  /**
   * Get specific pricing tier by name
   */
  getPricingTier: async (tierName: string): Promise<any> => {
    return apiClient.get(`/billing/pricing/tiers/${tierName}`);
  },

  /**
   * Calculate pricing based on tier, vehicle count, and billing cycle
   */
  calculatePricing: async (data: {
    tier: string;
    vehicleCount: number;
    billingCycle: string;
  }): Promise<any> => {
    return apiClient.post('/billing/pricing/calculate', data);
  },

  /**
   * Get recommended pricing tier
   */
  getRecommendedTier: async (vehicleCount: number, hasEVVehicles?: boolean): Promise<any> => {
    const params = { vehicleCount, hasEVVehicles: hasEVVehicles ?? true };
    return apiClient.get('/billing/pricing/recommend', params);
  },

  /**
   * Get billing address
   */
  getBillingAddress: async (): Promise<BillingAddress> => {
    return apiClient.get('/v1/billing/address');
  },

  /**
   * Update billing address
   */
  updateBillingAddress: async (data: Partial<BillingAddress>): Promise<BillingAddress> => {
    return apiClient.put('/v1/billing/address', data);
  },

  /**
   * Get payment methods
   */
  getPaymentMethods: async (): Promise<PaymentMethod[]> => {
    return apiClient.get('/v1/billing/payment-methods');
  },

  /**
   * Add payment method
   */
  addPaymentMethod: async (data: any): Promise<PaymentMethod> => {
    return apiClient.post('/v1/billing/payment-methods', data);
  },

  /**
   * Set default payment method
   */
  setDefaultPaymentMethod: async (paymentMethodId: number): Promise<void> => {
    return apiClient.post(`/v1/billing/payment-methods/${paymentMethodId}/set-default`, {});
  },

  /**
   * Delete payment method
   */
  deletePaymentMethod: async (paymentMethodId: number): Promise<void> => {
    return apiClient.delete(`/v1/billing/payment-methods/${paymentMethodId}`);
  },

  // ========== PR 18: INVOICE GENERATION & PAYMENT PROCESSING ==========

  /**
   * Finalize invoice and prepare for payment
   */
  finalizeInvoice: async (invoiceId: string): Promise<string> => {
    return apiClient.post(`/v1/billing/invoices/${invoiceId}/finalize`, {});
  },

  /**
   * Get invoice details with full breakdown by tier
   */
  getInvoiceDetails: async (invoiceId: string): Promise<any> => {
    return apiClient.get(`/v1/billing/invoices/${invoiceId}/details`);
  },

  /**
   * Process payment for an invoice
   * Supports: CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, UPI
   */
  processInvoicePayment: async (invoiceId: string, data: {
    amount: number;
    paymentMethod: string;
    transactionReference?: string;
    remarks?: string;
  }): Promise<Payment> => {
    return apiClient.post(`/v1/billing/invoices/${invoiceId}/pay`, data);
  },

  /**
   * Retry failed payment for an invoice
   */
  retryInvoicePayment: async (invoiceId: string, paymentMethod: string): Promise<Payment> => {
    return apiClient.post(`/v1/billing/invoices/${invoiceId}/retry-payment`, { paymentMethod });
  },

  /**
   * Get payment history for an invoice
   */
  getInvoicePaymentHistory: async (invoiceId: string): Promise<Payment[]> => {
    return apiClient.get(`/v1/billing/invoices/${invoiceId}/payment-history`);
  },

  /**
   * Verify payment status for an invoice
   */
  verifyInvoicePaymentStatus: async (invoiceId: string): Promise<boolean> => {
    return apiClient.get(`/v1/billing/invoices/${invoiceId}/payment-status`);
  },

  /**
   * Handle overdue invoice (apply late fees for > 30 days)
   */
  handleOverdueInvoice: async (invoiceId: string): Promise<string> => {
    return apiClient.post(`/v1/billing/invoices/${invoiceId}/handle-overdue`, {});
  },

  // ========== PR 18: MULTI-FUEL SURCHARGE CALCULATIONS ==========

  /**
   * Calculate EV surcharge
   * Rate: ₹15/kWh above 50 kWh/month free tier
   */
  calculateEVSurcharge: (energyConsumedKwh: number): number => {
    const freeEnergyTier = 50;
    const ratePerKwh = 15;

    if (energyConsumedKwh <= freeEnergyTier) {
      return 0;
    }

    return (energyConsumedKwh - freeEnergyTier) * ratePerKwh;
  },

  /**
   * Calculate ICE surcharge
   * Rate: ₹10/liter above 100 liters/month free tier
   */
  calculateICESurcharge: (fuelConsumedLiters: number): number => {
    const freeFuelTier = 100;
    const ratePerLiter = 10;

    if (fuelConsumedLiters <= freeFuelTier) {
      return 0;
    }

    return (fuelConsumedLiters - freeFuelTier) * ratePerLiter;
  },

  /**
   * Calculate HYBRID surcharge
   * Combines EV + ICE surcharges with 10% discount
   */
  calculateHybridSurcharge: (energyConsumedKwh: number, fuelConsumedLiters: number): number => {
    const evSurcharge = billingService.calculateEVSurcharge(energyConsumedKwh);
    const iceSurcharge = billingService.calculateICESurcharge(fuelConsumedLiters);
    const totalSurcharge = evSurcharge + iceSurcharge;

    // 10% discount for hybrid vehicles (more efficient)
    return totalSurcharge * 0.9;
  },

  /**
   * Calculate monthly charge for vehicles in a pricing tier
   * Includes base subscription + usage surcharges
   */
  calculateTierCharge: (monthlyPrice: number, vehicleCount: number, usageSurcharge: number): number => {
    const baseCharge = monthlyPrice * vehicleCount;
    return baseCharge + usageSurcharge;
  },
};

export default billingService;
