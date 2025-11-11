import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Grid,
  FormControlLabel,
  Checkbox,
  FormGroup,
  Alert,
  CircularProgress,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
} from '@mui/material';
import { PictureAsPdf, Download } from '@mui/icons-material';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import vehicleService from '../services/vehicleService';
import analyticsService from '../services/analyticsService';
import { toast } from 'react-toastify';

const VehicleReportPage: React.FC = () => {
  const [vehicles, setVehicles] = useState<any[]>([]);
  const [selectedVehicle, setSelectedVehicle] = useState<number | ''>('');
  const [startDate, setStartDate] = useState<Date | null>(
    new Date(Date.now() - 30 * 24 * 60 * 60 * 1000) // 30 days ago
  );
  const [endDate, setEndDate] = useState<Date | null>(new Date());
  const [loading, setLoading] = useState(false);
  const [loadingVehicles, setLoadingVehicles] = useState(true);

  // Report sections to include
  const [reportOptions, setReportOptions] = useState({
    includeVehicleInfo: true,
    includeEventHistory: true,
    includeTripHistory: true,
    includeMaintenanceHistory: true,
    includeChargingHistory: true,
    includeAlertHistory: true,
    includePerformanceMetrics: true,
    includeCostAnalysis: true,
  });

  useEffect(() => {
    fetchVehicles();
  }, []);

  const fetchVehicles = async () => {
    try {
      setLoadingVehicles(true);
      const data = await vehicleService.getVehicles();
      setVehicles(data);
    } catch (error) {
      console.error('Error fetching vehicles:', error);
      toast.error('Failed to load vehicles');
    } finally {
      setLoadingVehicles(false);
    }
  };

  const handleOptionChange = (option: keyof typeof reportOptions) => {
    setReportOptions((prev) => ({
      ...prev,
      [option]: !prev[option],
    }));
  };

  const handleGenerateReport = async () => {
    if (!selectedVehicle) {
      toast.error('Please select a vehicle');
      return;
    }

    if (!startDate || !endDate) {
      toast.error('Please select start and end dates');
      return;
    }

    setLoading(true);
    try {
      const blob = await analyticsService.generateVehicleReport({
        vehicleId: selectedVehicle as number,
        startDate: startDate.toISOString(),
        endDate: endDate.toISOString(),
        ...reportOptions,
      });

      // Download the file
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `vehicle_report_${selectedVehicle}_${Date.now()}.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);

      toast.success('Report generated successfully!');
    } catch (error) {
      console.error('Error generating report:', error);
      toast.error('Failed to generate report');
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateGenealogyReport = async () => {
    if (!selectedVehicle) {
      toast.error('Please select a vehicle');
      return;
    }

    if (!startDate || !endDate) {
      toast.error('Please select start and end dates');
      return;
    }

    setLoading(true);
    try {
      const blob = await analyticsService.generateGenealogyReport(
        selectedVehicle as number,
        startDate.toISOString(),
        endDate.toISOString()
      );

      // Download the file
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `vehicle_genealogy_${selectedVehicle}_${Date.now()}.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);

      toast.success('Genealogy report generated successfully!');
    } catch (error) {
      console.error('Error generating genealogy report:', error);
      toast.error('Failed to generate genealogy report');
    } finally {
      setLoading(false);
    }
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Box>
        <Box mb={4}>
          <Typography variant="h3" fontWeight={700} sx={{ color: 'text.primary', mb: 0.5 }}>
            Vehicle Reports (v.report)
          </Typography>
          <Typography variant="body1" color="text.secondary" fontWeight={400}>
            Generate comprehensive vehicle reports with genealogy and historical data
          </Typography>
        </Box>

        <Grid container spacing={3}>
          {/* Configuration Card */}
          <Grid item xs={12} md={6}>
            <Card>
              <CardContent sx={{ p: 3 }}>
                <Typography variant="h6" fontWeight={600} mb={3}>
                  Report Configuration
                </Typography>

                {/* Vehicle Selection */}
                <FormControl fullWidth sx={{ mb: 3 }}>
                  <InputLabel>Select Vehicle</InputLabel>
                  <Select
                    value={selectedVehicle}
                    onChange={(e) => setSelectedVehicle(e.target.value as number)}
                    label="Select Vehicle"
                    disabled={loadingVehicles}
                  >
                    <MenuItem value="">
                      <em>Select a vehicle</em>
                    </MenuItem>
                    {vehicles.map((vehicle) => (
                      <MenuItem key={vehicle.id} value={vehicle.id}>
                        {vehicle.vehicleNumber} - {vehicle.make} {vehicle.model}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>

                {/* Date Range */}
                <Box mb={3}>
                  <DateTimePicker
                    label="Start Date"
                    value={startDate}
                    onChange={(newValue) => setStartDate(newValue)}
                    sx={{ width: '100%', mb: 2 }}
                  />
                  <DateTimePicker
                    label="End Date"
                    value={endDate}
                    onChange={(newValue) => setEndDate(newValue)}
                    sx={{ width: '100%' }}
                  />
                </Box>

                {/* Report Sections */}
                <Typography variant="subtitle2" fontWeight={600} mb={2}>
                  Report Sections
                </Typography>
                <FormGroup>
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={reportOptions.includeVehicleInfo}
                        onChange={() => handleOptionChange('includeVehicleInfo')}
                      />
                    }
                    label="Vehicle Information"
                  />
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={reportOptions.includeEventHistory}
                        onChange={() => handleOptionChange('includeEventHistory')}
                      />
                    }
                    label="Event History"
                  />
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={reportOptions.includeTripHistory}
                        onChange={() => handleOptionChange('includeTripHistory')}
                      />
                    }
                    label="Trip History"
                  />
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={reportOptions.includeMaintenanceHistory}
                        onChange={() => handleOptionChange('includeMaintenanceHistory')}
                      />
                    }
                    label="Maintenance History"
                  />
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={reportOptions.includeChargingHistory}
                        onChange={() => handleOptionChange('includeChargingHistory')}
                      />
                    }
                    label="Charging History"
                  />
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={reportOptions.includeAlertHistory}
                        onChange={() => handleOptionChange('includeAlertHistory')}
                      />
                    }
                    label="Alert History"
                  />
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={reportOptions.includePerformanceMetrics}
                        onChange={() => handleOptionChange('includePerformanceMetrics')}
                      />
                    }
                    label="Performance Metrics"
                  />
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={reportOptions.includeCostAnalysis}
                        onChange={() => handleOptionChange('includeCostAnalysis')}
                      />
                    }
                    label="Cost Analysis"
                  />
                </FormGroup>
              </CardContent>
            </Card>
          </Grid>

          {/* Actions Card */}
          <Grid item xs={12} md={6}>
            <Card>
              <CardContent sx={{ p: 3 }}>
                <Typography variant="h6" fontWeight={600} mb={3}>
                  Generate Reports
                </Typography>

                <Alert severity="info" sx={{ mb: 3 }}>
                  Reports include comprehensive historical data for the selected vehicle within the specified time range.
                </Alert>

                {/* Comprehensive Report */}
                <Box mb={3}>
                  <Typography variant="subtitle2" fontWeight={600} mb={1}>
                    Comprehensive Report
                  </Typography>
                  <Typography variant="body2" color="text.secondary" mb={2}>
                    Generate a complete report with all selected sections including vehicle info, events, trips, maintenance, charging, alerts, performance metrics, and cost analysis.
                  </Typography>
                  <Button
                    variant="contained"
                    color="primary"
                    fullWidth
                    size="large"
                    startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <PictureAsPdf />}
                    onClick={handleGenerateReport}
                    disabled={loading || !selectedVehicle}
                  >
                    {loading ? 'Generating...' : 'Generate Comprehensive Report'}
                  </Button>
                </Box>

                {/* Genealogy Report */}
                <Box>
                  <Typography variant="subtitle2" fontWeight={600} mb={1}>
                    Genealogy Report
                  </Typography>
                  <Typography variant="body2" color="text.secondary" mb={2}>
                    Generate a genealogy report focusing on complete event history and timeline of the vehicle, including all operational events and activities.
                  </Typography>
                  <Button
                    variant="outlined"
                    color="primary"
                    fullWidth
                    size="large"
                    startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <Download />}
                    onClick={handleGenerateGenealogyReport}
                    disabled={loading || !selectedVehicle}
                  >
                    {loading ? 'Generating...' : 'Generate Genealogy Report'}
                  </Button>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </Box>
    </LocalizationProvider>
  );
};

export default VehicleReportPage;
