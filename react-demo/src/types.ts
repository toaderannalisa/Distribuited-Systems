// src/types.ts

// Pe baza enum-ului PersonRole.java
export enum PersonRole {
    ADMIN = 'ADMIN',
    CLIENT = 'CLIENT'
}

// Pe baza răspunsului de la /auth/me din AuthController.java
export interface User {
    id: string;
    username: string;
    name: string;
    role: PersonRole;
    age: number;
}

// Pe baza PersonDTO/PersonDetailsDTO din PersonController.java
export interface Person {
    id: string;
    name: string;
    username: string;
    address: string;
    age: number;
    role: PersonRole;
}

// Pe baza DeviceDTO/DeviceDetailsDTO din DeviceController.java
export interface Device {
    id: string;
    name: string;
    maxConsumption: number;
    personId: string | null;
}

// Pe baza răspunsului de la /api/energy/measurements/{deviceId}/hourly
export interface EnergyConsumption {
    hour: string;  // Ora în format "HH:00"
    averageValue: number;  // Media consumului pentru acea oră
    count: number;  // Numărul de măsurători agregate
}