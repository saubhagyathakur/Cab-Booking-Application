import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const savedUser = localStorage.getItem('cab_user');
    const savedToken = localStorage.getItem('cab_jwt_token');
    if (savedUser && savedToken) {
      try {
        setUser(JSON.parse(savedUser));
      } catch (e) {
        localStorage.removeItem('cab_user');
        localStorage.removeItem('cab_jwt_token');
      }
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    const res = await authApi.login({ email, password });
    if (res.data.success) {
      const data = res.data.data;
      const userInfo = {
        id: data.id,
        name: data.name,
        email: data.email,
        role: data.role,
        driverId: data.driverId,
        token: data.token,
      };
      localStorage.setItem('cab_jwt_token', data.token);
      localStorage.setItem('cab_user', JSON.stringify(userInfo));
      setUser(userInfo);
      return userInfo;
    }
    throw new Error(res.data.message || 'Login failed');
  };

  const register = async (userData) => {
    const res = await authApi.register(userData);
    if (res.data.success) {
      const data = res.data.data;
      const userInfo = {
        id: data.id,
        name: data.name,
        email: data.email,
        role: data.role,
        driverId: data.driverId,
        token: data.token,
      };
      localStorage.setItem('cab_jwt_token', data.token);
      localStorage.setItem('cab_user', JSON.stringify(userInfo));
      setUser(userInfo);
      return userInfo;
    }
    throw new Error(res.data.message || 'Registration failed');
  };

  const logout = () => {
    localStorage.removeItem('cab_jwt_token');
    localStorage.removeItem('cab_user');
    setUser(null);
  };

  // 1-Click Demo account switcher for seamless testing
  const switchDemoAccount = async (targetRole) => {
    let email = 'rider@example.com';
    let password = 'password123';
    if (targetRole === 'ROLE_DRIVER') {
      email = 'driver@example.com';
    } else if (targetRole === 'ROLE_ADMIN') {
      email = 'admin@example.com';
      password = 'admin123';
    }
    return await login(email, password);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        login,
        register,
        logout,
        switchDemoAccount,
        isAuthenticated: !!user,
        isRider: user?.role === 'ROLE_RIDER',
        isDriver: user?.role === 'ROLE_DRIVER',
        isAdmin: user?.role === 'ROLE_ADMIN',
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
