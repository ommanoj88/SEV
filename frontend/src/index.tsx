import React from 'react';
import ReactDOM from 'react-dom/client';
import { Provider } from 'react-redux';
import { store } from './redux/store';
import App from './App';
import reportWebVitals from './reportWebVitals';

// Suppress COOP warnings from Firebase popup authentication
// These are expected when using Firebase Google sign-in and don't affect functionality
// Note: This is a targeted suppression for known Firebase SDK warnings.
// The pattern matching is specific to avoid hiding legitimate errors.
// Trade-off: Better console cleanliness vs. slight risk of missing similar errors.
// Alternative considered: Logging library with filtering, deemed overkill for this single case.
if (process.env.NODE_ENV === 'production') {
  const originalError = console.error;
  console.error = (...args: any[]) => {
    // Only suppress if it matches the exact Firebase COOP warning pattern
    const message = String(args[0]);
    const isFirebaseCOOPWarning = 
      message.includes('Cross-Origin-Opener-Policy policy would block the window.closed call') ||
      (message.includes('popup.ts') && message.includes('Cross-Origin-Opener-Policy'));
    
    if (isFirebaseCOOPWarning) {
      return; // Suppress Firebase COOP warnings only
    }
    originalError.apply(console, args);
  };
}

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);

root.render(
  <React.StrictMode>
    <Provider store={store}>
      <App />
    </Provider>
  </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
