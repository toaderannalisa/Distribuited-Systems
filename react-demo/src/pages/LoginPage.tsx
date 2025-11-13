
import React, { useState } from 'react';
import { api } from '../services/apiService';
import { User } from '../types';
import {
    Container,
    Box,
    Typography,
    TextField,
    Button,
    CircularProgress,
    Alert
} from '@mui/material';

interface LoginProps {
    onLogin: (user: User) => void;
}

const LoginPage: React.FC<LoginProps> = ({ onLogin }) => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const userData = await api.login(username, password);
            onLogin(userData);
        } catch (err) {
            setError('Invalid username or password.');
            console.error('Login error:', err);
        }
        setLoading(false);
    };

    return (
        <Container maxWidth="xs">
            <Box
                sx={{
                    marginTop: 8,
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                }}
            >
                <Typography component="h1" variant="h4" gutterBottom>
                     Energy Management
                </Typography>
                <Typography component="p" variant="h6" sx={{ mb: 3 }}>
                    Sign In
                </Typography>

                <Box component="form" onSubmit={handleSubmit} noValidate sx={{ width: '100%' }}>
                    {error && (
                        <Alert severity="error" sx={{ mb: 2, width: '100%' }}>
                            {error}
                        </Alert>
                    )}

                    <TextField
                        margin="normal"
                        required
                        fullWidth
                        id="username"
                        label="Username"
                        name="username"
                        autoComplete="username"
                        autoFocus
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        disabled={loading}
                    />
                    <TextField
                        margin="normal"
                        required
                        fullWidth
                        name="password"
                        label="Password"
                        type="password"
                        id="password"
                        autoComplete="current-password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        disabled={loading}
                    />
                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        size="large"
                        sx={{ mt: 3, mb: 2, py: 1.5 }}
                        disabled={loading}
                    >
                        {loading ? <CircularProgress size={26} color="inherit" /> : 'Sign In'}
                    </Button>
                </Box>
            </Box>
        </Container>
    );
};

export default LoginPage;