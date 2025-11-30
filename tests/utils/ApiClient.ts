import { APIRequestContext, APIResponse } from '@playwright/test';

interface AuthResponse {
  token: string;
  user: {
    id: number;
    email: string;
    name: string;
    role: string;
    companyId: number;
  };
}

interface ApiError {
  status: number;
  message: string;
  details?: any;
}

export class ApiClient {
  private request: APIRequestContext;
  private baseUrl: string;
  private token: string | null = null;

  constructor(request: APIRequestContext, baseUrl: string = 'http://localhost:8080') {
    this.request = request;
    this.baseUrl = baseUrl;
  }

  private log(method: string, endpoint: string, status: number, duration: number) {
    console.log(`[API] ${method} ${endpoint} - ${status} (${duration}ms)`);
  }

  private async handleResponse<T>(response: APIResponse, endpoint: string, method: string): Promise<T> {
    const duration = Date.now();
    this.log(method, endpoint, response.status(), duration);

    if (!response.ok()) {
      const error: ApiError = {
        status: response.status(),
        message: response.statusText(),
        details: await response.json().catch(() => null),
      };
      console.error(`[API ERROR] ${method} ${endpoint}:`, error);
      throw error;
    }

    return response.json();
  }

  async login(email: string, password: string): Promise<AuthResponse> {
    const response = await this.request.post(`${this.baseUrl}/api/v1/auth/login`, {
      data: { email, password },
    });
    const data = await this.handleResponse<AuthResponse>(response, '/api/v1/auth/login', 'POST');
    this.token = data.token;
    return data;
  }

  private getHeaders(): Record<string, string> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }
    return headers;
  }

  async get<T>(endpoint: string): Promise<T> {
    const response = await this.request.get(`${this.baseUrl}${endpoint}`, {
      headers: this.getHeaders(),
    });
    return this.handleResponse<T>(response, endpoint, 'GET');
  }

  async post<T>(endpoint: string, data: any): Promise<T> {
    const response = await this.request.post(`${this.baseUrl}${endpoint}`, {
      headers: this.getHeaders(),
      data,
    });
    return this.handleResponse<T>(response, endpoint, 'POST');
  }

  async put<T>(endpoint: string, data: any): Promise<T> {
    const response = await this.request.put(`${this.baseUrl}${endpoint}`, {
      headers: this.getHeaders(),
      data,
    });
    return this.handleResponse<T>(response, endpoint, 'PUT');
  }

  async delete<T>(endpoint: string): Promise<T> {
    const response = await this.request.delete(`${this.baseUrl}${endpoint}`, {
      headers: this.getHeaders(),
    });
    return this.handleResponse<T>(response, endpoint, 'DELETE');
  }

  // Convenience methods for common endpoints
  async getVehicles() {
    return this.get('/api/v1/vehicles');
  }

  async getDrivers() {
    return this.get('/api/v1/drivers');
  }

  async getTrips() {
    return this.get('/api/v1/fleet/trips');
  }

  async getChargingStations() {
    return this.get('/api/v1/charging/stations');
  }

  async getMaintenanceRecords() {
    return this.get('/api/v1/maintenance/records');
  }

  async getDashboardSummary() {
    return this.get('/api/v1/dashboard/summary');
  }

  async getNotifications() {
    return this.get('/api/v1/notifications');
  }
}
