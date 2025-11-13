// src/components/UserFormDialog.tsx
import { Alert } from '@mui/material';
import React, { useState, useEffect } from 'react';
import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    Button,
    TextField,
    MenuItem,
    CircularProgress
} from '@mui/material';
import { Person, PersonRole } from '../types';
import { api } from '../services/apiService';

interface UserFormDialogProps {
    open: boolean;
    onClose: () => void;
    onSave: () => void;
    user: Omit<Person, 'id'> | Person | null;
}

const UserFormDialog: React.FC<UserFormDialogProps> = ({ open, onClose, onSave, user }) => {
    const [formData, setFormData] = useState({
        name: '',
        username: '',
        password: '',
        address: '',
        age: 18,
        role: PersonRole.CLIENT,
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const isEditMode = user && 'id' in user;

    useEffect(() => {

        if (isEditMode) {
            setFormData({
                name: user.name,
                username: user.username,
                password: '',
                address: user.address,
                age: user.age,
                role: user.role,
            });
        } else {

            setFormData({
                name: '',
                username: '',
                password: '',
                address: '',
                age: 18,
                role: PersonRole.CLIENT,
            });
        }
    }, [user, isEditMode, open]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: name === 'age' ? parseInt(value) : value,
        }));
    };

    const handleSubmit = async () => {
        setLoading(true);
        setError('');

        try {
            if (isEditMode) {

                const { password, ...personData } = formData;
                const payload: Partial<Person> = personData;
                if (password) {

                    (payload as any).password = password;
                }

                await api.updatePerson(user.id, payload);

            } else {

                await api.registerPerson(formData as Omit<Person, 'id'>);
            }
            onSave();
            onClose();
        } catch (err: any) {
            console.error('Failed to save user:', err);
            setError(err.response?.data?.message || 'Failed to save user');
        }
        setLoading(false);
    };

    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <DialogTitle>{isEditMode ? 'Edit User' : 'Register New User'}</DialogTitle>
            <DialogContent>
                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
                <TextField
                    autoFocus
                    margin="dense"
                    name="name"
                    label="Name"
                    type="text"
                    fullWidth
                    variant="outlined"
                    value={formData.name}
                    onChange={handleChange}
                />
                <TextField
                    margin="dense"
                    name="username"
                    label="Username"
                    type="text"
                    fullWidth
                    variant="outlined"
                    value={formData.username}
                    onChange={handleChange}
                />
                <TextField
                    margin="dense"
                    name="password"
                    label={isEditMode ? "New Password (Leave empty to keep)" : "Password"}
                    type="password"
                    fullWidth
                    variant="outlined"
                    value={formData.password}
                    onChange={handleChange}
                    required={!isEditMode}
                />
                <TextField
                    margin="dense"
                    name="address"
                    label="Address"
                    type="text"
                    fullWidth
                    variant="outlined"
                    value={formData.address}
                    onChange={handleChange}
                />
                <TextField
                    margin="dense"
                    name="age"
                    label="Age"
                    type="number"
                    fullWidth
                    variant="outlined"
                    value={formData.age}
                    onChange={handleChange}
                />
                <TextField
                    margin="dense"
                    name="role"
                    label="Role"
                    select
                    fullWidth
                    variant="outlined"
                    value={formData.role}
                    onChange={handleChange}
                >
                    <MenuItem value={PersonRole.ADMIN}>Admin</MenuItem>
                    <MenuItem value={PersonRole.CLIENT}>Client</MenuItem>
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


export default UserFormDialog;