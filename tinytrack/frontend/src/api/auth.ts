import { apiClient } from "./client";
import type { AuthResponse, User } from "@/types";

export interface RegisterRequest {
  email: string;
  password: string;
  name: string;
  preferredLanguage?: "EN" | "HU";
}

export interface LoginRequest {
  email: string;
  password: string;
}

export const authApi = {
  register: (data: RegisterRequest) =>
    apiClient.post<AuthResponse>("/auth/register", data).then((r) => r.data),

  login: (data: LoginRequest) =>
    apiClient.post<AuthResponse>("/auth/login", data).then((r) => r.data),

  logout: (refreshToken: string) =>
    apiClient.post("/auth/logout", { refreshToken }),

  me: () =>
    apiClient.get<User>("/users/me").then((r) => r.data),

  updateMe: (data: Partial<User> & { currentPassword?: string; newPassword?: string }) =>
    apiClient.put<User>("/users/me", data).then((r) => r.data),
};
