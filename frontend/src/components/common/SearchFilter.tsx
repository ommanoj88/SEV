import React from 'react';
import { Box, TextField, Button, Grid, Paper, SxProps, Theme } from '@mui/material';

interface FilterField {
  id: string;
  label: string;
  type?: 'text' | 'select' | 'date' | 'number';
  options?: { label: string; value: string }[];
  placeholder?: string;
}

interface SearchFilterProps {
  fields: FilterField[];
  onSearch: (values: Record<string, any>) => void;
  onReset?: () => void;
  sx?: SxProps<Theme>;
  showResetButton?: boolean;
}

const SearchFilter: React.FC<SearchFilterProps> = ({
  fields,
  onSearch,
  onReset,
  sx = {},
  showResetButton = true,
}) => {
  const [values, setValues] = React.useState<Record<string, any>>(
    fields.reduce((acc, field) => ({ ...acc, [field.id]: '' }), {})
  );

  const handleChange = (fieldId: string, value: any) => {
    setValues((prev) => ({ ...prev, [fieldId]: value }));
  };

  const handleSearch = () => {
    onSearch(values);
  };

  const handleReset = () => {
    setValues(fields.reduce((acc, field) => ({ ...acc, [field.id]: '' }), {}));
    onReset?.();
  };

  return (
    <Paper sx={{ p: 2, ...sx }}>
      <Grid container spacing={2}>
        {fields.map((field) => (
          <Grid item xs={12} sm={6} md={4} key={field.id}>
            {field.type === 'select' ? (
              <TextField
                select
                fullWidth
                label={field.label}
                value={values[field.id]}
                onChange={(e) => handleChange(field.id, e.target.value)}
                SelectProps={{
                  native: true,
                }}
              >
                <option value="">All</option>
                {field.options?.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </TextField>
            ) : (
              <TextField
                fullWidth
                label={field.label}
                type={field.type || 'text'}
                value={values[field.id]}
                onChange={(e) => handleChange(field.id, e.target.value)}
                placeholder={field.placeholder}
              />
            )}
          </Grid>
        ))}
        <Grid item xs={12} sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
          {showResetButton && (
            <Button variant="outlined" onClick={handleReset}>
              Reset
            </Button>
          )}
          <Button variant="contained" onClick={handleSearch}>
            Search
          </Button>
        </Grid>
      </Grid>
    </Paper>
  );
};

export default SearchFilter;
