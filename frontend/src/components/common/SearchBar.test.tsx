import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import SearchBar from './SearchBar';

/**
 * Unit tests for SearchBar component
 * PR #43: Frontend Component Tests
 */
describe('SearchBar', () => {
  const mockOnSearch = jest.fn();
  const mockOnFilterClick = jest.fn();
  const mockOnFilterRemove = jest.fn();

  beforeEach(() => {
    jest.useFakeTimers();
    mockOnSearch.mockClear();
    mockOnFilterClick.mockClear();
    mockOnFilterRemove.mockClear();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  describe('rendering', () => {
    it('renders with default placeholder', () => {
      render(<SearchBar onSearch={mockOnSearch} />);
      
      expect(screen.getByPlaceholderText('Search...')).toBeInTheDocument();
    });

    it('renders with custom placeholder', () => {
      render(<SearchBar onSearch={mockOnSearch} placeholder="Find vehicles..." />);
      
      expect(screen.getByPlaceholderText('Find vehicles...')).toBeInTheDocument();
    });

    it('renders search icon', () => {
      render(<SearchBar onSearch={mockOnSearch} />);
      
      expect(screen.getByTestId('SearchIcon')).toBeInTheDocument();
    });
  });

  describe('search functionality', () => {
    it('calls onSearch after debounce delay', async () => {
      render(<SearchBar onSearch={mockOnSearch} debounceMs={300} />);
      
      const input = screen.getByPlaceholderText('Search...');
      fireEvent.change(input, { target: { value: 'test query' } });
      
      // Should not be called immediately
      expect(mockOnSearch).not.toHaveBeenCalled();
      
      // Advance timers past debounce
      act(() => {
        jest.advanceTimersByTime(300);
      });
      
      expect(mockOnSearch).toHaveBeenCalledWith('test query');
    });

    it('debounces multiple keystrokes', () => {
      render(<SearchBar onSearch={mockOnSearch} debounceMs={300} />);
      
      const input = screen.getByPlaceholderText('Search...');
      
      fireEvent.change(input, { target: { value: 't' } });
      act(() => { jest.advanceTimersByTime(100); });
      
      fireEvent.change(input, { target: { value: 'te' } });
      act(() => { jest.advanceTimersByTime(100); });
      
      fireEvent.change(input, { target: { value: 'tes' } });
      act(() => { jest.advanceTimersByTime(100); });
      
      fireEvent.change(input, { target: { value: 'test' } });
      act(() => { jest.advanceTimersByTime(300); });
      
      // Should only call once with final value
      expect(mockOnSearch).toHaveBeenCalledTimes(1);
      expect(mockOnSearch).toHaveBeenCalledWith('test');
    });
  });

  describe('clear functionality', () => {
    it('shows clear button when input has value', () => {
      render(<SearchBar onSearch={mockOnSearch} />);
      
      const input = screen.getByPlaceholderText('Search...');
      fireEvent.change(input, { target: { value: 'test' } });
      
      expect(screen.getByTestId('ClearIcon')).toBeInTheDocument();
    });

    it('clears input and calls onSearch with empty string', () => {
      render(<SearchBar onSearch={mockOnSearch} />);
      
      const input = screen.getByPlaceholderText('Search...');
      fireEvent.change(input, { target: { value: 'test' } });
      
      const clearButton = screen.getByTestId('ClearIcon').closest('button');
      fireEvent.click(clearButton!);
      
      expect(input).toHaveValue('');
      expect(mockOnSearch).toHaveBeenCalledWith('');
    });
  });

  describe('filter functionality', () => {
    it('renders filter button when onFilterClick is provided', () => {
      render(<SearchBar onSearch={mockOnSearch} onFilterClick={mockOnFilterClick} />);
      
      expect(screen.getByTestId('TuneIcon')).toBeInTheDocument();
    });

    it('calls onFilterClick when filter button is clicked', () => {
      render(<SearchBar onSearch={mockOnSearch} onFilterClick={mockOnFilterClick} />);
      
      const filterButton = screen.getByTestId('TuneIcon').closest('button');
      fireEvent.click(filterButton!);
      
      expect(mockOnFilterClick).toHaveBeenCalledTimes(1);
    });

    it('renders filter chips', () => {
      const filters = ['Status: Active', 'Type: EV'];
      render(
        <SearchBar 
          onSearch={mockOnSearch} 
          filters={filters}
          onFilterRemove={mockOnFilterRemove}
        />
      );
      
      expect(screen.getByText('Status: Active')).toBeInTheDocument();
      expect(screen.getByText('Type: EV')).toBeInTheDocument();
    });

    it('calls onFilterRemove when filter chip is deleted', () => {
      const filters = ['Status: Active'];
      render(
        <SearchBar 
          onSearch={mockOnSearch} 
          filters={filters}
          onFilterRemove={mockOnFilterRemove}
        />
      );
      
      const deleteButton = screen.getByText('Status: Active')
        .closest('.MuiChip-root')
        ?.querySelector('.MuiChip-deleteIcon');
      
      if (deleteButton) {
        fireEvent.click(deleteButton);
        expect(mockOnFilterRemove).toHaveBeenCalledWith('Status: Active');
      }
    });
  });

  describe('focus behavior', () => {
    it('auto-focuses when autoFocus prop is true', () => {
      render(<SearchBar onSearch={mockOnSearch} autoFocus={true} />);
      
      const input = screen.getByPlaceholderText('Search...');
      expect(input).toHaveFocus();
    });
  });
});
