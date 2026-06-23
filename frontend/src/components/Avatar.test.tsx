import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import Avatar, { initials } from './Avatar';

describe('Avatar', () => {
  it('derives up to two initials from a name', () => {
    expect(initials('Alex Rivera')).toBe('AR');
    expect(initials('Demo')).toBe('D');
    expect(initials('Jordan Lee Smith')).toBe('JL');
  });

  it('renders the initials and a title for accessibility', () => {
    render(
      <Avatar user={{ id: 'u1', email: 'a@x.dev', displayName: 'Alex Rivera' }} />,
    );
    const el = screen.getByText('AR');
    expect(el).toBeInTheDocument();
    expect(el).toHaveAttribute('title', 'Alex Rivera');
  });
});
