// src/pages/AdminDashboard.tsx
import React, { useState } from 'react';
import { User } from '../types';
import {
    Box,
    AppBar,
    Toolbar,
    Typography,
    Button,
    Container,
    Tabs,
    Tab
} from '@mui/material';

import UserManagement from '../components/UserManagement';
import DeviceManagement from '../components/DeviceManagement';

interface AdminDashboardProps {
    user: User;
    onLogout: () => void;
}

const AdminDashboard: React.FC<AdminDashboardProps> = ({ user, onLogout }) => {
    const [tabIndex, setTabIndex] = useState(0);

    const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
        setTabIndex(newValue);
    };

    return (
        <Box sx={{ flexGrow: 1 }}>
            <AppBar position="static">
                <Toolbar>
                    <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
                        ⚡ Admin Dashboard
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
                <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
                    <Tabs value={tabIndex} onChange={handleTabChange}>
                        <Tab label="User Management" />
                        <Tab label="Device Management" />
                    </Tabs>
                </Box>

                {tabIndex === 0 && (
                    <UserManagement />
                )}


                {tabIndex === 1 && (
                    <DeviceManagement />
                )}
            </Container>
        </Box>
    );
};

export default AdminDashboard;