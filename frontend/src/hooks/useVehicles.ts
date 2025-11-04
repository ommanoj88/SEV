import { useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '../redux/hooks';
import { fetchVehicles, selectVehicles, selectVehicleLoading } from '../redux/slices/vehicleSlice';
import { VehicleFilters } from '../types';

export const useVehicles = (filters?: VehicleFilters) => {
  const dispatch = useAppDispatch();
  const vehicles = useAppSelector(selectVehicles);
  const loading = useAppSelector(selectVehicleLoading);

  useEffect(() => {
    dispatch(fetchVehicles(filters));
  }, [dispatch, JSON.stringify(filters)]);

  const refetch = () => {
    dispatch(fetchVehicles(filters));
  };

  return { vehicles, loading, refetch };
};
