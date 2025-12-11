
import React, { useState, useEffect } from 'react';
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
    Alert
} from '@mui/material';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { Person } from '../types';
import { api } from '../services/apiService';
import UserFormDialog from './UserFormDialog';

const UserManagement: React.FC = () => {
    const [users, setUsers] = useState<Person[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [dialogOpen, setDialogOpen] = useState(false);
    const [editingUser, setEditingUser] = useState<Person | null>(null);

    const fetchUsers = async () => {
        try {
            setLoading(true);
            setError('');
            const data = await api.getPeople(); 
            console.log('getPeople response:', data, 'Type:', typeof data, 'IsArray:', Array.isArray(data));
            setUsers(Array.isArray(data) ? data : []);
        } catch (err: any) {
            console.error('Failed to fetch users:', err);
            setError(err.response?.data?.message || 'Failed to fetch users');
        }
        setLoading(false);
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    const handleOpenDialog = (user: Person | null = null) => {
        setEditingUser(user);
        setDialogOpen(true);
    };

    const handleCloseDialog = () => {
        setDialogOpen(false);
        setEditingUser(null);
    };

    const handleSave = () => {
        fetchUsers();
    };

    const handleDelete = async (userId: string) => {
        if (window.confirm('Are you sure you want to delete this user?')) {
            try {
                await api.deletePerson(userId); //
                fetchUsers();
            } catch (err: any) {
                console.error('Failed to delete user:', err);
                setError(err.response?.data?.message || 'Failed to delete user');
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
                <Typography variant="h5">User List</Typography>
                <Button
                    variant="contained"
                    startIcon={<AddIcon />}
                    onClick={() => handleOpenDialog(null)}
                >
                    Register User
                </Button>
            </Box>

            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

            <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Name</TableCell>
                            <TableCell>Username</TableCell>
                            <TableCell>Role</TableCell>
                            <TableCell>Age</TableCell>
                            <TableCell>Address</TableCell>
                            <TableCell align="right">Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {users.map((user) => (
                            <TableRow key={user.id}>
                                <TableCell>{user.name}</TableCell>
                                <TableCell>{user.username}</TableCell>
                                <TableCell>{user.role}</TableCell>
                                <TableCell>{user.age}</TableCell>
                                <TableCell>{user.address}</TableCell>
                                <TableCell align="right">
                                    <IconButton onClick={() => handleOpenDialog(user)}>
                                        <EditIcon />
                                    </IconButton>
                                    <IconButton onClick={() => handleDelete(user.id)}>
                                        <DeleteIcon />
                                    </IconButton>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>

            <UserFormDialog
                open={dialogOpen}
                onClose={handleCloseDialog}
                onSave={handleSave}
                user={editingUser}
            />
        </Box>
    );
};

export default UserManagement;