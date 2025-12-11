
import axios from 'axios';
import { PERSON_API_URL, DEVICE_API_URL, MONITORING_API_URL } from '../config';
import { User, Person, Device, PersonRole } from '../types';


const personApi = axios.create({
    baseURL: PERSON_API_URL
});

const deviceApi = axios.create({
    baseURL: DEVICE_API_URL
});

const monitoringApi = axios.create({
    baseURL: MONITORING_API_URL
});

// Add auth interceptor to all instances
[personApi, deviceApi, monitoringApi].forEach(api => {
    api.interceptors.request.use((config) => {
        const auth = localStorage.getItem('auth');
        if (auth) {
            config.headers.Authorization = auth;
        }
        return config;
    });
});


const login = async (username: string, password: string): Promise<User> => {
    // Store Basic Auth header
    const authHeader = 'Basic ' + btoa(username + ':' + password);
    localStorage.setItem('auth', authHeader);
    
    // Get current user info from backend using the /auth/me endpoint
    const { data } = await personApi.get<User>('/auth/me');
    
    return data;
};

const logout = () => {
    localStorage.removeItem('auth');
    localStorage.removeItem('user');
};

const getMe = async (): Promise<User> => {
    const { data } = await personApi.get<User>('/auth/me');
    return data;
};


const getPeople = async (): Promise<Person[]> => {
    const { data } = await personApi.get<Person[]>('/people');
    return data;
};

const getPersonById = async (id: string): Promise<Person> => {
    const { data } = await personApi.get<Person>(`/people/${id}`);
    return data;
};


const registerPerson = async (personData: Omit<Person, 'id'>) => {
    const { data } = await personApi.post('/auth/register', personData);
    return data;
};

const updatePerson = async (id: string, personData: Partial<Person>) => {
    const { data } = await personApi.put(`/people/${id}`, personData);
    return data;
};

const deletePerson = async (id: string) => {
    await personApi.delete(`/people/${id}`);
};


const getDevices = async (): Promise<Device[]> => {
    const { data } = await deviceApi.get<Device[]>('/devices');
    return data;
};

const getDevicesByPersonId = async (personId: string): Promise<Device[]> => {
    const { data } = await deviceApi.get<Device[]>(`/devices/person/${personId}`);
    return data;
};

const getDeviceById = async (id: string): Promise<Device> => {
    const { data } = await deviceApi.get<Device>(`/devices/${id}`);
    return data;
};

const createDevice = async (deviceData: Omit<Device, 'id'>) => {
    const { data } = await deviceApi.post('/devices', deviceData);
    return data;
};

const updateDevice = async (id: string, deviceData: Partial<Device>) => {
    const { data } = await deviceApi.put(`/devices/${id}`, deviceData);
    return data;
};

const deleteDevice = async (id: string) => {
    await deviceApi.delete(`/devices/${id}`);
};

const getEnergyConsumption = async (deviceId: string, startDate: string, endDate: string) => {
    const { data } = await monitoringApi.get(`/api/energy/measurements/${deviceId}/hourly`, {
        params: { startDate, endDate }
    });
    return data;
};



export const api = {
    login,
    logout,
    getMe,
    getPeople,
    getPersonById,
    registerPerson,
    updatePerson,
    deletePerson,
    getDevices,
    getDevicesByPersonId,
    getDeviceById,
    createDevice,
    updateDevice,
    deleteDevice,
    getEnergyConsumption
};