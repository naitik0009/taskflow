import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { TOKEN_KEY } from '../api/client';
import { authApi } from '../api/endpoints';
import type { User } from '../types';

interface AuthContextValue {
  user: User | null;
  token: string | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, displayName: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY));
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState<boolean>(!!token);

  useEffect(() => {
    let active = true;
    if (token) {
      authApi
        .me()
        .then((u) => active && setUser(u))
        .catch(() => {
          if (active) {
            localStorage.removeItem(TOKEN_KEY);
            setToken(null);
          }
        })
        .finally(() => active && setLoading(false));
    } else {
      setLoading(false);
    }
    return () => {
      active = false;
    };
  }, [token]);

  const persist = (newToken: string, newUser: User) => {
    localStorage.setItem(TOKEN_KEY, newToken);
    setToken(newToken);
    setUser(newUser);
  };

  const login = async (email: string, password: string) => {
    const res = await authApi.login(email, password);
    persist(res.token, res.user);
  };

  const register = async (email: string, displayName: string, password: string) => {
    const res = await authApi.register(email, displayName, password);
    persist(res.token, res.user);
  };

  const logout = () => {
    localStorage.removeItem(TOKEN_KEY);
    setToken(null);
    setUser(null);
  };

  const value = useMemo(
    () => ({ user, token, loading, login, register, logout }),
    [user, token, loading],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return ctx;
}
