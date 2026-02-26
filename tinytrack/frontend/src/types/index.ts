export type Language = "EN" | "HU";
export type Gender = "MALE" | "FEMALE" | "OTHER";
export type FamilyRole = "OWNER" | "MEMBER";
export type MeasurementType = "WEIGHT" | "HEIGHT" | "HEAD_CIRCUMFERENCE";
export type FeedingType = "BREAST" | "FORMULA" | "SOLID";
export type BreastSide = "LEFT" | "RIGHT" | "BOTH";
export type DiaperType = "WET" | "DIRTY" | "BOTH" | "DRY";

export interface User {
  id: string;
  email: string;
  name: string;
  preferredLanguage: Language;
  createdAt: string;
}

export interface FamilyMember {
  userId: string;
  name: string;
  email: string;
  role: FamilyRole;
  joinedAt: string;
}

export interface Family {
  id: string;
  name: string;
  createdAt: string;
  members: FamilyMember[];
}

export interface Child {
  id: string;
  familyId: string;
  name: string;
  dateOfBirth: string;
  gender: Gender;
  photoUrl: string | null;
  feedingReminderHours: number;
  createdAt: string;
}

export interface Measurement {
  id: string;
  childId: string;
  type: MeasurementType;
  value: number;
  unit: string;
  recordedAt: string;
  notes: string | null;
  createdAt: string;
}

export interface FeedingLog {
  id: string;
  childId: string;
  type: FeedingType;
  startTime: string;
  endTime: string | null;
  amountMl: number | null;
  side: BreastSide | null;
  notes: string | null;
  createdAt: string;
}

export interface DiaperLog {
  id: string;
  childId: string;
  recordedAt: string;
  type: DiaperType;
  notes: string | null;
  createdAt: string;
}

export interface SleepLog {
  id: string;
  childId: string;
  startTime: string;
  endTime: string | null;
  notes: string | null;
  createdAt: string;
}

export interface WhoBand {
  month: number;
  p3: number;
  p15: number;
  p50: number;
  p85: number;
  p97: number;
}

export interface MeasurementPoint {
  ageMonths: number;
  value: number;
  unit: string;
  percentile: number | null;
}

export interface GrowthSeries {
  measurements: MeasurementPoint[];
  bands: WhoBand[];
}

export interface GrowthAnalysis {
  childId: string;
  childName: string;
  weight: GrowthSeries;
  height: GrowthSeries;
  headCircumference: GrowthSeries;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  user: User;
}

export interface TokenResponse {
  accessToken: string;
  tokenType: string;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  fieldErrors?: Record<string, string>;
}
