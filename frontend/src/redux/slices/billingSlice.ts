import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { Invoice, Payment, PricingPlan, BillingAddress, PaymentMethod } from '../../types';
import billingService from '../../services/billingService';
import { RootState } from '../store';

interface BillingState {
  invoices: Invoice[];
  selectedInvoice: Invoice | null;
  paymentHistory: Payment[];
  pricingPlans: PricingPlan[];
  billingAddress: BillingAddress | null;
  paymentMethods: PaymentMethod[];
  defaultPaymentMethod: PaymentMethod | null;
  loading: boolean;
  error: string | null;
  filters: {
    status?: string;
    companyId?: number;
    dateRange?: {
      startDate?: string;
      endDate?: string;
    };
  };
}

const initialState: BillingState = {
  invoices: [],
  selectedInvoice: null,
  paymentHistory: [],
  pricingPlans: [],
  billingAddress: null,
  paymentMethods: [],
  defaultPaymentMethod: null,
  loading: false,
  error: null,
  filters: {},
};

// Async thunks - Invoices
export const fetchAllInvoices = createAsyncThunk(
  'billing/fetchAllInvoices',
  async (params?: any) => {
    return await billingService.getInvoices(params);
  }
);

export const fetchInvoiceById = createAsyncThunk(
  'billing/fetchInvoiceById',
  async (invoiceId: number) => {
    return await billingService.getInvoiceById(invoiceId);
  }
);

export const createInvoice = createAsyncThunk(
  'billing/createInvoice',
  async (data: Partial<Invoice>) => {
    return await billingService.createInvoice(data);
  }
);

export const downloadInvoice = createAsyncThunk(
  'billing/downloadInvoice',
  async (invoiceId: number) => {
    return await billingService.downloadInvoice(invoiceId);
  }
);

// Async thunks - Payments
export const fetchPaymentHistory = createAsyncThunk(
  'billing/fetchPaymentHistory',
  async (params?: any) => {
    return await billingService.getPaymentHistory(params);
  }
);

export const fetchPaymentById = createAsyncThunk(
  'billing/fetchPaymentById',
  async (paymentId: number) => {
    return await billingService.getPaymentById(paymentId);
  }
);

export const processPayment = createAsyncThunk(
  'billing/processPayment',
  async (data: { invoiceId: number; paymentMethodId: number; amount: number }) => {
    return await billingService.processPayment(data);
  }
);

// Async thunks - Pricing Plans
export const fetchPricingPlans = createAsyncThunk(
  'billing/fetchPricingPlans',
  async () => {
    return await billingService.getPricingPlans();
  }
);

// Async thunks - Billing Address
export const fetchBillingAddress = createAsyncThunk(
  'billing/fetchBillingAddress',
  async () => {
    return await billingService.getBillingAddress();
  }
);

export const updateBillingAddress = createAsyncThunk(
  'billing/updateBillingAddress',
  async (data: Partial<BillingAddress>) => {
    return await billingService.updateBillingAddress(data);
  }
);

// Async thunks - Payment Methods
export const fetchPaymentMethods = createAsyncThunk(
  'billing/fetchPaymentMethods',
  async () => {
    return await billingService.getPaymentMethods();
  }
);

export const addPaymentMethod = createAsyncThunk(
  'billing/addPaymentMethod',
  async (data: Partial<PaymentMethod>) => {
    return await billingService.addPaymentMethod(data);
  }
);

export const setDefaultPaymentMethod = createAsyncThunk(
  'billing/setDefaultPaymentMethod',
  async (paymentMethodId: number) => {
    return await billingService.setDefaultPaymentMethod(paymentMethodId);
  }
);

export const deletePaymentMethod = createAsyncThunk(
  'billing/deletePaymentMethod',
  async (paymentMethodId: number) => {
    await billingService.deletePaymentMethod(paymentMethodId);
    return paymentMethodId;
  }
);

const billingSlice = createSlice({
  name: 'billing',
  initialState,
  reducers: {
    setFilters: (
      state,
      action: PayloadAction<{
        status?: string;
        companyId?: number;
        dateRange?: {
          startDate?: string;
          endDate?: string;
        };
      }>
    ) => {
      state.filters = action.payload;
    },
    clearFilters: (state) => {
      state.filters = {};
    },
    clearError: (state) => {
      state.error = null;
    },
    selectInvoice: (state, action: PayloadAction<Invoice | null>) => {
      state.selectedInvoice = action.payload;
    },
  },
  extraReducers: (builder) => {
    // Fetch all invoices
    builder
      .addCase(fetchAllInvoices.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAllInvoices.fulfilled, (state, action) => {
        state.loading = false;
        state.invoices = action.payload;
      })
      .addCase(fetchAllInvoices.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch invoices';
      });

    // Fetch invoice by ID
    builder
      .addCase(fetchInvoiceById.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchInvoiceById.fulfilled, (state, action) => {
        state.loading = false;
        state.selectedInvoice = action.payload;
      })
      .addCase(fetchInvoiceById.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch invoice';
      });

    // Create invoice
    builder
      .addCase(createInvoice.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createInvoice.fulfilled, (state, action) => {
        state.loading = false;
        state.invoices.push(action.payload);
        state.selectedInvoice = action.payload;
      })
      .addCase(createInvoice.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to create invoice';
      });

    // Download invoice
    builder
      .addCase(downloadInvoice.pending, (state) => {
        state.loading = true;
      })
      .addCase(downloadInvoice.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(downloadInvoice.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to download invoice';
      });

    // Fetch payment history
    builder
      .addCase(fetchPaymentHistory.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchPaymentHistory.fulfilled, (state, action) => {
        state.loading = false;
        state.paymentHistory = action.payload;
      })
      .addCase(fetchPaymentHistory.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch payment history';
      });

    // Fetch payment by ID
    builder
      .addCase(fetchPaymentById.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchPaymentById.fulfilled, (state, action) => {
        state.loading = false;
        // Update or add payment in history
        const index = state.paymentHistory.findIndex(
          (p) => p.id === action.payload.id
        );
        if (index !== -1) {
          state.paymentHistory[index] = action.payload;
        } else {
          state.paymentHistory.push(action.payload);
        }
      })
      .addCase(fetchPaymentById.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch payment';
      });

    // Process payment
    builder
      .addCase(processPayment.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(processPayment.fulfilled, (state, action) => {
        state.loading = false;
        state.paymentHistory.push(action.payload);
        // Update invoice status if it exists in state
        if (state.selectedInvoice) {
          state.selectedInvoice.status = 'PAID';
        }
      })
      .addCase(processPayment.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to process payment';
      });

    // Fetch pricing plans
    builder
      .addCase(fetchPricingPlans.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchPricingPlans.fulfilled, (state, action) => {
        state.loading = false;
        state.pricingPlans = action.payload;
      })
      .addCase(fetchPricingPlans.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch pricing plans';
      });

    // Fetch billing address
    builder
      .addCase(fetchBillingAddress.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchBillingAddress.fulfilled, (state, action) => {
        state.loading = false;
        state.billingAddress = action.payload;
      })
      .addCase(fetchBillingAddress.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch billing address';
      });

    // Update billing address
    builder
      .addCase(updateBillingAddress.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateBillingAddress.fulfilled, (state, action) => {
        state.loading = false;
        state.billingAddress = action.payload;
      })
      .addCase(updateBillingAddress.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to update billing address';
      });

    // Fetch payment methods
    builder
      .addCase(fetchPaymentMethods.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchPaymentMethods.fulfilled, (state, action) => {
        state.loading = false;
        state.paymentMethods = action.payload;
      })
      .addCase(fetchPaymentMethods.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch payment methods';
      });

    // Add payment method
    builder
      .addCase(addPaymentMethod.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(addPaymentMethod.fulfilled, (state, action) => {
        state.loading = false;
        state.paymentMethods.push(action.payload);
      })
      .addCase(addPaymentMethod.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to add payment method';
      });

    // Set default payment method
    builder
      .addCase(setDefaultPaymentMethod.pending, (state) => {
        state.loading = true;
      })
      .addCase(setDefaultPaymentMethod.fulfilled, (state, action) => {
        state.loading = false;
        // The service might return the payment method or void
        // Update payment methods to reflect default based on meta.arg
      })
      .addCase(setDefaultPaymentMethod.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to set default payment method';
      });

    // Delete payment method
    builder
      .addCase(deletePaymentMethod.pending, (state) => {
        state.loading = true;
      })
      .addCase(deletePaymentMethod.fulfilled, (state, action) => {
        state.loading = false;
        state.paymentMethods = state.paymentMethods.filter(
          (pm) => pm.id !== String(action.payload)
        );
      })
      .addCase(deletePaymentMethod.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to delete payment method';
      });
  },
});

// Selectors
export const selectAllInvoices = (state: RootState) => state.billing.invoices;
export const selectSelectedInvoice = (state: RootState) => state.billing.selectedInvoice;
export const selectPaymentHistory = (state: RootState) => state.billing.paymentHistory;
export const selectPricingPlans = (state: RootState) => state.billing.pricingPlans;
export const selectBillingAddress = (state: RootState) => state.billing.billingAddress;
export const selectPaymentMethods = (state: RootState) => state.billing.paymentMethods;
export const selectDefaultPaymentMethod = (state: RootState) => state.billing.defaultPaymentMethod;
export const selectBillingLoading = (state: RootState) => state.billing.loading;
export const selectBillingError = (state: RootState) => state.billing.error;
export const selectBillingFilters = (state: RootState) => state.billing.filters;

export const { setFilters, clearFilters, clearError, selectInvoice } =
  billingSlice.actions;

export default billingSlice.reducer;
