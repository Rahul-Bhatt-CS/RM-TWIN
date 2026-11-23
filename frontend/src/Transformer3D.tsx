import { useEffect, useRef, useState } from 'react';
import type { PointerEvent as ReactPointerEvent } from 'react';
import { api } from './api';
import type { Dashboard, Diagnostic, Protection } from './types';

type PartKey = 'THERMAL'|'OIL_DGA'|'INSULATION'|'MECHANICAL'|'PROTECTION';
type Part = { id:string; name:string; group:PartKey; kind:'box'|'cylinder'; x:number;y:number;z:number; w:number;h:number;d:number; color:string; description:string };
const PARTS:Part[]=[
 {id:'main-tank',name:'Main tank',group:'OIL_DGA',kind:'box',x:0,y:0.95,z:0,w:2.6,h:1.8,d:1.55,color:'#78939a',description:'Main tank containing the core, windings and insulating oil. Thermal readings are shown here as an asset-level view.'},
 {id:'conservator',name:'Conservator',group:'OIL_DGA',kind:'cylinder',x:0.15,y:2.55,z:-0.05,w:1.65,h:.42,d:.48,color:'#9bb0b4',description:'Oil expansion vessel. Review oil condition, moisture and dissolved-gas test records.'},
 {id:'radiator-left',name:'Radiator bank',group:'THERMAL',kind:'box',x:-1.62,y:1.05,z:0,w:.22,h:1.5,d:1.22,color:'#52747d',description:'Cooling radiator bank. Abnormal thermal trends or cooling-system issues should be investigated.'},
 {id:'radiator-right',name:'Radiator bank',group:'THERMAL',kind:'box',x:1.62,y:1.05,z:0,w:.22,h:1.5,d:1.22,color:'#52747d',description:'Cooling radiator bank. Abnormal thermal trends or cooling-system issues should be investigated.'},
 {id:'hv-bushing-a',name:'HV bushing A',group:'INSULATION',kind:'cylinder',x:-.72,y:2.15,z:-.35,w:.25,h:1.0,d:.25,color:'#c9b78d',description:'High-voltage bushing. Inspect insulation condition and available capacitance / tan-delta test records.'},
 {id:'hv-bushing-b',name:'HV bushing B',group:'INSULATION',kind:'cylinder',x:0,y:2.15,z:-.35,w:.25,h:1.0,d:.25,color:'#c9b78d',description:'High-voltage bushing. Inspect insulation condition and available capacitance / tan-delta test records.'},
 {id:'hv-bushing-c',name:'HV bushing C',group:'INSULATION',kind:'cylinder',x:.72,y:2.15,z:-.35,w:.25,h:1.0,d:.25,color:'#c9b78d',description:'High-voltage bushing. Inspect insulation condition and available capacitance / tan-delta test records.'},
 {id:'lv-bushing-a',name:'LV bushing A',group:'INSULATION',kind:'cylinder',x:-.72,y:2.05,z:.48,w:.2,h:.78,d:.2,color:'#d5c7a5',description:'Low-voltage bushing. Review insulation and condition-test history.'},
 {id:'lv-bushing-b',name:'LV bushing B',group:'INSULATION',kind:'cylinder',x:0,y:2.05,z:.48,w:.2,h:.78,d:.2,color:'#d5c7a5',description:'Low-voltage bushing. Review insulation and condition-test history.'},
 {id:'lv-bushing-c',name:'LV bushing C',group:'INSULATION',kind:'cylinder',x:.72,y:2.05,z:.48,w:.2,h:.78,d:.2,color:'#d5c7a5',description:'Low-voltage bushing. Review insulation and condition-test history.'},
 {id:'tap-changer',name:'Tap changer housing',group:'MECHANICAL',kind:'box',x:1.52,y:1.55,z:-.52,w:.48,h:.95,d:.42,color:'#8b9da0',description:'Tap-changer assembly. Review available operating events and maintenance records.'},
 {id:'control-cabinet',name:'Control cabinet',group:'PROTECTION',kind:'box',x:-1.65,y:.35,z:.65,w:.5,h:.68,d:.35,color:'#657f87',description:'Auxiliary control and monitoring cabinet. This view records reported statuses only; it does not operate protection equipment.'},
 {id:'cooling-fans',name:'Cooling fans',group:'PROTECTION',kind:'cylinder',x:0,y:.22,z:1.0,w:1.05,h:.22,d:.3,color:'#647d84',description:'Cooling auxiliaries. Review recorded fan/pump events and cooling status.'},
];
const GROUP_LABEL:Record<PartKey,string>={THERMAL:'Thermal & loading',OIL_DGA:'Oil & DGA',INSULATION:'Insulation',MECHANICAL:'Mechanical & electrical',PROTECTION:'Protection & auxiliary'};
const GROUP_HINT:Record<PartKey,string>={THERMAL:'Temperature, hot-spot, load and ambient readings',OIL_DGA:'DGA, moisture, BDV and oil-test records',INSULATION:'Bushing and winding insulation test records',MECHANICAL:'Tap-changer and mechanical condition records',PROTECTION:'Cooling auxiliaries and reported protection events'};
function project(x:number,y:number,z:number,rx:number,ry:number,scale:number,cx:number,cy:number){
 const xx=x*Math.cos(ry)-z*Math.sin(ry), zz=x*Math.sin(ry)+z*Math.cos(ry);
 const yy=y*Math.cos(rx)-zz*Math.sin(rx), depth=y*Math.sin(rx)+zz*Math.cos(rx);
 const perspective=5.5/(5.5+depth*.12);
 return {x:cx+xx*scale*perspective,y:cy-yy*scale*perspective,depth};
}
function hexShade(hex:string,f:number){const n=parseInt(hex.slice(1),16),r=Math.min(255,Math.round(((n>>16)&255)*f)),g=Math.min(255,Math.round(((n>>8)&255)*f)),b=Math.min(255,Math.round((n&255)*f));return `rgb(${r},${g},${b})`;}
export default function Transformer3D({data}:{data:Dashboard}){
 const canvasRef=useRef<HTMLCanvasElement|null>(null), drag=useRef({active:false,x:0,y:0,startX:0,startY:0}), angle=useRef({x:-.28,y:-.62}), zoom=useRef(1), hovered=useRef<string|null>(null);
 const [selected,setSelected]=useState<Part>(PARTS[0]);const [hoverName,setHoverName]=useState('');const [diagnostics,setDiagnostics]=useState<Diagnostic[]>([]);const [protection,setProtection]=useState<Protection[]>([]);const [recordsLoading,setRecordsLoading]=useState(false);
 useEffect(()=>{
  const canvas=canvasRef.current;if(!canvas)return;const ctx=canvas.getContext('2d');if(!ctx)return;
  let width=0,height=0,raf=0;const resize=()=>{const r=canvas.getBoundingClientRect();const dpr=Math.min(window.devicePixelRatio||1,2);width=r.width;height=r.height;canvas.width=Math.max(1,Math.floor(width*dpr));canvas.height=Math.max(1,Math.floor(height*dpr));ctx.setTransform(dpr,0,0,dpr,0,0);};
  const draw=()=>{ctx.clearRect(0,0,width,height);const cx=width*.51,cy=height*.62,scale=Math.min(width/6.2,height/4.5)*zoom.current;ctx.fillStyle='#f5f9f8';ctx.fillRect(0,0,width,height);
   // soft ground plane
   ctx.beginPath();ctx.ellipse(cx,cy+scale*.12,scale*2.5,scale*.55,0,0,Math.PI*2);ctx.fillStyle='#e7efed';ctx.fill();
   const drawPart=(p:Part)=>{
    const verts=[[-p.w/2,0,-p.d/2],[p.w/2,0,-p.d/2],[p.w/2,0,p.d/2],[-p.w/2,0,p.d/2],[-p.w/2,p.h,-p.d/2],[p.w/2,p.h,-p.d/2],[p.w/2,p.h,p.d/2],[-p.w/2,p.h,p.d/2]].map(v=>project(p.x+v[0],p.y+v[1],p.z+v[2],angle.current.x,angle.current.y,scale,cx,cy));
    const faces=[{i:[0,1,5,4],f:.78},{i:[1,2,6,5],f:.62},{i:[2,3,7,6],f:.9},{i:[3,0,4,7],f:.72},{i:[4,5,6,7],f:1.12},{i:[0,1,2,3],f:.55}];
    if(p.kind==='cylinder'){
      const top=Array.from({length:14},(_,i)=>{const a=i/14*Math.PI*2;return project(p.x+Math.cos(a)*p.w/2,p.y+p.h,p.z+Math.sin(a)*p.d/2,angle.current.x,angle.current.y,scale,cx,cy)});
      const bottom=Array.from({length:14},(_,i)=>{const a=i/14*Math.PI*2;return project(p.x+Math.cos(a)*p.w/2,p.y,p.z+Math.sin(a)*p.d/2,angle.current.x,angle.current.y,scale,cx,cy)});
      ctx.beginPath();top.forEach((v,i)=>i?ctx.lineTo(v.x,v.y):ctx.moveTo(v.x,v.y));ctx.closePath();ctx.fillStyle=hexShade(p.color,1.15);ctx.fill();ctx.strokeStyle='#405b61';ctx.lineWidth=1;ctx.stroke();
      for(let i=0;i<14;i++){const j=(i+1)%14;ctx.beginPath();ctx.moveTo(top[i].x,top[i].y);ctx.lineTo(top[j].x,top[j].y);ctx.lineTo(bottom[j].x,bottom[j].y);ctx.lineTo(bottom[i].x,bottom[i].y);ctx.closePath();ctx.fillStyle=hexShade(p.color,.68+(i%3)*.08);ctx.fill();ctx.strokeStyle='#526d72';ctx.stroke();}
    }else{
      faces.sort((a,b)=>{const da=a.i.reduce((s,i)=>s+verts[i].depth,0)/4,db=b.i.reduce((s,i)=>s+verts[i].depth,0)/4;return da-db;});
      faces.forEach(face=>{ctx.beginPath();face.i.forEach((i,j)=>j?ctx.lineTo(verts[i].x,verts[i].y):ctx.moveTo(verts[i].x,verts[i].y));ctx.closePath();const active=p.id===selected.id||p.id===hovered.current;ctx.fillStyle=active?'#d5a34b':hexShade(p.color,face.f);ctx.fill();ctx.strokeStyle=active?'#8b5d18':'#405b61';ctx.lineWidth=active?2:1;ctx.stroke();});
    }
    const c=project(p.x,p.y+p.h/2,p.z,angle.current.x,angle.current.y,scale,cx,cy);return {...c,part:p,radius:Math.max(14,Math.max(p.w,p.h,p.d)*scale*.42)};
   };
   const projected=PARTS.map(drawPart).sort((a,b)=>a.depth-b.depth);
   // structural pipe from conservator to tank
   const pipeA=project(.15,2.53,-.05,angle.current.x,angle.current.y,scale,cx,cy),pipeB=project(.15,1.85,-.05,angle.current.x,angle.current.y,scale,cx,cy);ctx.beginPath();ctx.moveTo(pipeA.x,pipeA.y);ctx.lineTo(pipeB.x,pipeB.y);ctx.strokeStyle='#8b9fa2';ctx.lineWidth=Math.max(3,scale*.045);ctx.stroke();
   projected.forEach(p=>{if(p.part.id===hovered.current||p.part.id===selected.id){ctx.beginPath();ctx.arc(p.x,p.y,p.radius*.72,0,Math.PI*2);ctx.strokeStyle=p.part.id===selected.id?'#b7791f':'#168c7b';ctx.lineWidth=2;ctx.stroke();}});
   ctx.fillStyle='#71858a';ctx.font='11px sans-serif';ctx.fillText('Drag to rotate · Scroll to zoom · Click a component to inspect',18,24);
   raf=requestAnimationFrame(draw);
  };
  resize();const observer=new ResizeObserver(resize);observer.observe(canvas);draw();return()=>{cancelAnimationFrame(raf);observer.disconnect();};
 },[selected]);
 const pick=(x:number,y:number)=>{const canvas=canvasRef.current;if(!canvas)return null;const r=canvas.getBoundingClientRect(),cx=r.width*.51,cy=r.height*.62,scale=Math.min(r.width/6.2,r.height/4.5)*zoom.current;return PARTS.map(p=>({...project(p.x,p.y+p.h/2,p.z,angle.current.x,angle.current.y,scale,cx,cy),part:p,radius:Math.max(18,Math.max(p.w,p.h,p.d)*scale*.48)})).map(p=>({...p,distance:Math.hypot(p.x-x,p.y-y)})).filter(p=>p.distance<p.radius).sort((a,b)=>a.distance-b.distance)[0]?.part??null;};
 const pointerMove=(e:ReactPointerEvent<HTMLCanvasElement>)=>{const r=e.currentTarget.getBoundingClientRect(),x=e.clientX-r.left,y=e.clientY-r.top;if(drag.current.active){angle.current.y+=(e.clientX-drag.current.x)*.009;angle.current.x=Math.max(-1.05,Math.min(.75,angle.current.x+(e.clientY-drag.current.y)*.006));drag.current.x=e.clientX;drag.current.y=e.clientY;return;}const p=pick(x,y);hovered.current=p?.id??null;setHoverName(p?.name??'');};
 const pointerDown=(e:ReactPointerEvent<HTMLCanvasElement>)=>{drag.current={active:true,x:e.clientX,y:e.clientY,startX:e.clientX,startY:e.clientY};e.currentTarget.setPointerCapture(e.pointerId);};
 const pointerUp=(e:ReactPointerEvent<HTMLCanvasElement>)=>{const moved=Math.abs(e.clientX-drag.current.startX)+Math.abs(e.clientY-drag.current.startY);drag.current.active=false;if(moved<5){const r=e.currentTarget.getBoundingClientRect(),p=pick(e.clientX-r.left,e.clientY-r.top);if(p)setSelected(p);}};
 const changeZoom=(delta:number)=>{zoom.current=Math.max(.65,Math.min(1.8,zoom.current+delta));};
 const reset=()=>{angle.current={x:-.28,y:-.62};zoom.current=1;};
 const group=data.health?.components.find(c=>c.key===selected.group);const readings=data.latestReadings.filter(r=>selected.group==='THERMAL'&&['WINDING_HOTSPOT_TEMPERATURE','TOP_OIL_TEMPERATURE','LOAD_PERCENT','AMBIENT_TEMPERATURE'].includes(r.measurementType));
 useEffect(()=>{let live=true;setRecordsLoading(true);if(['OIL_DGA','INSULATION','MECHANICAL'].includes(selected.group)){api.diagnostics(data.transformer.id,0,50).then(p=>{if(live)setDiagnostics(p.content);}).catch(()=>{if(live)setDiagnostics([]);}).finally(()=>{if(live)setRecordsLoading(false);});}else if(selected.group==='PROTECTION'){api.protection(data.transformer.id,0,50).then(p=>{if(live)setProtection(p.content);}).catch(()=>{if(live)setProtection([]);}).finally(()=>{if(live)setRecordsLoading(false);});}else{setRecordsLoading(false);}return()=>{live=false;};},[data.transformer.id,selected.group]);
 const diagnosticRows=diagnostics.filter(d=>selected.group==='OIL_DGA'?/DGA|GAS|MOISTURE|BDV|ACID|OIL|TAN_DELTA/.test(`${d.testType} ${d.parameterName}`.toUpperCase()):selected.group==='INSULATION'?/TAN|INSULATION|FURAN|FRA|IR|POLARIZATION|PI|BUSHING/.test(`${d.testType} ${d.parameterName}`.toUpperCase()):/FRA|WINDING|TAP|RESISTANCE|PARTIAL_DISCHARGE|PD/.test(`${d.testType} ${d.parameterName}`.toUpperCase()));
 return <section className="panel model-panel"><div className="section-title"><div><span className="eyebrow">INTERACTIVE ASSET MODEL</span><h3>Explore transformer components</h3><p>Rotate, zoom and select a component directly on the model to inspect its linked condition information.</p></div><span className="model-chip">CONCEPTUAL 3D MODEL</span></div>
  <div className="model-layout"><div className="model-stage"><canvas ref={canvasRef} onPointerDown={pointerDown} onPointerMove={pointerMove} onPointerUp={pointerUp} onPointerCancel={()=>{drag.current.active=false;}} onPointerLeave={()=>{if(!drag.current.active){hovered.current=null;setHoverName('');}}} onWheel={e=>{e.preventDefault();changeZoom(e.deltaY<0?.08:-.08);}} aria-label="Interactive transformer model. Drag to rotate, scroll to zoom, click components to inspect."/><div className="model-controls"><button onClick={()=>changeZoom(.12)} aria-label="Zoom in">＋</button><button onClick={()=>changeZoom(-.12)} aria-label="Zoom out">−</button><button onClick={reset}>Reset view</button></div>{hoverName&&<div className="model-hover">{hoverName}</div>}</div>
   <aside className="model-inspector"><span className="eyebrow">SELECTED COMPONENT</span><h4>{selected.name}</h4><span className="model-group">{GROUP_LABEL[selected.group]}</span><p>{selected.description}</p><div className="inspector-stat"><small>Linked health group</small><b>{group?.score==null?'Not assessed':`${group.score}/100`}</b><span>{group?.state??'UNKNOWN'}</span></div><p className="inspector-hint">{GROUP_HINT[selected.group]}</p>
    {recordsLoading?<div className="inspector-empty">Loading linked condition records…</div>:selected.group==='THERMAL'&&readings.length>0?<div className="inspector-readings">{readings.map(r=><div key={r.id}><span>{r.measurementType.replaceAll('_',' ')}</span><b>{r.value} {r.unit}</b></div>)}</div>:selected.group==='PROTECTION'?<div className="inspector-readings">{protection.slice(0,5).map(p=><div key={p.id}><span>{p.deviceName} · {p.eventType}</span><b>{p.active?'ACTIVE':'INACTIVE'}</b></div>)}{protection.length===0&&<small>No protection events recorded.</small>}</div>:<div className="inspector-readings">{diagnosticRows.slice(0,5).map(d=><div key={d.id}><span>{d.parameterName} · {new Date(d.testDate).toLocaleDateString()}</span><b>{d.value} {d.unit}</b></div>)}{diagnosticRows.length===0&&<small>No matching diagnostic records recorded.</small>}</div>}
    <div className="model-part-list"><small>SELECT A COMPONENT</small>{PARTS.map(p=><button key={p.id} className={selected.id===p.id?'active':''} onClick={()=>setSelected(p)}>{p.name}<span>›</span></button>)}</div>
   </aside></div><p className="model-disclaimer">This is a conceptual transformer geometry for navigation, not a manufacturer-specific CAD model or engineering drawing. Component selection currently links to condition groups; it does not claim internal physical sensor placement.</p>
 </section>;
}
