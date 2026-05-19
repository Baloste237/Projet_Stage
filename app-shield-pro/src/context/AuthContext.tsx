import { createContext, useContext, useState, type ReactNode } from "react";

interface User {
  name: string;
  email: string;
  role: string;
}

interface AuthContextType {
  user: User | null;
  login: (identifier: string, password: string) => Promise<boolean>;
  register: (name: string, email: string, password: string) => Promise<boolean>;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType>({
  user: null,
  login: async () => false,
  register: async () => false,
  logout: () => {},
  isAuthenticated: false,
});

export const useAuth = () => useContext(AuthContext);

function parseJwt(token: string) {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));

    return JSON.parse(jsonPayload);
  } catch (e) {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() => {
    try {
      const saved = localStorage.getItem("vulnscan-user");
      return saved ? JSON.parse(saved) : null;
    } catch (e) {
      console.error("Failed to parse user from localStorage:", e);
      localStorage.removeItem("vulnscan-user");
      return null;
    }
  });

  const login = async (identifier: string, password: string) => {
    try {
      const res = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userName: identifier, password }),
      });
      
      if (!res.ok) {
        throw new Error("Invalid credentials");
      }
      
      const token = await res.text();
      
      if (token === "Failure" || !token) {
        return false;
      }
      
      const payload = parseJwt(token);
      const userName = payload?.sub || identifier;
      
      // Lire le rôle depuis le JWT : Spring Security le met sous 'role', 'roles' ou 'authorities'
      let role = "ROLE_ANALYSTE_SECURITE"; // fallback par défaut
      if (payload?.role) {
        role = payload.role;
      } else if (Array.isArray(payload?.roles) && payload.roles.length > 0) {
        role = payload.roles[0];
      } else if (Array.isArray(payload?.authorities) && payload.authorities.length > 0) {
        role = payload.authorities[0]?.authority || payload.authorities[0];
      }
      
      const u = { name: userName, email: identifier, role };
      setUser(u);
      localStorage.setItem("vulnscan-user", JSON.stringify(u));
      localStorage.setItem("vulnscan-token", token);
      return true;
    } catch (e) {
      console.error("Login failed:", e);
      return false;
    }
  };

  const register = async (name: string, email: string, password: string) => {
    try {
      const res = await fetch("/api/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userName: name, email, password }), 
      });
      
      if (!res.ok) {
        throw new Error("Registration failed");
      }
      
      // Auto-authentification une fois le compte créé
      return await login(email, password);
    } catch (e) {
      console.error("Registration failed:", e);
      return false;
    }
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem("vulnscan-user");
    localStorage.removeItem("vulnscan-token");
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  );
}
