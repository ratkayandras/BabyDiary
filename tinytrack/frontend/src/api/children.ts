import { apiClient } from "./client";
import type {
  Child, Measurement, FeedingLog, DiaperLog, SleepLog, GrowthAnalysis,
  Gender, MeasurementType, FeedingType, BreastSide, DiaperType,
} from "@/types";

export const childrenApi = {
  list: () => apiClient.get<Child[]>("/children").then((r) => r.data),
  get: (id: string) => apiClient.get<Child>(`/children/${id}`).then((r) => r.data),
  create: (data: {
    familyId: string;
    name: string;
    dateOfBirth: string;
    gender: Gender;
    photoUrl?: string;
    feedingReminderHours?: number;
  }) => apiClient.post<Child>("/children", data).then((r) => r.data),
  update: (id: string, data: Partial<{
    name: string;
    dateOfBirth: string;
    gender: Gender;
    photoUrl: string;
    feedingReminderHours: number;
  }>) => apiClient.put<Child>(`/children/${id}`, data).then((r) => r.data),
  delete: (id: string) => apiClient.delete(`/children/${id}`),

  // Measurements
  listMeasurements: (childId: string) =>
    apiClient.get<Measurement[]>(`/children/${childId}/measurements`).then((r) => r.data),
  addMeasurement: (childId: string, data: {
    type: MeasurementType;
    value: number;
    recordedAt: string;
    inputUnit?: string;
    notes?: string;
  }) => apiClient.post<Measurement>(`/children/${childId}/measurements`, data).then((r) => r.data),
  deleteMeasurement: (childId: string, measurementId: string) =>
    apiClient.delete(`/children/${childId}/measurements/${measurementId}`),

  // Feeding logs
  listFeedingLogs: (childId: string, limit = 50) =>
    apiClient.get<FeedingLog[]>(`/children/${childId}/feeding-logs`, { params: { limit } }).then((r) => r.data),
  addFeedingLog: (childId: string, data: {
    type: FeedingType;
    startTime: string;
    endTime?: string;
    amountMl?: number;
    side?: BreastSide;
    notes?: string;
  }) => apiClient.post<FeedingLog>(`/children/${childId}/feeding-logs`, data).then((r) => r.data),
  deleteFeedingLog: (childId: string, logId: string) =>
    apiClient.delete(`/children/${childId}/feeding-logs/${logId}`),

  // Diaper logs
  listDiaperLogs: (childId: string, limit = 50) =>
    apiClient.get<DiaperLog[]>(`/children/${childId}/diaper-logs`, { params: { limit } }).then((r) => r.data),
  addDiaperLog: (childId: string, data: {
    recordedAt: string;
    type: DiaperType;
    notes?: string;
  }) => apiClient.post<DiaperLog>(`/children/${childId}/diaper-logs`, data).then((r) => r.data),
  deleteDiaperLog: (childId: string, logId: string) =>
    apiClient.delete(`/children/${childId}/diaper-logs/${logId}`),

  // Sleep logs
  listSleepLogs: (childId: string, limit = 50) =>
    apiClient.get<SleepLog[]>(`/children/${childId}/sleep-logs`, { params: { limit } }).then((r) => r.data),
  addSleepLog: (childId: string, data: {
    startTime: string;
    endTime?: string;
    notes?: string;
  }) => apiClient.post<SleepLog>(`/children/${childId}/sleep-logs`, data).then((r) => r.data),
  deleteSleepLog: (childId: string, logId: string) =>
    apiClient.delete(`/children/${childId}/sleep-logs/${logId}`),

  // Growth analysis
  growthAnalysis: (childId: string) =>
    apiClient.get<GrowthAnalysis>(`/children/${childId}/growth-analysis`).then((r) => r.data),

  // Export (returns blob)
  exportData: (childId: string, format: "pdf" | "csv") =>
    apiClient.get(`/children/${childId}/export`, {
      params: { format },
      responseType: "blob",
    }),
};

export const familiesApi = {
  list: () => apiClient.get("/families").then((r) => r.data),
  create: (name: string) => apiClient.post("/families", { name }).then((r) => r.data),
  get: (id: string) => apiClient.get(`/families/${id}`).then((r) => r.data),
  invite: (familyId: string, email: string) =>
    apiClient.post(`/families/${familyId}/invite`, { email }),
};
