import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import type { ReactNode } from 'react';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import BoardsPage from './pages/BoardsPage';
import BoardPage from './pages/BoardPage';
import Spinner from './components/Spinner';

function ProtectedRoute({ children }: { children: ReactNode }) {
  const { token, loading } = useAuth();
  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Spinner />
      </div>
    );
  }
  return token ? <>{children}</> : <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <BoardsPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/boards/:boardId"
        element={
          <ProtectedRoute>
            <BoardPage />
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
