
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Box,
    Button,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    IconButton,
    CircularProgress,
    Typography,
    Alert,
    Chip
} from '@mui/material';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon, ShowChart as ShowChartIcon } from '@mui/icons-material';
import { Device, Person, PersonRole } from '../types';
import { api } from '../services/apiService';
import DeviceFormDialog from './DeviceFormDialog';

const DeviceManagement: React.FC = () => {
    const navigate = useNavigate();
    const [devices, setDevices] = useState<Device[]>([]);
    const [clients, setClients] = useState<Person[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [dialogOpen, setDialogOpen] = useState(false);
    const [editingDevice, setEditingDevice] = useState<Device | null>(null);


    const fetchData = async () => {
        try {
            setLoading(true);
            setError('');

            const [devicesData, peopleData] = await Promise.all([
                api.getDevices(),
                api.getPeople()
            ]);
            console.log('DeviceManagement - devices:', devicesData, 'IsArray:', Array.isArray(devicesData));
            console.log('DeviceManagement - people:', peopleData, 'IsArray:', Array.isArray(peopleData));
            setDevices(Array.isArray(devicesData) ? devicesData : []);
            setClients(Array.isArray(peopleData) ? peopleData.filter(p => p.role === PersonRole.CLIENT) : []);
        } catch (err: any) {
            console.error('Failed to fetch data:', err);
            setError(err.response?.data?.message || 'Failed to fetch data');
        }
        setLoading(false);
    };

    useEffect(() => {
        fetchData();
    }, []);


    const getClientName = (personId: string | null) => {
        if (!personId) return null;
        const client = clients.find(c => c.id === personId);
        return client ? client.name : 'Unknown User';
    };

    const handleOpenDialog = (device: Device | null = null) => {
        setEditingDevice(device);
        setDialogOpen(true);
    };

    const handleCloseDialog = () => {
        setDialogOpen(false);
        setEditingDevice(null);
    };

    const handleSave = () => {
        fetchData();
    };

    const handleDelete = async (deviceId: string) => {
        if (window.confirm('Are you sure you want to delete this device?')) {
            try {
                await api.deleteDevice(deviceId); //
                fetchData();
            } catch (err: any) {
                console.error('Failed to delete device:', err);
                setError(err.response?.data?.message || 'Failed to delete device');
            }
        }
    };

    if (loading) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                <CircularProgress />
            </Box>
        );
    }

    return (
        <Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                <Typography variant="h5">Device List</Typography>
                <Button
                    variant="contained"
                    startIcon={<AddIcon />}
                    onClick={() => handleOpenDialog(null)}
                >
                    Add Device
                </Button>
            </Box>

            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

            <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Device Name</TableCell>
                            <TableCell>Max Consumption (kWh)</TableCell>
                            <TableCell>Assigned To</TableCell>
                            <TableCell align="right">Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {devices.map((device) => {
                            const clientName = getClientName(device.personId);
                            return (
                                <TableRow key={device.id}>
                                    <TableCell>{device.name}</TableCell>
                                    <TableCell>{device.maxConsumption}</TableCell>
                                    <TableCell>
                                        {clientName ? (
                                            <Chip label={clientName} color="primary" variant="outlined" />
                                        ) : (
                                            <Chip label="Unassigned" variant="outlined" />
                                        )}
                                    </TableCell>
                                    <TableCell align="right">
                                        <IconButton 
                                            onClick={() => navigate(`/device/${device.id}/chart`)}
                                            title="View Chart"
                                        >
                                            <ShowChartIcon />
                                        </IconButton>
                                        <IconButton onClick={() => handleOpenDialog(device)}>
                                            <EditIcon />
                                        </IconButton>
                                        <IconButton onClick={() => handleDelete(device.id)}>
                                            <DeleteIcon />
                                        </IconButton>
                                    </TableCell>
                                </TableRow>
                            );
                        })}
                    </TableBody>
                </Table>
            </TableContainer>

            <DeviceFormDialog
                open={dialogOpen}
                onClose={handleCloseDialog}
                onSave={handleSave}
                device={editingDevice}
            />
        </Box>
    );
};

export default DeviceManagement;