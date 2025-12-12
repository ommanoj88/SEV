import React from 'react';
import {
  Box,
  Typography,
  Chip,
  Divider,
  List,
  ListItem,
  ListItemText,
  Paper,
  IconButton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Accordion,
  AccordionSummary,
  AccordionDetails,
} from '@mui/material';
import {
  Close as CloseIcon,
  ExpandMore as ExpandMoreIcon,
  Storage as DatabaseIcon,
  Api as ApiIcon,
  Event as EventIcon,
  Memory as TechIcon,
  Key as KeyIcon,
} from '@mui/icons-material';
import { Node } from 'reactflow';
import { NodeDataType, nodeColors } from './flowUtils';

interface NodeDetailsPanelProps {
  node: Node<NodeDataType> | null;
  onClose: () => void;
}

const NodeDetailsPanel: React.FC<NodeDetailsPanelProps> = ({ node, onClose }) => {
  if (!node) return null;

  const data = node.data;
  const color = nodeColors[data.type] || '#666';

  return (
    <Paper
      elevation={8}
      sx={{
        position: 'absolute',
        right: 16,
        top: 16,
        bottom: 16,
        width: 380,
        overflow: 'auto',
        zIndex: 1000,
        borderRadius: 2,
        bgcolor: 'background.paper',
      }}
    >
      {/* Header */}
      <Box
        sx={{
          p: 2,
          background: `linear-gradient(135deg, ${color} 0%, ${color}dd 100%)`,
          color: 'white',
          position: 'sticky',
          top: 0,
          zIndex: 1,
        }}
      >
        <Box display="flex" justifyContent="space-between" alignItems="flex-start">
          <Box>
            <Typography variant="h6" fontWeight="bold">
              {data.icon} {data.label}
            </Typography>
            {data.port && (
              <Chip
                label={`Port: ${data.port}`}
                size="small"
                sx={{ mt: 1, bgcolor: 'rgba(255,255,255,0.2)', color: 'white' }}
              />
            )}
          </Box>
          <IconButton onClick={onClose} sx={{ color: 'white' }}>
            <CloseIcon />
          </IconButton>
        </Box>
      </Box>

      <Box p={2}>
        {/* Description */}
        {data.description && (
          <Box mb={2}>
            <Typography variant="body2" color="text.secondary">
              {data.description}
            </Typography>
          </Box>
        )}

        {/* Technologies */}
        {data.technologies && data.technologies.length > 0 && (
          <Box mb={2}>
            <Box display="flex" alignItems="center" mb={1}>
              <TechIcon fontSize="small" sx={{ mr: 1, color: 'primary.main' }} />
              <Typography variant="subtitle2" fontWeight="bold">
                Technologies
              </Typography>
            </Box>
            <Box display="flex" flexWrap="wrap" gap={0.5}>
              {data.technologies.map((tech, idx) => (
                <Chip
                  key={idx}
                  label={tech}
                  size="small"
                  variant="outlined"
                  sx={{ fontSize: '0.75rem' }}
                />
              ))}
            </Box>
          </Box>
        )}

        <Divider sx={{ my: 2 }} />

        {/* API Endpoints */}
        {data.endpoints && data.endpoints.length > 0 && (
          <Accordion defaultExpanded>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Box display="flex" alignItems="center">
                <ApiIcon fontSize="small" sx={{ mr: 1, color: 'success.main' }} />
                <Typography variant="subtitle2" fontWeight="bold">
                  API Endpoints ({data.endpoints.length})
                </Typography>
              </Box>
            </AccordionSummary>
            <AccordionDetails sx={{ p: 0 }}>
              <List dense disablePadding>
                {data.endpoints.map((endpoint, idx) => (
                  <ListItem key={idx} divider>
                    <Chip
                      label={endpoint.method}
                      size="small"
                      sx={{
                        mr: 1,
                        minWidth: 60,
                        bgcolor:
                          endpoint.method === 'GET'
                            ? 'success.light'
                            : endpoint.method === 'POST'
                            ? 'primary.light'
                            : endpoint.method === 'PUT'
                            ? 'warning.light'
                            : 'error.light',
                        color: 'white',
                        fontWeight: 'bold',
                        fontSize: '0.65rem',
                      }}
                    />
                    <ListItemText
                      primary={
                        <Typography variant="body2" fontFamily="monospace" fontSize="0.8rem">
                          {endpoint.path}
                        </Typography>
                      }
                      secondary={endpoint.description}
                    />
                  </ListItem>
                ))}
              </List>
            </AccordionDetails>
          </Accordion>
        )}

        {/* Database Tables */}
        {data.tables && data.tables.length > 0 && (
          <Accordion defaultExpanded>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Box display="flex" alignItems="center">
                <DatabaseIcon fontSize="small" sx={{ mr: 1, color: 'info.main' }} />
                <Typography variant="subtitle2" fontWeight="bold">
                  Database Tables ({data.tables.length})
                </Typography>
              </Box>
            </AccordionSummary>
            <AccordionDetails sx={{ p: 1 }}>
              {data.tables.map((table, idx) => (
                <Box key={idx} mb={2}>
                  <Typography
                    variant="subtitle2"
                    fontWeight="bold"
                    fontFamily="monospace"
                    sx={{ mb: 1, color: 'primary.main' }}
                  >
                    📋 {table.name}
                  </Typography>
                  <TableContainer component={Paper} variant="outlined">
                    <Table size="small">
                      <TableHead>
                        <TableRow sx={{ bgcolor: 'grey.100' }}>
                          <TableCell sx={{ fontWeight: 'bold', fontSize: '0.75rem' }}>
                            Column
                          </TableCell>
                          <TableCell sx={{ fontWeight: 'bold', fontSize: '0.75rem' }}>
                            Type
                          </TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {table.columns.map((col, colIdx) => (
                          <TableRow key={colIdx}>
                            <TableCell sx={{ fontSize: '0.75rem' }}>
                              {col.key && (
                                <KeyIcon
                                  sx={{ fontSize: 12, mr: 0.5, color: 'warning.main' }}
                                />
                              )}
                              <code>{col.name}</code>
                            </TableCell>
                            <TableCell sx={{ fontSize: '0.7rem', color: 'text.secondary' }}>
                              {col.type}
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                  {table.relationships && (
                    <Box mt={1}>
                      <Typography variant="caption" color="text.secondary">
                        Relationships: {table.relationships.join(', ')}
                      </Typography>
                    </Box>
                  )}
                </Box>
              ))}
            </AccordionDetails>
          </Accordion>
        )}

        {/* Events */}
        {data.events && data.events.length > 0 && (
          <Accordion defaultExpanded>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Box display="flex" alignItems="center">
                <EventIcon fontSize="small" sx={{ mr: 1, color: 'warning.main' }} />
                <Typography variant="subtitle2" fontWeight="bold">
                  Events ({data.events.length})
                </Typography>
              </Box>
            </AccordionSummary>
            <AccordionDetails>
              <Box display="flex" flexWrap="wrap" gap={0.5}>
                {data.events.map((event, idx) => (
                  <Chip
                    key={idx}
                    label={event}
                    size="small"
                    sx={{
                      bgcolor: 'warning.light',
                      color: 'warning.dark',
                      fontSize: '0.7rem',
                    }}
                  />
                ))}
              </Box>
            </AccordionDetails>
          </Accordion>
        )}

        {/* Node Type Badge */}
        <Box mt={3} display="flex" justifyContent="center">
          <Chip
            label={data.type.toUpperCase()}
            sx={{
              bgcolor: color,
              color: 'white',
              fontWeight: 'bold',
            }}
          />
        </Box>
      </Box>
    </Paper>
  );
};

export default NodeDetailsPanel;
