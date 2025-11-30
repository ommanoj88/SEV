import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import StatusBadge from './StatusBadge';

/**
 * Unit tests for StatusBadge component
 * PR #43: Frontend Component Tests
 */
describe('StatusBadge', () => {
  describe('rendering', () => {
    it('renders status text', () => {
      render(<StatusBadge status="ACTIVE" />);
      
      expect(screen.getByText('ACTIVE')).toBeInTheDocument();
    });

    it('renders as a chip', () => {
      render(<StatusBadge status="ACTIVE" />);
      
      const chip = screen.getByText('ACTIVE').closest('.MuiChip-root');
      expect(chip).toBeInTheDocument();
    });
  });

  describe('default color mapping', () => {
    it('renders ACTIVE with success color', () => {
      render(<StatusBadge status="ACTIVE" />);
      
      const chip = screen.getByText('ACTIVE').closest('.MuiChip-root');
      expect(chip).toHaveClass('MuiChip-colorSuccess');
    });

    it('renders INACTIVE with error color', () => {
      render(<StatusBadge status="INACTIVE" />);
      
      const chip = screen.getByText('INACTIVE').closest('.MuiChip-root');
      expect(chip).toHaveClass('MuiChip-colorError');
    });

    it('renders PENDING with warning color', () => {
      render(<StatusBadge status="PENDING" />);
      
      const chip = screen.getByText('PENDING').closest('.MuiChip-root');
      expect(chip).toHaveClass('MuiChip-colorWarning');
    });

    it('renders COMPLETED with success color', () => {
      render(<StatusBadge status="COMPLETED" />);
      
      const chip = screen.getByText('COMPLETED').closest('.MuiChip-root');
      expect(chip).toHaveClass('MuiChip-colorSuccess');
    });

    it('renders ONGOING with info color', () => {
      render(<StatusBadge status="ONGOING" />);
      
      const chip = screen.getByText('ONGOING').closest('.MuiChip-root');
      expect(chip).toHaveClass('MuiChip-colorInfo');
    });

    it('renders CANCELLED with error color', () => {
      render(<StatusBadge status="CANCELLED" />);
      
      const chip = screen.getByText('CANCELLED').closest('.MuiChip-root');
      expect(chip).toHaveClass('MuiChip-colorError');
    });

    it('renders CHARGING with warning color', () => {
      render(<StatusBadge status="CHARGING" />);
      
      const chip = screen.getByText('CHARGING').closest('.MuiChip-root');
      expect(chip).toHaveClass('MuiChip-colorWarning');
    });

    it('renders FAILED with error color', () => {
      render(<StatusBadge status="FAILED" />);
      
      const chip = screen.getByText('FAILED').closest('.MuiChip-root');
      expect(chip).toHaveClass('MuiChip-colorError');
    });
  });

  describe('unknown status', () => {
    it('renders unknown status with default color', () => {
      render(<StatusBadge status="UNKNOWN_STATUS" />);
      
      const chip = screen.getByText('UNKNOWN_STATUS').closest('.MuiChip-root');
      expect(chip).toHaveClass('MuiChip-colorDefault');
    });
  });

  describe('custom status colors', () => {
    it('uses custom status colors when provided', () => {
      const customColors = {
        CUSTOM: 'primary',
      };
      
      render(<StatusBadge status="CUSTOM" statusColors={customColors} />);
      
      const chip = screen.getByText('CUSTOM').closest('.MuiChip-root');
      expect(chip).toHaveClass('MuiChip-colorPrimary');
    });
  });

  describe('size', () => {
    it('renders with small size by default', () => {
      render(<StatusBadge status="ACTIVE" />);
      
      const chip = screen.getByText('ACTIVE').closest('.MuiChip-root');
      expect(chip).toHaveClass('MuiChip-sizeSmall');
    });

    it('renders with medium size when specified', () => {
      render(<StatusBadge status="ACTIVE" size="medium" />);
      
      const chip = screen.getByText('ACTIVE').closest('.MuiChip-root');
      expect(chip).toHaveClass('MuiChip-sizeMedium');
    });
  });
});
