/**
 * Supported notification channels for the SafeVision ecosystem.
 * Matches the AlertType enum in the Common Java module.
 */
export enum AlertType {
  TELEGRAM = 'TELEGRAM',
  EMAIL = 'EMAIL',
  SMS = 'SMS'
}

/**
 * Core User entity representation in the frontend.
 */
export interface User {
  id: string;
  username: string;
  email?: string;
  phoneNumber?: string;
  cameraConnectionUrl?: string;
  roles: string[];
}

/**
 * Credentials payload for the authentication process.
 */
export interface LoginRequest {
  username: string;
  password: string;
}

/**
 * Payload for new user account creation.
 * Follows the ADR-011 specification for initial setup.
 */
export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  phoneNumber: string;
  cameraUrl: string;
  roles: string[];
  alertTypes: AlertType[];
}

/**
 * Payload for updating existing user profiles.
 * All fields are optional to allow partial updates (Patch-like behavior).
 */
export interface UserUpdateRequest {
  email?: string;
  phoneNumber?: string;
  cameraConnectionUrl?: string;
  password?: string;
  alertPreferences?: AlertType[];
}

/**
 * Response received after successful JWT authentication.
 */
export interface AuthResponse {
  token: string;
}

/**
 * Basic user information returned by the Auth Service.
 */
export interface UserResponse {
  id: string;
  username: string;
  roles: string[];
}

/**
 * Security alert event received from the Alert Service.
 * Includes geolocation data for physical response mapping.
 */
export interface Alert {
  id: string;
  alertType: string;
  description: string;
  severity: 'INFO' | 'WARNING' | 'CRITICAL';
  cameraId: string;
  acknowledged: boolean;
  /** ISO Date String */
  createdAt: string; 
  /** MinIO public URL for evidence image */
  snapshotUrl?: string; 
  latitude?: number;
  longitude?: number;
  /** Human-readable location provided by Reverse Geocoding or Edge Agent */
  address?: string; 
}

/**
 * Detailed profile view for the User Settings dashboard.
 */
export interface UserProfileDTO {
  id: string;
  username: string;
  email: string;
  phoneNumber: string;
  cameraConnectionUrl: string;
  alertPreferences: AlertType[];
}

/**
 * Generic interface for Spring Data paginated responses.
 * @template T The type of the content list.
 */
export interface Page<T> {
  content: T[];
  page: {
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
  };
}