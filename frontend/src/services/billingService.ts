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
};

export default billingService;
