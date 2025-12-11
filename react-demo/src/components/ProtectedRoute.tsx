// Salvează acesta ca src/components/ProtectedRoute.tsx

import React from 'react';
import { Navigate } from 'react-router-dom';
import { User, PersonRole } from '../types';

interface ProtectedRouteProps {
    user: User | null;
    requiredRole: PersonRole;
    children: React.ReactElement;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ user, requiredRole, children }) => {


    if (!user) {

        return <Navigate to="/login" replace />;
    }


    if (user.role !== requiredRole) {

        return <Navigate to="/" replace />;
    }


    return children;
};

export default ProtectedRoute;