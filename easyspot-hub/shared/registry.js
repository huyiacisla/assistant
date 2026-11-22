/* App Registry: the single place an EasySpot app is declared.
   The Hub draws tiles, decides what a tap does and builds the launch URL from this table.
   Adding an app = adding one entry here (plus an icon colour in hub.css). */
const APP_REGISTRY=[
 {id:'ops',slug:'ops',name:'EasySpotOps',shortName:'Ops',category:'Operations',icon:'square-parking',color:'#2B55F2',
  description:'Keep parking fair: permits, guest passes and entries in one place.',
  keywords:'parking permits guest passes entries tow spots',
  highlights:['Guest passes and permits','Entries and open spots','Tow requests'],
  lifecycle:'live',
  /* The real product. It refuses to be framed (X-Frame-Options: DENY), so it opens in its own tab.
     Sample of the receiving side: apps/ops.html. */
  launch:{type:'url',url:'https://easyspotops.com/',target:'blank',params:['app','companyId','propertyId','role','userId','theme','locale','returnUrl']},
  requiresSingleProperty:true,allowedRoles:null},
 {id:'home',slug:'home',name:'EasySpotHome',shortName:'Home',category:'Residents',icon:'house',color:'#0F766E',
  description:'Handle building requests faster and make every move-in smooth.',
  keywords:'requests move-in residents maintenance tickets',
  highlights:['Building requests','Move-in checklists','Resident messages'],
  lifecycle:'coming_soon',
  launch:{type:'modal_preview'},
  requiresSingleProperty:true,allowedRoles:null},
 {id:'evt',slug:'events',name:'EasySpotEvents',shortName:'Events',category:'Community',icon:'calendar-heart',color:'#DB2777',
  description:'Bring the community together: plan events and see who is coming.',
  keywords:'events community rsvp calendar gatherings',
  highlights:['Event calendar','RSVPs','Reminders'],
  lifecycle:'coming_soon',
  launch:{type:'modal_preview'},
  requiresSingleProperty:false,allowedRoles:null},
 {id:'fin',slug:'finance',name:'EasySpotFinance',shortName:'Finance',category:'Finance',icon:'landmark',color:'#7C3AED',
  description:'See charges and deposits clearly, without spreadsheets.',
  keywords:'charges deposits payments invoices money',
  highlights:['Charges and deposits','Monthly statements','Exports'],
  lifecycle:'coming_soon',
  launch:{type:'modal_preview'},
  requiresSingleProperty:false,allowedRoles:['account_admin','property_manager']}
];
const APP_KEYS=APP_REGISTRY.map(a=>a.id);
const appDef=id=>APP_REGISTRY.find(a=>a.id===id);
/* Short aliases the role pages already use. */
const APPS=APP_REGISTRY.map(a=>({k:a.id,name:a.name,icon:a.icon,desc:a.description}));
const A=k=>APPS.find(a=>a.k===k);

/* One vocabulary for every view. */
const LIFECYCLES=['live','beta','coming_soon','maintenance'];
const LIFECYCLE={
 live:{label:'Live'},
 beta:{label:'Beta',chip:'c-new',icon:'sparkles'},
 coming_soon:{label:'Coming soon',chip:'c-pending',icon:'clock'},
 maintenance:{label:'Maintenance',chip:'c-pending',icon:'triangle-alert'}};
const APP_STATES=['open','new','pending','available','request_access','locked'];
const APP_STATE={
 open:{label:'Ready to open',icon:'circle-check'},
 new:{label:'New',icon:'sparkles'},
 pending:{label:'Pending approval',icon:'clock'},
 available:{label:'Add to your Hub',icon:'plus'},
 request_access:{label:'No access yet',icon:'key-round'},
 locked:{label:'Not available',icon:'lock'}};
const REQ_STATES=['pending','approved','denied','expired'];
const REQ_STATE={
 pending:{chip:'c-pending',icon:'clock',label:'Pending approval',event:'pending'},
 approved:{chip:'c-open',icon:'circle-check',label:'Approved',event:'approved'},
 denied:{chip:'c-locked',icon:'circle-x',label:'Not approved',event:'not_approved'},
 expired:{chip:'c-locked',icon:'clock',label:'Expired',event:'expired'}};

/* System-wide config, owned by EasySpot (admin console). */
const SYS_KEY='ea:sys:config';
const SYS_DEFAULT=()=>({v:1,lifecycle:{},enableTtlDays:14});
function sysConfig(){
 const o=typeof storeGet==='function'?storeGet(SYS_KEY):null,d=SYS_DEFAULT();
 if(!o||o.v!==1)return d;
 if(o.lifecycle&&typeof o.lifecycle==='object')APP_KEYS.forEach(k=>{if(lifecycleChoices(k).includes(o.lifecycle[k]))d.lifecycle[k]=o.lifecycle[k]});
 if([7,14,30].includes(o.enableTtlDays))d.enableTtlDays=o.enableTtlDays;
 return d;
}
/* An app with no destination yet can't be switched to live or beta. */
function lifecycleChoices(id){const a=appDef(id);return a&&a.launch&&a.launch.type!=='modal_preview'?LIFECYCLES.slice():['coming_soon','maintenance']}
function appLifecycle(id){const a=appDef(id);if(!a)return 'coming_soon';return sysConfig().lifecycle[id]||a.lifecycle}

/* The one answer to "what can this person do with this app, here, now?"
   c = {role:'client'|'staff', userRole, catalog, ent, grants, reqs, enableReqs, prop, now, viewer} */
function resolveAppState(id,c){
 const app=appDef(id),lc=appLifecycle(id);
 const out=(state,reason,extra)=>{const ready=state==='open'||state==='new';return Object.assign({app:id,state,reason:reason||null,lifecycle:lc,ready,launchable:ready&&(lc==='live'||lc==='beta')},extra||{})};
 const newest=(a,b)=>String(b.id).localeCompare(String(a.id));
 if(!app)return out('locked','unknown');
 if(!(c.catalog&&c.catalog[id])){
  if(c.role==='client'){
   const r=(c.enableReqs||[]).filter(x=>x.app===id).sort(newest)[0],vs=r?reqViewSt(r):null;
   if(vs==='pending')return out('pending','easyspot_review',{req:r});
   if(vs==='denied')return out('locked','easyspot_denied',{req:r});
  }
  return out('locked','not_enabled');
 }
 const e=(c.ent&&c.ent[id])||{st:'none'};
 if(c.role==='client'){
  if(e.st==='active')return out(c.now-e.on<7?'new':'open',null,{since:e.on});
  if(e.st==='pending')return out('pending','company_pending',{ent:e});
  return out('available','not_added');
 }
 if(e.st!=='active')return out('locked','not_on_account');
 if(app.allowedRoles&&c.userRole&&!app.allowedRoles.includes(c.userRole))return out('locked','role_not_allowed');
 const g=(c.grants||[]).find(x=>x.app===id&&x.prop===c.prop);
 if(g)return out(c.now-g.on<7?'new':'open',null,{since:g.on});
 const r=(c.reqs||[]).filter(x=>x.app===id&&x.prop===c.prop).sort(newest)[0],vs=r&&r.st!=='approved'?reqViewSt(r):null;
 if(vs==='pending')return out('pending','request_pending',{req:r});
 return out('request_access',c.viewer?'viewer':vs==='denied'?'request_denied':vs==='expired'?'request_expired':'no_grant',{req:vs?r:null});
}
