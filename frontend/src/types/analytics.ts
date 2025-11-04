export interface FleetAnalytics {
  totalVehicles: number;
  activeVehicles: number;
  inactiveVehicles: number;
  chargingVehicles: number;
  maintenanceVehicles: number;
  inTripVehicles: number;
  averageBatteryLevel: number;
  averageBatteryHealth: number;
  totalDistance: number; // in km
  totalTrips: number;
  totalEnergyConsumed: number; // in kWh
  utilizationRate: number; // percentage
  averageUtilization: number; // hours per day
  summary?: {
    totalVehicles: number;
    totalTrips: number;
    totalDistance: number;
    averageUtilization: number;
  };
  trends?: {
    utilization: Array<{
      date: string;
      value: number;
    }>;
  };
}

export interface UtilizationData {
  date: string;
  vehicleId?: string;
  vehicleName?: string;
  hoursActive: number;
  hoursIdle: number;
  hoursCharging: number;
  distance: number;
  trips: number;
  utilizationRate: number;
}

export interface EnergyConsumption {
  date: string;
  vehicleId?: string;
  energyConsumed: number; // in kWh
  distance: number; // in km
  efficiency: number; // kWh per 100km
  chargingCost: number;
  regenEnergy?: number; // regenerative braking energy
}

export interface CostAnalysis {
  period: string;
  energyCost: number;
  maintenanceCost: number;
  insuranceCost?: number;
  otherCosts?: number;
  totalCost: number;
  costPerKm: number;
  costPerVehicle: number;
}

export interface TCOAnalysis {
  vehicleId?: string;
  vehicleName?: string;
  purchasePrice: number;
  depreciation: number;
  energyCosts: number;
  maintenanceCosts: number;
  insuranceCosts: number;
  taxesFees: number;
  otherCosts: number;
  totalCost: number;
  costPerKm: number;
  costPerYear: number;
  comparisonWithICE?: {
    fuelSavings: number;
    maintenanceSavings: number;
    totalSavings: number;
    paybackPeriod: number; // in months
  };
}

export interface CarbonFootprint {
  date: string;
  energyConsumed: number; // in kWh
  co2Avoided: number; // in kg
  equivalentTrees: number;
  equivalentICEVehicles?: number;
  gridCarbonIntensity?: number; // g CO2/kWh
}

export interface PerformanceMetrics {
  vehicleId?: string;
  averageSpeed: number;
  maxSpeed: number;
  averageRange: number;
  energyEfficiency: number; // kWh/100km
  regenEfficiency?: number; // percentage
  brakingScore: number;
  accelerationScore: number;
  ecoScore: number;
}

export interface FinancialSummary {
  period: string;
  revenue?: number;
  totalCosts: number;
  netIncome?: number;
  roi?: number;
  costBreakdown: {
    category: string;
    amount: number;
    percentage: number;
  }[];
  trends: {
    month: string;
    revenue?: number;
    costs: number;
    profit?: number;
  }[];
}

export interface BatteryAnalytics {
  averageSOC: number;
  averageSOH: number;
  totalCapacity: number; // kWh
  degradationRate: number; // % per year
  chargingCycles: number;
  batteryHealthDistribution: {
    range: string;
    count: number;
  }[];
  healthTrend: {
    date: string;
    avgHealth: number;
  }[];
  predictedReplacement?: {
    vehicleId: string;
    vehicleName: string;
    currentSOH: number;
    estimatedMonths: number;
  }[];
}

export interface DriverPerformance {
  driverId: string;
  driverName: string;
  totalTrips: number;
  totalDistance: number;
  averageScore: number;
  safetyScore: number;
  efficiencyScore: number;
  violations: number;
  accidents: number;
  fuelEfficiency: number;
  onTimePercentage: number;
}

export interface AlertStatistics {
  total: number;
  critical: number;
  warning: number;
  info: number;
  resolved: number;
  pending: number;
  byCategory: {
    category: string;
    count: number;
  }[];
  trend: {
    date: string;
    count: number;
  }[];
}

export interface TimeRangeFilter {
  start: string;
  end: string;
  preset?: 'today' | 'week' | 'month' | 'quarter' | 'year' | 'custom';
}
