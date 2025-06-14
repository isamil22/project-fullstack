import React from 'react';
import { Outlet } from 'react-router-dom';
import AdminSidebar from './AdminSidebar';

const AdminLayout = () => {
    return (
        <div className="flex min-h-screen">
            <AdminSidebar />
            <main className="flex-grow bg-gray-100">
                <Outlet />
            </main>
        </div>
    );
};

export default AdminLayout;