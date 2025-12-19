// --- MODELOS DE USUÁRIO ---


export enum AlertType {
  TELEGRAM = 'TELEGRAM',
  EMAIL = 'EMAIL',
  SMS = 'SMS'
}
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
  roles: string[];
  alertTypes: AlertType[];
}

export interface UserUpdateRequest {
  email?: string;
  phoneNumber?: string;
  cameraConnectionUrl?: string;
  password?: string;
  alertPreferences?: AlertType[]; // 📍 NOVO
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
  createdAt: string; // ISO Date String
  snapshotUrl?: string; // Opcional: nem todo alerta tem foto

  // 📍 NOVOS CAMPOS DE GEOLOCALIZAÇÃO
  // Devem ser opcionais (?) pois alertas antigos ou câmeras sem GPS virão nulos
  latitude?: number;
  longitude?: number;
  address?: string; // Endereço legível (Ex: "Rua X, Centro")
}

export interface UserProfileDTO {
  id: string;
  username: string;
  email: string;
  phoneNumber: string;
  cameraConnectionUrl: string;
  alertPreferences: AlertType[];
}

export interface Page<T> {
  content: T[];
  page: {          // 👈 Agora os metadados ficam aqui dentro
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
  };
}
