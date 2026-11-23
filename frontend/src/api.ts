import type { Page, Transformer, Dashboard, Reading, Alert, Health, Diagnostic, Protection } from './types';
const base=(import.meta.env.VITE_API_BASE_URL||'http://localhost:8080/api').replace(/\/$/,'');
async function request<T>(path:string,options:RequestInit={}):Promise<T>{
 const response=await fetch(`${base}${path}`,{...options,headers:{'Content-Type':'application/json',...(options.headers||{})}});
 if(!response.ok){let message=`Request failed (${response.status})`;try{const body=await response.json();message=body.message||message;}catch{}throw new Error(message);}
 return response.json() as Promise<T>;
}
export const api={
 transformers:(page=0,size=100)=>request<Page<Transformer>>(`/transformers?page=${page}&size=${size}`),
 createTransformer:(body:unknown)=>request<Transformer>('/transformers',{method:'POST',body:JSON.stringify(body)}),
 dashboard:(id:number)=>request<Dashboard>(`/transformers/${id}/dashboard`),
 readings:(id:number,metric:string,page=0,size=25)=>request<Page<Reading>>(`/transformers/${id}/readings?metric=${encodeURIComponent(metric)}&page=${page}&size=${size}`),
 alerts:(id:number,page=0,size=25)=>request<Page<Alert>>(`/transformers/${id}/alerts?page=${page}&size=${size}`),
 healthHistory:(id:number,page=0,size=25)=>request<Page<Health>>(`/transformers/${id}/health-history?page=${page}&size=${size}`),
 diagnostics:(id:number,page=0,size=25)=>request<Page<Diagnostic>>(`/transformers/${id}/diagnostics?page=${page}&size=${size}`),
 addDiagnostic:(id:number,body:unknown)=>request<Diagnostic>(`/transformers/${id}/diagnostics`,{method:'POST',body:JSON.stringify(body)}),
 protection:(id:number,page=0,size=25)=>request<Page<Protection>>(`/transformers/${id}/protection-events?page=${page}&size=${size}`),
 addProtection:(id:number,body:unknown)=>request<Protection>(`/transformers/${id}/protection-events`,{method:'POST',body:JSON.stringify(body)}),
 eventUrl:(id:number)=>`${base}/transformers/${id}/events`
};
