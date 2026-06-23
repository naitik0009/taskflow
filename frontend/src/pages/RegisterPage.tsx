import { FormEvent, useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { apiErrorMessage } from '../api/client';
import AuthLayout from '../components/AuthLayout';

export default function RegisterPage() {
  const { register, token } = useAuth();
  const navigate = useNavigate();
  const [displayName, setDisplayName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  if (token) return <Navigate to="/" replace />;

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await register(email.trim(), displayName.trim(), password);
      navigate('/');
    } catch (err) {
      setError(apiErrorMessage(err, 'Unable to create account'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthLayout title="Create your account" subtitle="Start collaborating in seconds">
      <form onSubmit={onSubmit} className="space-y-4">
        {error && (
          <div className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{error}</div>
        )}
        <Field label="Name" type="text" value={displayName} onChange={setDisplayName} />
        <Field label="Email" type="email" value={email} onChange={setEmail} />
        <Field
          label="Password"
          type="password"
          value={password}
          onChange={setPassword}
          hint="At least 6 characters"
        />
        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-lg bg-brand-600 py-2.5 font-semibold text-white transition hover:bg-brand-700 disabled:opacity-60"
        >
          {submitting ? 'Creating…' : 'Create account'}
        </button>
      </form>
      <p className="mt-5 text-center text-sm text-slate-500">
        Already registered?{' '}
        <Link to="/login" className="font-semibold text-brand-600 hover:underline">
          Sign in
        </Link>
      </p>
    </AuthLayout>
  );
}

interface FieldProps {
  label: string;
  type: string;
  value: string;
  onChange: (v: string) => void;
  hint?: string;
}

function Field({ label, type, value, onChange, hint }: FieldProps) {
  return (
    <label className="block">
      <span className="mb-1 block text-sm font-medium text-slate-600">{label}</span>
      <input
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        required
        minLength={type === 'password' ? 6 : undefined}
        className="w-full rounded-lg border border-slate-300 px-3 py-2 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
      />
      {hint && <span className="mt-1 block text-xs text-slate-400">{hint}</span>}
    </label>
  );
}
