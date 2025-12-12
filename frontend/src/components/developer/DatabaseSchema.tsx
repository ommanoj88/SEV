import React, { useEffect, useMemo, useState } from 'react';
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Box,
  Chip,
  Divider,
  InputAdornment,
  List,
  ListItemButton,
  ListItemText,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  Storage as DatabaseIcon,
  Search as SearchIcon,
  ListAlt as ListAltIcon,
  Category as CategoryIcon,
} from '@mui/icons-material';
import {
  databaseTables,
  databaseEnums,
  dbStats,
  moduleColors as databaseModuleColors,
  type TableDef,
} from './data';

const DatabaseSchema: React.FC = () => {
  const [search, setSearch] = useState('');
  const [selectedTableId, setSelectedTableId] = useState<string>(databaseTables[0]?.id ?? '');

  const filteredTables = useMemo(() => {
    const term = search.toLowerCase();
    return databaseTables
      .filter((table) =>
        [table.name, table.displayName, table.module, table.description]
          .some((value) => value.toLowerCase().includes(term))
      )
      .sort((a, b) => a.module.localeCompare(b.module) || a.name.localeCompare(b.name));
  }, [search]);

  useEffect(() => {
    if (!filteredTables.find((t) => t.id === selectedTableId) && filteredTables.length > 0) {
      setSelectedTableId(filteredTables[0].id);
    }
  }, [filteredTables, selectedTableId]);

  const selectedTable: TableDef | undefined = useMemo(
    () => filteredTables.find((t) => t.id === selectedTableId) ?? filteredTables[0],
    [filteredTables, selectedTableId]
  );

  const moduleEnums = useMemo(
    () => databaseEnums.filter((e) => e.module === selectedTable?.module),
    [selectedTable?.module]
  );

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%', p: 2, gap: 2, overflow: 'hidden' }}>
      {/* Stats */}
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} sx={{ flexWrap: 'wrap', flexShrink: 0 }}>
        <Chip icon={<DatabaseIcon />} label={`Tables: ${dbStats.totalTables}`} color="primary" />
        <Chip icon={<ListAltIcon />} label={`Enums: ${dbStats.totalEnums}`} variant="outlined" />
        <Chip icon={<CategoryIcon />} label={`Modules: ${dbStats.modules}`} variant="outlined" />
      </Stack>

      <Paper
        variant="outlined"
        sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '320px 1fr' }, gap: 2, flex: 1, overflow: 'hidden', minHeight: 0 }}
      >
        {/* Left: table list */}
        <Box sx={{ p: 2, borderRight: { md: '1px solid', xs: 'none' }, borderColor: 'divider', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
          <TextField
            fullWidth
            size="small"
            placeholder="Search tables, modules, descriptions"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon fontSize="small" />
                </InputAdornment>
              ),
            }}
            sx={{ flexShrink: 0 }}
          />
          <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 1, flexShrink: 0 }}>
            {filteredTables.length} tables
          </Typography>

          <List dense sx={{ mt: 1, flex: 1, overflow: 'auto', minHeight: 0 }}>
            {filteredTables.map((table) => (
              <ListItemButton
                key={table.id}
                selected={table.id === selectedTableId}
                onClick={() => setSelectedTableId(table.id)}
                sx={{ borderRadius: 1, mb: 0.5 }}
              >
                <ListItemText
                  primary={
                    <Typography variant="body2" fontWeight={600} noWrap>
                      {table.displayName}
                    </Typography>
                  }
                  secondary={
                    <Stack direction="row" spacing={0.5} alignItems="center" flexWrap="wrap">
                      <Chip
                        label={table.module}
                        size="small"
                        sx={{
                          bgcolor: databaseModuleColors[table.module] || 'grey.200',
                          color: 'white',
                          height: 20,
                        }}
                      />
                      <Typography variant="caption" color="text.secondary" noWrap>
                        {table.name}
                      </Typography>
                    </Stack>
                  }
                />
              </ListItemButton>
            ))}
          </List>
        </Box>

        {/* Right: table details */}
        <Box sx={{ p: 2, overflow: 'auto' }}>
          {selectedTable ? (
            <Stack spacing={3}>
              {/* Header with module and table name */}
              <Box display="flex" alignItems="center" gap={1} flexWrap="wrap">
                <Chip
                  label={selectedTable.module}
                  sx={{
                    bgcolor: databaseModuleColors[selectedTable.module] || 'grey.200',
                    color: 'white',
                    textTransform: 'uppercase',
                  }}
                />
                <Typography variant="h6" fontWeight="bold">
                  {selectedTable.displayName}
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ fontFamily: 'monospace' }}>
                  ({selectedTable.name})
                </Typography>
              </Box>

              {/* Purpose/Description */}
              {selectedTable.description && (
                <Box sx={{ bgcolor: 'info.light', p: 1.5, borderRadius: 1, borderLeft: '4px solid', borderColor: 'info.main' }}>
                  <Typography variant="subtitle2" fontWeight="bold" color="info.dark">
                    📋 Purpose
                  </Typography>
                  <Typography variant="body2" color="info.dark" sx={{ mt: 0.5 }}>
                    {selectedTable.description}
                  </Typography>
                </Box>
              )}

              {/* Relationships Summary */}
              {selectedTable.relationships && selectedTable.relationships.length > 0 && (
                <Box sx={{ bgcolor: 'success.light', p: 1.5, borderRadius: 1, borderLeft: '4px solid', borderColor: 'success.main' }}>
                  <Typography variant="subtitle2" fontWeight="bold" color="success.dark">
                    🔗 Connected Tables
                  </Typography>
                  <Stack spacing={0.5} sx={{ mt: 1 }}>
                    {selectedTable.relationships.map((rel, idx) => (
                      <Box key={`${rel.table}-${idx}`} sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
                        <Typography variant="caption" color="success.dark" sx={{ minWidth: 60 }}>
                          <Chip label={rel.type} size="small" variant="outlined" />
                        </Typography>
                        <Stack spacing={0}>
                          <Typography variant="body2" fontWeight={500} color="success.dark" sx={{ fontFamily: 'monospace' }}>
                            → {rel.table}
                          </Typography>
                          <Typography variant="caption" color="success.dark">
                            {rel.description}
                          </Typography>
                        </Stack>
                      </Box>
                    ))}
                  </Stack>
                </Box>
              )}

              <Divider />

              {/* Columns table with full details */}
              <Box>
                <Typography variant="subtitle2" fontWeight="bold" sx={{ mb: 1 }}>
                  📊 Columns & Data Types
                </Typography>
                <TableContainer component={Paper} variant="outlined">
                  <Table size="small">
                    <TableHead>
                      <TableRow sx={{ bgcolor: 'grey.100' }}>
                        <TableCell sx={{ fontWeight: 700, width: '20%' }}>Column</TableCell>
                        <TableCell sx={{ fontWeight: 700, width: '15%' }}>Type</TableCell>
                        <TableCell sx={{ fontWeight: 700, width: '15%' }}>Constraints</TableCell>
                        <TableCell sx={{ fontWeight: 700, width: '50%' }}>Description</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {selectedTable.columns.map((col) => (
                        <TableRow key={col.name} sx={{ '&:hover': { bgcolor: 'action.hover' } }}>
                          <TableCell sx={{ fontFamily: 'monospace', fontWeight: 500 }}>
                            {col.name}
                          </TableCell>
                          <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.85rem' }}>
                            {col.type}
                          </TableCell>
                          <TableCell>
                            <Stack direction="row" spacing={0.5} flexWrap="wrap">
                              {col.primaryKey && <Chip label="PK" size="small" color="warning" variant="filled" />}
                              {col.foreignKey && <Chip label={`FK`} size="small" color="info" variant="filled" />}
                              {col.unique && <Chip label="UNIQUE" size="small" variant="outlined" />}
                              {!col.nullable && !col.primaryKey && <Chip label="NOT NULL" size="small" variant="outlined" color="error" />}
                              {col.nullable && <Chip label="NULL" size="small" variant="outlined" />}
                            </Stack>
                          </TableCell>
                          <TableCell sx={{ fontSize: '0.85rem', color: 'text.secondary' }}>
                            <Stack spacing={0.5}>
                              {col.description && (
                                <Typography variant="caption">{col.description}</Typography>
                              )}
                              {col.foreignKey && (
                                <Typography variant="caption" sx={{ fontFamily: 'monospace', color: 'primary.main' }}>
                                  References: {col.foreignKey}
                                </Typography>
                              )}
                              {col.defaultValue && (
                                <Typography variant="caption" color="warning.main">
                                  Default: {col.defaultValue}
                                </Typography>
                              )}
                            </Stack>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Box>

              {/* Indexes */}
              {selectedTable.indexes && selectedTable.indexes.length > 0 && (
                <Accordion>
                  <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography variant="subtitle2" fontWeight="bold">
                      ⚡ Indexes (Performance Optimization)
                    </Typography>
                  </AccordionSummary>
                  <AccordionDetails>
                    <Table size="small">
                      <TableHead>
                        <TableRow sx={{ bgcolor: 'grey.100' }}>
                          <TableCell sx={{ fontWeight: 700 }}>Index Name</TableCell>
                          <TableCell sx={{ fontWeight: 700 }}>Columns</TableCell>
                          <TableCell sx={{ fontWeight: 700 }}>Type</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {selectedTable.indexes.map((idx) => (
                          <TableRow key={idx.name}>
                            <TableCell sx={{ fontFamily: 'monospace', fontWeight: 500 }}>
                              {idx.name}
                            </TableCell>
                            <TableCell sx={{ fontFamily: 'monospace' }}>
                              {idx.columns.join(', ')}
                            </TableCell>
                            <TableCell>
                              <Chip label={idx.unique ? 'UNIQUE' : 'STANDARD'} size="small" />
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </AccordionDetails>
                </Accordion>
              )}

              {/* Relationships Details */}
              {selectedTable.relationships && selectedTable.relationships.length > 0 && (
                <Accordion defaultExpanded>
                  <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography variant="subtitle2" fontWeight="bold">
                      🔗 Relationship Details & Data Flow
                    </Typography>
                  </AccordionSummary>
                  <AccordionDetails>
                    <Stack spacing={2}>
                      {selectedTable.relationships.map((rel, idx) => (
                        <Box key={`${rel.table}-${idx}`} sx={{ bgcolor: 'grey.50', p: 1.5, borderRadius: 1, borderLeft: '3px solid', borderColor: 'primary.main' }}>
                          <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
                            <Chip label={rel.type.replace(/-/g, ' ').toUpperCase()} size="small" color="primary" />
                            <Typography variant="body2" fontWeight={600} sx={{ fontFamily: 'monospace' }}>
                              {selectedTable.name} ↔ {rel.table}
                            </Typography>
                          </Stack>
                          <Typography variant="body2" color="text.secondary">
                            {rel.description}
                          </Typography>
                          <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 1 }}>
                            <strong>What happens:</strong> When you query {selectedTable.displayName}, you can fetch related {rel.table} records through this {rel.type} relationship.
                          </Typography>
                        </Box>
                      ))}
                    </Stack>
                  </AccordionDetails>
                </Accordion>
              )}

              {/* Enums used in this module */}
              {moduleEnums.length > 0 && (
                <Accordion defaultExpanded>
                  <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography variant="subtitle2" fontWeight="bold">
                      📋 Enums Used in {selectedTable.module} Module
                    </Typography>
                  </AccordionSummary>
                  <AccordionDetails>
                    <Stack spacing={1.5}>
                      {moduleEnums.map((enm) => (
                        <Box key={enm.name} sx={{ bgcolor: 'grey.50', p: 1, borderRadius: 1 }}>
                          <Typography variant="body2" fontWeight={700} sx={{ mb: 0.5 }}>
                            {enm.name}
                          </Typography>
                          <Stack direction="row" spacing={0.5} flexWrap="wrap">
                            {enm.values.map((val) => (
                              <Chip key={val} label={val} size="small" variant="outlined" />
                            ))}
                          </Stack>
                        </Box>
                      ))}
                    </Stack>
                  </AccordionDetails>
                </Accordion>
              )}

              {/* Audit Fields Info */}
              {selectedTable.columns.some((c) => c.name === 'created_at' || c.name === 'updated_at') && (
                <Box sx={{ bgcolor: 'warning.light', p: 1.5, borderRadius: 1, borderLeft: '4px solid', borderColor: 'warning.main' }}>
                  <Typography variant="subtitle2" fontWeight="bold" color="warning.dark">
                    📝 Audit Information
                  </Typography>
                  <Typography variant="body2" color="warning.dark" sx={{ mt: 0.5 }}>
                    This table includes created_at and updated_at fields for audit tracking. All changes are automatically recorded for compliance and debugging.
                  </Typography>
                </Box>
              )}

              {/* Multi-tenant Note if company_id exists */}
              {selectedTable.columns.some((c) => c.name === 'company_id') && (
                <Box sx={{ bgcolor: 'secondary.light', p: 1.5, borderRadius: 1, borderLeft: '4px solid', borderColor: 'secondary.main' }}>
                  <Typography variant="subtitle2" fontWeight="bold" color="secondary.dark">
                    🏢 Multi-Tenant
                  </Typography>
                  <Typography variant="body2" color="secondary.dark" sx={{ mt: 0.5 }}>
                    This table uses company_id for multi-tenancy. Data is isolated by company, ensuring data privacy and security across different organizations.
                  </Typography>
                </Box>
              )}
            </Stack>
          ) : (
            <Typography variant="body2" color="text.secondary">
              No tables match your search.
            </Typography>
          )}
        </Box>
      </Paper>
    </Box>
  );
};

export default DatabaseSchema;
