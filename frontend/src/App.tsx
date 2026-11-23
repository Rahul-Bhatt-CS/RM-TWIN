import { useCallback, useEffect, useMemo, useState } from 'react';
import type { FormEvent } from 'react';
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { api } from './api';
import Transformer3D from './Transformer3D';
import type { Alert, Dashboard, Diagnostic, Health, Page, Protection, Reading, Transformer } from './types';

type Tab='overview'|'readings'|'diagnostics'|'alerts'|'health'|'protection';
const metrics=[['WINDING_HOTSPOT_TEMPERATURE','Winding hot spot'],['TOP_OIL_TEMPERATURE','Top-oil temperature'],['LOAD_PERCENT','Load'],['AMBIENT_TEMPERATURE','Ambient temperature'],['APPARENT_POWER_MVA','Apparent power'],['HV_LINE_CURRENT_A','HV line current'],['LV_LINE_CURRENT_A','LV line current'],['COOLING_FAN_STATUS','Cooling fan status'],['COOLING_PUMP_STATUS','Cooling pump status'],['OIL_LEVEL_PERCENT','Oil level'],['RADIATOR_OUTLET_TEMPERATURE','Radiator outlet temperature'],['OLTC_TAP_POSITION','OLTC tap position'],['OLTC_OPERATION_COUNT','OLTC operation count'],['OLTC_MOTOR_CURRENT_A','OLTC motor current']] as const;
const fmt=(value:string|null|undefined)=>value?new Date(value).toLocaleString():'—';
const label=(s:string)=>s.toLowerCase().replaceAll('_',' ').replace(/\b\w/g,c=>c.toUpperCase());
function Pager({data,onPage}:{data:Page<unknown>|null,onPage:(p:number)=>void}){
 if(!data||data.totalPages<=1)return null;
 return <div className="pager"><span>Showing page {data.page+1} of {data.totalPages} · {data.totalElements} records</span><div><button disabled={data.first} onClick={()=>onPage(data.page-1)}>Previous</button><button disabled={data.last} onClick={()=>onPage(data.page+1)}>Next</button></div></div>;
}
function Status({value}:{value:string}){return <span className={`status ${value.toLowerCase()}`}>{label(value)}</span>;}
export default function App(){
 const [fleet,setFleet]=useState<Transformer[]>([]),[selected,setSelected]=useState<number|null>(null),[dashboard,setDashboard]=useState<Dashboard|null>(null);
 const [tab,setTab]=useState<Tab>('overview'),[loading,setLoading]=useState(true),[error,setError]=useState('');
 const [showCreate,setShowCreate]=useState(false);
 const [metric,setMetric]=useState<string>(metrics[0][0]);
 const [readingPage,setReadingPage]=useState<Page<Reading>|null>(null),[alertPage,setAlertPage]=useState<Page<Alert>|null>(null),[healthPage,setHealthPage]=useState<Page<Health>|null>(null),[diagnosticPage,setDiagnosticPage]=useState<Page<Diagnostic>|null>(null),[protectionPage,setProtectionPage]=useState<Page<Protection>|null>(null);
 const [diagnosticForm,setDiagnosticForm]=useState({testType:'OIL_TEST',parameterName:'MOISTURE',value:'',unit:'ppm',testDate:new Date().toISOString().slice(0,16),laboratory:'',referenceNote:'',notes:'',componentKey:'MAIN_TANK',phase:''});
 const [protectionForm,setProtectionForm]=useState({deviceName:'Buchholz relay',eventType:'ALARM',active:true,occurredAt:new Date().toISOString().slice(0,16),notes:''});
 const referenceProfile='CEA-100MVA-220-33KV-3PH';
 const [form,setForm]=useState({name:'100 MVA 220/33 kV Power Transformer',assetCode:'TR-100MVA-220-33',manufacturer:'',model:'',serialNumber:'',voltageClassKv:'220',lvVoltageKv:'33',ratedPowerMva:'100',ratedFrequencyHz:'50',referenceProfile,coolingType:'ONAN/ONAF',insulationType:'MINERAL_OIL_PAPER',commissioningDate:'',location:'',description:'Reference profile based on CEA-listed 100 MVA, 220/33 kV, three-phase power transformer rating. Cooling arrangement and detailed design data must be confirmed from OEM nameplate/GTP.',simulationEnabled:true});
 const [formError,setFormError]=useState('');
 const loadFleet=useCallback(async()=>{const p=await api.transformers(0,100);setFleet(p.content);if(selected&& !p.content.some(t=>t.id===selected))setSelected(null);},[selected]);
 const loadDashboard=useCallback((id:number)=>api.dashboard(id),[]);
 useEffect(()=>{let live=true;api.transformers(0,100).then(p=>{if(live){setFleet(p.content);setSelected(p.content[0]?.id??null);}}).catch(e=>setError(e.message)).finally(()=>{if(live)setLoading(false);});return()=>{live=false;};},[]);
 useEffect(()=>{if(selected===null){setDashboard(null);return;}let active=true;setDashboard(null);loadDashboard(selected).then(d=>{if(active)setDashboard(d);}).catch(e=>{if(active)setError(e.message);});const es=new EventSource(api.eventUrl(selected));es.addEventListener('dashboard',event=>{try{const next=JSON.parse((event as MessageEvent).data) as Dashboard;if(active)setDashboard(next);}catch{/* ignore malformed event */}});es.onerror=()=>{/* browser reconnects automatically */};return()=>{active=false;es.close();};},[selected,loadDashboard]);
 const loadTab=useCallback(async(p=0)=>{if(selected===null)return;try{
  if(tab==='readings')setReadingPage(await api.readings(selected,metric,p));
  if(tab==='alerts')setAlertPage(await api.alerts(selected,p));
  if(tab==='health')setHealthPage(await api.healthHistory(selected,p));
  if(tab==='diagnostics')setDiagnosticPage(await api.diagnostics(selected,p));
  if(tab==='protection')setProtectionPage(await api.protection(selected,p));
 }catch(e){setError(e instanceof Error?e.message:'Could not load records');}},[selected,tab,metric]);
 useEffect(()=>{if(tab!=='overview'&&selected!==null)loadTab(0);},[tab,selected,metric,loadTab]);
 const createTransformer=async(e:FormEvent)=>{e.preventDefault();setFormError('');try{
  const created=await api.createTransformer({...form,voltageClassKv:form.voltageClassKv?Number(form.voltageClassKv):null,lvVoltageKv:form.lvVoltageKv?Number(form.lvVoltageKv):null,ratedPowerMva:form.ratedPowerMva?Number(form.ratedPowerMva):null,ratedFrequencyHz:form.ratedFrequencyHz?Number(form.ratedFrequencyHz):null,referenceProfile:form.referenceProfile,commissioningDate:form.commissioningDate||null});
  await loadFleet();setSelected(created.id);setTab('overview');setShowCreate(false);setForm({name:'100 MVA 220/33 kV Power Transformer',assetCode:'TR-100MVA-220-33',manufacturer:'',model:'',serialNumber:'',voltageClassKv:'220',lvVoltageKv:'33',ratedPowerMva:'100',ratedFrequencyHz:'50',referenceProfile,coolingType:'ONAN/ONAF',insulationType:'MINERAL_OIL_PAPER',commissioningDate:'',location:'',description:'Reference profile based on CEA-listed 100 MVA, 220/33 kV, three-phase power transformer rating. Cooling arrangement and detailed design data must be confirmed from OEM nameplate/GTP.',simulationEnabled:true});
 }catch(e){setFormError(e instanceof Error?e.message:'Unable to create transformer');}};
 const addDiagnostic=async(e:FormEvent)=>{e.preventDefault();if(!selected)return;try{await api.addDiagnostic(selected,{...diagnosticForm,value:Number(diagnosticForm.value),testDate:new Date(diagnosticForm.testDate).toISOString(),phase:diagnosticForm.phase||null});setDiagnosticForm(v=>({...v,value:''}));await loadTab(0);setDashboard(await loadDashboard(selected));}catch(e){setError(e instanceof Error?e.message:'Unable to save test');}};
 const addProtection=async(e:FormEvent)=>{e.preventDefault();if(!selected)return;try{await api.addProtection(selected,{...protectionForm,occurredAt:new Date(protectionForm.occurredAt).toISOString()});await loadTab(0);}catch(e){setError(e instanceof Error?e.message:'Unable to save event');}};
 const chart=useMemo(()=>readingPage?.content.slice().reverse().map(r=>({time:new Date(r.recordedAt).toLocaleTimeString(),value:r.value,stamp:r.recordedAt}))??[],[readingPage]);
 if(loading)return <div className="center"><div className="spinner"/><p>Loading transformer workspace…</p></div>;
 return <div className="app">
  <header className="topbar"><div className="brand"><div className="brand-icon">T</div><div><b>GRIDPULSE</b><small>TRANSFORMER DIGITAL TWIN</small></div></div><div className="top-actions"><span className="sim-badge">DEMO / SIMULATED</span><button className="primary" onClick={()=>setShowCreate(v=>!v)}>＋ Add transformer</button></div></header>
  <main className="wrap">
   {error&&<div className="error">{error}<button onClick={()=>setError('')}>Dismiss</button></div>}
   {showCreate&&<section className="panel create-panel"><div className="section-title"><div><span className="eyebrow">ASSET REGISTRY</span><h2>Add transformer</h2><p>Create an asset record and optionally enable its demo simulator.</p></div><button className="ghost" onClick={()=>setShowCreate(false)}>Close</button></div>
    <form onSubmit={createTransformer} className="form-grid">
     <label>Transformer name *<input required maxLength={120} value={form.name} onChange={e=>setForm({...form,name:e.target.value})}/></label>
     <label>Asset code *<input required pattern="[A-Za-z0-9._-]{2,60}" value={form.assetCode} onChange={e=>setForm({...form,assetCode:e.target.value})}/></label>
     <label>Manufacturer<input maxLength={120} value={form.manufacturer} onChange={e=>setForm({...form,manufacturer:e.target.value})}/></label>
     <label>Model<input maxLength={120} value={form.model} onChange={e=>setForm({...form,model:e.target.value})}/></label>
     <label>Serial number<input maxLength={100} value={form.serialNumber} onChange={e=>setForm({...form,serialNumber:e.target.value})}/></label>
     <label>HV voltage (kV)<input type="number" min="0.01" max="2000" step="0.01" value={form.voltageClassKv} readOnly/></label>
     <label>LV voltage (kV)<input type="number" min="0.01" max="2000" step="0.01" value={form.lvVoltageKv} readOnly/></label>
     <label>Rated power (MVA)<input type="number" min="0.01" max="10000" step="0.01" value={form.ratedPowerMva} readOnly/></label>
     <label>Frequency (Hz)<input type="number" min="1" max="400" step="0.01" value={form.ratedFrequencyHz} readOnly/></label>
     <label>Reference profile<input readOnly value={form.referenceProfile}/></label>
     <label>Cooling type<select value={form.coolingType} disabled><option>ONAN/ONAF</option><option>ONAN</option><option>ONAF</option><option>OFAF</option><option>OFWF</option><option>OTHER</option></select></label>
     <label>Insulation type<input value={form.insulationType} onChange={e=>setForm({...form,insulationType:e.target.value})}/></label>
     <label>Commissioning date<input type="date" value={form.commissioningDate} onChange={e=>setForm({...form,commissioningDate:e.target.value})}/></label>
     <label>Location<input maxLength={180} value={form.location} onChange={e=>setForm({...form,location:e.target.value})}/></label>
     <label className="wide">Description<textarea maxLength={1000} value={form.description} onChange={e=>setForm({...form,description:e.target.value})}/></label>
     <label className="check"><input type="checkbox" checked={form.simulationEnabled} onChange={e=>setForm({...form,simulationEnabled:e.target.checked})}/> Enable demonstration simulator</label>
     {formError&&<p className="form-error wide">{formError}</p>}<div className="wide form-actions"><button className="primary">Create transformer</button></div>
    </form>
   </section>}
   <section className="fleet-head"><div><span className="eyebrow">ASSET MONITORING</span><h1>Transformer fleet</h1><p>Select a transformer to inspect its live condition, diagnostic history and alerts.</p></div><div className="fleet-count"><strong>{fleet.length.toString().padStart(2,'0')}</strong><span>REGISTERED ASSETS</span></div></section>
   {fleet.length===0?<section className="panel empty"><h3>No transformers registered</h3><p>Add a transformer to start its digital-twin workspace. The simulator creates clearly labelled demo readings when enabled.</p><button className="primary" onClick={()=>setShowCreate(true)}>Add first transformer</button></section>:<>
    <div className="fleet-grid">{fleet.map(t=><button key={t.id} className={`fleet-card ${selected===t.id?'chosen':''}`} onClick={()=>{setSelected(t.id);setTab('overview');}}>
      <div className="card-top"><span className="asset-symbol">⚡</span><Status value={selected===t.id?'SELECTED':t.simulationEnabled?'SIMULATOR ON':'MANUAL DATA'}/></div><h3>{t.name}</h3><p>{t.assetCode} · {t.ratedPowerMva?`${t.ratedPowerMva} MVA`:'Rating not entered'}</p><div className="card-bottom"><span>{t.location||'Location not specified'}</span><b>Open dashboard →</b></div>
    </button>)}</div>
    {selected!==null&&<section className="workspace">
     <div className="asset-heading"><div><span className="eyebrow">SELECTED TRANSFORMER</span><h2>{dashboard?.transformer.name||fleet.find(t=>t.id===selected)?.name}</h2><p>{dashboard?.transformer.assetCode||fleet.find(t=>t.id===selected)?.assetCode} · {dashboard?.transformer.ratedPowerMva??'—'} MVA · Updated {fmt(dashboard?.lastUpdated)}</p></div><select aria-label="Select transformer" value={selected} onChange={e=>setSelected(Number(e.target.value))}>{fleet.map(t=><option key={t.id} value={t.id}>{t.name} ({t.assetCode})</option>)}</select></div>
     <nav className="tabs">{(['overview','readings','diagnostics','alerts','health','protection'] as Tab[]).map(x=><button key={x} className={tab===x?'active':''} onClick={()=>setTab(x)}>{label(x)}</button>)}</nav>
     {tab==='overview'&&<Overview data={dashboard} onOpen={setTab}/>}
     {tab==='readings'&&<section className="panel"><div className="section-title"><div><h3>Sensor history</h3><p>Server-side paginated readings. Values are not interpolated.</p></div><select value={metric} onChange={e=>setMetric(e.target.value)}>{metrics.map(m=><option key={m[0]} value={m[0]}>{m[1]}</option>)}</select></div><div className="chart"><ResponsiveContainer width="100%" height="100%"><AreaChart data={chart}><CartesianGrid strokeDasharray="3 3"/><XAxis dataKey="time"/><YAxis/><Tooltip labelFormatter={(_,p)=>p?.[0]?.payload?.stamp?fmt(p[0].payload.stamp):''}/><Area type="monotone" dataKey="value" stroke="#188c7c" fill="#188c7c" fillOpacity={.14}/></AreaChart></ResponsiveContainer></div><ReadingsTable data={readingPage}/><Pager data={readingPage} onPage={loadTab}/></section>}
     {tab==='diagnostics'&&<section className="panel"><div className="section-title"><div><h3>Diagnostic test records</h3><p>Imported laboratory or field-test results; not simulated live readings.</p></div></div><form className="inline-form" onSubmit={addDiagnostic}><label>Test type<input value={diagnosticForm.testType} onChange={e=>setDiagnosticForm({...diagnosticForm,testType:e.target.value})}/></label><label>Parameter<input value={diagnosticForm.parameterName} onChange={e=>setDiagnosticForm({...diagnosticForm,parameterName:e.target.value.toUpperCase()})}/></label><label>Value<input type="number" step="any" required value={diagnosticForm.value} onChange={e=>setDiagnosticForm({...diagnosticForm,value:e.target.value})}/></label><label>Unit<input required value={diagnosticForm.unit} onChange={e=>setDiagnosticForm({...diagnosticForm,unit:e.target.value})}/></label><label>Component<select value={diagnosticForm.componentKey} onChange={e=>setDiagnosticForm({...diagnosticForm,componentKey:e.target.value})}><option value="MAIN_TANK">Main tank / active part</option><option value="CONSERVATOR">Conservator / oil system</option><option value="HV_BUSHING">HV bushing</option><option value="LV_BUSHING">LV bushing</option><option value="RADIATOR">Radiator bank</option><option value="COOLING_FAN">Cooling fan</option><option value="OLTC">OLTC</option><option value="CONTROL_CABINET">Control cabinet</option></select></label><label>Phase (optional)<select value={diagnosticForm.phase} onChange={e=>setDiagnosticForm({...diagnosticForm,phase:e.target.value})}><option value="">Not phase-specific</option><option>A</option><option>B</option><option>C</option><option>a</option><option>b</option><option>c</option></select></label><label>Test date<input type="datetime-local" required value={diagnosticForm.testDate} onChange={e=>setDiagnosticForm({...diagnosticForm,testDate:e.target.value})}/></label><label>Laboratory<input value={diagnosticForm.laboratory} onChange={e=>setDiagnosticForm({...diagnosticForm,laboratory:e.target.value})}/></label><label className="wide">Reference / method note<input value={diagnosticForm.referenceNote} onChange={e=>setDiagnosticForm({...diagnosticForm,referenceNote:e.target.value})}/></label><label className="wide">Notes<input value={diagnosticForm.notes} onChange={e=>setDiagnosticForm({...diagnosticForm,notes:e.target.value})}/></label><button className="primary">Add diagnostic record</button></form><DataTable headers={['DATE','TEST','PARAMETER','COMPONENT','PHASE','VALUE','UNIT','SOURCE']} rows={(diagnosticPage?.content??[]).map(d=>[fmt(d.testDate),d.testType,d.parameterName,d.componentKey||'—',d.phase||'—',d.value,d.unit,d.source])}/><Pager data={diagnosticPage} onPage={loadTab}/></section>}
     {tab==='alerts'&&<section className="panel"><div className="section-title"><div><h3>Degradation alerts</h3><p>Alerts are generated from configured demonstration rules.</p></div></div><DataTable headers={['CREATED','ALERT','DETAIL','SEVERITY','STATUS','OBSERVED']} rows={(alertPage?.content??[]).map(a=>[fmt(a.createdAt),a.title,a.message,a.severity,a.status,a.observedValue===null?'—':`${a.observedValue} ${a.unit??''}`])}/><Pager data={alertPage} onPage={loadTab}/></section>}
     {tab==='health'&&<section className="panel"><div className="section-title"><div><h3>Health assessment history</h3><p>Explainable demo score with coverage; not an engineering certification.</p></div></div><DataTable headers={['ASSESSED','STATUS','INDEX','COVERAGE','EXPLANATION']} rows={(healthPage?.content??[]).map(h=>[fmt(h.assessedAt),h.status,h.healthIndex===null?'Not calculated':h.healthIndex.toFixed(2),`${(h.coverage*100).toFixed(0)}%`,h.explanation])}/><Pager data={healthPage} onPage={loadTab}/></section>}
     {tab==='protection'&&<section className="panel"><div className="section-title"><div><h3>Protection and auxiliary events</h3><p>Recorded status/events only. This application does not control protective equipment.</p></div></div><form className="inline-form" onSubmit={addProtection}><label>Device<input value={protectionForm.deviceName} onChange={e=>setProtectionForm({...protectionForm,deviceName:e.target.value})}/></label><label>Event type<input value={protectionForm.eventType} onChange={e=>setProtectionForm({...protectionForm,eventType:e.target.value})}/></label><label>Occurred at<input type="datetime-local" value={protectionForm.occurredAt} onChange={e=>setProtectionForm({...protectionForm,occurredAt:e.target.value})}/></label><label className="check"><input type="checkbox" checked={protectionForm.active} onChange={e=>setProtectionForm({...protectionForm,active:e.target.checked})}/> Active event</label><label className="wide">Notes<input value={protectionForm.notes} onChange={e=>setProtectionForm({...protectionForm,notes:e.target.value})}/></label><button className="primary">Record event</button></form><DataTable headers={['DATE','DEVICE','EVENT','ACTIVE','SOURCE','NOTES']} rows={(protectionPage?.content??[]).map(p=>[fmt(p.occurredAt),p.deviceName,p.eventType,p.active?'Yes':'No',p.source,p.notes||'—'])}/><Pager data={protectionPage} onPage={loadTab}/></section>}
    </section>}
   </>}
   <footer>GridPulse Transformer Digital Twin · Demonstration software · Not a protection, trip-control or operational safety system.</footer>
  </main>
 </div>;
}
function Overview({data,onOpen}:{data:Dashboard|null,onOpen:(tab:Tab)=>void}){
 if(!data)return <div className="panel empty">Waiting for the first dashboard assessment…</div>;
 const h=data.health;const readings=data.latestReadings;
 return <div className="overview">
  <section className="health-hero"><div><span className="eyebrow light">CONDITION ASSESSMENT · DEMONSTRATION</span><h2>{h?.healthIndex===null||!h?'Health index unavailable':`${h.healthIndex.toFixed(2)} / 100`}</h2><p>{h?.explanation||'Waiting for sufficient measurements to assess condition.'}</p></div><div className="hero-state"><Status value={h?.status||'UNAVAILABLE'}/><small>Coverage {h?`${(h.coverage*100).toFixed(0)}%`:'—'}</small></div></section>
  <Transformer3D data={data}/>
  <div className="metric-cards">{readings.map(r=><div className="metric" key={r.id}><small>{label(r.measurementType)}</small><strong>{r.value} <i>{r.unit}</i></strong><span>{r.source} · {fmt(r.recordedAt)}</span></div>)}</div>
  <div className="split">
   <section className="panel"><div className="section-title"><div><h3>Health components</h3><p>Scores are calculated only where inputs exist.</p></div></div>{h?.components.map(c=><div className="component" key={c.key}><div><b>{c.label}</b><span>{c.score===null?'Unknown':`${c.score}/100`} · {c.state}</span></div><div className="bar"><i style={{width:`${c.score??0}%`}}/></div><p>{c.explanation}</p></div>)}</section>
   <section className="panel"><div className="section-title"><div><h3>Monitoring shortcuts</h3><p>Open detailed, paginated records.</p></div></div><div className="shortcut-list"><button onClick={()=>onOpen('readings')}>Sensor reading history <b>→</b></button><button onClick={()=>onOpen('diagnostics')}>Oil and diagnostic tests <b>→</b></button><button onClick={()=>onOpen('alerts')}>Degradation alerts <b>{data.activeAlerts} active →</b></button><button onClick={()=>onOpen('health')}>Health assessment history <b>→</b></button><button onClick={()=>onOpen('protection')}>Protection events <b>→</b></button></div></section>
  </div>
  <p className="disclaimer">Health index is a transparent demonstration calculation using available readings and imported tests. Missing tests reduce coverage; they are never treated as healthy. Thresholds and weights require transformer-specific engineering validation.</p>
 </div>;
}
function DataTable({headers,rows}:{headers:string[],rows:(string|number)[][]}){
 return <div className="table-wrap"><table><thead><tr>{headers.map(h=><th key={h}>{h}</th>)}</tr></thead><tbody>{rows.length===0?<tr><td colSpan={headers.length} className="no-data">No records available.</td></tr>:rows.map((r,i)=><tr key={i}>{r.map((v,j)=><td key={j}>{v}</td>)}</tr>)}</tbody></table></div>;
}
function ReadingsTable({data}:{data:Page<Reading>|null}){
 return <DataTable headers={['TIMESTAMP','METRIC','VALUE','UNIT','SOURCE']} rows={(data?.content??[]).map(r=>[fmt(r.recordedAt),label(r.measurementType),r.value,r.unit,r.source])}/>;
}
