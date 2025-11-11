import React, { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  Grid,
  IconButton,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
  Alert,
  Tabs,
  Tab,
  Badge,
} from '@mui/material';
import {
  Upload as UploadIcon,
  Description as DescriptionIcon,
  Verified as VerifiedIcon,
  Warning as WarningIcon,
  Delete as DeleteIcon,
  Visibility as VisibilityIcon,
  GetApp as DownloadIcon,
} from '@mui/icons-material';
import { format } from 'date-fns';

interface Document {
  id: number;
  documentNumber: string;
  documentType: string;
  documentStatus: string;
  entityType: string;
  entityId: number;
  title: string;
  description?: string;
  fileName: string;
  filePath: string;
  fileSize?: number;
  mimeType?: string;
  issueDate?: string;
  expiryDate?: string;
  issuingAuthority?: string;
  isVerified: boolean;
  verifiedBy?: string;
  daysUntilExpiry?: number;
  isExpired: boolean;
  requiresAction: boolean;
  createdAt: string;
}

const DocumentManagementPage: React.FC = () => {
  const [documents, setDocuments] = useState<Document[]>([]);
  const [filteredDocuments, setFilteredDocuments] = useState<Document[]>([]);
  const [activeTab, setActiveTab] = useState(0);
  const [uploadDialogOpen, setUploadDialogOpen] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [formData, setFormData] = useState({
    documentNumber: '',
    documentType: 'VEHICLE_RC',
    entityType: 'VEHICLE',
    entityId: '',
    title: '',
    description: '',
    issueDate: '',
    expiryDate: '',
    issuingAuthority: '',
    issuingLocation: '',
    notes: '',
  });

  const documentTypes = [
    { value: 'VEHICLE_RC', label: 'Vehicle RC' },
    { value: 'VEHICLE_INSURANCE', label: 'Vehicle Insurance' },
    { value: 'VEHICLE_PERMIT', label: 'Vehicle Permit' },
    { value: 'VEHICLE_FITNESS', label: 'Fitness Certificate' },
    { value: 'VEHICLE_PUC', label: 'PUC Certificate' },
    { value: 'VEHICLE_TAX', label: 'Road Tax' },
    { value: 'DRIVER_LICENSE', label: 'Driving License' },
    { value: 'DRIVER_BADGE', label: 'Driver Badge' },
    { value: 'DRIVER_AADHAR', label: 'Aadhar Card' },
    { value: 'DRIVER_PAN', label: 'PAN Card' },
    { value: 'OTHER', label: 'Other Document' },
  ];

  const filterDocuments = useCallback(() => {
    let filtered = documents;
    
    switch (activeTab) {
      case 0: // All
        filtered = documents;
        break;
      case 1: // Expiring Soon
        filtered = documents.filter(doc => 
          doc.documentStatus === 'EXPIRING_SOON' && !doc.isExpired
        );
        break;
      case 2: // Expired
        filtered = documents.filter(doc => doc.isExpired);
        break;
      case 3: // Unverified
        filtered = documents.filter(doc => !doc.isVerified);
        break;
    }
    
    setFilteredDocuments(filtered);
  }, [documents, activeTab]);

  useEffect(() => {
    fetchDocuments();
  }, []);

  useEffect(() => {
    filterDocuments();
  }, [documents, activeTab, filterDocuments]);

  const fetchDocuments = async () => {
    try {
      const response = await fetch('/api/documents/action-required');
      if (response.ok) {
        const data = await response.json();
        setDocuments(data);
      }
    } catch (err) {
      setError('Failed to fetch documents');
    }
  };

  const handleUploadDocument = async () => {
    if (!selectedFile) {
      setError('Please select a file to upload');
      return;
    }

    const formDataToSend = new FormData();
    formDataToSend.append('file', selectedFile);
    
    const documentDto = {
      documentNumber: formData.documentNumber,
      documentType: formData.documentType,
      entityType: formData.entityType,
      entityId: parseInt(formData.entityId),
      title: formData.title,
      description: formData.description,
      issueDate: formData.issueDate,
      expiryDate: formData.expiryDate,
      issuingAuthority: formData.issuingAuthority,
      issuingLocation: formData.issuingLocation,
      notes: formData.notes,
      createdBy: 'current-user', // Get from auth context
    };
    
    formDataToSend.append('document', new Blob([JSON.stringify(documentDto)], {
      type: 'application/json'
    }));

    try {
      const response = await fetch('/api/documents', {
        method: 'POST',
        body: formDataToSend,
      });

      if (response.ok) {
        setSuccess('Document uploaded successfully');
        setUploadDialogOpen(false);
        fetchDocuments();
        resetForm();
      } else {
        setError('Failed to upload document');
      }
    } catch (err) {
      setError('Error uploading document');
    }
  };

  const handleVerifyDocument = async (id: number) => {
    try {
      const response = await fetch(`/api/documents/${id}/verify?verifiedBy=current-user`, {
        method: 'PUT',
      });

      if (response.ok) {
        setSuccess('Document verified successfully');
        fetchDocuments();
      } else {
        setError('Failed to verify document');
      }
    } catch (err) {
      setError('Error verifying document');
    }
  };

  const handleDeleteDocument = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this document?')) {
      try {
        const response = await fetch(`/api/documents/${id}`, {
          method: 'DELETE',
        });

        if (response.ok) {
          setSuccess('Document deleted successfully');
          fetchDocuments();
        } else {
          setError('Failed to delete document');
        }
      } catch (err) {
        setError('Error deleting document');
      }
    }
  };

  const resetForm = () => {
    setFormData({
      documentNumber: '',
      documentType: 'VEHICLE_RC',
      entityType: 'VEHICLE',
      entityId: '',
      title: '',
      description: '',
      issueDate: '',
      expiryDate: '',
      issuingAuthority: '',
      issuingLocation: '',
      notes: '',
    });
    setSelectedFile(null);
  };

  const getStatusChip = (doc: Document) => {
    if (doc.isExpired) {
      return <Chip label="Expired" color="error" size="small" />;
    }
    if (doc.documentStatus === 'EXPIRING_SOON') {
      return <Chip label="Expiring Soon" color="warning" size="small" />;
    }
    if (!doc.isVerified) {
      return <Chip label="Unverified" color="default" size="small" />;
    }
    return <Chip label="Active" color="success" size="small" />;
  };

  const expiringCount = documents.filter(d => d.documentStatus === 'EXPIRING_SOON').length;
  const expiredCount = documents.filter(d => d.isExpired).length;
  const unverifiedCount = documents.filter(d => !d.isVerified).length;

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">Document Management</Typography>
        <Button
          variant="contained"
          startIcon={<UploadIcon />}
          onClick={() => setUploadDialogOpen(true)}
        >
          Upload Document
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess(null)}>{success}</Alert>}

      {/* Summary Cards */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>Total Documents</Typography>
              <Typography variant="h4">{documents.length}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ bgcolor: 'warning.light' }}>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>Expiring Soon</Typography>
              <Typography variant="h4">{expiringCount}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ bgcolor: 'error.light' }}>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>Expired</Typography>
              <Typography variant="h4">{expiredCount}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ bgcolor: 'info.light' }}>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>Unverified</Typography>
              <Typography variant="h4">{unverifiedCount}</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Tabs */}
      <Paper sx={{ mb: 2 }}>
        <Tabs value={activeTab} onChange={(e, newValue) => setActiveTab(newValue)}>
          <Tab label="All Documents" />
          <Tab 
            label={
              <Badge badgeContent={expiringCount} color="warning">
                Expiring Soon
              </Badge>
            } 
          />
          <Tab 
            label={
              <Badge badgeContent={expiredCount} color="error">
                Expired
              </Badge>
            } 
          />
          <Tab 
            label={
              <Badge badgeContent={unverifiedCount} color="info">
                Unverified
              </Badge>
            } 
          />
        </Tabs>
      </Paper>

      {/* Documents Table */}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Document Type</TableCell>
              <TableCell>Document Number</TableCell>
              <TableCell>Entity</TableCell>
              <TableCell>Issue Date</TableCell>
              <TableCell>Expiry Date</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Verified</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredDocuments.map((doc) => (
              <TableRow key={doc.id}>
                <TableCell>
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <DescriptionIcon sx={{ mr: 1 }} />
                    {doc.documentType.replace(/_/g, ' ')}
                  </Box>
                </TableCell>
                <TableCell>{doc.documentNumber}</TableCell>
                <TableCell>
                  {doc.entityType} #{doc.entityId}
                </TableCell>
                <TableCell>
                  {doc.issueDate ? format(new Date(doc.issueDate), 'MMM dd, yyyy') : '-'}
                </TableCell>
                <TableCell>
                  {doc.expiryDate ? (
                    <>
                      {format(new Date(doc.expiryDate), 'MMM dd, yyyy')}
                      {doc.daysUntilExpiry !== undefined && doc.daysUntilExpiry >= 0 && (
                        <Typography variant="caption" display="block" color="textSecondary">
                          ({doc.daysUntilExpiry} days left)
                        </Typography>
                      )}
                    </>
                  ) : '-'}
                </TableCell>
                <TableCell>{getStatusChip(doc)}</TableCell>
                <TableCell>
                  {doc.isVerified ? (
                    <Chip icon={<VerifiedIcon />} label="Verified" color="success" size="small" />
                  ) : (
                    <Chip icon={<WarningIcon />} label="Not Verified" color="default" size="small" />
                  )}
                </TableCell>
                <TableCell>
                  <IconButton size="small" onClick={() => handleVerifyDocument(doc.id)} disabled={doc.isVerified}>
                    <VerifiedIcon />
                  </IconButton>
                  <IconButton size="small">
                    <VisibilityIcon />
                  </IconButton>
                  <IconButton size="small">
                    <DownloadIcon />
                  </IconButton>
                  <IconButton size="small" onClick={() => handleDeleteDocument(doc.id)}>
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Upload Dialog */}
      <Dialog open={uploadDialogOpen} onClose={() => setUploadDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>Upload Document</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>Document Type</InputLabel>
                <Select
                  value={formData.documentType}
                  onChange={(e) => setFormData({ ...formData, documentType: e.target.value })}
                >
                  {documentTypes.map(type => (
                    <MenuItem key={type.value} value={type.value}>{type.label}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Document Number"
                value={formData.documentNumber}
                onChange={(e) => setFormData({ ...formData, documentNumber: e.target.value })}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>Entity Type</InputLabel>
                <Select
                  value={formData.entityType}
                  onChange={(e) => setFormData({ ...formData, entityType: e.target.value })}
                >
                  <MenuItem value="VEHICLE">Vehicle</MenuItem>
                  <MenuItem value="DRIVER">Driver</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Entity ID"
                type="number"
                value={formData.entityId}
                onChange={(e) => setFormData({ ...formData, entityId: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Title"
                value={formData.title}
                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Description"
                multiline
                rows={2}
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Issue Date"
                type="date"
                InputLabelProps={{ shrink: true }}
                value={formData.issueDate}
                onChange={(e) => setFormData({ ...formData, issueDate: e.target.value })}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Expiry Date"
                type="date"
                InputLabelProps={{ shrink: true }}
                value={formData.expiryDate}
                onChange={(e) => setFormData({ ...formData, expiryDate: e.target.value })}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Issuing Authority"
                value={formData.issuingAuthority}
                onChange={(e) => setFormData({ ...formData, issuingAuthority: e.target.value })}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Issuing Location"
                value={formData.issuingLocation}
                onChange={(e) => setFormData({ ...formData, issuingLocation: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Notes"
                multiline
                rows={2}
                value={formData.notes}
                onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <Button
                variant="outlined"
                component="label"
                fullWidth
                startIcon={<UploadIcon />}
              >
                {selectedFile ? selectedFile.name : 'Select File'}
                <input
                  type="file"
                  hidden
                  onChange={(e) => {
                    if (e.target.files && e.target.files[0]) {
                      setSelectedFile(e.target.files[0]);
                    }
                  }}
                />
              </Button>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setUploadDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleUploadDocument} variant="contained">
            Upload
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default DocumentManagementPage;
