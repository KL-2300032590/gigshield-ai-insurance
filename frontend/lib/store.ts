import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { WorkerResponse, PolicyResponse, ClaimResponse } from './api'

interface AuthState {
  accessToken: string | null
  refreshToken: string | null
  worker: WorkerResponse | null
  isAuthenticated: boolean
  setAuth: (accessToken: string, refreshToken: string, worker: WorkerResponse) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      worker: null,
      isAuthenticated: false,
      setAuth: (accessToken, refreshToken, worker) => 
        set({ accessToken, refreshToken, worker, isAuthenticated: true }),
      logout: () => 
        set({ accessToken: null, refreshToken: null, worker: null, isAuthenticated: false }),
    }),
    {
      name: 'auth-storage',
    }
  )
)

interface DashboardState {
  activePolicy: PolicyResponse | null
  policies: PolicyResponse[]
  claims: ClaimResponse[]
  isLoading: boolean
  setActivePolicy: (policy: PolicyResponse | null) => void
  setPolicies: (policies: PolicyResponse[]) => void
  setClaims: (claims: ClaimResponse[]) => void
  setLoading: (isLoading: boolean) => void
}

export const useDashboardStore = create<DashboardState>((set) => ({
  activePolicy: null,
  policies: [],
  claims: [],
  isLoading: false,
  setActivePolicy: (policy) => set({ activePolicy: policy }),
  setPolicies: (policies) => set({ policies }),
  setClaims: (claims) => set({ claims }),
  setLoading: (isLoading) => set({ isLoading }),
}))
