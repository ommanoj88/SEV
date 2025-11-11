# API Documentation - Route Optimization & Customer Management

## Overview
This document provides comprehensive API documentation for the Route Optimization and Customer Management features added in v2.1.0.

**Base URL**: `http://localhost:8080/api` (via API Gateway)
**Service**: fleet-service (Port 8082)

---

## Route Optimization APIs

### Base Path: `/api/routes`

### 1. Create Route Plan
**POST** `/api/routes`

Creates a new route plan with origin, destination, and optional parameters.

**Request Body:**
```json
{
  "routeName": "Delhi to Mumbai Delivery",
  "routeDescription": "Express delivery route",
  "vehicleId": 123,
  "driverId": 456,
  "originLat": 28.6139,
  "originLng": 77.2090,
  "originAddress": "New Delhi, India",
  "destinationLat": 19.0760,
  "destinationLng": 72.8777,
  "destinationAddress": "Mumbai, India",
  "plannedStartTime": "2025-11-15T08:00:00",
  "plannedEndTime": "2025-11-15T20:00:00",
  "optimizationCriteria": "DISTANCE",
  "trafficConsidered": true,
  "tollRoadsAllowed": true
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "routeName": "Delhi to Mumbai Delivery",
  "status": "PLANNED",
  "createdAt": "2025-11-11T10:00:00",
  ...
}
```

### 2. Get Route Plan
**GET** `/api/routes/{id}`

Retrieves a specific route plan by ID.

**Response:** `200 OK`

### 3. Get All Route Plans
**GET** `/api/routes`

Retrieves all route plans.

**Response:** `200 OK` - Array of route plans

### 4. Get Routes by Vehicle
**GET** `/api/routes/vehicle/{vehicleId}`

Retrieves all routes assigned to a specific vehicle.

### 5. Get Routes by Driver
**GET** `/api/routes/driver/{driverId}`

Retrieves all routes assigned to a specific driver.

### 6. Get Routes by Status
**GET** `/api/routes/status/{status}`

Retrieves routes with specific status: `PLANNED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`

### 7. Get Active Routes
**GET** `/api/routes/active`

Retrieves all routes that are PLANNED or IN_PROGRESS.

### 8. Get Today's Routes
**GET** `/api/routes/today`

Retrieves routes planned for today.

### 9. Update Route Plan
**PUT** `/api/routes/{id}`

Updates an existing route plan.

### 10. Delete Route Plan
**DELETE** `/api/routes/{id}`

Deletes a route plan and all associated waypoints.

**Response:** `204 No Content`

---

## Waypoint Management APIs

### Base Path: `/api/routes/{routePlanId}/waypoints`

### 1. Add Waypoint
**POST** `/api/routes/{routePlanId}/waypoints`

Adds a waypoint to a route plan.

**Request Body:**
```json
{
  "waypointName": "Customer Location 1",
  "latitude": 28.5355,
  "longitude": 77.3910,
  "address": "Noida, UP, India",
  "serviceType": "DELIVERY",
  "customerName": "John Doe",
  "customerPhone": "+91-9876543210",
  "customerEmail": "john@example.com",
  "itemsDescription": "Package #12345",
  "weight": 25.5,
  "stopDuration": 15,
  "arrivalWindowStart": "2025-11-15T10:00:00",
  "arrivalWindowEnd": "2025-11-15T12:00:00"
}
```

**Response:** `201 Created`

### 2. Get Waypoints
**GET** `/api/routes/{routePlanId}/waypoints`

Retrieves all waypoints for a route, ordered by sequence number.

### 3. Update Waypoint
**PUT** `/api/routes/waypoints/{waypointId}`

Updates a waypoint (status, POD information, completion notes, etc.)

**Request Body (POD Update Example):**
```json
{
  "status": "COMPLETED",
  "actualArrivalTime": "2025-11-15T11:30:00",
  "podSignaturePath": "/uploads/signatures/sig_123.png",
  "podPhotoPath": "/uploads/photos/photo_123.jpg",
  "podNotes": "Delivered successfully to customer",
  "podCapturedBy": "John Driver"
}
```

### 4. Delete Waypoint
**DELETE** `/api/routes/waypoints/{waypointId}`

Removes a waypoint from the route.

---

## Route Execution APIs

### 1. Start Route
**POST** `/api/routes/{id}/start`

Marks a route as IN_PROGRESS.

**Response:** `200 OK` - Updated route plan

### 2. Complete Route
**POST** `/api/routes/{id}/complete`

Marks a route as COMPLETED and records actual performance.

**Query Parameters:**
- `actualDistance` (optional): Actual distance traveled in km
- `actualDuration` (optional): Actual duration in minutes
- `actualFuelConsumption` (optional): Actual fuel used
- `actualCost` (optional): Actual cost incurred

**Example:**
```
POST /api/routes/123/complete?actualDistance=1425.5&actualDuration=720&actualCost=3500.00
```

### 3. Cancel Route
**POST** `/api/routes/{id}/cancel`

Marks a route as CANCELLED.

---

## Customer Management APIs

### Base Path: `/api/customers`

### 1. Create Customer
**POST** `/api/customers`

Creates a new customer.

**Request Body:**
```json
{
  "customerCode": "CUST001",
  "customerName": "ABC Corporation",
  "customerType": "BUSINESS",
  "primaryContactName": "Jane Smith",
  "primaryPhone": "+91-9876543210",
  "secondaryPhone": "+91-9876543211",
  "email": "contact@abccorp.com",
  "addressLine1": "Plot 123, Sector 18",
  "addressLine2": "Industrial Area",
  "city": "Noida",
  "state": "Uttar Pradesh",
  "postalCode": "201301",
  "country": "India",
  "latitude": 28.5706,
  "longitude": 77.3272,
  "gstin": "07AAAAA0000A1Z5",
  "pan": "AAAAA0000A",
  "creditLimit": 100000.00,
  "specialInstructions": "Delivery between 9 AM - 6 PM only"
}
```

**Response:** `201 Created`

### 2. Get Customer
**GET** `/api/customers/{id}`

Retrieves a customer by ID.

### 3. Get Customer by Code
**GET** `/api/customers/code/{customerCode}`

Retrieves a customer by their unique customer code.

### 4. Get All Customers
**GET** `/api/customers`

Retrieves all customers.

### 5. Get Active Customers
**GET** `/api/customers/active`

Retrieves only active customers.

### 6. Search Customers
**GET** `/api/customers/search?name={searchTerm}`

Searches customers by name (partial match).

### 7. Get Customers by City
**GET** `/api/customers/city/{city}`

Retrieves all customers in a specific city.

### 8. Get Top Rated Customers
**GET** `/api/customers/top-rated?minRating={rating}`

Retrieves customers with rating >= minRating (default: 4.0).

### 9. Get Customers Over Credit Limit
**GET** `/api/customers/over-credit-limit`

Retrieves customers whose outstanding balance exceeds their credit limit.

### 10. Update Customer
**PUT** `/api/customers/{id}`

Updates customer information.

### 11. Delete Customer
**DELETE** `/api/customers/{id}`

Deletes a customer.

---

## Customer Balance & Delivery Tracking

### 1. Update Outstanding Balance
**POST** `/api/customers/{id}/balance?amount={amount}`

Adds the specified amount to customer's outstanding balance (can be negative to reduce balance).

### 2. Record Delivery
**POST** `/api/customers/{id}/delivery?success={true|false}`

Records a delivery for the customer and updates statistics.

---

## Customer Feedback APIs

### 1. Submit Feedback
**POST** `/api/customers/feedback`

Submits feedback for a customer.

**Request Body:**
```json
{
  "customerId": 123,
  "waypointId": 456,
  "routePlanId": 789,
  "rating": 5,
  "feedbackText": "Excellent service and timely delivery",
  "feedbackCategory": "DELIVERY_QUALITY"
}
```

**Feedback Categories:**
- `DELIVERY_QUALITY`
- `DRIVER_BEHAVIOR`
- `TIMELINESS`
- `COMMUNICATION`

**Response:** `201 Created`

### 2. Get Customer Feedback
**GET** `/api/customers/{id}/feedback`

Retrieves all feedback for a specific customer.

### 3. Get Unaddressed Feedback
**GET** `/api/customers/feedback/unaddressed`

Retrieves all feedback that hasn't been addressed yet.

### 4. Get Negative Feedback
**GET** `/api/customers/feedback/negative`

Retrieves all feedback with rating <= 2.

### 5. Respond to Feedback
**POST** `/api/customers/feedback/{id}/respond`

Responds to customer feedback.

**Query Parameters:**
- `responseText`: Response message
- `respondedBy`: Name/ID of responder

**Example:**
```
POST /api/customers/feedback/123/respond?responseText=Thank%20you%20for%20feedback&respondedBy=Manager
```

---

## Data Models

### RoutePlan
```json
{
  "id": 1,
  "routeName": "string",
  "routeDescription": "string",
  "vehicleId": 123,
  "driverId": 456,
  "originLat": 28.6139,
  "originLng": 77.2090,
  "originAddress": "string",
  "destinationLat": 19.0760,
  "destinationLng": 72.8777,
  "destinationAddress": "string",
  "totalDistance": 1400.5,
  "estimatedDuration": 720,
  "estimatedFuelConsumption": 150.0,
  "estimatedCost": 3000.00,
  "plannedStartTime": "2025-11-15T08:00:00",
  "plannedEndTime": "2025-11-15T20:00:00",
  "optimizationCriteria": "DISTANCE",
  "trafficConsidered": true,
  "tollRoadsAllowed": true,
  "status": "PLANNED",
  "actualDistance": 1425.5,
  "actualDuration": 735,
  "actualFuelConsumption": 155.0,
  "actualCost": 3100.00,
  "createdAt": "2025-11-11T10:00:00",
  "updatedAt": "2025-11-15T20:30:00"
}
```

### RouteWaypoint
```json
{
  "id": 1,
  "sequenceNumber": 1,
  "waypointName": "Customer Location 1",
  "latitude": 28.5355,
  "longitude": 77.3910,
  "address": "string",
  "arrivalWindowStart": "2025-11-15T10:00:00",
  "arrivalWindowEnd": "2025-11-15T12:00:00",
  "plannedArrivalTime": "2025-11-15T11:00:00",
  "actualArrivalTime": "2025-11-15T11:15:00",
  "stopDuration": 15,
  "serviceType": "DELIVERY",
  "customerName": "John Doe",
  "customerPhone": "+91-9876543210",
  "customerEmail": "john@example.com",
  "itemsDescription": "Package #12345",
  "weight": 25.5,
  "volume": 0.5,
  "podSignaturePath": "/uploads/signatures/sig_123.png",
  "podPhotoPath": "/uploads/photos/photo_123.jpg",
  "podNotes": "string",
  "podTimestamp": "2025-11-15T11:30:00",
  "podCapturedBy": "John Driver",
  "status": "COMPLETED",
  "completionNotes": "string"
}
```

### Customer
```json
{
  "id": 1,
  "customerCode": "CUST001",
  "customerName": "ABC Corporation",
  "customerType": "BUSINESS",
  "primaryContactName": "Jane Smith",
  "primaryPhone": "+91-9876543210",
  "secondaryPhone": "+91-9876543211",
  "email": "contact@abccorp.com",
  "addressLine1": "string",
  "addressLine2": "string",
  "city": "Noida",
  "state": "Uttar Pradesh",
  "postalCode": "201301",
  "country": "India",
  "latitude": 28.5706,
  "longitude": 77.3272,
  "gstin": "07AAAAA0000A1Z5",
  "pan": "AAAAA0000A",
  "preferredDeliveryTime": "9 AM - 6 PM",
  "specialInstructions": "string",
  "isActive": true,
  "creditLimit": 100000.00,
  "outstandingBalance": 5000.00,
  "serviceRating": 4.5,
  "totalDeliveries": 100,
  "successfulDeliveries": 95,
  "failedDeliveries": 5,
  "createdAt": "2025-01-01T00:00:00",
  "updatedAt": "2025-11-11T10:00:00"
}
```

### CustomerFeedback
```json
{
  "id": 1,
  "customerId": 123,
  "waypointId": 456,
  "routePlanId": 789,
  "rating": 5,
  "feedbackText": "string",
  "feedbackCategory": "DELIVERY_QUALITY",
  "isAddressed": false,
  "responseText": "string",
  "respondedBy": "Manager",
  "respondedAt": "2025-11-12T10:00:00",
  "createdAt": "2025-11-11T15:00:00"
}
```

---

## Error Handling

All endpoints follow standard HTTP status codes:

- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `204 No Content` - Deletion successful
- `400 Bad Request` - Invalid request data
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

Error Response Format:
```json
{
  "timestamp": "2025-11-11T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation error message",
  "path": "/api/routes"
}
```

---

## Swagger UI

Interactive API documentation is available at:
- **Fleet Service**: http://localhost:8082/swagger-ui.html
- **Via Gateway**: http://localhost:8080/fleet-service/swagger-ui.html

---

**Version:** 2.1.0
**Last Updated:** November 11, 2025
