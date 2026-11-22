export type Reading = {
  id: number;
  transformerId: number;
  measurementType: 'OIL_TEMPERATURE' | 'HOTSPOT_TEMPERATURE' | 'LOAD';
  value: number;
  unit: string;
  recordedAt: string;
  source: 'SIMULATED';
};
export type Health = {
  id: number;
  transformerId: number;
  healthStatus: 'NORMAL' | 'WARNING' | 'CRITICAL' | 'UNAVAILABLE';
  score: number | null;
  explanation: string;
  assessedAt: string;
};
export type Alert = {
  id: number;
  transformerId: number;
  condition: string;
  severity: string | null;
  observedValue: number | null;
  thresholdValue: number | null;
  unit: string | null;
  status: string | null;
  createdAt: string;
};
export type Transformer = { id: number; name: string; assetCode: string | null; rating: string | null };
export type Dashboard = {
  transformerId: number;
  assetName: string;
  source: string;
  health: Health | null;
  lastUpdated: string | null;
  readings: Reading[];
  activeAlertCount: number | null;
};
export type HistoryResponse = { transformerId: number; from: string; to: string; readings: Reading[] };
export type HealthHistoryResponse = { transformerId: number; assessments: Health[] };
export type AlertsResponse = { transformerId: number; alerts: Alert[] };
export type LiveEvent = { eventType: string; dashboard: Dashboard; alert: Alert | null };
