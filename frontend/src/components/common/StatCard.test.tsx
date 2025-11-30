import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import StatCard from './StatCard';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';

/**
 * Unit tests for StatCard component
 * PR #43: Frontend Component Tests
 */
describe('StatCard', () => {
  const defaultProps = {
    title: 'Total Vehicles',
    value: 150,
  };

  describe('rendering', () => {
    it('renders title and value', () => {
      render(<StatCard {...defaultProps} />);
      
      expect(screen.getByText('Total Vehicles')).toBeInTheDocument();
      expect(screen.getByText('150')).toBeInTheDocument();
    });

    it('renders string value', () => {
      render(<StatCard {...defaultProps} value="1,250 km" />);
      
      expect(screen.getByText('1,250 km')).toBeInTheDocument();
    });

    it('renders with subtitle', () => {
      render(<StatCard {...defaultProps} subtitle="Active fleet" />);
      
      expect(screen.getByText('Active fleet')).toBeInTheDocument();
    });

    it('renders with icon', () => {
      render(<StatCard {...defaultProps} icon={<TrendingUpIcon data-testid="icon" />} />);
      
      expect(screen.getByTestId('icon')).toBeInTheDocument();
    });
  });

  describe('trend indicator', () => {
    it('renders positive trend', () => {
      render(<StatCard {...defaultProps} trend={12.5} />);
      
      expect(screen.getByText('+12.5%')).toBeInTheDocument();
      expect(screen.getByText('vs last period')).toBeInTheDocument();
    });

    it('renders negative trend', () => {
      render(<StatCard {...defaultProps} trend={-8.3} />);
      
      expect(screen.getByText('-8.3%')).toBeInTheDocument();
    });

    it('renders zero trend as positive', () => {
      render(<StatCard {...defaultProps} trend={0} />);
      
      expect(screen.getByText('+0%')).toBeInTheDocument();
    });

    it('renders custom trend label', () => {
      render(<StatCard {...defaultProps} trend={5} trendLabel="compared to yesterday" />);
      
      expect(screen.getByText('compared to yesterday')).toBeInTheDocument();
    });
  });

  describe('loading state', () => {
    it('shows loading indicator when loading', () => {
      render(<StatCard {...defaultProps} loading={true} />);
      
      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });

    it('does not show loading indicator when not loading', () => {
      render(<StatCard {...defaultProps} loading={false} />);
      
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    });
  });

  describe('click behavior', () => {
    it('calls onClick when clicked', () => {
      const handleClick = jest.fn();
      render(<StatCard {...defaultProps} onClick={handleClick} />);
      
      const card = screen.getByText('Total Vehicles').closest('.MuiCard-root');
      fireEvent.click(card!);
      
      expect(handleClick).toHaveBeenCalledTimes(1);
    });

    it('has pointer cursor when clickable', () => {
      const handleClick = jest.fn();
      render(<StatCard {...defaultProps} onClick={handleClick} />);
      
      const card = screen.getByText('Total Vehicles').closest('.MuiCard-root');
      expect(card).toHaveStyle({ cursor: 'pointer' });
    });

    it('has default cursor when not clickable', () => {
      render(<StatCard {...defaultProps} />);
      
      const card = screen.getByText('Total Vehicles').closest('.MuiCard-root');
      expect(card).toHaveStyle({ cursor: 'default' });
    });
  });

  describe('styling', () => {
    it('applies custom sx prop', () => {
      render(<StatCard {...defaultProps} sx={{ backgroundColor: 'red' }} />);
      
      const card = screen.getByText('Total Vehicles').closest('.MuiCard-root');
      expect(card).toHaveStyle({ backgroundColor: 'red' });
    });
  });
});
