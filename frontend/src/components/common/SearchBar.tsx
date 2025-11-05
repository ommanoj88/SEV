import React, { useState, useCallback } from 'react';
import {
  Paper,
  InputBase,
  IconButton,
  Box,
  alpha,
  Chip,
  Collapse,
  Divider,
} from '@mui/material';
import {
  Search as SearchIcon,
  Clear as ClearIcon,
  Tune as FilterIcon,
} from '@mui/icons-material';

interface SearchBarProps {
  placeholder?: string;
  onSearch: (query: string) => void;
  onFilterClick?: () => void;
  filters?: string[];
  onFilterRemove?: (filter: string) => void;
  autoFocus?: boolean;
  fullWidth?: boolean;
  debounceMs?: number;
}

const SearchBar: React.FC<SearchBarProps> = ({
  placeholder = 'Search...',
  onSearch,
  onFilterClick,
  filters = [],
  onFilterRemove,
  autoFocus = false,
  fullWidth = true,
  debounceMs = 300,
}) => {
  const [query, setQuery] = useState('');
  const [isFocused, setIsFocused] = useState(false);

  // Debounced search handler
  const debouncedSearch = useCallback(
    (() => {
      let timeoutId: NodeJS.Timeout;
      return (value: string) => {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => {
          onSearch(value);
        }, debounceMs);
      };
    })(),
    [onSearch, debounceMs]
  );

  const handleSearch = (value: string) => {
    setQuery(value);
    debouncedSearch(value);
  };

  const handleClear = () => {
    setQuery('');
    onSearch('');
  };

  return (
    <Box sx={{ width: fullWidth ? '100%' : 'auto' }}>
      <Paper
        elevation={0}
        sx={{
          p: '4px 12px',
          display: 'flex',
          alignItems: 'center',
          width: fullWidth ? '100%' : 400,
          borderRadius: 3,
          border: (theme) =>
            isFocused
              ? `2px solid ${theme.palette.primary.main}`
              : `1px solid ${alpha(theme.palette.divider, 0.5)}`,
          background: (theme) =>
            theme.palette.mode === 'light'
              ? alpha(theme.palette.background.paper, 0.8)
              : alpha(theme.palette.background.paper, 0.6),
          backdropFilter: 'blur(10px)',
          transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
          '&:hover': {
            border: (theme) => `1px solid ${alpha(theme.palette.primary.main, 0.5)}`,
            boxShadow: (theme) => `0px 4px 20px ${alpha(theme.palette.primary.main, 0.1)}`,
          },
        }}
      >
        <SearchIcon sx={{ color: 'text.secondary', mr: 1 }} />
        <InputBase
          sx={{ ml: 1, flex: 1 }}
          placeholder={placeholder}
          value={query}
          onChange={(e) => handleSearch(e.target.value)}
          onFocus={() => setIsFocused(true)}
          onBlur={() => setIsFocused(false)}
          autoFocus={autoFocus}
        />
        {query && (
          <IconButton
            size="small"
            onClick={handleClear}
            sx={{
              transition: 'transform 0.2s ease',
              '&:hover': {
                transform: 'rotate(90deg)',
              },
            }}
          >
            <ClearIcon fontSize="small" />
          </IconButton>
        )}
        {onFilterClick && (
          <>
            <Divider sx={{ height: 28, m: 0.5 }} orientation="vertical" />
            <IconButton
              size="small"
              onClick={onFilterClick}
              sx={{
                ml: 1,
                transition: 'all 0.2s ease',
                '&:hover': {
                  transform: 'scale(1.1)',
                  color: 'primary.main',
                },
              }}
            >
              <FilterIcon fontSize="small" />
            </IconButton>
          </>
        )}
      </Paper>

      <Collapse in={filters.length > 0}>
        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mt: 2 }}>
          {filters.map((filter) => (
            <Chip
              key={filter}
              label={filter}
              onDelete={() => onFilterRemove?.(filter)}
              size="small"
              sx={{
                borderRadius: 2,
                fontWeight: 600,
              }}
            />
          ))}
        </Box>
      </Collapse>
    </Box>
  );
};

export default SearchBar;
