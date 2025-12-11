import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    Box,
    Paper,
    Typography,
    CircularProgress,
    Alert,
    Button,
    TextField,
    Grid,
    AppBar,
    Toolbar,
    Container,
    Chip
} from '@mui/material';
import { ArrowBack as ArrowBackIcon, Wifi as WifiIcon, WifiOff as WifiOffIcon } from '@mui/icons-material';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { api } from '../services/apiService';
import { Device } from '../types';
import { WEBSOCKET_URL } from '../config';

interface EnergyData {
    timestamp: string;
    consumption: number;
}

interface HourlyData {
    hour: string;
    average: number;
    count: number;
}

const DeviceChartPage: React.FC = () => {
    const { deviceId } = useParams<{ deviceId: string }>();
    const navigate = useNavigate();
    const [device, setDevice] = useState<Device | null>(null);
    const [energyData, setEnergyData] = useState<EnergyData[]>([]);
    const [hourlyData, setHourlyData] = useState<HourlyData[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [wsConnected, setWsConnected] = useState(false);
    const stompClientRef = useRef<Client | null>(null);
    const [startDate, setStartDate] = useState(() => {
        const date = new Date();
        date.setDate(date.getDate() - 7);
        return date.toISOString().split('T')[0];
    });
    const [endDate, setEndDate] = useState(() => new Date().toISOString().split('T')[0]);

    const handleLogout = () => {
        api.logout();
        navigate('/login');
    };

    const fetchDeviceData = async () => {
        if (!deviceId) return;

        try {
            setLoading(true);
            setError('');
            const deviceData = await api.getDeviceById(deviceId);
            setDevice(deviceData);
        } catch (err: any) {
            console.error('Failed to fetch device:', err);
            setError(err.response?.data?.message || 'Failed to fetch device data');
        } finally {
            setLoading(false);
        }
    };

    const fetchEnergyData = async () => {
        if (!deviceId) return;

        try {
            setError('');
            const data = await api.getEnergyConsumption(deviceId, startDate, endDate);
            
            // Backend returns hourly aggregated data: {hour: "2025-12-08 21:00:00", averageValue: 0.95, count: 12}
            const transformed = data.map((item: any) => {
                // Parse hour timestamp
                let dateObj;
                if (Array.isArray(item.hour)) {
                    const [year, month, day, hour, minute, second] = item.hour;
                    dateObj = new Date(year, month - 1, day, hour, minute, second);
                } else if (typeof item.hour === 'string') {
                    dateObj = new Date(item.hour);
                } else {
                    dateObj = new Date();
                }
                
                return {
                    hour: dateObj.toLocaleString('en-US', {
                        month: 'short',
                        day: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit'
                    }),
                    average: item.averageValue || item.average || 0,
                    count: item.count || 0
                };
            });
            
            setHourlyData(transformed);
        } catch (err: any) {
            console.error('Failed to fetch energy data:', err);
            setError(err.response?.data?.message || 'Failed to fetch energy consumption data');
        }
    };

    useEffect(() => {
        fetchDeviceData();
    }, [deviceId]);

    useEffect(() => {
        if (device) {
            fetchEnergyData();
        }
    }, [device, startDate, endDate]);

    useEffect(() => {
        if (!deviceId) return;

        // Create WebSocket connection
        const client = new Client({
            webSocketFactory: () => new SockJS(WEBSOCKET_URL),
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            onConnect: () => {
                console.log('WebSocket connected');
                setWsConnected(true);
                
                // Subscribe to device energy updates
                client.subscribe(`/topic/energy/device/${deviceId}`, (message) => {
                    try {
                        const data = JSON.parse(message.body);
                        console.log('Received real-time data:', data);
                        
                        // Add raw measurement
                        setEnergyData(prev => {
                            const newPoint = {
                                timestamp: new Date(data.timestamp).toLocaleString(),
                                consumption: data.consumption
                            };
                            return [...prev, newPoint].slice(-50);
                        });

                        // Update hourly averages in real-time
                        if (data.hourlyAverage !== null && data.hourlyAverage !== undefined) {
                            setHourlyData(prev => {
                                const hourLabel = `Hour ${data.hourIndex}`;
                                
                                // Find existing hour or add new one
                                const existingIndex = prev.findIndex(item => item.hour === hourLabel);
                                
                                if (existingIndex >= 0) {
                                    // Update existing hour with new average
                                    const updated = [...prev];
                                    updated[existingIndex] = {
                                        hour: hourLabel,
                                        average: data.hourlyAverage,
                                        count: data.hourlyCount
                                    };
                                    return updated;
                                } else {
                                    // Add new hour
                                    return [...prev, {
                                        hour: hourLabel,
                                        average: data.hourlyAverage,
                                        count: data.hourlyCount
                                    }].slice(-24); // Keep last 24 "hours"
                                }
                            });
                        }
                    } catch (err) {
                        console.error('Error parsing WebSocket message:', err);
                    }
                });
            },
            onDisconnect: () => {
                console.log('WebSocket disconnected');
                setWsConnected(false);
            },
            onStompError: (frame) => {
                console.error('STOMP error:', frame);
                setWsConnected(false);
            }
        });

        client.activate();
        stompClientRef.current = client;

        return () => {
            if (stompClientRef.current) {
                stompClientRef.current.deactivate();
            }
        };
    }, [deviceId]);

    if (loading) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                <CircularProgress />
            </Box>
        );
    }

    if (!device) {
        return (
            <Box sx={{ p: 3 }}>
                <Alert severity="error">Device not found</Alert>
                <Button
                    startIcon={<ArrowBackIcon />}
                    onClick={() => navigate(-1)}
                    sx={{ mt: 2 }}
                >
                    Go Back
                </Button>
            </Box>
        );
    }

    return (
        <Box sx={{ flexGrow: 1 }}>
            <AppBar position="static">
                <Toolbar>
                    <Button 
                        color="inherit" 
                        startIcon={<ArrowBackIcon />}
                        onClick={() => navigate(-1)}
                        sx={{ mr: 2 }}
                    >
                        Back
                    </Button>
                    <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
                        ⚡ Energy Chart
                    </Typography>
                    <Chip 
                        icon={wsConnected ? <WifiIcon /> : <WifiOffIcon />}
                        label={wsConnected ? "Live" : "Offline"}
                        color={wsConnected ? "success" : "default"}
                        sx={{ mr: 2 }}
                    />
                    <Button color="inherit" onClick={handleLogout}>
                        Logout
                    </Button>
                </Toolbar>
            </AppBar>

            <Container maxWidth="lg" sx={{ mt: 4 }}>
            <Paper sx={{ p: 3, mb: 3 }}>
                <Typography variant="h4" gutterBottom>
                    {device.name}
                </Typography>
                <Typography variant="body1" color="text.secondary">
                    Device ID: {device.id}
                </Typography>
                <Typography variant="body1" color="text.secondary">
                    Max Consumption: {device.maxConsumption} kWh
                </Typography>
            </Paper>

            <Paper sx={{ p: 3 }}>
                <Typography variant="h5" gutterBottom>
                    Energy Consumption Chart
                </Typography>

                <Grid container spacing={2} sx={{ mb: 3 }}>
                    <Grid size={{ xs: 12, md: 5 }}>
                        <TextField
                            label="Start Date"
                            type="date"
                            value={startDate}
                            onChange={(e) => setStartDate(e.target.value)}
                            InputLabelProps={{ shrink: true }}
                            fullWidth
                        />
                    </Grid>
                    <Grid size={{ xs: 12, md: 5 }}>
                        <TextField
                            label="End Date"
                            type="date"
                            value={endDate}
                            onChange={(e) => setEndDate(e.target.value)}
                            InputLabelProps={{ shrink: true }}
                            fullWidth
                        />
                    </Grid>
                    <Grid size={{ xs: 12, md: 2 }}>
                        <Button
                            variant="contained"
                            onClick={fetchEnergyData}
                            fullWidth
                            sx={{ height: '56px' }}
                        >
                            Refresh
                        </Button>
                    </Grid>
                </Grid>

                {error && (
                    <Alert severity="error" sx={{ mb: 2 }}>
                        {error}
                    </Alert>
                )}

                {/* Hourly Average Chart - ONLY CHART */}
                {hourlyData.length === 0 ? (
                    <Alert severity="info">
                        No hourly data available for the selected period
                    </Alert>
                ) : (
                    <ResponsiveContainer width="100%" height={500}>
                        <LineChart data={hourlyData}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis
                                dataKey="hour"
                                label={{ value: 'Hour', position: 'insideBottom', offset: -5 }}
                            />
                            <YAxis
                                label={{ value: 'Avg Consumption (kWh)', angle: -90, position: 'insideLeft' }}
                            />
                            <Tooltip 
                                formatter={(value: any, name: string, props: any) => {
                                    if (name === 'average') {
                                        return [`${value.toFixed(2)} kWh (${props.payload.count}/10 measurements)`, 'Hourly Average'];
                                    }
                                    return value;
                                }}
                            />
                            <Legend />
                            <Line
                                type="monotone"
                                dataKey="average"
                                stroke="#82ca9d"
                                strokeWidth={2}
                                activeDot={{ r: 8 }}
                                name="Hourly Average Consumption"
                            />
                        </LineChart>
                    </ResponsiveContainer>
                )}
            </Paper>
            </Container>
        </Box>
    );
};

export default DeviceChartPage;
