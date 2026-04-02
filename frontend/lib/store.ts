import { create } from 'zustand'
import { persist, createJSONStorage } from 'zustand/middleware'
import { WorkerResponse, PolicyResponse, ClaimResponse } from './api'

interface AuthState {
  accessToken: string | null
  refreshToken: string | null
  worker: WorkerResponse | null
  isAuthenticated: boolean
  _hasHydrated: boolean
  setAuth: (accessToken: string, refreshToken: string, worker: WorkerResponse) => void
  logout: () => void
  setHasHydrated: (state: boolean) => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      worker: null,
      isAuthenticated: false,
      _hasHydrated: false,
      setAuth: (accessToken, refreshToken, worker) => {
        // Also update localStorage directly for axios interceptor
        if (typeof window !== 'undefined') {
          localStorage.setItem('accessToken', accessToken)
          localStorage.setItem('refreshToken', refreshToken)
        }
        set({ accessToken, refreshToken, worker, isAuthenticated: true })
      },
      logout: () => {
        // Clear localStorage
        if (typeof window !== 'undefined') {
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
        }
        set({ accessToken: null, refreshToken: null, worker: null, isAuthenticated: false })
      },
      setHasHydrated: (state) => set({ _hasHydrated: state }),
    }),
    {
      name: 'auth-storage',
      storage: createJSONStorage(() => localStorage),
      onRehydrateStorage: () => (state) => {
        // Sync localStorage with store on rehydration
        if (state && typeof window !== 'undefined') {
          if (state.accessToken) {
            localStorage.setItem('accessToken', state.accessToken)
          }
          if (state.refreshToken) {
            localStorage.setItem('refreshToken', state.refreshToken)
          }
        }
        state?.setHasHydrated(true)
      },
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
