import React, { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme, CssBaseline, CircularProgress, Box } from '@mui/material';
import { api } from './services/apiService';
import { User, PersonRole } from './types';
import LoginPage from './pages/LoginPage';
import AdminDashboard from './pages/AdminDashboard';
import ClientDashboard from './pages/ClientDashboard';
import DeviceChartPage from './pages/DeviceChartPage';
import ProtectedRoute from './components/ProtectedRoute';
import OverconsumptionNotification from './components/OverconsumptionNotification';
import ChatbotWidget from './components/ChatbotWidget';


const theme = createTheme({
  palette: {
    primary: {
      main: '#667eea',
    },
    secondary: {
      main: '#764ba2',
    },
    background: {
      default: '#f5f7fa'
    }
  }
});

function App() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const verifyAuth = async () => {
      try {
        if (localStorage.getItem('auth')) {
          const userData = await api.getMe();
          localStorage.setItem('user', JSON.stringify(userData));
          setUser(userData);
        }
      } catch (error) {
        console.error('Auth verification failed:', error);
        api.logout();
      }
      setLoading(false);
    };
    verifyAuth();
  }, []);


  const handleLogin = (userData: User) => {
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
  };


  const handleLogout = () => {
    api.logout();
    setUser(null);
  };


  if (loading) {
    return (
        <ThemeProvider theme={theme}>
          <Box
              display="flex"
              justifyContent="center"
              alignItems="center"
              minHeight="100vh"
          >
            <CircularProgress />
          </Box>
        </ThemeProvider>
    );
  }


  return (
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <OverconsumptionNotification user={user} />
        {user && user.role === PersonRole.CLIENT && <ChatbotWidget />}
        <BrowserRouter>
          <Routes>

            <Route path="/" element={
              user ? (user.role === PersonRole.ADMIN ? <Navigate to="/admin" /> : <Navigate to="/client" />)
                  : <Navigate to="/login" />
            } />


            <Route path="/login" element={
              !user ? (
                  <LoginPage onLogin={handleLogin} />
              ) : (

                  user.role === PersonRole.ADMIN ? <Navigate to="/admin" /> : <Navigate to="/client" />
              )
            } />


            <Route path="/admin" element={
              <ProtectedRoute user={user} requiredRole={PersonRole.ADMIN}>

                <AdminDashboard user={user!} onLogout={handleLogout} />
              </ProtectedRoute>
            } />


            <Route path="/client" element={
              <ProtectedRoute user={user} requiredRole={PersonRole.CLIENT}>

                <ClientDashboard user={user!} onLogout={handleLogout} />
              </ProtectedRoute>
            } />

            <Route path="/device/:deviceId/chart" element={
              user ? (
                <DeviceChartPage />
              ) : (
                <Navigate to="/login" />
              )
            } />

            <Route path="*" element={<Navigate to="/" />} />

          </Routes>
        </BrowserRouter>
      </ThemeProvider>
  );
}

export default App;