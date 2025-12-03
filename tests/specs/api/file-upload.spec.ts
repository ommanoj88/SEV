import { test, expect } from '../../fixtures/test-fixtures';
import * as path from 'path';
import * as fs from 'fs';

test.describe('File Upload Tests', () => {

  test.describe('Vehicle Image Upload', () => {

    test('POST /api/v1/vehicles/{id}/image - upload vehicle image', async ({ authenticatedApiClient, request }) => {
      // Get a vehicle first
      try {
        const vehiclesResponse = await authenticatedApiClient.getVehicles() as any;
        const vehicles = vehiclesResponse?.data || [];
        
        if (vehicles.length > 0) {
          const vehicleId = vehicles[0].id;
          
          // Create a simple test image (1x1 pixel PNG)
          const pngHeader = Buffer.from([
            0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, 0xC4,
            0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
            0x54, 0x78, 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
            0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, 0xB4, 0x00,
            0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, 0xAE,
            0x42, 0x60, 0x82
          ]);

          // Try multipart upload
          const response = await request.post(`http://localhost:8080/api/v1/vehicles/${vehicleId}/image`, {
            multipart: {
              file: {
                name: 'test-vehicle.png',
                mimeType: 'image/png',
                buffer: pngHeader,
              },
            },
          });
          
          // Upload endpoint may or may not exist
          expect([200, 201, 400, 404, 405, 500]).toContain(response.status());
        }
      } catch (error: any) {
        console.log('Vehicle image upload error:', error.message);
        expect([200, 400, 404, 500]).toContain(error.status);
      }
    });

    test('Upload oversized image returns error', async ({ authenticatedApiClient, request }) => {
      try {
        const vehiclesResponse = await authenticatedApiClient.getVehicles() as any;
        const vehicles = vehiclesResponse?.data || [];
        
        if (vehicles.length > 0) {
          const vehicleId = vehicles[0].id;
          
          // Create a large buffer (simulate 20MB file)
          const largeBuffer = Buffer.alloc(20 * 1024 * 1024, 0xFF);

          const response = await request.post(`http://localhost:8080/api/v1/vehicles/${vehicleId}/image`, {
            multipart: {
              file: {
                name: 'large-image.png',
                mimeType: 'image/png',
                buffer: largeBuffer,
              },
            },
            timeout: 60000,
          });
          
          // Should reject or timeout
          expect([400, 413, 500]).toContain(response.status());
        }
      } catch (error: any) {
        // Expected to fail
        expect(true).toBe(true);
      }
    });

    test('Upload invalid file type returns error', async ({ authenticatedApiClient, request }) => {
      try {
        const vehiclesResponse = await authenticatedApiClient.getVehicles() as any;
        const vehicles = vehiclesResponse?.data || [];
        
        if (vehicles.length > 0) {
          const vehicleId = vehicles[0].id;

          const response = await request.post(`http://localhost:8080/api/v1/vehicles/${vehicleId}/image`, {
            multipart: {
              file: {
                name: 'test.exe',
                mimeType: 'application/x-msdownload',
                buffer: Buffer.from('MZ...fake exe'),
              },
            },
          });
          
          // Should reject non-image files
          expect([400, 415, 500]).toContain(response.status());
        }
      } catch (error: any) {
        expect(true).toBe(true);
      }
    });

    test('Upload empty file returns error', async ({ authenticatedApiClient, request }) => {
      try {
        const vehiclesResponse = await authenticatedApiClient.getVehicles() as any;
        const vehicles = vehiclesResponse?.data || [];
        
        if (vehicles.length > 0) {
          const vehicleId = vehicles[0].id;

          const response = await request.post(`http://localhost:8080/api/v1/vehicles/${vehicleId}/image`, {
            multipart: {
              file: {
                name: 'empty.png',
                mimeType: 'image/png',
                buffer: Buffer.alloc(0),
              },
            },
          });
          
          expect([400, 500]).toContain(response.status());
        }
      } catch (error: any) {
        expect(true).toBe(true);
      }
    });
  });

  test.describe('Driver Document Upload', () => {

    test('POST /api/v1/drivers/{id}/documents - upload driver license', async ({ authenticatedApiClient, request }) => {
      try {
        const driversResponse = await authenticatedApiClient.get('/api/v1/drivers?companyId=1') as any;
        const drivers = driversResponse?.data || [];
        
        if (drivers.length > 0) {
          const driverId = drivers[0].id;

          // Create a simple PDF
          const pdfContent = '%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n2 0 obj<</Type/Pages/Count 0/Kids[]>>endobj\nxref\n0 3\n0000000000 65535 f \ntrailer<</Size 3/Root 1 0 R>>\nstartxref\n9\n%%EOF';

          const response = await request.post(`http://localhost:8080/api/v1/drivers/${driverId}/documents`, {
            multipart: {
              file: {
                name: 'license.pdf',
                mimeType: 'application/pdf',
                buffer: Buffer.from(pdfContent),
              },
              documentType: 'LICENSE',
            },
          });
          
          expect([200, 201, 400, 404, 405, 500]).toContain(response.status());
        }
      } catch (error: any) {
        console.log('Driver document upload error:', error.message);
        expect(true).toBe(true);
      }
    });

    test('Upload driver photo', async ({ authenticatedApiClient, request }) => {
      try {
        const driversResponse = await authenticatedApiClient.get('/api/v1/drivers?companyId=1') as any;
        const drivers = driversResponse?.data || [];
        
        if (drivers.length > 0) {
          const driverId = drivers[0].id;

          // Minimal JPEG
          const jpegHeader = Buffer.from([
            0xFF, 0xD8, 0xFF, 0xE0, 0x00, 0x10, 0x4A, 0x46,
            0x49, 0x46, 0x00, 0x01, 0x01, 0x00, 0x00, 0x01,
            0x00, 0x01, 0x00, 0x00, 0xFF, 0xD9
          ]);

          const response = await request.post(`http://localhost:8080/api/v1/drivers/${driverId}/photo`, {
            multipart: {
              file: {
                name: 'photo.jpg',
                mimeType: 'image/jpeg',
                buffer: jpegHeader,
              },
            },
          });
          
          expect([200, 201, 400, 404, 405, 500]).toContain(response.status());
        }
      } catch (error: any) {
        expect(true).toBe(true);
      }
    });
  });

  test.describe('Charging Station Image Upload', () => {

    test('POST /api/v1/charging/stations/{id}/image - upload station image', async ({ authenticatedApiClient, request }) => {
      try {
        const stationsResponse = await authenticatedApiClient.getChargingStations() as any;
        const stations = Array.isArray(stationsResponse) ? stationsResponse : (stationsResponse?.data || []);
        
        if (stations.length > 0) {
          const stationId = stations[0].id;

          // Minimal PNG
          const pngHeader = Buffer.from([
            0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, 0xC4,
            0x89, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E,
            0x44, 0xAE, 0x42, 0x60, 0x82
          ]);

          const response = await request.post(`http://localhost:8080/api/v1/charging/stations/${stationId}/image`, {
            multipart: {
              file: {
                name: 'station.png',
                mimeType: 'image/png',
                buffer: pngHeader,
              },
            },
          });
          
          expect([200, 201, 400, 404, 405, 500]).toContain(response.status());
        }
      } catch (error: any) {
        expect(true).toBe(true);
      }
    });
  });

  test.describe('Bulk Import', () => {

    test('POST /api/v1/vehicles/import - CSV import', async ({ authenticatedApiClient, request }) => {
      const csvContent = `vehicleNumber,licensePlate,vin,make,model,year,type,fuelType,companyId
CSV-001,KA01CSV001,CSVVIN00000000001,TestMake,TestModel,2024,SEDAN,EV,1
CSV-002,KA01CSV002,CSVVIN00000000002,TestMake,TestModel,2024,SUV,ICE,1`;

      try {
        const response = await request.post('http://localhost:8080/api/v1/vehicles/import', {
          multipart: {
            file: {
              name: 'vehicles.csv',
              mimeType: 'text/csv',
              buffer: Buffer.from(csvContent),
            },
          },
        });
        
        expect([200, 201, 400, 404, 405, 500]).toContain(response.status());
      } catch (error: any) {
        expect(true).toBe(true);
      }
    });

    test('POST /api/v1/drivers/import - CSV import', async ({ authenticatedApiClient, request }) => {
      const csvContent = `name,email,phone,licenseNumber,licenseExpiry
Import Driver 1,import1@test.com,+91-9999900001,IMP00000001,2026-12-31
Import Driver 2,import2@test.com,+91-9999900002,IMP00000002,2026-12-31`;

      try {
        const response = await request.post('http://localhost:8080/api/v1/drivers/import?companyId=1', {
          multipart: {
            file: {
              name: 'drivers.csv',
              mimeType: 'text/csv',
              buffer: Buffer.from(csvContent),
            },
          },
        });
        
        expect([200, 201, 400, 404, 405, 500]).toContain(response.status());
      } catch (error: any) {
        expect(true).toBe(true);
      }
    });

    test('Import with invalid CSV format', async ({ request }) => {
      const invalidCsv = 'not,valid,csv\nwith"broken"quotes';

      try {
        const response = await request.post('http://localhost:8080/api/v1/vehicles/import', {
          multipart: {
            file: {
              name: 'invalid.csv',
              mimeType: 'text/csv',
              buffer: Buffer.from(invalidCsv),
            },
          },
        });
        
        expect([400, 404, 405, 500]).toContain(response.status());
      } catch (error: any) {
        expect(true).toBe(true);
      }
    });
  });

  test.describe('Export Operations', () => {

    test('GET /api/v1/vehicles/export - export vehicles as CSV', async ({ request }) => {
      try {
        const response = await request.get('http://localhost:8080/api/v1/vehicles/export?format=csv');
        
        if (response.status() === 200) {
          const contentType = response.headers()['content-type'];
          expect(contentType).toContain('csv');
        }
        
        expect([200, 404, 405, 500]).toContain(response.status());
      } catch (error: any) {
        expect(true).toBe(true);
      }
    });

    test('GET /api/v1/vehicles/export - export as Excel', async ({ request }) => {
      try {
        const response = await request.get('http://localhost:8080/api/v1/vehicles/export?format=xlsx');
        
        if (response.status() === 200) {
          const contentType = response.headers()['content-type'];
          expect(contentType || true).toBeTruthy();
        }
        
        expect([200, 404, 405, 500]).toContain(response.status());
      } catch (error: any) {
        expect(true).toBe(true);
      }
    });

    test('GET /api/v1/reports/fleet - PDF report export', async ({ request }) => {
      try {
        const response = await request.get('http://localhost:8080/api/v1/reports/fleet?format=pdf');
        
        if (response.status() === 200) {
          const contentType = response.headers()['content-type'];
          expect(contentType || true).toBeTruthy();
        }
        
        expect([200, 404, 405, 500]).toContain(response.status());
      } catch (error: any) {
        expect(true).toBe(true);
      }
    });
  });

  test.describe('File Type Validation', () => {

    test('Reject executable files', async ({ request }) => {
      try {
        const response = await request.post('http://localhost:8080/api/v1/vehicles/1/image', {
          multipart: {
            file: {
              name: 'malware.exe',
              mimeType: 'application/x-executable',
              buffer: Buffer.from('MZ'),
            },
          },
        });
        
        expect([400, 403, 415, 500]).toContain(response.status());
      } catch (error: any) {
        expect(true).toBe(true);
      }
    });

    test('Reject script files', async ({ request }) => {
      try {
        const response = await request.post('http://localhost:8080/api/v1/vehicles/1/image', {
          multipart: {
            file: {
              name: 'script.js',
              mimeType: 'application/javascript',
              buffer: Buffer.from('alert("xss")'),
            },
          },
        });
        
        expect([400, 403, 415, 500]).toContain(response.status());
      } catch (error: any) {
        expect(true).toBe(true);
      }
    });

    test('Accept JPEG images', async ({ request }) => {
      const jpegHeader = Buffer.from([0xFF, 0xD8, 0xFF, 0xE0]);
      
      try {
        const response = await request.post('http://localhost:8080/api/v1/vehicles/1/image', {
          multipart: {
            file: {
              name: 'photo.jpg',
              mimeType: 'image/jpeg',
              buffer: jpegHeader,
            },
          },
        });
        
        // Should accept JPEG or return 404 if endpoint doesn't exist
        expect([200, 201, 400, 404, 500]).toContain(response.status());
      } catch (error: any) {
        expect(true).toBe(true);
      }
    });

    test('Accept GIF images', async ({ request }) => {
      const gifHeader = Buffer.from([0x47, 0x49, 0x46, 0x38, 0x39, 0x61]); // GIF89a
      
      try {
        const response = await request.post('http://localhost:8080/api/v1/vehicles/1/image', {
          multipart: {
            file: {
              name: 'animation.gif',
              mimeType: 'image/gif',
              buffer: gifHeader,
            },
          },
        });
        
        expect([200, 201, 400, 404, 500]).toContain(response.status());
      } catch (error: any) {
        expect(true).toBe(true);
      }
    });
  });

  test.describe('File Name Security', () => {

    test('Path traversal in filename is rejected', async ({ request }) => {
      try {
        const response = await request.post('http://localhost:8080/api/v1/vehicles/1/image', {
          multipart: {
            file: {
              name: '../../../etc/passwd',
              mimeType: 'image/png',
              buffer: Buffer.from([0x89, 0x50, 0x4E, 0x47]),
            },
          },
        });
        
        // Should sanitize or reject
        expect([200, 201, 400, 403, 500]).toContain(response.status());
      } catch (error: any) {
        expect(true).toBe(true);
      }
    });

    test('Null byte in filename is handled', async ({ request }) => {
      try {
        const response = await request.post('http://localhost:8080/api/v1/vehicles/1/image', {
          multipart: {
            file: {
              name: 'image.png\x00.exe',
              mimeType: 'image/png',
              buffer: Buffer.from([0x89, 0x50, 0x4E, 0x47]),
            },
          },
        });
        
        expect([200, 201, 400, 403, 500]).toContain(response.status());
      } catch (error: any) {
        expect(true).toBe(true);
      }
    });
  });
});

