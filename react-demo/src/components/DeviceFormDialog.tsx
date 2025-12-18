
import React, { useState, useEffect } from 'react';
import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    Button,
    TextField,
    MenuItem,
    CircularProgress,
    Alert
} from '@mui/material';
import { Device, Person, PersonRole } from '../types';
import { api } from '../services/apiService';

interface DeviceFormDialogProps {
    open: boolean;
    onClose: () => void;
    onSave: () => void;
    device: Omit<Device, 'id'> | Device | null;
}

const DeviceFormDialog: React.FC<DeviceFormDialogProps> = ({ open, onClose, onSave, device }) => {
    const [formData, setFormData] = useState({
        name: '',
            maxConsumption: 0 as number,
        personId: '' as string | null,
    });

    const [clients, setClients] = useState<Person[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const isEditMode = device && 'id' in device;


    useEffect(() => {
        if (open) {
            const fetchClients = async () => {
                try {
                    const allPeople = await api.getPeople(); //
                    // Filtrăm să afișăm doar clienții în dropdown
                    setClients(allPeople.filter(p => p.role === PersonRole.CLIENT));
                } catch (err) {
                    console.error("Failed to fetch clients:", err);
                    setError("Could not load client list.");
                }
            };
            fetchClients();


            if (isEditMode) {
                setFormData({
                    name: device.name,
                    maxConsumption: typeof device.maxConsumption === 'number' ? device.maxConsumption : Number(device.maxConsumption) || 0,
                    personId: device.personId || '',
                });
            } else {
                setFormData({
                    name: '',
                    maxConsumption: 0,
                    personId: '',
                });
            }
        }
    }, [device, isEditMode, open]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | { name?: string; value: unknown }>) => {
        const { name, value } = e.target;
            if (name === 'maxConsumption') {
                let num = typeof value === 'number' ? value : Number(value);
                setFormData(prev => ({
                    ...prev,
                    maxConsumption: isNaN(num) ? 0 : num,
                }));
            } else {
                setFormData(prev => ({
                    ...prev,
                    [name as string]: value,
                }));
            }
    };

    const handleSubmit = async () => {
        setLoading(true);
        setError('');

        // Validare robustă pentru maxConsumption
        if (
            typeof formData.maxConsumption !== 'number' ||
            isNaN(formData.maxConsumption) ||
            formData.maxConsumption === null ||
            formData.maxConsumption === undefined ||
            formData.maxConsumption <= 0
        ) {
            setError('Max Consumption must be a positive number!');
            setLoading(false);
            return;
        }

        const payload = {
            ...formData,
            personId: formData.personId || null,
        };

        try {
            if (isEditMode) {
                await api.updateDevice(device.id, payload);
            } else {
                await api.createDevice(payload as Omit<Device, 'id'>);
            }
            onSave();
            onClose();
        } catch (err: any) {
            console.error('Failed to save device:', err);
            setError(err.response?.data?.message || 'Failed to save device');
        }
        setLoading(false);
    };

    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <DialogTitle>{isEditMode ? 'Edit Device' : 'Add New Device'}</DialogTitle>
            <DialogContent>
                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
                <TextField
                    autoFocus
                    margin="dense"
                    name="name"
                    label="Device Name"
                    type="text"
                    fullWidth
                    variant="outlined"
                    value={formData.name}
                    onChange={handleChange}
                />
                <TextField
                    margin="dense"
                    name="maxConsumption"
                    label="Max Consumption (kWh)"
                    type="number"
                    fullWidth
                    variant="outlined"
                    value={formData.maxConsumption}
                    onChange={handleChange}
                    InputProps={{ inputProps: { min: 0.01, step: "0.01" } }}
                    error={!!error && error.toLowerCase().includes('max consumption')}
                    helperText={!!error && error.toLowerCase().includes('max consumption') ? error : ''}
                />
                <TextField
                    margin="dense"
                    name="personId"
                    label="Assign to Client"
                    select
                    fullWidth
                    variant="outlined"
                    value={formData.personId || ''}
                    onChange={handleChange}
                >
                    <MenuItem value="">
                        <em>Unassigned</em>
                    </MenuItem>
                    {clients.map((client) => (
                        <MenuItem key={client.id} value={client.id}>
                            {client.name} (@{client.username})
                        </MenuItem>
                    ))}
                </TextField>
            </DialogContent>
            <DialogActions sx={{ p: '0 24px 20px' }}>
                <Button onClick={onClose} color="inherit">Cancel</Button>
                <Button onClick={handleSubmit} variant="contained" disabled={loading}>
                    {loading ? <CircularProgress size={24} /> : 'Save'}
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default DeviceFormDialog;