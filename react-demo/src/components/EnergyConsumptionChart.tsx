import React, { useState, useEffect, useRef } from 'react';
import {
    Box,
    Card,
    CardContent,
    CardHeader,
    Grid,
    TextField,
    ToggleButton,
    ToggleButtonGroup,
    CircularProgress,
    Alert,
    Chip
} from '@mui/material';
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { format } from 'date-fns';
import { api } from '../services/apiService';
import { Device, EnergyConsumption } from '../types';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { WEBSOCKET_URL } from '../config';

interface EnergyChartProps {
    device: Device;
}

export const EnergyConsumptionChart: React.FC<EnergyChartProps> = ({ device }) => {
    const [selectedDate, setSelectedDate] = useState<string>(format(new Date(), 'yyyy-MM-dd'));
    const [chartType, setChartType] = useState<'line' | 'bar'>('line');
    const [data, setData] = useState<EnergyConsumption[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [realtimeValue, setRealtimeValue] = useState<number | null>(null);
    const [connected, setConnected] = useState(false);
    const stompClientRef = useRef<Client | null>(null);

    useEffect(() => {
        fetchEnergyData();
    }, [selectedDate, device.id]);

    useEffect(() => {
        // WebSocket connection
        const socket = new SockJS(WEBSOCKET_URL);
        const stompClient = new Client({
            webSocketFactory: () => socket as any,
            debug: (str) => console.log('STOMP:', str),
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        stompClient.onConnect = () => {
            console.log('WebSocket Connected');
            setConnected(true);
            
            stompClient.subscribe(`/topic/energy/device/${device.id}`, (message) => {
                try {
                    const update = JSON.parse(message.body);
                    console.log('Realtime update:', update);
                    setRealtimeValue(update.measurementValue);
                    
                    // Refresh data if date matches today
                    const today = format(new Date(), 'yyyy-MM-dd');
                    if (selectedDate === today) {
                        fetchEnergyData();
                    }
                } catch (err) {
                    console.error('Failed to parse WebSocket message:', err);
                }
            });
        };

        stompClient.onDisconnect = () => {
            console.log('WebSocket Disconnected');
            setConnected(false);
        };

        stompClient.activate();
        stompClientRef.current = stompClient;

        return () => {
            if (stompClientRef.current) {
                stompClientRef.current.deactivate();
            }
        };
    }, [device.id, selectedDate]);

    const fetchEnergyData = async () => {
        setLoading(true);
        setError(null);
        try {
            const startDate = format(new Date(selectedDate), 'yyyy-MM-dd');
            const endDate = format(new Date(selectedDate), 'yyyy-MM-dd');
            
            // Fetch HOURLY AVERAGES instead of all raw measurements
            const response = await fetch(
                `http://localhost/api/energy/measurements/${device.id}/hourly?startDate=${startDate}&endDate=${endDate}`
            );
            
            if (!response.ok) {
                throw new Error('Failed to fetch energy data');
            }
            
            const energyData = await response.json();
            
            // Ensure energyData is an array
            if (Array.isArray(energyData)) {
                setData(energyData);
            } else {
                console.warn('Energy data is not an array:', energyData);
                setData([]);
            }
        } catch (err) {
            setError('Failed to load energy consumption data');
            console.error(err);
            setData([]);
        } finally {
            setLoading(false);
        }
    };

    const chartData = Array.isArray(data) ? data.map(item => ({
        time: item.hour,
        measurementValue: Number(item.averageValue)
    })) : [];

    return (
        <Card>
            <CardHeader
                title={`Energy Consumption - ${device.name}`}
                subheader={`Device ID: ${device.id}`}
                action={
                    <Box display="flex" gap={1} alignItems="center">
                        <Chip 
                            label={connected ? '🟢 Live' : '🔴 Offline'} 
                            color={connected ? 'success' : 'error'}
                            size="small"
                        />
                        {realtimeValue !== null && (
                            <Chip 
                                label={`Current: ${realtimeValue.toFixed(2)} kWh`}
                                color="primary"
                                size="small"
                            />
                        )}
                    </Box>
                }
            />
            <CardContent>
                <Grid container spacing={2} sx={{ mb: 3 }}>
                    <Grid size={{ xs: 12, sm: 6 }}>
                        <TextField
                            type="date"
                            value={selectedDate}
                            onChange={(e) => setSelectedDate(e.target.value)}
                            InputLabelProps={{ shrink: true }}
                            fullWidth
                        />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 6 }}>
                        <ToggleButtonGroup
                            value={chartType}
                            exclusive
                            onChange={(e, newType) => {
                                if (newType !== null) {
                                    setChartType(newType);
                                }
                            }}
                            fullWidth
                        >
                            <ToggleButton value="line">Line Chart</ToggleButton>
                            <ToggleButton value="bar">Bar Chart</ToggleButton>
                        </ToggleButtonGroup>
                    </Grid>
                </Grid>

                {error && <Alert severity="error">{error}</Alert>}

                {loading ? (
                    <Box display="flex" justifyContent="center" py={4}>
                        <CircularProgress />
                    </Box>
                ) : (
                    <ResponsiveContainer width="100%" height={400}>
                        {chartType === 'line' ? (
                            <LineChart data={chartData}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="time" />
                                <YAxis label={{ value: 'Energy (kWh)', angle: -90, position: 'insideLeft' }} />
                                <Tooltip formatter={(value) => Number(value).toFixed(2)} />
                                <Legend />
                                <Line type="monotone" dataKey="measurementValue" stroke="#8884d8" name="Measurement" />
                            </LineChart>
                        ) : (
                            <BarChart data={chartData}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="time" />
                                <YAxis label={{ value: 'Energy (kWh)', angle: -90, position: 'insideLeft' }} />
                                <Tooltip formatter={(value) => Number(value).toFixed(2)} />
                                <Legend />
                                <Bar dataKey="measurementValue" fill="#8884d8" name="Measurement" />
                            </BarChart>
                        )}
                    </ResponsiveContainer>
                )}

                {chartData.length === 0 && !loading && (
                    <Alert severity="info">No energy data available for the selected date</Alert>
                )}
            </CardContent>
        </Card>
    );
};
