import { useCallback, useEffect, useMemo, useState } from 'react';
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { api } from './api';
import type { Alert, Dashboard, HistoryResponse, Transformer } from './types';

type Screen = 'overview' | 'alerts' | 'history';
const metricLabels: Record<string, string> = {
  OIL_TEMPERATURE: 'Oil temperature',
  HOTSPOT_TEMPERATURE: 'Winding hot-spot',
  LOAD: 'Load'
};
const timeLabel = (value?: string | null) => value ? new Date(value).toLocaleString() : 'Unavailable';
const pretty = (value: string) => value.replaceAll('_', ' ').toLowerCase().replace(/\\b\\w/g, c => c.toUpperCase());

export default function App() {
  const [screen, setScreen] = useState<Screen>('overview');
  const [transformer, setTransformer] = useState<Transformer | null>(null);
  const [dashboard, setDashboard] = useState<Dashboard | null>(null);
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [history, setHistory] = useState<HistoryResponse | null>(null);
  const [healthHistory, setHealthHistory] = useState<import('./types').Health[]>([]);
  const [metric, setMetric] = useState('HOTSPOT_TEMPERATURE');
  const [range, setRange] = useState('24h');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [historyError, setHistoryError] = useState('');
  const [liveState, setLiveState] = useState('Connecting');

  const loadDashboard = useCallback(async (id: number) => {
    const [snapshot, alertResponse] = await Promise.all([api.dashboard(id), api.alerts(id)]);
    setDashboard(snapshot);
    setAlerts(alertResponse.alerts);
  }, []);

  useEffect(() => {
    let active = true;
    api.transformers().then(async list => {
      if (!active) return;
      const first = list[0];
      if (!first) throw new Error('No transformer is configured.');
      setTransformer(first);
      await loadDashboard(first.id);
      if (active) { setLoading(false); setError(''); }
    }).catch(e => { if (active) { setError(e.message); setLoading(false); } });
    return () => { active = false; };
  }, [loadDashboard]);

  useEffect(() => {
    if (!transformer) return;
    const source = new EventSource(api.eventUrl(transformer.id));
    source.onopen = () => setLiveState('Live');
    source.addEventListener('twin-update', event => {
      try {
        const payload = JSON.parse((event as MessageEvent).data);
        if (payload.dashboard) setDashboard(payload.dashboard);
        api.alerts(transformer.id).then(r => setAlerts(r.alerts)).catch(() => undefined);
      } catch { /* Ignore malformed event; next REST snapshot remains authoritative. */ }
    });
    source.onerror = () => setLiveState('Reconnecting');
    return () => source.close();
  }, [transformer]);

  const loadHistory = useCallback(async () => {
    if (!transformer) return;
    const now = new Date();
    const from = new Date(now.getTime() - (range === '1h' ? 3600000 : range === '7d' ? 7 * 86400000 : 86400000));
    setHistoryError('');
    try {
      const [readings, health] = await Promise.all([
        api.history(transformer.id, from.toISOString(), now.toISOString(), metric),
        api.healthHistory(transformer.id)
      ]);
      setHistory(readings);
      setHealthHistory(health.assessments);
    } catch (e) { setHistoryError(e instanceof Error ? e.message : 'Unable to load history.'); }
  }, [transformer, range, metric]);

  useEffect(() => { if (screen === 'history') loadHistory(); }, [screen, loadHistory]);

  const chartData = useMemo(() => (history?.readings ?? []).slice().reverse().map(r => ({
    time: new Date(r.recordedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    value: r.value,
    timestamp: r.recordedAt
  })), [history]);

  const retry = () => {
    if (!transformer) return;
    setLoading(true);
    loadDashboard(transformer.id).then(() => { setLoading(false); setError(''); })
      .catch(e => { setError(e.message); setLoading(false); });
  };

  const latest = useMemo(() => {
    const map = new Map((dashboard?.readings ?? []).map(r => [r.measurementType, r]));
    return ['OIL_TEMPERATURE', 'HOTSPOT_TEMPERATURE', 'LOAD'].flatMap(k => {
      const value = map.get(k as import('./types').Reading['measurementType']);
      return value ? [value] : [];
    });
  }, [dashboard]);

  if (loading && !dashboard) return <div className="center-state"><div className="spinner" /><p>Loading transformer twin…</p></div>;
  if (error && !dashboard) return <div className="center-state"><h2>Unable to load the transformer</h2><p>{error}</p><button onClick={retry}>Retry</button></div>;

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand"><div className="brand-mark">T</div><div><strong>TRANSFORMER TWIN</strong><small>Asset intelligence workspace</small></div></div>
        <div className="topbar-right"><span className="source-pill">● SIMULATED DATA</span><span className={`connection ${liveState === 'Live' ? 'connected' : ''}`}>● {liveState}</span></div>
      </header>
      <div className="layout">
        <aside className="sidebar">
          <span className="nav-label">WORKSPACE</span>
          <button className={screen === 'overview' ? 'nav-item active' : 'nav-item'} onClick={() => setScreen('overview')}>▦ <span>Overview</span></button>
          <button className={screen === 'alerts' ? 'nav-item active' : 'nav-item'} onClick={() => setScreen('alerts')}>◉ <span>Alerts</span><b>{alerts.filter(a => a.status === 'ACTIVE').length || ''}</b></button>
          <button className={screen === 'history' ? 'nav-item active' : 'nav-item'} onClick={() => setScreen('history')}>◷ <span>History</span></button>
          <div className="sidebar-footer"><span className="asset-dot" /> <div><strong>{transformer?.name ?? 'Transformer'}</strong><small>{transformer?.assetCode ?? 'DEMO ASSET'}</small></div></div>
        </aside>
        <main className="content">
          {error && <div className="inline-error">{error} <button onClick={retry}>Retry</button></div>}
          {screen === 'overview' && <Overview dashboard={dashboard} alerts={alerts} latest={latest} onHistory={() => setScreen('history')} onAlerts={() => setScreen('alerts')} />}
          {screen === 'alerts' && <AlertsScreen alerts={alerts} />}
          {screen === 'history' && <HistoryScreen metric={metric} setMetric={setMetric} range={range} setRange={setRange} history={history} healthHistory={healthHistory} chartData={chartData} error={historyError} reload={loadHistory} />}
        </main>
      </div>
    </div>
  );
}

function Overview({ dashboard, alerts, latest, onHistory, onAlerts }: {
  dashboard: Dashboard | null; alerts: Alert[]; latest: Dashboard['readings']; onHistory: () => void; onAlerts: () => void;
}) {
  const status = dashboard?.health?.healthStatus ?? 'UNAVAILABLE';
  return <div className="page">
    <div className="page-heading"><div><p className="eyebrow">ASSET MONITORING / TRANSFORMER</p><h1>Transformer overview</h1><p className="muted">Live condition monitoring and operational telemetry.</p></div><div className="updated">Last updated<strong>{timeLabel(dashboard?.lastUpdated)}</strong></div></div>
    <section className="health-banner">
      <div><span className="eyebrow light">CURRENT HEALTH STATUS</span><h2 className={`status-${status.toLowerCase()}`}>{pretty(status)}</h2><p>{dashboard?.health?.explanation ?? 'Health has not been assessed yet. Waiting for sufficient simulated readings.'}</p></div>
      <div className="health-side"><span className="health-icon">✦</span><small>HEALTH ASSESSMENT</small><strong>{timeLabel(dashboard?.health?.assessedAt)}</strong></div>
    </section>
    <div className="section-heading"><div><h2>Live sensor readings</h2><p>Latest simulated measurements from the transformer.</p></div><span className="live-tag">● LIVE FEED</span></div>
    <div className="metric-grid">
      {['OIL_TEMPERATURE','HOTSPOT_TEMPERATURE','LOAD'].map(type => {
        const reading = latest.find(r => r.measurementType === type);
        return <article className="metric-card" key={type}><div className="metric-top"><span>{metricLabels[type]}</span><span className="metric-dot" /></div><div className="metric-value">{reading ? Number(reading.value).toFixed(1) : '—'}<small>{reading?.unit ?? ''}</small></div><div className="metric-foot"><span>{reading ? 'Latest reading' : 'No data yet'}</span><span>{reading ? new Date(reading.recordedAt).toLocaleTimeString() : 'Unavailable'}</span></div></article>;
      })}
    </div>
    <div className="two-col">
      <section className="panel"><div className="panel-heading"><div><h3>Health assessment</h3><p>Current rule-based assessment</p></div><span className={`status-chip ${status.toLowerCase()}`}>{pretty(status)}</span></div><div className="assessment-copy"><div className="assessment-mark">↗</div><div><strong>{dashboard?.health?.explanation ?? 'Assessment unavailable'}</strong><p>Health status is derived from the configured demo rules. It is not a predictive diagnostic model.</p></div></div><button className="text-button" onClick={onHistory}>Explore historical data →</button></section>
      <section className="panel"><div className="panel-heading"><div><h3>Recent alerts</h3><p>Degradation events detected</p></div><button className="text-button" onClick={onAlerts}>View all →</button></div>
        {alerts.length === 0 ? <div className="empty-state"><span>✓</span><strong>No alerts yet</strong><p>No degradation conditions have been recorded.</p></div> :
          <div className="alert-preview">{alerts.slice(0,3).map(a => <div className="alert-preview-row" key={a.id}><span className={`severity-dot ${(a.severity ?? 'warning').toLowerCase()}`} /><div><strong>{pretty(a.condition)}</strong><small>{timeLabel(a.createdAt)}</small></div><span className={`status-chip ${(a.status ?? 'active').toLowerCase()}`}>{a.status}</span></div>)}</div>}
      </section>
    </div>
    <p className="disclaimer">Prototype telemetry is simulated. Thresholds are illustrative demo triggers, not transformer operating limits.</p>
  </div>;
}

function AlertsScreen({ alerts }: { alerts: Alert[] }) {
  const [filter, setFilter] = useState('ALL');
  const filtered = alerts.filter(a => filter === 'ALL' || a.status === filter);
  return <div className="page"><div className="page-heading"><div><p className="eyebrow">MONITORING / EVENTS</p><h1>Alerts</h1><p className="muted">Review conditions detected by the simulated health assessment.</p></div></div>
    <section className="panel table-panel"><div className="table-toolbar"><div><h3>Alert log</h3><p>{filtered.length} events</p></div><select value={filter} onChange={e => setFilter(e.target.value)}><option value="ALL">All alerts</option><option value="ACTIVE">Active</option><option value="RESOLVED">Resolved</option></select></div>
      {filtered.length === 0 ? <div className="empty-state"><span>✓</span><strong>No alerts found</strong><p>There are no alerts for this filter.</p></div> :
      <div className="table-scroll"><table><thead><tr><th>CONDITION</th><th>SEVERITY</th><th>OBSERVED VALUE</th><th>TRIGGER VALUE</th><th>STATUS</th><th>CREATED</th></tr></thead><tbody>
        {filtered.map(a => <tr key={a.id}><td><strong>{pretty(a.condition)}</strong><small>Transformer #{a.transformerId}</small></td><td><span className={`status-chip ${(a.severity ?? 'warning').toLowerCase()}`}>{a.severity ?? '—'}</span></td><td>{a.observedValue ?? '—'} {a.unit}</td><td>{a.thresholdValue ?? '—'} {a.unit}</td><td>{a.status ?? '—'}</td><td>{timeLabel(a.createdAt)}</td></tr>)}
      </tbody></table></div>}
    </section>
  </div>;
}

function HistoryScreen({ metric, setMetric, range, setRange, history, healthHistory, chartData, error, reload }: {
  metric: string; setMetric: (v: string) => void; range: string; setRange: (v: string) => void;
  history: HistoryResponse | null; healthHistory: import('./types').Health[]; chartData: {time:string;value:number;timestamp:string}[]; error: string; reload: () => void;
}) {
  return <div className="page"><div className="page-heading"><div><p className="eyebrow">MONITORING / HISTORICAL DATA</p><h1>History</h1><p className="muted">Explore previous simulated readings over time.</p></div></div>
    <section className="panel history-panel"><div className="filter-bar"><label>MEASUREMENT<select value={metric} onChange={e => setMetric(e.target.value)}><option value="HOTSPOT_TEMPERATURE">Winding hot-spot</option><option value="OIL_TEMPERATURE">Oil temperature</option><option value="LOAD">Load</option></select></label><label>TIME RANGE<select value={range} onChange={e => setRange(e.target.value)}><option value="1h">Last hour</option><option value="24h">Last 24 hours</option><option value="7d">Last 7 days</option></select></label><button onClick={reload}>Refresh data</button></div>
      <div className="chart-heading"><div><h3>{metricLabels[metric]}</h3><p>{history?.readings.length ?? 0} recorded samples · {history ? `${timeLabel(history.from)} – ${timeLabel(history.to)}` : 'Select a range'}</p></div></div>
      {error ? <div className="error-state"><strong>Unable to load history</strong><p>{error}</p><button onClick={reload}>Retry</button></div> :
      chartData.length === 0 ? <div className="empty-state"><span>◷</span><strong>No historical readings</strong><p>There are no readings for the selected metric and time range yet.</p></div> :
      <div className="chart-wrap"><ResponsiveContainer width="100%" height="100%"><AreaChart data={chartData}><defs><linearGradient id="fill" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#55b7a4" stopOpacity={0.28}/><stop offset="100%" stopColor="#55b7a4" stopOpacity={0.02}/></linearGradient></defs><CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e8edf2"/><XAxis dataKey="time" tickLine={false} axisLine={false}/><YAxis width={48} tickLine={false} axisLine={false}/><Tooltip labelFormatter={(_, p) => p?.[0]?.payload?.timestamp ? timeLabel(p[0].payload.timestamp) : ''}/><Area type="monotone" dataKey="value" stroke="#279b88" strokeWidth={2.5} fill="url(#fill)" dot={false}/></AreaChart></ResponsiveContainer></div>}
      <div className="history-table"><h3>Reading records</h3>{(history?.readings ?? []).length === 0 ? <p className="muted">No records to display.</p> : <div className="table-scroll"><table><thead><tr><th>TIMESTAMP</th><th>METRIC</th><th>VALUE</th><th>UNIT</th><th>SOURCE</th></tr></thead><tbody>{history!.readings.map(r => <tr key={r.id}><td>{timeLabel(r.recordedAt)}</td><td>{metricLabels[r.measurementType]}</td><td>{r.value}</td><td>{r.unit}</td><td><span className="source-pill">SIMULATED</span></td></tr>)}</tbody></table></div>}</div>
      <div className="history-table"><h3>Health assessment history</h3>{healthHistory.length === 0 ? <p className="muted">No health assessments are available yet.</p> : <div className="table-scroll"><table><thead><tr><th>ASSESSED AT</th><th>HEALTH STATUS</th><th>EXPLANATION</th></tr></thead><tbody>{healthHistory.slice(0, 100).map(h => <tr key={h.id}><td>{timeLabel(h.assessedAt)}</td><td><span className={`status-chip ${h.healthStatus.toLowerCase()}`}>{pretty(h.healthStatus)}</span></td><td>{h.explanation}</td></tr>)}</tbody></table></div>}</div>
    </section>
  </div>;
}
