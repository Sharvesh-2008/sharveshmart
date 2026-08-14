import { createContext, useCallback, useMemo, useState } from 'react'
import * as authService from '../services/auth'
import * as storage from '../utils/storage'

export const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => storage.getToken())
  const [user, setUser] = useState(() => storage.getUser())

  const login = useCallback(async (email, password) => {
    const data = await authService.login(email, password)
    storage.setSession(data.token, data.user)
    setToken(data.token)
    setUser(data.user)
    return data.user
  }, [])

  const register = useCallback(async (payload) => authService.register(payload), [])

  const logout = useCallback(() => {
    storage.clearSession()
    setToken(null)
    setUser(null)
  }, [])

  const value = useMemo(
    () => ({ token, user, isAuthenticated: Boolean(token), login, register, logout }),
    [token, user, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}