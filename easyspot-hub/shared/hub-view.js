/* The Hub itself: show the apps, say what state each one is in, and open them.
   Nothing here manages people, access or companies; that lives in the role page's admin area.
   The role page supplies: hubStateCtx(), hubProps(), hubPropChoices(), PROP_ID(k),
   and the flows a tap can lead to (addModal / enableReqModal for account admins, requestModal for staff). */
Object.assign(ICONS,{
 pin:'<path d="M12 17v5"/><path d="M9 10.76a2 2 0 0 1-1.11 1.79l-1.78.9A2 2 0 0 0 5 15.24V16a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-.76a2 2 0 0 0-1.11-1.79l-1.78-.9A2 2 0 0 1 15 10.76V7a1 1 0 0 1 1-1 2 2 0 0 0 0-4H8a2 2 0 0 0 0 4 1 1 0 0 1 1 1z"/>',
 building:'<path d="M6 22V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v18Z"/><path d="M6 12H4a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2"/><path d="M18 9h2a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2h-2"/><path d="M10 6h4"/><path d="M10 10h4"/><path d="M10 14h4"/><path d="M10 18h4"/>'});

const appSt=k=>resolveAppState(k,hubStateCtx());
const tileState=k=>appSt(k).state;
const curTab=()=>HUB_VIEWS.some(x=>x[0]===S.tab)?S.tab:'all';
let launching=null;

/* Chip on a tile: [css state, icon, text]. */
function tileBadge(s){
 if(s.ready&&s.lifecycle==='coming_soon')return ['pending','clock','Coming soon'];
 if(s.ready&&s.lifecycle==='maintenance')return ['pending','triangle-alert','Maintenance'];
 if(s.state==='open'&&s.lifecycle==='beta')return ['new','sparkles','Beta'];
 const label={not_on_account:'Not on your account',request_pending:'Request pending',easyspot_review:'Asked EasySpot',role_not_allowed:'Not for your role'}[s.reason]||APP_STATE[s.state].label;
 return [s.state,APP_STATE[s.state].icon,label];
}
function stateLine(s){
 if(s.reason==='viewer')return 'Ask your admin for access';
 if(s.reason==='request_pending')return `Sent ${day(s.req.day)} (Sample)`;
 if(s.reason==='request_denied')return 'Not approved · send again';
 if(s.reason==='request_expired')return 'Expired · send again';
 return '';
}
/* Why the app can't be opened right now, and what to do next: [icon, text]. */
function stateWhy(s){
 const n=appDef(s.app).name;
 if(s.ready&&s.lifecycle==='coming_soon')return ['clock',`${n} is coming soon. Your access is saved, so it opens from your Hub the day it's ready.`];
 if(s.ready&&s.lifecycle==='maintenance')return ['triangle-alert',`${n} is down for maintenance. Nothing is lost. It opens again when the work is done.`];
 switch(s.reason){
  case 'company_pending':return ['clock',"This app is pending approval. It opens once it's approved."];
  case 'request_pending':return ['clock',`Request sent ${day(s.req.day)}. It stays open until ${day(s.req.day+reqTtl(s.req))} (Sample).`];
  case 'easyspot_review':return ['clock',`You asked EasySpot to turn this on ${day(s.req.day)} (Sample). We'll tell you when they answer.`];
  case 'easyspot_denied':return ['circle-x',"EasySpot didn't approve this app for your company. You can ask again."];
  case 'not_enabled':return ['lock',STORE_ROLE==='client'?"EasySpot hasn't turned this on for your company yet.":'This app is not available for your company right now.'];
  case 'not_on_account':return ['lock',"Your company hasn't added this app yet. Ask your account admin."];
  case 'role_not_allowed':return ['lock',"Your role can't open this app. Ask your account admin if you need it."];
  case 'viewer':return ['info','Ask your admin for access'];
 }
 return ['info',''];
}

function tileHTML(k){
 const a=appDef(k),s=appSt(k),[c,i,t]=tileBadge(s),line=stateLine(s),pinned=SETTINGS.pins.includes(k);
 if(typeof emitTile==='function')emitTile(k,s);
 return `<div class="tile-wrap"><button type="button" class="tile" data-k="tile-${k}" data-app="${k}" data-state="${s.state}" data-lifecycle="${s.lifecycle}"${s.reason==='request_pending'?' data-req="pending"':''}${launching===k?' aria-busy="true"':''}><span class="t-ic ic-${k}">${ic(a.icon,28)}</span><span class="t-n" id="tn-${k}">${a.name}</span><span class="t-d" id="td-${k}">${a.description}</span><span class="chip c-${c}" id="tc-${k}">${ic(i,13)}${t}</span>${line?`<span class="t-line" id="tl-${k}">${esc(line)}</span>`:''}</button><button type="button" class="pin-btn" data-k="pin-${k}" data-pin="${k}" aria-pressed="${pinned}" aria-label="${pinned?'Unpin':'Pin'} ${a.name}">${ic('pin',16)}</button></div>`;
}
function quickHTML(){
 const pins=SETTINGS.pins.filter(k=>appDef(k)),recent=SETTINGS.recent.filter(k=>appDef(k)&&!pins.includes(k));
 const row=(id,title,list)=>list.length?`<section class="quick" aria-labelledby="${id}"><h2 class="quick-h" id="${id}">${title}</h2><div class="quick-row">${list.map(k=>{const a=appDef(k),s=appSt(k);return `<button type="button" class="quick-btn" data-k="${id}-${k}" data-launch="${k}" data-state="${s.state}"><span class="mini-ic ic-${k}">${ic(a.icon,16)}</span><span>${a.shortName}</span>${s.launchable?'':`<span class="q-st">${ic(tileBadge(s)[1],13)}<span class="sr-only">${tileBadge(s)[2]}</span></span>`}</button>`}).join('')}</div></section>`:'';
 return row('qPinned','Pinned',pins)+row('qRecent','Recent',recent);
}
function hubContextLabel(){const p=S.prop==='all'?null:P(S.prop);return CLIENT_COMPANY+' · '+(p?p.name:'All buildings')}

function hubView(){
 const tab=curTab(),all=APP_KEYS.map(appSt);
 const count={all:all.length,ready:all.filter(s=>s.ready).length,pending:all.filter(s=>s.state==='pending').length};
 let h=`<div class="hub-head"><div><h1>Apps</h1><p class="sub hub-ctx" id="hubCtx">${ic('building',15)}<span>${esc(hubContextLabel())}</span><span class="sample">Sample</span></p></div>
 <div class="seg" role="radiogroup" aria-label="Which apps to show" id="tabSeg">${HUB_VIEWS.map(([v,l])=>`<button type="button" role="radio" data-k="tab-${v}" data-tab="${v}" aria-checked="${tab===v}" tabindex="${tab===v?0:-1}">${l}${v!=='all'&&count[v]?` <span class="seg-n" aria-label="${count[v]} app${count[v]===1?'':'s'}">${count[v]}</span>`:''}</button>`).join('')}</div></div>`;
 if(S.loading)return h+`<div class="grid" aria-busy="true" aria-label="Loading apps">${'<div class="skel"></div>'.repeat(4)}</div><p class="sr-only">Loading</p>`;
 h+=quickHTML();
 if(typeof hubNotice==='function')h+=hubNotice(all,tab);
 const shown=all.filter(s=>tab==='ready'?s.ready:tab==='pending'?s.state==='pending':true).map(s=>s.app);
 if(!shown.length){
  const e=typeof hubEmpty==='function'?hubEmpty(tab):null;
  return h+`<div class="empty" id="hubEmpty"><span class="e-ic">${ic(tab==='pending'?'clock':'layers',26)}</span><h2>${tab==='pending'?'Nothing is pending':'No apps ready yet'}</h2><p>${e?e.text:tab==='pending'?'Requests and apps waiting for approval show up here.':'Open <b>All apps</b> to see what you can get.'}</p>${e&&e.action?e.action:`<button type="button" class="btn btn-primary" id="browseAll" data-k="browse-all">See all apps</button>`}</div>`;}
 return h+`<div class="grid" id="grid">${shown.map(tileHTML).join('')}${typeof hubExtraTile==='function'?hubExtraTile(all,tab):''}</div>`;
}

function setHubProp(v){S.prop=v;if(v!=='all')settingsSave({lastProp:v},true)}
function expireSession(){closeAll();S.session='expired';render();const b=$('#signIn');b&&b.focus()}

/* Everything the app needs to start in the right place. See shared/contract.js. */
function hubLaunchCtx(k,prop){
 const u=me(),p=P(prop);
 return {app:k,companyId:AMC_ID,company:CLIENT_COMPANY,propertyKey:p?p.k:'',propertyId:p?PROP_ID(p.k):'',property:p?p.name:'',role:u.role,userId:u.id,user:u.name,theme,locale:SETTINGS.lang,dateFormat:SETTINGS.dateFormat,timeZone:SETTINGS.timeZone,returnUrl:location.href.split('#')[0]};
}

/* One tap on an app always ends somewhere useful:
   signed out -> sign in · no access -> the flow that gets access · waiting/locked -> why and what next
   not launched yet -> preview + "tell me" · offline -> say so · needs one building -> pick it · else -> open. */
function launchApp(k,opener,opts){
 const app=appDef(k);if(!app||launching)return;
 if(S.session!=='ok'){expireSession();return}
 const s=appSt(k);
 if(s.state==='available'&&typeof addModal==='function'){addModal(k,opener);return}
 if(s.state==='request_access'&&s.reason!=='viewer'&&typeof requestModal==='function'){requestModal(k,opener);return}
 if(!s.launchable){appDetailModal(k,opener);return}
 if(S.offline){launchProblem(k,opener,'offline');return}
 const prop=(opts&&opts.prop)||S.prop;
 if(app.requiresSingleProperty&&(prop==='all'||!P(prop))){propPickModal(k,opener);return}
 beginLaunch(k,opener,prop);
}
function beginLaunch(k,opener,prop){
 const app=appDef(k);launching=k;
 const tile=document.querySelector(`[data-k="tile-${k}"]`);tile&&tile.setAttribute('aria-busy','true');
 live(`Opening ${app.name}…`);
 setTimeout(()=>{
  launching=null;const tl=document.querySelector(`[data-k="tile-${k}"]`);tl&&tl.removeAttribute('aria-busy');
  const f=S.failNext;S.failNext=null;
  if(S.session!=='ok'||f==='session'){expireSession();return}
  if(S.offline||f==='error'){launchProblem(k,opener,S.offline?'offline':'error');return}
  if(f==='noaccess'||!appSt(k).launchable){launchProblem(k,opener,'noaccess');return}
  pushRecent(k);uiSave();
  const url=HubContract.build(app.launch,hubLaunchCtx(k,prop));
  if(app.launch.target==='blank'){const w=window.open(url,'_blank','noopener');if(!w)launchProblem(k,opener,'blocked');else render();return}
  location.assign(url);
 },450);
}
function launchProblem(k,opener,why){
 const n=appDef(k).name,canAsk=typeof requestModal==='function'&&!(typeof isViewer==='function'&&isViewer());
 const text={offline:`You're offline. ${n} will open when you're back online.`,error:`We couldn't open ${n}. Please try again.`,blocked:`Your browser blocked the new tab. Allow pop-ups for the Hub, then try again.`,noaccess:`You don't have access to ${n} yet.${canAsk?'':' Ask your account admin to check your access, or contact us from Help.'}`}[why];
 const act=why==='noaccess'?(canAsk?'<button type="button" class="btn btn-primary btn-block" id="naRequest">Request access</button>':'<button type="button" class="btn btn-primary btn-block" id="naHelp">Contact support</button>'):'<button type="button" class="btn btn-primary btn-block" id="lpRetry">Try again</button>';
 const m=openModal('launchModal',`<div class="am-top"><span class="am-ic ic-${k}">${ic(appDef(k).icon,40)}</span><h2 id="launchModal-t">${why==='noaccess'?'No access yet':"Couldn't open "+n}</h2></div><div class="msg-err" id="${why==='noaccess'?'openNoAccess':'openErr'}" role="alert">${ic(why==='noaccess'?'lock':'triangle-alert',16)}<span>${text}</span></div><div class="acts-col" style="margin-top:12px">${act}<button type="button" class="btn btn-outline btn-block" data-close>Close</button></div>`,{opener});
 const back=()=>document.querySelector(`[data-k="tile-${k}"]`)||opener;
 const on=(id,f)=>{const b=m.querySelector(id);if(b)b.onclick=()=>{closeModal({noReturn:true});f()}};
 on('#lpRetry',()=>launchApp(k,back()));
 on('#naRequest',()=>requestModal(k,back()));
 on('#naHelp',()=>helpModal($('#helpBtn')));
}
function propPickModal(k,opener){
 const n=appDef(k).name,props=hubProps();
 const m=openModal('propModal',`<h2 id="propModal-t">Which building?</h2><p class="lead">${n} works one building at a time. Pick one and the Hub switches to it too.</p><div class="acts-col" id="propPick">${props.map(p=>`<button type="button" class="btn btn-outline btn-block" data-pick="${p.k}">${ic('building',16)}${esc(p.name)}</button>`).join('')}</div><div class="acts-col" style="margin-top:8px"><button type="button" class="btn btn-ghost btn-block" data-close>Cancel</button></div>`,{opener,focus:'[data-pick]'});
 m.querySelectorAll('[data-pick]').forEach(b=>{b.onclick=()=>{const v=b.dataset.pick;if(!P(v))return;setHubProp(v);closeModal({noReturn:true});const t=document.querySelector(`[data-k="tile-${k}"]`);t&&t.focus();launchApp(k,t||opener,{prop:v})}});
}
function appDetailModal(k,opener){
 const a=appDef(k),s=appSt(k),[c,i,t]=tileBadge(s),[wi,wt]=stateWhy(s);
 let acts='';
 if(s.ready&&!s.launchable)acts=`<button type="button" class="sw" role="switch" id="amNotify" aria-checked="${!!SETTINGS.notifyReady[k]}"><span class="trk"></span><span>Tell me when it's ready</span></button>`;
 else if(s.reason==='company_pending'&&typeof hubReview==='function')acts='<button type="button" class="btn btn-outline btn-block" id="amReview">Review in App access</button>';
 else if((s.reason==='not_enabled'||s.reason==='easyspot_denied')&&typeof enableReqModal==='function')acts=`<button type="button" class="btn btn-primary btn-block" id="amAsk">${s.reason==='easyspot_denied'?'Ask again':'Ask EasySpot to turn it on'}</button>`;
 const m=openModal('appModal',`<div class="am-top"><span class="am-ic ic-${k}">${ic(a.icon,40)}</span><h2 id="appModal-t">${a.name}</h2><span class="chip c-${c}" id="amState">${ic(i,13)}${t}</span></div><p class="small muted" style="text-align:center;margin:6px 0 0">${a.category} · ${LIFECYCLE[s.lifecycle].label}</p>${wt?`<p class="why" id="amWhy">${ic(wi,15)}<span>${wt}</span></p>`:''}${acts?`<div class="acts-col">${acts}</div>`:''}<p class="am-desc">${a.description}</p><ul class="am-list">${a.highlights.map(x=>`<li>${ic('check',14)}<span>${x}</span></li>`).join('')}</ul>`,{opener});
 const key=modalStack.at(-1).key,back=()=>document.querySelector(`[data-k="${key}"]`)||opener;
 const nb=m.querySelector('#amNotify');if(nb)nb.onclick=()=>{const on=!SETTINGS.notifyReady[k],nr=Object.assign({},SETTINGS.notifyReady);if(on)nr[k]=true;else delete nr[k];settingsSave({notifyReady:nr},true);nb.setAttribute('aria-checked',on);live(on?`We'll tell you when ${a.name} is ready.`:'Okay, no reminder.')};
 const rv=m.querySelector('#amReview');if(rv)rv.onclick=()=>{closeModal({noReturn:true});hubReview(k)};
 const ak=m.querySelector('#amAsk');if(ak)ak.onclick=()=>{closeModal({noReturn:true});enableReqModal(k,back())};
}

function handleHubClick(t){
 if(t.dataset.tab){S.tab=t.dataset.tab;render();return true}
 if(t.dataset.pin){const k=t.dataset.pin,on=togglePin(k);render();const b=document.querySelector(`[data-k="pin-${k}"]`);b&&b.focus();live(`${appDef(k).name} ${on?'pinned':'unpinned'}.`);return true}
 if(t.dataset.launch){launchApp(t.dataset.launch,t);return true}
 if(t.classList.contains('tile')&&t.dataset.app){launchApp(t.dataset.app,t);return true}
 if(t.id==='browseAll'){S.tab='all';render();const f=document.querySelector(`[data-k="tile-${APP_KEYS[0]}"]`)||$('[data-k="tab-all"]');f&&f.focus();return true}
 return false;
}
function focusTile(k){S.view='hub';S.tab='all';render();const t=document.querySelector(`[data-k="tile-${k}"]`)||$('#main');t.focus()}
function hubSearchApps(){return APP_REGISTRY.map(a=>({key:a.id,label:a.name,terms:a.category+' '+a.keywords+' '+a.shortName,run:()=>focusTile(a.id)}))}
function hubSearchBuildings(){return hubProps().map(p=>({key:'b-'+p.k,label:p.name,terms:'building property',run:()=>{setHubProp(p.k);go('hub');live(`Now showing ${p.name}.`)}}))}

/* First paint: defaults from Settings, unless this tab already had a place (back from an app, reload). */
function hubBoot(){
 if(!uiRestore()){
  S.tab=SETTINGS.defaultView;
  const want=SETTINGS.defaultProp==='last'?SETTINGS.lastProp:SETTINGS.defaultProp;
  if(want&&hubPropChoices().some(x=>x[0]===want))S.prop=want;
 }
 if(!hubPropChoices().some(x=>x[0]===S.prop))S.prop=hubPropChoices()[0][0];
}
/* An app sent the person back: say so and put focus where they left. */
function hubReturn(){
 const r=HubContract.parseReturn();if(!r)return;
 try{history.replaceState(null,'',location.pathname+location.search)}catch(e){}
 const a=appDef(r.app);if(!a||S.session!=='ok')return;
 S.view='hub';render();live(r.message||`Back from ${a.name}.`);
 const t=document.querySelector(`[data-k="tile-${a.id}"]`)||$('#main');t.focus({preventScroll:true});
}
