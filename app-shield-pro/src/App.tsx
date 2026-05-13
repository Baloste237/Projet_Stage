import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Route, Routes, Navigate } from "react-router-dom";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { Toaster } from "@/components/ui/toaster";
import { TooltipProvider } from "@/components/ui/tooltip";
import { ThemeProvider } from "@/context/ThemeContext";
import { AuthProvider, useAuth } from "@/context/AuthContext";
import { AppLayout } from "@/components/AppLayout";
import Dashboard from "@/pages/Dashboard";
import Login from "@/pages/Login";
import Register from "@/pages/Register";
import ForgotPassword from "@/pages/ForgotPassword";
import ResetPassword from "@/pages/ResetPassword";
import ScanWeb from "@/pages/ScanWeb";
import ScanMobile from "@/pages/ScanMobile";
import Vulnerabilities from "@/pages/Vulnerabilities";
import ScanVulnerabilities from "@/pages/ScanVulnerabilities";
import Reports from "@/pages/Reports";
import Statistics from "@/pages/Statistics";
import SettingsPage from "@/pages/Settings";
import AdminDashboard from "@/pages/AdminDashboard";
import AdminMonitoringDashboard from "@/pages/admin/AdminMonitoringDashboard";
import AdminLogs from "@/pages/admin/AdminLogs";
import AdminLive from "@/pages/admin/AdminLive";
import NotFound from "@/pages/NotFound";

const queryClient = new QueryClient();

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <AppLayout>{children}</AppLayout>;
}

function AuthRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth();
  if (isAuthenticated) return <Navigate to="/" replace />;
  return <>{children}</>;
}

const App = () => (
  <QueryClientProvider client={queryClient}>
    <ThemeProvider>
      <AuthProvider>
        <TooltipProvider>
          <Toaster />
          <Sonner />
          <BrowserRouter>
            <Routes>
              <Route path="/login"           element={<AuthRoute><Login /></AuthRoute>} />
              <Route path="/register"        element={<AuthRoute><Register /></AuthRoute>} />
              <Route path="/forgot-password" element={<AuthRoute><ForgotPassword /></AuthRoute>} />
              <Route path="/reset-password"  element={<AuthRoute><ResetPassword /></AuthRoute>} />
              <Route path="/"                element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
              <Route path="/scan-web"        element={<ProtectedRoute><ScanWeb /></ProtectedRoute>} />
              <Route path="/scan-mobile"     element={<ProtectedRoute><ScanMobile /></ProtectedRoute>} />
              <Route path="/vulnerabilities" element={<ProtectedRoute><Vulnerabilities /></ProtectedRoute>} />
              <Route path="/scan/:scanId/vulnerabilities" element={<ProtectedRoute><ScanVulnerabilities /></ProtectedRoute>} />
              <Route path="/reports"         element={<ProtectedRoute><Reports /></ProtectedRoute>} />
              <Route path="/statistics"      element={<ProtectedRoute><Statistics /></ProtectedRoute>} />
              <Route path="/settings"        element={<ProtectedRoute><SettingsPage /></ProtectedRoute>} />
              <Route path="/admin"           element={<ProtectedRoute><AdminDashboard /></ProtectedRoute>} />
              <Route path="/admin/dashboard" element={<ProtectedRoute><AdminMonitoringDashboard /></ProtectedRoute>} />
              <Route path="/admin/logs"      element={<ProtectedRoute><AdminLogs /></ProtectedRoute>} />
              <Route path="/admin/live"      element={<ProtectedRoute><AdminLive /></ProtectedRoute>} />
              <Route path="*"               element={<NotFound />} />
            </Routes>
          </BrowserRouter>
        </TooltipProvider>
      </AuthProvider>
    </ThemeProvider>
  </QueryClientProvider>
);

export default App;
