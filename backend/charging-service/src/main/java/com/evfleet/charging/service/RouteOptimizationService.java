package com.evfleet.charging.service;

import com.evfleet.charging.dto.RouteOptimizationRequest;
import com.evfleet.charging.dto.RouteOptimizationResponse;

import java.util.List;

public interface RouteOptimizationService {
    RouteOptimizationResponse optimizeRoute(RouteOptimizationRequest request);
    RouteOptimizationResponse getRouteById(Long id);
    List<RouteOptimizationResponse> getRoutesByVehicle(Long vehicleId);
}
