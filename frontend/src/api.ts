import type { Alert, AlertsResponse, Dashboard, HealthHistoryResponse, HistoryResponse, Transformer } from './types';

const API = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api';

async function request<T>(path: string): Promise<T> {
  const response = await fetch(`${API}${path}`);
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.message ?? `Request failed (${response.status})`);
  }
  return response.json() as Promise<T>;
}

export const api = {
  transformers: () => request<Transformer[]>('/transformers'),
  dashboard: (id: number) => request<Dashboard>(`/transformers/${id}/dashboard`),
  alerts: (id: number) => request<AlertsResponse>(`/transformers/${id}/alerts`),
  history: (id: number, from: string, to: string, metric: string) =>
    request<HistoryResponse>(`/transformers/${id}/history?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&measurementType=${metric}`),
  healthHistory: (id: number) => request<HealthHistoryResponse>(`/transformers/${id}/health/history`),
  eventUrl: (id: number) => `${API}/transformers/${id}/events`
};
