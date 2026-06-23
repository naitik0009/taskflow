import { FormEvent, useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { apiErrorMessage } from '../api/client';
import AuthLayout from '../components/AuthLayout';

export default function LoginPage() {
  const { login, token } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('demo@taskflow.dev');
  const [password, setPassword] = useState('password123');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  if (token) return <Navigate to="/" replace />;

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await login(email.trim(), password);
      navigate('/');
    } catch (err) {
      setError(apiErrorMessage(err, 'Unable to sign in'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthLayout title="Welcome back" subtitle="Sign in to your TaskFlow workspace">
      <form onSubmit={onSubmit} className="space-y-4">
        {error && (
          <div className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{error}</div>
        )}
        <Field label="Email" type="email" value={email} onChange={setEmail} autoComplete="email" />
        <Field
          label="Password"
          type="password"
          value={password}
          onChange={setPassword}
          autoComplete="current-password"
        />
        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-lg bg-brand-600 py-2.5 font-semibold text-white transition hover:bg-brand-700 disabled:opacity-60"
        >
          {submitting ? 'Signing in…' : 'Sign in'}
        </button>
      </form>
      <p className="mt-5 text-center text-sm text-slate-500">
        No account?{' '}
        <Link to="/register" className="font-semibold text-brand-600 hover:underline">
          Create one
        </Link>
      </p>
      <p className="mt-2 text-center text-xs text-slate-400">
        Demo: demo@taskflow.dev / password123
      </p>
    </AuthLayout>
  );
}

interface FieldProps {
  label: string;
  type: string;
  value: string;
  onChange: (v: string) => void;
  autoComplete?: string;
}

function Field({ label, type, value, onChange, autoComplete }: FieldProps) {
  return (
    <label className="block">
      <span className="mb-1 block text-sm font-medium text-slate-600">{label}</span>
      <input
        type={type}
        value={value}
        autoComplete={autoComplete}
        onChange={(e) => onChange(e.target.value)}
        required
        className="w-full rounded-lg border border-slate-300 px-3 py-2 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
      />
    </label>
  );
}
