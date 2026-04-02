import axios from 'axios'

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Add auth token to requests
api.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
  }
  return config
})

// Handle auth errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      if (typeof window !== 'undefined') {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

// Auth API
export const authApi = {
  register: (data: RegisterRequest) => 
    api.post<AuthResponse>('/api/workers/register', data),
  
  login: (data: LoginRequest) => 
    api.post<AuthResponse>('/api/auth/login', data),
  
  refresh: (refreshToken: string) =>
    api.post<AuthResponse>('/api/auth/refresh', null, {
      headers: { Authorization: `Bearer ${refreshToken}` }
    }),
}

// Worker API
export const workerApi = {
  getMe: () => api.get<WorkerResponse>('/api/workers/me'),
}

// Policy API
export const policyApi = {
  purchase: (data: PolicyPurchaseRequest) => 
    api.post<PolicyResponse>('/api/policies', data),
  
  getActive: () => 
    api.get<PolicyResponse>('/api/policies/active'),
  
  getAll: () => 
    api.get<PolicyResponse[]>('/api/policies'),
  
  getById: (id: string) => 
    api.get<PolicyResponse>(`/api/policies/${id}`),
}

// Claims API
export const claimsApi = {
  getAll: (workerId: string) => 
    api.get<ClaimResponse[]>(`/api/claims/worker/${workerId}`),
  
  getById: (id: string) => 
    api.get<ClaimResponse>(`/api/claims/${id}`),
}

// Types
export interface RegisterRequest {
  name: string
  email: string
  phone: string
  password: string
  latitude: number
  longitude: number
  city: string
  state?: string
  pincode?: string
  platform?: string
  platformId?: string
}

export interface LoginRequest {
  identifier: string
  password: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  worker: WorkerResponse
}

export interface WorkerResponse {
  id: string
  name: string
  email: string
  phone: string
  location: {
    latitude: number
    longitude: number
    city: string
    state?: string
    pincode?: string
  }
  platform?: string
  platformId?: string
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED'
  createdAt: string
}

export interface PolicyPurchaseRequest {
  workerId?: string
  weekNumber?: number
  year?: number
  coverageLimit?: number
}

export interface PolicyResponse {
  id: string
  workerId: string
  weekNumber: number
  year: number
  premium: number
  coverageLimit: number
  riskScore: number
  startDate: string
  endDate: string
  status: 'ACTIVE' | 'EXPIRED' | 'CLAIMED' | 'CANCELLED'
  createdAt: string
}

export interface ClaimResponse {
  id: string
  policyId: string
  workerId: string
  triggerType: 'HEAVY_RAIN' | 'FLOOD' | 'AIR_POLLUTION' | 'EXTREME_HEAT' | 'EXTREME_COLD'
  triggerData: {
    value: number
    threshold: number
    location: string
    source: string
    measuredAt: string
  }
  amount: number
  status: 'PENDING' | 'VALIDATING' | 'APPROVED' | 'REJECTED' | 'PAID' | 'FAILED'
  statusReason?: string
  triggeredAt: string
  processedAt?: string
  createdAt: string
}

export interface PayoutResponse {
  id: string
  claimId: string
  workerId: string
  amount: number
  transactionId?: string
  paymentMethod: string
  status: 'INITIATED' | 'PROCESSING' | 'SUCCESS' | 'FAILED' | 'CANCELLED'
  statusReason?: string
  initiatedAt: string
  completedAt?: string
  createdAt: string
}

export default api
