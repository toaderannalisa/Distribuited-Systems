
import axios from 'axios';
import { API_BASE_URL } from '../config';
import { User, Person, Device } from '../types';


const apiService = axios.create({
    baseURL: API_BASE_URL
});


apiService.interceptors.request.use((config) => {
    const auth = localStorage.getItem('auth');
    if (auth) {
        config.headers.Authorization = auth;
    }
    return config;
});


const login = async (username: string, password: string): Promise<User> => {
    const authHeader = 'Basic ' + btoa(username + ':' + password);

    localStorage.setItem('auth', authHeader);

    const { data } = await apiService.get<User>('/auth/me', {
        headers: {
            'Authorization': authHeader
        }
    });
    return data;
};

const logout = () => {
    localStorage.removeItem('auth');
    localStorage.removeItem('user');
};

const getMe = async (): Promise<User> => {
    const { data } = await apiService.get<User>('/auth/me');
    return data;
};


const getPeople = async (): Promise<Person[]> => {
    const { data } = await apiService.get<Person[]>('/people');
    return data;
};

const getPersonById = async (id: string): Promise<Person> => {
    const { data } = await apiService.get<Person>(`/people/${id}`);
    return data;
};


const registerPerson = async (personData: Omit<Person, 'id'>) => {
    const { data } = await apiService.post('/auth/register', personData);
    return data;
};

const updatePerson = async (id: string, personData: Partial<Person>) => {
    const { data } = await apiService.put(`/people/${id}`, personData);
    return data;
};

const deletePerson = async (id: string) => {
    await apiService.delete(`/people/${id}`);
};


const getDevices = async (): Promise<Device[]> => {
    const { data } = await apiService.get<Device[]>('/devices');
    return data;
};

const getDevicesByPersonId = async (personId: string): Promise<Device[]> => {
    const { data } = await apiService.get<Device[]>(`/devices/person/${personId}`);
    return data;
};

const getDeviceById = async (id: string): Promise<Device> => {
    const { data } = await apiService.get<Device>(`/devices/${id}`);
    return data;
};

const createDevice = async (deviceData: Omit<Device, 'id'>) => {
    const { data } = await apiService.post('/devices', deviceData);
    return data;
};

const updateDevice = async (id: string, deviceData: Partial<Device>) => {
    const { data } = await apiService.put(`/devices/${id}`, deviceData);
    return data;
};

const deleteDevice = async (id: string) => {
    await apiService.delete(`/devices/${id}`);
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
    deleteDevice
};