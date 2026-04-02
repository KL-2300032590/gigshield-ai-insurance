/**
 * API Client for GigShield Backend Services
 * 
 * Centralized API client for all backend calls.
 * Handles authentication, error handling, and request configuration.
 */

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
const SIMULATOR_URL = process.env.NEXT_PUBLIC_SIMULATOR_URL || 'http://localhost:8091';

/**
 * Service health status
 */
export interface ServiceHealth {
  name: string;
  status: 'UP' | 'DOWN' | 'DEGRADED';
  port: number;
  responseTime?: number;
  details?: Record<string, string>;
}

/**
 * Simulation request payload
 */
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

/**
 * Simulation response
 */
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

/**
 * Claim data
 */
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

/**
 * Policy data
 */
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

/**
 * Worker data
 */
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

/**
 * Kafka event
 */
export interface KafkaEvent {
  id: string;
  topic: string;
  type: string;
  timestamp: string;
  payload: Record<string, unknown>;
}

/**
 * API Error type
 */
export class ApiError extends Error {
  status: number;
  
  constructor(message: string, status: number) {
    super(message);
    this.status = status;
    this.name = 'ApiError';
  }
}

/**
 * Generic fetch wrapper with error handling
 */
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

    return response.json();
  } catch (error) {
    if (error instanceof ApiError) throw error;
    throw new ApiError(`Network error: ${(error as Error).message}`, 0);
  }
}

/**
 * API Client object with all endpoints
 */
export const api = {
  /**
   * Health check endpoints
   */
  health: {
    /**
     * Get aggregated health status of all services
     */
    async getServicesHealth(): Promise<ServiceHealth[]> {
      return fetchWithErrorHandling<ServiceHealth[]>(`${API_BASE_URL}/api/admin/health`);
    },

    /**
     * Check specific service health
     */
    async checkService(serviceName: string): Promise<ServiceHealth> {
      return fetchWithErrorHandling<ServiceHealth>(`${API_BASE_URL}/api/admin/health/${serviceName}`);
    },
  },

  /**
   * Simulation endpoints
   */
  simulation: {
    /**
     * Trigger a weather simulation
     */
    async trigger(request: SimulationRequest): Promise<SimulationResponse> {
      return fetchWithErrorHandling<SimulationResponse>(`${SIMULATOR_URL}/api/admin/simulate/weather`, {
        method: 'POST',
        body: JSON.stringify(request),
      });
    },

    /**
     * Get all simulations
     */
    async getAll(): Promise<SimulationResponse[]> {
      return fetchWithErrorHandling<SimulationResponse[]>(`${SIMULATOR_URL}/api/admin/simulate/simulations`);
    },

    /**
     * Get simulation by ID
     */
    async getById(id: string): Promise<SimulationResponse> {
      return fetchWithErrorHandling<SimulationResponse>(`${SIMULATOR_URL}/api/admin/simulate/simulations/${id}`);
    },

    /**
     * Delete simulation
     */
    async delete(id: string): Promise<void> {
      await fetchWithErrorHandling<void>(`${SIMULATOR_URL}/api/admin/simulate/simulations/${id}`, {
        method: 'DELETE',
      });
    },
  },

  /**
   * Claims endpoints
   */
  claims: {
    /**
     * Get all claims with optional filters
     */
    async getAll(filters?: { status?: string; city?: string }): Promise<Claim[]> {
      const params = new URLSearchParams();
      if (filters?.status) params.append('status', filters.status);
      if (filters?.city) params.append('city', filters.city);
      const query = params.toString() ? `?${params.toString()}` : '';
      return fetchWithErrorHandling<Claim[]>(`${API_BASE_URL}/api/claims${query}`);
    },

    /**
     * Get claim by ID
     */
    async getById(id: string): Promise<Claim> {
      return fetchWithErrorHandling<Claim>(`${API_BASE_URL}/api/claims/${id}`);
    },
  },

  /**
   * Policies endpoints
   */
  policies: {
    /**
     * Get all policies
     */
    async getAll(): Promise<Policy[]> {
      return fetchWithErrorHandling<Policy[]>(`${API_BASE_URL}/api/policies`);
    },

    /**
     * Get policy by ID
     */
    async getById(id: string): Promise<Policy> {
      return fetchWithErrorHandling<Policy>(`${API_BASE_URL}/api/policies/${id}`);
    },
  },

  /**
   * Workers endpoints
   */
  workers: {
    /**
     * Get all workers
     */
    async getAll(): Promise<Worker[]> {
      return fetchWithErrorHandling<Worker[]>(`${API_BASE_URL}/api/workers`);
    },

    /**
     * Get worker by ID
     */
    async getById(id: string): Promise<Worker> {
      return fetchWithErrorHandling<Worker>(`${API_BASE_URL}/api/workers/${id}`);
    },
  },

  /**
   * Events stream
   */
  events: {
    /**
     * Create SSE connection for real-time events
     */
    createStream(onEvent: (event: KafkaEvent) => void, onError?: (error: Error) => void): EventSource {
      const eventSource = new EventSource(`${API_BASE_URL}/api/admin/events/stream`);
      
      eventSource.onmessage = (e) => {
        try {
          const event = JSON.parse(e.data) as KafkaEvent;
          onEvent(event);
        } catch (err) {
          console.error('Failed to parse event:', err);
        }
      };

      eventSource.onerror = (e) => {
        console.error('EventSource error:', e);
        onError?.(new Error('SSE connection error'));
      };

      return eventSource;
    },
  },

  /**
   * System metrics
   */
  metrics: {
    /**
     * Get system metrics
     */
    async get(): Promise<Record<string, number>> {
      return fetchWithErrorHandling<Record<string, number>>(`${API_BASE_URL}/api/admin/metrics`);
    },
  },
};

export default api;
