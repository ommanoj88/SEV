# UI/UX Design System Documentation

## Overview
This document describes the modern UI/UX design system implemented for the EV Fleet Management Platform, incorporating the latest design trends and best practices from 2025.

## Design Philosophy

### Core Principles
1. **Modern & Clean**: Minimalist design with purposeful whitespace
2. **Accessible**: WCAG 2.1 AA compliant with proper contrast and keyboard navigation
3. **Responsive**: Mobile-first approach with fluid layouts
4. **Performant**: Optimized animations and lazy loading
5. **Consistent**: Unified design language across all components

## Color Palette

### Light Mode
- **Primary**: #3B82F6 (Modern Blue)
- **Secondary**: #10B981 (EV Green)
- **Accent**: #8B5CF6 (Purple)
- **Error**: #EF4444
- **Warning**: #F59E0B
- **Info**: #06B6D4
- **Success**: #10B981

### Dark Mode
- **Primary**: #60A5FA
- **Secondary**: #34D399
- **Accent**: #A78BFA
- **Background**: #0F172A (Slate)

## Typography

### Font Family
- **Primary**: Inter, SF Pro Display, Segoe UI, Roboto
- **Monospace**: JetBrains Mono, Fira Code

### Scale
- **H1**: 3rem (48px) - 800 weight
- **H2**: 2.5rem (40px) - 700 weight
- **H3**: 2rem (32px) - 700 weight
- **H4**: 1.5rem (24px) - 600 weight
- **H5**: 1.25rem (20px) - 600 weight
- **H6**: 1.125rem (18px) - 600 weight
- **Body1**: 1rem (16px) - 400 weight
- **Body2**: 0.875rem (14px) - 400 weight
- **Caption**: 0.75rem (12px) - 400 weight

## Spacing System

Based on 8pt grid:
- **XS**: 4px
- **S**: 8px
- **M**: 16px
- **L**: 24px
- **XL**: 32px
- **XXL**: 48px

## Components

### Cards
- **Border Radius**: 16px
- **Shadow**: 0px 4px 20px rgba(0, 0, 0, 0.04)
- **Hover Effect**: translateY(-4px) with enhanced shadow
- **Glassmorphism**: backdrop-filter: blur(10px)

### Buttons
- **Border Radius**: 10px
- **Padding**: 10px 20px
- **Font Weight**: 600
- **Transitions**: all 0.3s cubic-bezier(0.4, 0, 0.2, 1)
- **Hover**: translateY(-2px) + scale effect

### Inputs
- **Border Radius**: 10px
- **Border Width**: 1px (2px on focus/hover)
- **Transition**: Smooth border and background transitions

## Animations & Transitions

### Fade In
```css
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
```

### Pulse (for alerts)
```css
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}
```

### Shimmer (for skeleton loaders)
```css
@keyframes shimmer {
  0% { background-position: -1000px 0; }
  100% { background-position: 1000px 0; }
}
```

## Special Effects

### Glassmorphism
- **Background**: rgba with 70-80% opacity
- **Backdrop Filter**: blur(10px) saturate(180%)
- **Border**: 1px solid with 30% white opacity

### Gradient Text
- **Background**: linear-gradient(135deg, primary, secondary)
- **-webkit-background-clip**: text
- **-webkit-text-fill-color**: transparent

### Gradient Buttons
- **Background**: linear-gradient(135deg, primary, secondary)
- **Hover**: Reverse gradient direction
- **Shadow**: Dynamic shadow with primary color

## Accessibility Features

### Keyboard Navigation
- All interactive elements accessible via Tab
- Clear focus indicators (3px outline)
- Skip navigation links

### Screen Readers
- Proper ARIA labels
- Semantic HTML structure
- Alt text for images

### Reduced Motion
- Respects `prefers-reduced-motion` media query
- Disables animations for users who prefer reduced motion

### Contrast
- Minimum 4.5:1 for normal text
- Minimum 3:1 for large text
- High contrast mode support

## Responsive Breakpoints

- **XS**: 0px - 599px (Mobile)
- **SM**: 600px - 899px (Tablet)
- **MD**: 900px - 1199px (Small Desktop)
- **LG**: 1200px - 1535px (Desktop)
- **XL**: 1536px+ (Large Desktop)

## Custom Components

### SkeletonLoader
Provides loading states with shimmer animation:
- Variants: card, table, list, dashboard
- Configurable count

### PageTransition
Smooth page transitions:
- Animations: fadeIn, slideLeft, slideRight, scaleIn
- Configurable duration and delay

### FloatingActionButton
Modern FAB with gradient:
- Single action or SpeedDial variants
- Configurable position
- Animated hover effects

### SearchBar
Advanced search with filters:
- Auto-complete support
- Filter chips
- Clear functionality

### ModernBreadcrumbs
Navigation breadcrumbs:
- Auto-generated from path
- Icon support
- Glassmorphic background

### StatCard
Enhanced statistics display:
- Trend indicators
- Loading states
- Gradient backgrounds
- Click handlers

## Theme Customization

### Dark Mode Toggle
Users can switch between light and dark modes:
- Preference saved to localStorage
- System preference detection
- Smooth transitions between modes

### Custom Scrollbar
Styled scrollbars for better UX:
- Gradient thumb
- Smooth hover effects
- Consistent across browsers

## Performance Optimizations

1. **Code Splitting**: Lazy loading of routes
2. **Image Optimization**: WebP format with fallbacks
3. **Bundle Size**: Gzipped to ~399KB
4. **Animations**: GPU-accelerated transforms
5. **Debounced Search**: 300ms delay

## Best Practices

### Do's
- Use consistent spacing (8pt grid)
- Implement proper loading states
- Provide clear feedback for user actions
- Use semantic HTML
- Test on multiple devices
- Ensure keyboard accessibility

### Don'ts
- Don't use more than 3 colors in a single component
- Avoid animation durations > 500ms
- Don't skip loading states
- Avoid fixed pixel widths
- Don't use color alone to convey information

## Browser Support

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## Future Enhancements

1. **Command Palette**: Keyboard-driven navigation (Ctrl+K)
2. **Advanced Filters**: Saved filter presets
3. **Customizable Dashboard**: Drag-and-drop widgets
4. **Data Export**: Multiple format support
5. **Real-time Updates**: WebSocket integration
6. **Offline Mode**: Service worker implementation

## Resources

- [Material Design 3](https://m3.material.io/)
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [Tailwind CSS Color System](https://tailwindcss.com/docs/customizing-colors)
- [Inter Font Family](https://rsms.me/inter/)

---

**Version**: 2.0.0
**Last Updated**: January 2025
**Maintained by**: EV Fleet Management Platform Team
