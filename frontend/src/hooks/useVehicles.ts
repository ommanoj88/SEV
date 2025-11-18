import { useEffect, useCallback } from 'react';
import { useAppDispatch, useAppSelector } from '../redux/hooks';
import { fetchVehicles, selectVehicles, selectVehicleLoading } from '../redux/slices/vehicleSlice';
import { VehicleFilters } from '../types';

export const useVehicles = (filters?: VehicleFilters) => {
  const dispatch = useAppDispatch();
  const vehicles = useAppSelector(selectVehicles);
  const loading = useAppSelector(selectVehicleLoading);

  useEffect(() => {
    // Only fetch if companyId is available
    if (filters?.companyId) {
      dispatch(fetchVehicles(filters));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dispatch, filters?.companyId, filters?.status, filters?.type, filters?.batteryMin, filters?.batteryMax, filters?.search, filters?.assignedDriver]);

  const refetch = useCallback(() => {
    if (filters?.companyId) {
      dispatch(fetchVehicles(filters));
    }
  }, [dispatch, filters]);

  return { vehicles, loading, refetch };
};
