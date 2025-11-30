import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  Grid,
  Card,
  CardContent,
  CardActionArea,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  SelectChangeEvent,
  Button,
  IconButton,
  Tooltip,
  Chip,
  Stepper,
  Step,
  StepLabel,
  StepContent,
  TextField,
  Checkbox,
  FormControlLabel,
  FormGroup,
  Radio,
  RadioGroup,
  Divider,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  ListItemSecondaryAction,
  Avatar,
  LinearProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  AlertTitle,
  Switch,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  useTheme,
  alpha,
  Tabs,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from '@mui/material';
import {
  Eco as EcoIcon,
  Co2 as Co2Icon,
  Assessment as ReportIcon,
  Download as DownloadIcon,
  Preview as PreviewIcon,
  Schedule as ScheduleIcon,
  Email as EmailIcon,
  Today as TodayIcon,
  DateRange as DateRangeIcon,
  DirectionsCar as VehicleIcon,
  LocalShipping as FleetIcon,
  CheckCircle as CheckIcon,
  RadioButtonUnchecked as UncheckedIcon,
  ExpandMore as ExpandMoreIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  Nature as NatureIcon,
  Air as AirIcon,
  WaterDrop as WaterIcon,
  Battery90 as BatteryIcon,
  Speed as SpeedIcon,
  Compare as CompareIcon,
  PictureAsPdf as PdfIcon,
  Description as DocIcon,
  TableChart as ExcelIcon,
  Send as SendIcon,
  Close as CloseIcon,
  Visibility as VisibilityIcon,
  CalendarMonth as CalendarIcon,
  Repeat as RecurringIcon,
  Info as InfoIcon,
  BarChart as ChartIcon,
} from '@mui/icons-material';
import {
  LineChart,
  Line,
  AreaChart,
  Area,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip as RechartsTooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { DatePicker, LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';

// Types
interface ReportType {
  id: string;
  title: string;
  description: string;
  icon: React.ReactNode;
  color: string;
  sections: string[];
}

interface VehicleSelection {
  id: string;
  name: string;
  licensePlate: string;
  type: string;
  selected: boolean;
}

interface ESGMetrics {
  carbonFootprint: number; // tons CO2
  carbonReduction: number; // percentage vs ICE
  energyEfficiency: number; // kWh/100km
  renewableEnergy: number; // percentage
  treesEquivalent: number;
  gasGallonsSaved: number;
  emissionsAvoided: number; // tons CO2
}

interface MonthlyEmissions {
  month: string;
  evEmissions: number;
  iceEquivalent: number;
  savings: number;
}

interface RecurringSchedule {
  enabled: boolean;
  frequency: 'weekly' | 'monthly' | 'quarterly';
  dayOfWeek?: number;
  dayOfMonth?: number;
  recipients: string[];
}

// Mock data
const reportTypes: ReportType[] = [
  {
    id: 'carbon',
    title: 'Carbon Footprint Report',
    description: 'Comprehensive analysis of CO2 emissions from your EV fleet operations',
    icon: <Co2Icon />,
    color: '#4caf50',
    sections: ['Total Emissions', 'Emissions by Vehicle', 'Monthly Trends', 'Reduction Targets'],
  },
  {
    id: 'comparison',
    title: 'Emissions Comparison Report',
    description: 'Compare EV fleet emissions against equivalent ICE vehicle fleet',
    icon: <CompareIcon />,
    color: '#2196f3',
    sections: ['EV vs ICE Comparison', 'Cost Savings', 'Environmental Impact', 'Future Projections'],
  },
  {
    id: 'sustainability',
    title: 'Sustainability Summary',
    description: 'Executive summary of environmental achievements and sustainability metrics',
    icon: <NatureIcon />,
    color: '#9c27b0',
    sections: ['Executive Summary', 'Key Achievements', 'ESG Scorecard', 'Recommendations'],
  },
  {
    id: 'regulatory',
    title: 'Regulatory Compliance Report',
    description: 'Report formatted for environmental regulatory compliance',
    icon: <ReportIcon />,
    color: '#ff9800',
    sections: ['Compliance Status', 'Emissions Data', 'Certifications', 'Audit Trail'],
  },
];

const mockVehicles: VehicleSelection[] = [
  { id: 'v-001', name: 'Tesla Model 3', licensePlate: 'EV-1234', type: 'Sedan', selected: true },
  { id: 'v-002', name: 'Rivian R1T', licensePlate: 'EV-5678', type: 'Truck', selected: true },
  { id: 'v-003', name: 'Ford E-Transit', licensePlate: 'EV-9012', type: 'Van', selected: true },
  { id: 'v-004', name: 'Chevy Bolt', licensePlate: 'EV-3456', type: 'Compact', selected: true },
  { id: 'v-005', name: 'Tesla Model Y', licensePlate: 'EV-7890', type: 'SUV', selected: true },
];

const mockESGMetrics: ESGMetrics = {
  carbonFootprint: 12.5,
  carbonReduction: 78,
  energyEfficiency: 15.2,
  renewableEnergy: 65,
  treesEquivalent: 576,
  gasGallonsSaved: 8420,
  emissionsAvoided: 42.8,
};

const mockMonthlyEmissions: MonthlyEmissions[] = [
  { month: 'Jan', evEmissions: 2.1, iceEquivalent: 8.5, savings: 6.4 },
  { month: 'Feb', evEmissions: 1.9, iceEquivalent: 8.2, savings: 6.3 },
  { month: 'Mar', evEmissions: 2.3, iceEquivalent: 9.1, savings: 6.8 },
  { month: 'Apr', evEmissions: 2.0, iceEquivalent: 8.4, savings: 6.4 },
  { month: 'May', evEmissions: 2.2, iceEquivalent: 8.9, savings: 6.7 },
  { month: 'Jun', evEmissions: 2.4, iceEquivalent: 9.5, savings: 7.1 },
  { month: 'Jul', evEmissions: 2.6, iceEquivalent: 10.2, savings: 7.6 },
  { month: 'Aug', evEmissions: 2.5, iceEquivalent: 9.8, savings: 7.3 },
  { month: 'Sep', evEmissions: 2.3, iceEquivalent: 9.2, savings: 6.9 },
  { month: 'Oct', evEmissions: 2.2, iceEquivalent: 8.8, savings: 6.6 },
  { month: 'Nov', evEmissions: 2.1, iceEquivalent: 8.5, savings: 6.4 },
  { month: 'Dec', evEmissions: 2.0, iceEquivalent: 8.3, savings: 6.3 },
];

const emissionsBySource = [
  { name: 'Grid Electricity', value: 75, color: '#2196f3' },
  { name: 'Manufacturing', value: 15, color: '#ff9800' },
  { name: 'Maintenance', value: 7, color: '#9c27b0' },
  { name: 'Other', value: 3, color: '#607d8b' },
];

// Main Component
const ESGReportGenerator: React.FC = () => {
  const theme = useTheme();

  // State
  const [activeStep, setActiveStep] = useState(0);
  const [selectedReport, setSelectedReport] = useState<string | null>(null);
  const [startDate, setStartDate] = useState<Date | null>(new Date(new Date().setMonth(new Date().getMonth() - 3)));
  const [endDate, setEndDate] = useState<Date | null>(new Date());
  const [vehicles, setVehicles] = useState<VehicleSelection[]>(mockVehicles);
  const [selectAllVehicles, setSelectAllVehicles] = useState(true);
  const [previewOpen, setPreviewOpen] = useState(false);
  const [scheduleOpen, setScheduleOpen] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [exportFormat, setExportFormat] = useState<'pdf' | 'docx' | 'xlsx'>('pdf');
  const [recurringSchedule, setRecurringSchedule] = useState<RecurringSchedule>({
    enabled: false,
    frequency: 'monthly',
    dayOfMonth: 1,
    recipients: [],
  });
  const [recipientEmail, setRecipientEmail] = useState('');

  // Handlers
  const handleReportSelect = (reportId: string) => {
    setSelectedReport(reportId);
    setActiveStep(1);
  };

  const handleVehicleToggle = (vehicleId: string) => {
    setVehicles(vehicles.map(v => 
      v.id === vehicleId ? { ...v, selected: !v.selected } : v
    ));
  };

  const handleSelectAllVehicles = () => {
    const newValue = !selectAllVehicles;
    setSelectAllVehicles(newValue);
    setVehicles(vehicles.map(v => ({ ...v, selected: newValue })));
  };

  const handleNext = () => {
    setActiveStep(activeStep + 1);
  };

  const handleBack = () => {
    setActiveStep(activeStep - 1);
  };

  const handleGenerate = async () => {
    setGenerating(true);
    // Simulate report generation
    await new Promise(resolve => setTimeout(resolve, 2000));
    setGenerating(false);
    setPreviewOpen(true);
  };

  const handleDownload = () => {
    console.log(`Downloading report as ${exportFormat}...`);
    // Implementation would generate and download the file
  };

  const handleScheduleRecurring = () => {
    console.log('Scheduling recurring report:', recurringSchedule);
    setScheduleOpen(false);
  };

  const handleAddRecipient = () => {
    if (recipientEmail && !recurringSchedule.recipients.includes(recipientEmail)) {
      setRecurringSchedule({
        ...recurringSchedule,
        recipients: [...recurringSchedule.recipients, recipientEmail],
      });
      setRecipientEmail('');
    }
  };

  const handleRemoveRecipient = (email: string) => {
    setRecurringSchedule({
      ...recurringSchedule,
      recipients: recurringSchedule.recipients.filter(r => r !== email),
    });
  };

  const selectedVehicleCount = vehicles.filter(v => v.selected).length;
  const selectedReportData = reportTypes.find(r => r.id === selectedReport);

  const steps = [
    { label: 'Select Report Type', description: 'Choose the type of ESG report to generate' },
    { label: 'Date Range', description: 'Select the reporting period' },
    { label: 'Vehicle Selection', description: 'Choose vehicles to include' },
    { label: 'Generate Report', description: 'Preview and download your report' },
  ];

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Box sx={{ p: 3 }}>
        {/* Header */}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Box>
            <Typography variant="h4" fontWeight={700}>
              ESG Report Generator
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Generate environmental sustainability reports for your EV fleet
            </Typography>
          </Box>
          <Button
            variant="outlined"
            startIcon={<RecurringIcon />}
            onClick={() => setScheduleOpen(true)}
          >
            Schedule Recurring
          </Button>
        </Box>

        {/* ESG Summary Cards */}
        <Grid container spacing={2} sx={{ mb: 4 }}>
          <Grid item xs={12} sm={6} md={3}>
            <Paper sx={{ p: 2, background: `linear-gradient(135deg, ${alpha('#4caf50', 0.1)} 0%, ${alpha('#4caf50', 0.05)} 100%)` }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Avatar sx={{ bgcolor: alpha('#4caf50', 0.2), color: '#4caf50' }}>
                  <EcoIcon />
                </Avatar>
                <Box>
                  <Typography variant="h5" fontWeight={700} color="success.main">
                    {mockESGMetrics.carbonReduction}%
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Carbon Reduction vs ICE
                  </Typography>
                </Box>
              </Box>
            </Paper>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Paper sx={{ p: 2, background: `linear-gradient(135deg, ${alpha('#2196f3', 0.1)} 0%, ${alpha('#2196f3', 0.05)} 100%)` }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Avatar sx={{ bgcolor: alpha('#2196f3', 0.2), color: '#2196f3' }}>
                  <Co2Icon />
                </Avatar>
                <Box>
                  <Typography variant="h5" fontWeight={700} color="primary">
                    {mockESGMetrics.emissionsAvoided}t
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    CO₂ Emissions Avoided
                  </Typography>
                </Box>
              </Box>
            </Paper>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Paper sx={{ p: 2, background: `linear-gradient(135deg, ${alpha('#9c27b0', 0.1)} 0%, ${alpha('#9c27b0', 0.05)} 100%)` }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Avatar sx={{ bgcolor: alpha('#9c27b0', 0.2), color: '#9c27b0' }}>
                  <NatureIcon />
                </Avatar>
                <Box>
                  <Typography variant="h5" fontWeight={700} sx={{ color: '#9c27b0' }}>
                    {mockESGMetrics.treesEquivalent}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Trees Equivalent Planted
                  </Typography>
                </Box>
              </Box>
            </Paper>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Paper sx={{ p: 2, background: `linear-gradient(135deg, ${alpha('#ff9800', 0.1)} 0%, ${alpha('#ff9800', 0.05)} 100%)` }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Avatar sx={{ bgcolor: alpha('#ff9800', 0.2), color: '#ff9800' }}>
                  <BatteryIcon />
                </Avatar>
                <Box>
                  <Typography variant="h5" fontWeight={700} color="warning.main">
                    {mockESGMetrics.gasGallonsSaved.toLocaleString()}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Gallons of Gas Saved
                  </Typography>
                </Box>
              </Box>
            </Paper>
          </Grid>
        </Grid>

        {/* Stepper */}
        <Paper sx={{ p: 3 }}>
          <Stepper activeStep={activeStep} orientation="vertical">
            {/* Step 1: Report Type */}
            <Step>
              <StepLabel>
                <Typography fontWeight={600}>{steps[0].label}</Typography>
                <Typography variant="caption" color="text.secondary">{steps[0].description}</Typography>
              </StepLabel>
              <StepContent>
                <Grid container spacing={2} sx={{ mt: 1 }}>
                  {reportTypes.map((report) => (
                    <Grid item xs={12} sm={6} md={3} key={report.id}>
                      <Card
                        sx={{
                          height: '100%',
                          border: selectedReport === report.id ? `2px solid ${report.color}` : '1px solid',
                          borderColor: selectedReport === report.id ? report.color : 'divider',
                          transition: 'all 0.2s ease',
                          '&:hover': {
                            boxShadow: theme.shadows[4],
                            transform: 'translateY(-2px)',
                          },
                        }}
                      >
                        <CardActionArea onClick={() => handleReportSelect(report.id)} sx={{ height: '100%' }}>
                          <CardContent>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                              <Avatar sx={{ bgcolor: alpha(report.color, 0.15), color: report.color }}>
                                {report.icon}
                              </Avatar>
                              {selectedReport === report.id && (
                                <CheckIcon color="success" sx={{ ml: 'auto' }} />
                              )}
                            </Box>
                            <Typography variant="h6" fontWeight={600} gutterBottom>
                              {report.title}
                            </Typography>
                            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                              {report.description}
                            </Typography>
                            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                              {report.sections.slice(0, 3).map((section, idx) => (
                                <Chip key={idx} label={section} size="small" variant="outlined" />
                              ))}
                              {report.sections.length > 3 && (
                                <Chip label={`+${report.sections.length - 3}`} size="small" />
                              )}
                            </Box>
                          </CardContent>
                        </CardActionArea>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              </StepContent>
            </Step>

            {/* Step 2: Date Range */}
            <Step>
              <StepLabel>
                <Typography fontWeight={600}>{steps[1].label}</Typography>
                <Typography variant="caption" color="text.secondary">{steps[1].description}</Typography>
              </StepLabel>
              <StepContent>
                <Grid container spacing={3} sx={{ mt: 1 }}>
                  <Grid item xs={12} sm={6} md={4}>
                    <DatePicker
                      label="Start Date"
                      value={startDate}
                      onChange={(date) => setStartDate(date)}
                      slotProps={{ textField: { fullWidth: true } }}
                    />
                  </Grid>
                  <Grid item xs={12} sm={6} md={4}>
                    <DatePicker
                      label="End Date"
                      value={endDate}
                      onChange={(date) => setEndDate(date)}
                      slotProps={{ textField: { fullWidth: true } }}
                    />
                  </Grid>
                  <Grid item xs={12} md={4}>
                    <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                      <Chip
                        label="Last Month"
                        onClick={() => {
                          const now = new Date();
                          setStartDate(new Date(now.getFullYear(), now.getMonth() - 1, 1));
                          setEndDate(new Date(now.getFullYear(), now.getMonth(), 0));
                        }}
                        variant="outlined"
                      />
                      <Chip
                        label="Last Quarter"
                        onClick={() => {
                          const now = new Date();
                          setStartDate(new Date(now.getFullYear(), now.getMonth() - 3, 1));
                          setEndDate(now);
                        }}
                        variant="outlined"
                      />
                      <Chip
                        label="YTD"
                        onClick={() => {
                          setStartDate(new Date(new Date().getFullYear(), 0, 1));
                          setEndDate(new Date());
                        }}
                        variant="outlined"
                      />
                      <Chip
                        label="Last Year"
                        onClick={() => {
                          const now = new Date();
                          setStartDate(new Date(now.getFullYear() - 1, 0, 1));
                          setEndDate(new Date(now.getFullYear() - 1, 11, 31));
                        }}
                        variant="outlined"
                      />
                    </Box>
                  </Grid>
                </Grid>
                <Box sx={{ mt: 2 }}>
                  <Button onClick={handleBack} sx={{ mr: 1 }}>Back</Button>
                  <Button variant="contained" onClick={handleNext}>Continue</Button>
                </Box>
              </StepContent>
            </Step>

            {/* Step 3: Vehicle Selection */}
            <Step>
              <StepLabel>
                <Typography fontWeight={600}>{steps[2].label}</Typography>
                <Typography variant="caption" color="text.secondary">{steps[2].description}</Typography>
              </StepLabel>
              <StepContent>
                <Box sx={{ mt: 1 }}>
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={selectAllVehicles}
                        indeterminate={selectedVehicleCount > 0 && selectedVehicleCount < vehicles.length}
                        onChange={handleSelectAllVehicles}
                      />
                    }
                    label={
                      <Typography fontWeight={600}>
                        Select All Vehicles ({selectedVehicleCount} of {vehicles.length} selected)
                      </Typography>
                    }
                  />
                  <Divider sx={{ my: 1 }} />
                  <Grid container spacing={1}>
                    {vehicles.map((vehicle) => (
                      <Grid item xs={12} sm={6} md={4} key={vehicle.id}>
                        <Paper
                          variant="outlined"
                          sx={{
                            p: 1.5,
                            cursor: 'pointer',
                            bgcolor: vehicle.selected ? alpha(theme.palette.primary.main, 0.05) : 'transparent',
                            borderColor: vehicle.selected ? 'primary.main' : 'divider',
                            transition: 'all 0.2s ease',
                            '&:hover': {
                              borderColor: 'primary.main',
                            },
                          }}
                          onClick={() => handleVehicleToggle(vehicle.id)}
                        >
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <Checkbox checked={vehicle.selected} size="small" />
                            <Avatar sx={{ bgcolor: alpha(theme.palette.primary.main, 0.15), width: 32, height: 32 }}>
                              <VehicleIcon fontSize="small" color="primary" />
                            </Avatar>
                            <Box sx={{ flex: 1 }}>
                              <Typography variant="body2" fontWeight={600}>{vehicle.name}</Typography>
                              <Typography variant="caption" color="text.secondary">
                                {vehicle.licensePlate} • {vehicle.type}
                              </Typography>
                            </Box>
                          </Box>
                        </Paper>
                      </Grid>
                    ))}
                  </Grid>
                </Box>
                <Box sx={{ mt: 2 }}>
                  <Button onClick={handleBack} sx={{ mr: 1 }}>Back</Button>
                  <Button variant="contained" onClick={handleNext} disabled={selectedVehicleCount === 0}>
                    Continue
                  </Button>
                </Box>
              </StepContent>
            </Step>

            {/* Step 4: Generate Report */}
            <Step>
              <StepLabel>
                <Typography fontWeight={600}>{steps[3].label}</Typography>
                <Typography variant="caption" color="text.secondary">{steps[3].description}</Typography>
              </StepLabel>
              <StepContent>
                <Box sx={{ mt: 1 }}>
                  {/* Summary */}
                  <Alert severity="info" sx={{ mb: 2 }}>
                    <AlertTitle>Report Summary</AlertTitle>
                    <Typography variant="body2">
                      <strong>Report Type:</strong> {selectedReportData?.title || 'Not selected'}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Date Range:</strong> {startDate?.toLocaleDateString()} - {endDate?.toLocaleDateString()}
                    </Typography>
                    <Typography variant="body2">
                      <strong>Vehicles:</strong> {selectedVehicleCount} selected
                    </Typography>
                  </Alert>

                  {/* Export Format */}
                  <Typography variant="subtitle2" gutterBottom>Export Format</Typography>
                  <RadioGroup
                    row
                    value={exportFormat}
                    onChange={(e) => setExportFormat(e.target.value as 'pdf' | 'docx' | 'xlsx')}
                    sx={{ mb: 2 }}
                  >
                    <FormControlLabel
                      value="pdf"
                      control={<Radio />}
                      label={
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                          <PdfIcon color="error" /> PDF
                        </Box>
                      }
                    />
                    <FormControlLabel
                      value="docx"
                      control={<Radio />}
                      label={
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                          <DocIcon color="primary" /> Word
                        </Box>
                      }
                    />
                    <FormControlLabel
                      value="xlsx"
                      control={<Radio />}
                      label={
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                          <ExcelIcon color="success" /> Excel
                        </Box>
                      }
                    />
                  </RadioGroup>

                  {/* Sections to Include */}
                  {selectedReportData && (
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="subtitle2" gutterBottom>Sections to Include</Typography>
                      <FormGroup row>
                        {selectedReportData.sections.map((section, idx) => (
                          <FormControlLabel
                            key={idx}
                            control={<Checkbox defaultChecked size="small" />}
                            label={section}
                          />
                        ))}
                      </FormGroup>
                    </Box>
                  )}
                </Box>

                <Box sx={{ mt: 2 }}>
                  <Button onClick={handleBack} sx={{ mr: 1 }}>Back</Button>
                  <Button
                    variant="outlined"
                    startIcon={<PreviewIcon />}
                    onClick={() => setPreviewOpen(true)}
                    sx={{ mr: 1 }}
                  >
                    Preview
                  </Button>
                  <Button
                    variant="contained"
                    startIcon={generating ? undefined : <DownloadIcon />}
                    onClick={handleGenerate}
                    disabled={generating}
                  >
                    {generating ? 'Generating...' : 'Generate & Download'}
                  </Button>
                </Box>
                {generating && <LinearProgress sx={{ mt: 2 }} />}
              </StepContent>
            </Step>
          </Stepper>
        </Paper>

        {/* Preview Dialog */}
        <Dialog open={previewOpen} onClose={() => setPreviewOpen(false)} maxWidth="lg" fullWidth>
          <DialogTitle>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                {selectedReportData && (
                  <Avatar sx={{ bgcolor: alpha(selectedReportData.color, 0.15), color: selectedReportData.color }}>
                    {selectedReportData.icon}
                  </Avatar>
                )}
                <Typography variant="h6">{selectedReportData?.title || 'Report'} Preview</Typography>
              </Box>
              <IconButton onClick={() => setPreviewOpen(false)}>
                <CloseIcon />
              </IconButton>
            </Box>
          </DialogTitle>
          <DialogContent dividers>
            {/* Report Header */}
            <Box sx={{ textAlign: 'center', mb: 4, p: 3, bgcolor: alpha(theme.palette.primary.main, 0.05), borderRadius: 2 }}>
              <Typography variant="h4" fontWeight={700} gutterBottom>
                {selectedReportData?.title}
              </Typography>
              <Typography variant="subtitle1" color="text.secondary">
                Reporting Period: {startDate?.toLocaleDateString()} - {endDate?.toLocaleDateString()}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Generated on {new Date().toLocaleDateString()} • {selectedVehicleCount} vehicles included
              </Typography>
            </Box>

            {/* Executive Summary */}
            <Typography variant="h5" fontWeight={600} gutterBottom>
              Executive Summary
            </Typography>
            <Grid container spacing={3} sx={{ mb: 4 }}>
              <Grid item xs={12} md={6}>
                <Paper sx={{ p: 3, height: '100%' }}>
                  <Typography variant="h6" gutterBottom>Key Environmental Metrics</Typography>
                  <List dense>
                    <ListItem>
                      <ListItemIcon><Co2Icon color="success" /></ListItemIcon>
                      <ListItemText
                        primary={`${mockESGMetrics.carbonReduction}% Carbon Reduction`}
                        secondary="Compared to equivalent ICE fleet"
                      />
                    </ListItem>
                    <ListItem>
                      <ListItemIcon><EcoIcon color="success" /></ListItemIcon>
                      <ListItemText
                        primary={`${mockESGMetrics.emissionsAvoided} tons CO₂ Avoided`}
                        secondary="During reporting period"
                      />
                    </ListItem>
                    <ListItem>
                      <ListItemIcon><NatureIcon color="success" /></ListItemIcon>
                      <ListItemText
                        primary={`${mockESGMetrics.treesEquivalent} Trees Equivalent`}
                        secondary="Environmental offset equivalent"
                      />
                    </ListItem>
                    <ListItem>
                      <ListItemIcon><BatteryIcon color="success" /></ListItemIcon>
                      <ListItemText
                        primary={`${mockESGMetrics.renewableEnergy}% Renewable Energy`}
                        secondary="Charging from renewable sources"
                      />
                    </ListItem>
                  </List>
                </Paper>
              </Grid>
              <Grid item xs={12} md={6}>
                <Paper sx={{ p: 3, height: '100%' }}>
                  <Typography variant="h6" gutterBottom>Emissions by Source</Typography>
                  <ResponsiveContainer width="100%" height={200}>
                    <PieChart>
                      <Pie
                        data={emissionsBySource}
                        cx="50%"
                        cy="50%"
                        innerRadius={50}
                        outerRadius={80}
                        paddingAngle={2}
                        dataKey="value"
                      >
                        {emissionsBySource.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={entry.color} />
                        ))}
                      </Pie>
                      <RechartsTooltip />
                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                </Paper>
              </Grid>
            </Grid>

            {/* Emissions Trend */}
            <Typography variant="h5" fontWeight={600} gutterBottom>
              Monthly Emissions Comparison
            </Typography>
            <Paper sx={{ p: 3, mb: 4 }}>
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={mockMonthlyEmissions}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="month" />
                  <YAxis label={{ value: 'Tons CO₂', angle: -90, position: 'insideLeft' }} />
                  <RechartsTooltip />
                  <Legend />
                  <Bar dataKey="evEmissions" name="EV Fleet Emissions" fill="#4caf50" />
                  <Bar dataKey="iceEquivalent" name="ICE Equivalent" fill="#f44336" />
                </BarChart>
              </ResponsiveContainer>
              <Box sx={{ display: 'flex', justifyContent: 'center', gap: 4, mt: 2 }}>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography variant="h4" color="success.main" fontWeight={700}>
                    {mockMonthlyEmissions.reduce((sum, m) => sum + m.evEmissions, 0).toFixed(1)}t
                  </Typography>
                  <Typography variant="caption" color="text.secondary">Total EV Emissions</Typography>
                </Box>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography variant="h4" color="error.main" fontWeight={700}>
                    {mockMonthlyEmissions.reduce((sum, m) => sum + m.iceEquivalent, 0).toFixed(1)}t
                  </Typography>
                  <Typography variant="caption" color="text.secondary">ICE Equivalent</Typography>
                </Box>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography variant="h4" color="primary.main" fontWeight={700}>
                    {mockMonthlyEmissions.reduce((sum, m) => sum + m.savings, 0).toFixed(1)}t
                  </Typography>
                  <Typography variant="caption" color="text.secondary">Total Savings</Typography>
                </Box>
              </Box>
            </Paper>

            {/* Vehicle Breakdown */}
            <Typography variant="h5" fontWeight={600} gutterBottom>
              Vehicle Emissions Breakdown
            </Typography>
            <TableContainer component={Paper} sx={{ mb: 4 }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Vehicle</TableCell>
                    <TableCell align="right">Distance (km)</TableCell>
                    <TableCell align="right">Energy (kWh)</TableCell>
                    <TableCell align="right">CO₂ (kg)</TableCell>
                    <TableCell align="right">Efficiency</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {vehicles.filter(v => v.selected).map((vehicle) => (
                    <TableRow key={vehicle.id}>
                      <TableCell>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Avatar sx={{ width: 24, height: 24, bgcolor: alpha(theme.palette.primary.main, 0.15) }}>
                            <VehicleIcon sx={{ fontSize: 14 }} color="primary" />
                          </Avatar>
                          <Box>
                            <Typography variant="body2" fontWeight={600}>{vehicle.name}</Typography>
                            <Typography variant="caption" color="text.secondary">{vehicle.licensePlate}</Typography>
                          </Box>
                        </Box>
                      </TableCell>
                      <TableCell align="right">{(Math.random() * 5000 + 2000).toFixed(0)}</TableCell>
                      <TableCell align="right">{(Math.random() * 800 + 300).toFixed(0)}</TableCell>
                      <TableCell align="right">{(Math.random() * 500 + 100).toFixed(0)}</TableCell>
                      <TableCell align="right">
                        <Chip
                          size="small"
                          label={`${(Math.random() * 20 + 80).toFixed(0)}%`}
                          color="success"
                          variant="outlined"
                        />
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setPreviewOpen(false)}>Close</Button>
            <Button
              variant="contained"
              startIcon={<DownloadIcon />}
              onClick={handleDownload}
            >
              Download {exportFormat.toUpperCase()}
            </Button>
          </DialogActions>
        </Dialog>

        {/* Schedule Recurring Dialog */}
        <Dialog open={scheduleOpen} onClose={() => setScheduleOpen(false)} maxWidth="sm" fullWidth>
          <DialogTitle>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Typography variant="h6">Schedule Recurring Report</Typography>
              <IconButton onClick={() => setScheduleOpen(false)} size="small">
                <CloseIcon />
              </IconButton>
            </Box>
          </DialogTitle>
          <DialogContent dividers>
            <FormControlLabel
              control={
                <Switch
                  checked={recurringSchedule.enabled}
                  onChange={(e) => setRecurringSchedule({ ...recurringSchedule, enabled: e.target.checked })}
                />
              }
              label="Enable Recurring Reports"
            />

            {recurringSchedule.enabled && (
              <Box sx={{ mt: 2 }}>
                <FormControl fullWidth sx={{ mb: 2 }}>
                  <InputLabel>Frequency</InputLabel>
                  <Select
                    value={recurringSchedule.frequency}
                    onChange={(e) => setRecurringSchedule({ ...recurringSchedule, frequency: e.target.value as any })}
                    label="Frequency"
                  >
                    <MenuItem value="weekly">Weekly</MenuItem>
                    <MenuItem value="monthly">Monthly</MenuItem>
                    <MenuItem value="quarterly">Quarterly</MenuItem>
                  </Select>
                </FormControl>

                {recurringSchedule.frequency === 'monthly' && (
                  <FormControl fullWidth sx={{ mb: 2 }}>
                    <InputLabel>Day of Month</InputLabel>
                    <Select
                      value={recurringSchedule.dayOfMonth}
                      onChange={(e) => setRecurringSchedule({ ...recurringSchedule, dayOfMonth: Number(e.target.value) })}
                      label="Day of Month"
                    >
                      {Array.from({ length: 28 }, (_, i) => i + 1).map((day) => (
                        <MenuItem key={day} value={day}>{day}</MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                )}

                <Divider sx={{ my: 2 }} />

                <Typography variant="subtitle2" gutterBottom>Email Recipients</Typography>
                <Box sx={{ display: 'flex', gap: 1, mb: 1 }}>
                  <TextField
                    size="small"
                    placeholder="email@example.com"
                    value={recipientEmail}
                    onChange={(e) => setRecipientEmail(e.target.value)}
                    fullWidth
                  />
                  <Button variant="outlined" onClick={handleAddRecipient}>
                    Add
                  </Button>
                </Box>
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                  {recurringSchedule.recipients.map((email) => (
                    <Chip
                      key={email}
                      label={email}
                      onDelete={() => handleRemoveRecipient(email)}
                      size="small"
                    />
                  ))}
                </Box>
              </Box>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setScheduleOpen(false)}>Cancel</Button>
            <Button
              variant="contained"
              startIcon={<ScheduleIcon />}
              onClick={handleScheduleRecurring}
              disabled={!recurringSchedule.enabled || recurringSchedule.recipients.length === 0}
            >
              Save Schedule
            </Button>
          </DialogActions>
        </Dialog>
      </Box>
    </LocalizationProvider>
  );
};

export default ESGReportGenerator;
