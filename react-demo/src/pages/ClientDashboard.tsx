
import React, { useState, useEffect } from 'react';
import { User, Device } from '../types';
import { api } from '../services/apiService';
import {
    Box,
    AppBar,
    Toolbar,
    Typography,
    Button,
    Container,
    CircularProgress,
    List,
    ListItem,
    ListItemText
} from '@mui/material';

interface ClientDashboardProps {
    user: User;
    onLogout: () => void;
}

const ClientDashboard: React.FC<ClientDashboardProps> = ({ user, onLogout }) => {
    const [devices, setDevices] = useState<Device[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchDevices = async () => {
            try {
                setLoading(true);
                const userDevices = await api.getDevicesByPersonId(user.id);
                setDevices(userDevices);
            } catch (error) {
                console.error("Failed to fetch devices:", error);
            }
            setLoading(false);
        };

        fetchDevices();
    }, [user.id]);

    return (
        <Box sx={{ flexGrow: 1 }}>

            <AppBar position="static">
                <Toolbar>
                    <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
                        ⚡ My Devices
                    </Typography>
                    <Typography sx={{ mr: 2 }}>
                        Welcome, {user.name}
                    </Typography>
                    <Button color="inherit" onClick={onLogout}>
                        Logout
                    </Button>
                </Toolbar>
            </AppBar>


            <Container maxWidth="lg" sx={{ mt: 4 }}>
                <Typography variant="h4" gutterBottom>Your Assigned Devices</Typography>
                {loading ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                        <CircularProgress />
                    </Box>
                ) : devices.length === 0 ? (
                    <Typography>
                        You have no devices assigned. Please contact an administrator.
                    </Typography>
                ) : (
                    <List sx={{ bgcolor: 'background.paper', borderRadius: 2 }}>
                        {devices.map((device) => (
                            <ListItem key={device.id} divider>
                                <ListItemText
                                    primary={device.name}
                                    secondary={`Max Consumption: ${device.maxConsumption} kWh`}
                                />
                            </ListItem>
                        ))}
                    </List>
                )}
            </Container>
        </Box>
    );
};

export default ClientDashboard;