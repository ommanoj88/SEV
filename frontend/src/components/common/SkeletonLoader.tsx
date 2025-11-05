import React from 'react';
import { Box, Skeleton, Card, CardContent, Grid } from '@mui/material';

/**
 * SkeletonLoader Component
 * 
 * Provides loading state placeholders with shimmer animation.
 * 
 * @interface SkeletonLoaderProps
 * @property {string} [variant='card'] - Type of skeleton to display:
 *   - 'card': Card-style skeleton with image and text placeholders
 *   - 'table': Table-style skeleton with rows
 *   - 'list': List-style skeleton with avatar and text
 *   - 'dashboard': Dashboard grid of card skeletons
 * @property {number} [count=3] - Number of skeleton items to render
 */
interface SkeletonLoaderProps {
  variant?: 'card' | 'table' | 'list' | 'dashboard';
  count?: number;
}

const SkeletonLoader: React.FC<SkeletonLoaderProps> = ({ variant = 'card', count = 3 }) => {
  const renderCardSkeleton = () => (
    <Card>
      <CardContent>
        <Skeleton variant="text" width="60%" height={32} sx={{ mb: 2 }} />
        <Skeleton variant="rectangular" height={100} sx={{ borderRadius: 2, mb: 2 }} />
        <Box display="flex" gap={1}>
          <Skeleton variant="circular" width={40} height={40} />
          <Box flex={1}>
            <Skeleton variant="text" width="80%" />
            <Skeleton variant="text" width="60%" />
          </Box>
        </Box>
      </CardContent>
    </Card>
  );

  const renderTableSkeleton = () => (
    <Box>
      <Box display="flex" gap={2} mb={2}>
        <Skeleton variant="rectangular" width={200} height={40} sx={{ borderRadius: 2 }} />
        <Skeleton variant="rectangular" width={150} height={40} sx={{ borderRadius: 2 }} />
      </Box>
      {Array.from({ length: count }).map((_, index) => (
        <Box key={index} display="flex" gap={2} mb={1}>
          <Skeleton variant="rectangular" width="100%" height={60} sx={{ borderRadius: 2 }} />
        </Box>
      ))}
    </Box>
  );

  const renderListSkeleton = () => (
    <Box>
      {Array.from({ length: count }).map((_, index) => (
        <Box key={index} display="flex" gap={2} alignItems="center" mb={2}>
          <Skeleton variant="circular" width={48} height={48} />
          <Box flex={1}>
            <Skeleton variant="text" width="70%" height={24} />
            <Skeleton variant="text" width="40%" height={20} />
          </Box>
          <Skeleton variant="rectangular" width={80} height={32} sx={{ borderRadius: 2 }} />
        </Box>
      ))}
    </Box>
  );

  const renderDashboardSkeleton = () => (
    <Grid container spacing={3}>
      <Grid item xs={12}>
        <Skeleton variant="text" width={200} height={40} sx={{ mb: 2 }} />
      </Grid>
      {Array.from({ length: count }).map((_, index) => (
        <Grid item xs={12} sm={6} md={4} key={index}>
          {renderCardSkeleton()}
        </Grid>
      ))}
    </Grid>
  );

  switch (variant) {
    case 'table':
      return renderTableSkeleton();
    case 'list':
      return renderListSkeleton();
    case 'dashboard':
      return renderDashboardSkeleton();
    case 'card':
    default:
      return (
        <Grid container spacing={3}>
          {Array.from({ length: count }).map((_, index) => (
            <Grid item xs={12} sm={6} md={4} key={index}>
              {renderCardSkeleton()}
            </Grid>
          ))}
        </Grid>
      );
  }
};

export default SkeletonLoader;
