// --- MODELOS DE USUÁRIO ---

export interface User {
  id: string;
  username: string;
  email?: string;
  phoneNumber?: string;
  cameraConnectionUrl?: string;
  roles: string[];
}



export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  phoneNumber: string;
  cameraUrl: string;
}

export interface UserUpdateRequest {
  email?: string;
  phoneNumber?: string;
  cameraConnectionUrl?: string;
}

// --- MODELOS DE RESPOSTA (RESPONSES) ---

export interface AuthResponse {
  token: string;
}

export interface UserResponse {
  id: string;
  username: string;
  roles: string[];
}



export interface Alert {
  id: string;
  alertType: string;
  description: string;
  severity: 'INFO' | 'WARNING' | 'CRITICAL';
  cameraId: string;
  acknowledged: boolean;
  createdAt: string;
  snapshotUrl?: string
}
