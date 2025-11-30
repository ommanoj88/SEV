import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import LoadingSpinner from './LoadingSpinner';

/**
 * Unit tests for LoadingSpinner component
 * PR #43: Frontend Component Tests
 */
describe('LoadingSpinner', () => {
  it('renders with default message', () => {
    render(<LoadingSpinner />);
    
    expect(screen.getByText('Loading...')).toBeInTheDocument();
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('renders with custom message', () => {
    render(<LoadingSpinner message="Fetching data..." />);
    
    expect(screen.getByText('Fetching data...')).toBeInTheDocument();
  });

  it('renders without message when message is empty', () => {
    render(<LoadingSpinner message="" />);
    
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
    expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
  });

  it('renders with custom size', () => {
    const { container } = render(<LoadingSpinner size={60} />);
    
    const progressBar = container.querySelector('.MuiCircularProgress-root');
    expect(progressBar).toHaveStyle({ width: '60px', height: '60px' });
  });

  it('uses default size when not specified', () => {
    const { container } = render(<LoadingSpinner />);
    
    const progressBar = container.querySelector('.MuiCircularProgress-root');
    expect(progressBar).toHaveStyle({ width: '40px', height: '40px' });
  });
});
