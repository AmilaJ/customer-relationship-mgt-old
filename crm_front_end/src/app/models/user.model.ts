export interface User {
  id?: number;
  username: string;
  email: string;
  passwordHash?: string;
  role: string;
  createdAt?: string;
  updatedAt?: string;
  active?: boolean;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  message: string;
  token?: string;
  user?: User;
} 