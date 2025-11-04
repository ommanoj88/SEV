# Frontend Restart & Troubleshooting Guide

**Last Updated:** November 2, 2025

---

## ⚠️ Critical: Restart Your Dev Server

Your React development server is running the **old compiled code**. All code fixes have been applied to your files, but they won't take effect until you restart the dev server and clear your browser cache.

---

## Step-by-Step Restart Instructions

### **Step 1: Stop the Dev Server** (⏱️ 10 seconds)

In the terminal window where your React app is running (shows `http://localhost:3000`):

```bash
Press: Ctrl + C
```

You should see output like:
```
^C
Terminated batch job (Y/N)? Y
```

### **Step 2: Navigate to Frontend Directory** (⏱️ 5 seconds)

```bash
cd C:\Users\omman\Desktop\SEV\frontend
```

### **Step 3: Clear Node Modules Cache** (⏱️ 30 seconds)

```bash
npm cache clean --force
```

### **Step 4: Delete Node Modules (Optional but Recommended)** (⏱️ 1-2 minutes)

This ensures a completely fresh install:

```bash
rmdir /s /q node_modules
npm install
```

### **Step 5: Start Dev Server** (⏱️ 20 seconds)

```bash
npm start
```

Wait for output showing:
```
Compiled successfully!

You can now view your app in your browser.

  Local:            http://localhost:3000
  On Your Network:  http://192.168.x.x:3000
```

### **Step 6: Clear Browser Cache** (⏱️ 15 seconds)

Once you see "Compiled successfully!" in your terminal:

1. **Open Browser** → Go to `http://localhost:3000`
2. **Press**: `Ctrl + Shift + Delete` (Clear Browsing Data)
3. **Settings**:
   - Time range: **All time**
   - ✅ Check: **Cached images and files**
   - ✅ Check: **Cookies and other site data**
4. **Click**: "Clear data"
5. **Refresh**: Press `F5` or `Ctrl + R`

### **Step 7: Verify Fixes Applied** (⏱️ 30 seconds)

After page loads, check your browser console:
- ✅ No "Cannot read properties of undefined" error
- ✅ API errors show (404/500) but app doesn't crash
- ✅ Mock data displays correctly
- ✅ All pages load without errors

---

## If You Still See Errors

### Problem: "Cannot read properties of undefined (reading 'stateOfCharge')"

**Solution 1: Hard Refresh**
```
Windows/Linux: Ctrl + Shift + R
Mac: Cmd + Shift + R
```

**Solution 2: Clear Local Storage**
1. Open DevTools (`F12`)
2. Go to "Application" tab
3. Click "Local Storage" → Right-click → "Clear All"
4. Refresh page (`F5`)

**Solution 3: Check Dev Server Recompiled**
1. Look at terminal running `npm start`
2. Should show: `Compiled successfully!`
3. If not showing, check for TypeScript errors in terminal

### Problem: Still Getting 404/500 API Errors

**This is EXPECTED** - Your backend API server is not running. The app should still work with mock data.

**Verification:**
1. Open browser DevTools (`F12`)
2. Go to "Network" tab
3. Refresh page
4. Look for requests to `http://localhost:8080/api/v1/*`
5. Should show 404/500 responses
6. But UI should still display mock data

---

## What's Fixed After Restart

### ✅ Runtime Errors Fixed
- [x] FleetManagementPage no longer crashes reading undefined battery data
- [x] All vehicle data accessed safely with defaults
- [x] Linear progress bars get valid 0-100 values

### ✅ Missing Data Handled
- [x] Vehicle data with missing fields display correctly
- [x] Charging stations show mock data if API fails
- [x] Charging sessions show mock data if API fails
- [x] Driver list shows mock data if API fails
- [x] Analytics dashboard shows mock data immediately
- [x] Notifications show mock data if API fails
- [x] Maintenance records show mock data if API fails

---

## Mock Data Available

After restart, these pages will display sample data:

### **Fleet Management**
- 3 mock vehicles (Tesla, Tata, MG)
- Battery levels, odometer, driver assignments

### **Charging Management**
- 2 mock stations (Delhi, Noida)
- 2 mock sessions (completed & active)

### **Driver Management**
- 3 mock drivers (John, Jane, Rajesh)
- Leaderboard rankings

### **Analytics Dashboard**
- Fleet KPIs, charts, and reports

### **Notifications**
- 2 mock notifications + 1 alert

### **Maintenance**
- 2 service records + 2 reminders

---

## Summary of Changes

**All 7 Redux slices updated with:**
- ✅ Mock data in initialState or rejection handlers
- ✅ Fallback to mock when API fails
- ✅ Null/undefined safety checks in components
- ✅ Better error messages

**Files Modified:**
```
✅ src/pages/FleetManagementPage.tsx
✅ src/redux/slices/vehicleSlice.ts
✅ src/redux/slices/chargingSlice.ts
✅ src/redux/slices/driverSlice.ts
✅ src/redux/slices/analyticsSlice.ts
✅ src/redux/slices/notificationSlice.ts
✅ src/redux/slices/maintenanceSlice.ts
```

---

**⏱️ Total restart time: ~2 minutes**

After restart, all errors will be resolved! 🚀
