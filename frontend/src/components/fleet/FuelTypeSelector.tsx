import React from 'react';
import {
  FormControl,
  FormLabel,
  RadioGroup,
  FormControlLabel,
  Radio,
  Box,
  Typography,
  Paper,
} from '@mui/material';
import { FuelType, FUEL_TYPE_OPTIONS } from '../../constants/fuelTypes';

interface FuelTypeSelectorProps {
  value: FuelType;
  onChange: (fuelType: FuelType) => void;
  error?: boolean;
  helperText?: string;
}

/**
 * FuelTypeSelector Component
 * Allows users to select the fuel/power type for a vehicle
 * Displays options with icons, labels, and descriptions
 */
const FuelTypeSelector: React.FC<FuelTypeSelectorProps> = ({
  value,
  onChange,
  error,
  helperText,
}) => {
  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    onChange(event.target.value as FuelType);
  };

  return (
    <FormControl component="fieldset" error={error} fullWidth>
      <FormLabel component="legend" sx={{ mb: 2 }}>
        Fuel Type *
      </FormLabel>
      <RadioGroup
        value={value}
        onChange={handleChange}
        sx={{ gap: 2 }}
      >
        {FUEL_TYPE_OPTIONS.map((option) => (
          <Paper
            key={option.value}
            elevation={value === option.value ? 3 : 1}
            sx={{
              p: 2,
              border: value === option.value ? 2 : 1,
              borderColor: value === option.value ? option.color : 'divider',
              cursor: 'pointer',
              transition: 'all 0.2s',
              '&:hover': {
                elevation: 2,
                borderColor: option.color,
              },
            }}
          >
            <FormControlLabel
              value={option.value}
              control={<Radio />}
              label={
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, width: '100%' }}>
                  <Box
                    sx={{
                      fontSize: '2rem',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      width: 60,
                      height: 60,
                      borderRadius: 2,
                      bgcolor: `${option.color}20`,
                    }}
                  >
                    {option.icon}
                  </Box>
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="subtitle1" fontWeight="bold">
                      {option.label}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {option.description}
                    </Typography>
                  </Box>
                </Box>
              }
              sx={{
                m: 0,
                width: '100%',
                '& .MuiFormControlLabel-label': {
                  width: '100%',
                },
              }}
            />
          </Paper>
        ))}
      </RadioGroup>
      {helperText && (
        <Typography variant="caption" color="error" sx={{ mt: 1 }}>
          {helperText}
        </Typography>
      )}
    </FormControl>
  );
};

export default FuelTypeSelector;
