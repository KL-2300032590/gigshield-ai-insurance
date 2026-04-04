/**
 * API Client for Parametrix Admin Dashboard
 *
 * Centralized API client for all backend calls.
 */

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8091';
const SIMULATOR_URL = process.env.NEXT_PUBLIC_SIMULATOR_URL || API_BASE_URL;

export interface ServiceHealth {
  name: string;
  status: 'UP' | 'DOWN' | 'DEGRADED' | 'UNKNOWN';
  port: number;
  responseTime?: number;
  details?: Record<string, string>;
}

export interface SimulationRequest {
  city: string;
  eventType: string;
  severity: string;
  duration: string;
  simulatedValue: number;
  affectedWorkers?: number;
  triggerClaims?: boolean;
  triggeredBy?: string;
}

export interface SimulationResponse {
  simulationId: string;
  status: string;
  city: string;
  eventType: string;
  severity: string;
  eventsPublished: number;
  claimsTriggered: number;
  claimIds: string[];
  executionTime: string;
  timestamp: string;
  message: string;
  error?: string;
}

export interface Claim {
  id: string;
  policyId: string;
  workerId: string;
  triggerType: string;
  status: string;
  amount: number;
  city: string;
  createdAt: string;
  updatedAt: string;
}

export interface Policy {
  id: string;
  workerId: string;
  planType: string;
  city: string;
  status: string;
  premium: number;
  coverage: number;
  startDate: string;
  endDate: string;
}

export interface Worker {
  id: string;
  name: string;
  email: string;
  phone: string;
  city: string;
  gigType: string;
  status: string;
  registeredAt: string;
}

export interface KafkaEvent {
  id: string;
  topic: string;
  type: string;
  timestamp: string;
  payload: Record<string, unknown>;
}

export interface LogEntry {
  id: string;
  level: 'ERROR' | 'WARN' | 'INFO' | 'DEBUG' | string;
  service: string;
  message: string;
  timestamp: string;
}

export class ApiError extends Error {
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.status = status;
    this.name = 'ApiError';
  }
}

async function fetchWithErrorHandling<T>(url: string, options?: RequestInit): Promise<T> {
  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options?.headers,
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new ApiError(errorText || `HTTP Error ${response.status}`, response.status);
    }

    if (response.status === 204) {
      return undefined as T;
    }

    const text = await response.text();
    if (!text) {
      return undefined as T;
    }

    return JSON.parse(text) as T;
  } catch (error) {
    if (error instanceof ApiError) throw error;
    throw new ApiError(`Network error: ${(error as Error).message}`, 0);
  }
}

export const api = {
  health: {
    async getServicesHealth(): Promise<ServiceHealth[]> {
      return fetchWithErrorHandling<ServiceHealth[]>(`${API_BASE_URL}/api/admin/health`);
    },

    async checkService(serviceName: string): Promise<ServiceHealth> {
      return fetchWithErrorHandling<ServiceHealth>(`${API_BASE_URL}/api/admin/health/${serviceName}`);
    },
  },

  simulation: {
    async trigger(request: SimulationRequest): Promise<SimulationResponse> {
      return fetchWithErrorHandling<SimulationResponse>(`${SIMULATOR_URL}/api/admin/simulate/weather`, {
        method: 'POST',
        body: JSON.stringify(request),
      });
    },

    async getAll(): Promise<SimulationResponse[]> {
      return fetchWithErrorHandling<SimulationResponse[]>(`${SIMULATOR_URL}/api/admin/simulate/simulations`);
    },

    async getById(id: string): Promise<SimulationResponse> {
      return fetchWithErrorHandling<SimulationResponse>(`${SIMULATOR_URL}/api/admin/simulate/simulations/${id}`);
    },

    async delete(id: string): Promise<void> {
      await fetchWithErrorHandling<void>(`${SIMULATOR_URL}/api/admin/simulate/simulations/${id}`, {
        method: 'DELETE',
      });
    },
  },

  claims: {
    async getAll(filters?: { status?: string; city?: string }): Promise<Claim[]> {
      const params = new URLSearchParams();
      if (filters?.status && filters.status !== 'all') params.append('status', filters.status);
      if (filters?.city && filters.city !== 'all') params.append('city', filters.city);
      const query = params.toString() ? `?${params.toString()}` : '';
      return fetchWithErrorHandling<Claim[]>(`${API_BASE_URL}/api/claims${query}`);
    },

    async getById(id: string): Promise<Claim> {
      return fetchWithErrorHandling<Claim>(`${API_BASE_URL}/api/claims/${id}`);
    },
  },

  policies: {
    async getAll(): Promise<Policy[]> {
      return fetchWithErrorHandling<Policy[]>(`${API_BASE_URL}/api/policies`);
    },

    async getById(id: string): Promise<Policy> {
      return fetchWithErrorHandling<Policy>(`${API_BASE_URL}/api/policies/${id}`);
    },
  },

  workers: {
    async getAll(): Promise<Worker[]> {
      return fetchWithErrorHandling<Worker[]>(`${API_BASE_URL}/api/workers`);
    },

    async getById(id: string): Promise<Worker> {
      return fetchWithErrorHandling<Worker>(`${API_BASE_URL}/api/workers/${id}`);
    },
  },

  events: {
    async getAll(): Promise<KafkaEvent[]> {
      return fetchWithErrorHandling<KafkaEvent[]>(`${API_BASE_URL}/api/admin/events`);
    },

    async getStreamSnapshot(): Promise<KafkaEvent[]> {
      return fetchWithErrorHandling<KafkaEvent[]>(`${API_BASE_URL}/api/admin/events/stream`);
    },
  },

  logs: {
    async getAll(): Promise<LogEntry[]> {
      return fetchWithErrorHandling<LogEntry[]>(`${API_BASE_URL}/api/admin/logs`);
    },
  },

  metrics: {
    async get(): Promise<Record<string, number>> {
      return fetchWithErrorHandling<Record<string, number>>(`${API_BASE_URL}/api/admin/metrics`);
    },
  },
};

export default api;
