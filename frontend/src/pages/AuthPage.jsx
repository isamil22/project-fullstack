// PATH: frontend/src/pages/AuthPage.jsx

import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
// --- 1. IMPORT THE NEW confirmEmail FUNCTION ---
import { loginUser, registerUser, getUserProfile, confirmEmail } from '../api/apiService';

const AuthPage = ({ setIsAuthenticated }) => {
    // --- 2. ADD A 'confirm' MODE ---
    const [mode, setMode] = useState('login'); // 'login', 'signup', or 'confirm'
    const [formData, setFormData] = useState({
        fullName: '',
        email: '',
        password: '',
        confirmationCode: '' // --- 3. ADD confirmationCode to state ---
    });
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
        setError('');
        setSuccess('');
    };

    const handleLoginSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        try {
            const loginData = { email: formData.email, password: formData.password };
            const response = await loginUser(loginData);
            localStorage.setItem('token', response.data);

            setIsAuthenticated(true);
            setSuccess('Login successful! Redirecting...');

            const profileResponse = await getUserProfile();
            const userRole = profileResponse.data.role;

            if (userRole === 'ADMIN') {
                navigate('/admin/dashboard');
            } else {
                navigate('/');
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.response?.data || 'An error occurred.';
            // Provide specific feedback for unconfirmed users
            if (errorMessage.includes("disabled")) {
                setError("Your account is not enabled. Please confirm your email.");
                setMode('confirm'); // Switch to confirmation form
            } else {
                setError(errorMessage);
            }
            setIsAuthenticated(false);
        }
    };

    const handleRegisterSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        try {
            await registerUser({ fullName: formData.fullName, email: formData.email, password: formData.password });
            setSuccess('Registration successful! Please check your email for a confirmation code, then enter it below.');
            setMode('confirm'); // Switch to confirm mode after registration
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.response?.data || 'Registration failed.';
            setError(errorMessage);
        }
    };

    // --- 4. CREATE A HANDLER FOR THE CONFIRMATION SUBMIT ---
    const handleConfirmSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        try {
            await confirmEmail({ email: formData.email, confirmationCode: formData.confirmationCode });
            setSuccess('Email confirmed successfully! You can now log in.');
            setMode('login'); // Switch to login mode
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.response?.data || 'Confirmation failed.';
            setError(errorMessage);
        }
    };

    const switchMode = (newMode) => {
        setMode(newMode);
        setError('');
        setSuccess('');
        setFormData({ fullName: '', email: '', password: '', confirmationCode: '' });
    };

    // Determine which form submission handler to use
    const handleSubmit = (e) => {
        if (mode === 'login') {
            handleLoginSubmit(e);
        } else if (mode === 'signup') {
            handleRegisterSubmit(e);
        } else {
            handleConfirmSubmit(e);
        }
    };

    return (
        <div className="flex items-center justify-center min-h-full py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-md w-full space-y-8 bg-white p-10 rounded-xl shadow-lg">
                <div>
                    <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
                        {mode === 'login' && 'Sign in to your account'}
                        {mode === 'signup' && 'Create a new account'}
                        {mode === 'confirm' && 'Confirm your Email'}
                    </h2>
                </div>
                <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
                    {error && <p className="text-red-500 text-center">{error}</p>}
                    {success && <p className="text-green-500 text-center">{success}</p>}

                    <div className="rounded-md shadow-sm">
                        {/* --- Render form fields based on the current mode --- */}

                        {mode === 'signup' && (
                            <div className="mb-2">
                                <label htmlFor="full-name" className="sr-only">Full name</label>
                                <input id="full-name" name="fullName" type="text" required className="appearance-none rounded-md relative block w-full px-3 py-2 border border-gray-300" placeholder="Full name" value={formData.fullName} onChange={handleChange} />
                            </div>
                        )}

                        {(mode === 'signup' || mode === 'login' || mode === 'confirm') && (
                            <div className="mb-2">
                                <label htmlFor="email-address" className="sr-only">Email address</label>
                                <input id="email-address" name="email" type="email" required className="appearance-none rounded-md relative block w-full px-3 py-2 border border-gray-300" placeholder="Email address" value={formData.email} onChange={handleChange} />
                            </div>
                        )}

                        {(mode === 'signup' || mode === 'login') && (
                            <div>
                                <label htmlFor="password" className="sr-only">Password</label>
                                <input id="password" name="password" type="password" required className="appearance-none rounded-md relative block w-full px-3 py-2 border border-gray-300" placeholder="Password" value={formData.password} onChange={handleChange} />
                            </div>
                        )}

                        {mode === 'confirm' && (
                            <div>
                                <label htmlFor="confirmationCode" className="sr-only">Confirmation Code</label>
                                <input id="confirmationCode" name="confirmationCode" type="text" required className="appearance-none rounded-md relative block w-full px-3 py-2 border border-gray-300" placeholder="Confirmation Code" value={formData.confirmationCode} onChange={handleChange} />
                            </div>
                        )}
                    </div>

                    {mode === 'login' && (
                        <div className="flex items-center justify-between">
                            <div className="text-sm">
                                <Link to="/forgot-password" className="font-medium text-pink-600 hover:text-pink-500">
                                    Forgot your password?
                                </Link>
                            </div>
                        </div>
                    )}

                    <div>
                        <button type="submit" className="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-pink-600 hover:bg-pink-700">
                            {mode === 'login' && 'Sign in'}
                            {mode === 'signup' && 'Register'}
                            {mode === 'confirm' && 'Confirm'}
                        </button>
                    </div>
                </form>

                <div className="text-sm text-center">
                    {mode === 'login' && <button onClick={() => switchMode('signup')} className="font-medium text-pink-600 hover:text-pink-500">Don't have an account? Sign Up</button>}
                    {mode === 'signup' && <button onClick={() => switchMode('login')} className="font-medium text-pink-600 hover:text-pink-500">Already have an account? Sign In</button>}
                    {mode === 'confirm' && <button onClick={() => switchMode('login')} className="font-medium text-pink-600 hover:text-pink-500">Back to Login</button>}
                </div>
            </div>
        </div>
    );
};

export default AuthPage;