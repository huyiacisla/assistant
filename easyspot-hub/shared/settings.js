/* Settings: one stored object per person, read by the Hub and applied the moment it changes.
   Personal settings  -> localStorage "ea-settings:<userId>" (this device)
   Theme              -> localStorage "ea-web-theme" (light | dark | system), read before first paint
   Organization/System-> the role page owns them (settingsOrgHTML / settingsOrgChange hooks) */
const THEME_KEY='ea-web-theme';
const DATE_FORMATS=[['mdy','Nov 14'],['dmy','14 Nov'],['iso','2026-11-14']];
const TIME_ZONES=[['auto','Automatic (this device)'],['UTC','UTC'],['America/New_York','New York'],['America/Chicago','Chicago'],['America/Los_Angeles','Los Angeles'],['Europe/London','London'],['Asia/Ho_Chi_Minh','Ho Chi Minh City']];
const HUB_VIEWS=[['all','All apps'],['ready','Ready'],['pending','Pending']];
const SESSION_MINUTES=[[15,'15 minutes'],[30,'30 minutes'],[60,'1 hour'],[0,'Never']];
const NOTIF_TYPES=[
 ['req','Access requests','When someone asks for an app, or a request you sent changes'],
 ['appr','Approvals','When an app or request is approved or not'],
 ['apps','App updates','When an app is turned on or off, or its status changes'],
 ['digest','Weekly summary','A short recap every Monday']];
const SETTINGS_SECTIONS=[['appearance','Appearance and defaults','theme dark light density compact default view building'],['region','Language and region','language date format time zone'],['notifications','Notifications','bell email alerts'],['security','Security and session','sign out session timeout'],['org','Organization and system','buildings policy requests availability']];
const SETTINGS_DEFAULT=()=>({v:1,density:'comfortable',defaultView:'all',defaultProp:'last',lastProp:null,lang:'en',dateFormat:'mdy',timeZone:'auto',
 notif:{inapp:{req:true,appr:true,apps:true,digest:false},email:{req:false,appr:false,apps:false,digest:false}},
 sessionMin:30,pins:[],recent:[],notifyReady:{}});
let SETTINGS=SETTINGS_DEFAULT();
let settingsOwner='';
let sessionStart=Date.now(),lastActivity=Date.now();

function settingsKey(){return 'ea-settings:'+settingsOwner}
function settingsClean(o){
 const d=SETTINGS_DEFAULT();
 if(!o||typeof o!=='object'||o.v!==1)return d;
 const pick=(k,list)=>{if(list.includes(o[k]))d[k]=o[k]};
 pick('density',['comfortable','compact']);pick('defaultView',HUB_VIEWS.map(x=>x[0]));pick('lang',['en']);
 pick('dateFormat',DATE_FORMATS.map(x=>x[0]));pick('timeZone',TIME_ZONES.map(x=>x[0]));pick('sessionMin',SESSION_MINUTES.map(x=>x[0]));
 if(typeof o.defaultProp==='string'&&/^[a-z0-9]{1,12}$/.test(o.defaultProp))d.defaultProp=o.defaultProp;
 if(typeof o.lastProp==='string'&&/^[a-z0-9]{1,12}$/.test(o.lastProp))d.lastProp=o.lastProp;
 ['inapp','email'].forEach(ch=>NOTIF_TYPES.forEach(([k])=>{if(o.notif&&o.notif[ch]&&typeof o.notif[ch][k]==='boolean')d.notif[ch][k]=o.notif[ch][k]}));
 const ids=a=>Array.isArray(a)?a.filter((k,i)=>APP_KEYS.includes(k)&&a.indexOf(k)===i):[];
 d.pins=ids(o.pins);d.recent=ids(o.recent).slice(0,4);
 if(o.notifyReady&&typeof o.notifyReady==='object')APP_KEYS.forEach(k=>{if(o.notifyReady[k]===true)d.notifyReady[k]=true});
 return d;
}
function settingsLoad(){let o=null;try{o=JSON.parse(localStorage.getItem(settingsKey())||'null')}catch(e){}SETTINGS=settingsClean(o)}
function settingsSave(patch,quiet){
 Object.assign(SETTINGS,patch||{});
 try{localStorage.setItem(settingsKey(),JSON.stringify(SETTINGS))}catch(e){}
 settingsApply();
 if(!quiet){settingsSaved=true;if(typeof render==='function')render();live('Saved.')}
}
let settingsSaved=false;
function settingsApply(){document.documentElement.classList.toggle('dens-compact',SETTINGS.density==='compact')}

function sysDark(){return !!(window.matchMedia&&matchMedia('(prefers-color-scheme: dark)').matches)}
function readThemeMode(){let t=null;try{t=new URLSearchParams(location.search).get('theme')||localStorage.getItem(THEME_KEY)}catch(e){}return t==='dark'||t==='light'?t:'system'}

/* Dates in this preview are sample days counted from a fixed base, so they are formatted in UTC. */
function fmtDate(dt){
 const f=SETTINGS.dateFormat;
 if(f==='iso')return dt.toISOString().slice(0,10);
 return dt.toLocaleDateString(f==='dmy'?'en-GB':'en-US',{month:'short',day:'numeric',timeZone:'UTC'});
}
function fmtTime(ts){
 const o={hour:'numeric',minute:'2-digit',timeZoneName:'short'};
 if(SETTINGS.timeZone!=='auto')o.timeZone=SETTINGS.timeZone;
 try{return new Date(ts).toLocaleTimeString(SETTINGS.dateFormat==='mdy'?'en-US':'en-GB',o)}catch(e){return new Date(ts).toLocaleTimeString()}
}

function togglePin(k){const on=SETTINGS.pins.includes(k);settingsSave({pins:on?SETTINGS.pins.filter(x=>x!==k):SETTINGS.pins.concat(k)},true);return !on}
function pushRecent(k){settingsSave({recent:[k].concat(SETTINGS.recent.filter(x=>x!==k)).slice(0,4)},true)}
function notifOn(type){const n=SETTINGS.notif.inapp;return n[type]!==false}

function settingsInit(userId){
 settingsOwner=userId;settingsLoad();settingsApply();
 try{const k='ea-session-start:'+userId,v=+sessionStorage.getItem(k);if(v)sessionStart=v;else sessionStorage.setItem(k,String(sessionStart))}catch(e){}
 ['pointerdown','keydown'].forEach(ev=>addEventListener(ev,()=>{lastActivity=Date.now()},true));
 setInterval(sessionTick,5000);
 if(window.matchMedia)matchMedia('(prefers-color-scheme: dark)').addEventListener('change',()=>{if(themeMode==='system'){setTheme('system');render()}});
 addEventListener('storage',e=>{if(e.key===settingsKey()){settingsLoad();settingsApply();render()}if(e.key===HubContract.sessionKey(settingsOwner)&&HubContract.signedOut(settingsOwner)&&S.session==='ok'){closeAll();S.session='out';render()}});
}
function sessionTick(now){
 if(S.session!=='ok'||!SETTINGS.sessionMin)return;
 if((now||Date.now())-lastActivity<SETTINGS.sessionMin*6e4)return;
 closeAll();S.session='expired';render();const b=$('#signIn');b&&b.focus();
}

function settingsView(back){
 const s=SETTINGS,hub=typeof hubView==='function';
 const head=(k,t)=>`<h2 class="h3" id="set-${k}" tabindex="-1">${t}</h2>`;
 const seg=(key,label,opts,cur)=>`<div class="set-row"><span class="set-l" id="setl-${key}">${label}</span><div class="seg" role="radiogroup" aria-labelledby="setl-${key}">${opts.map(([v,l])=>`<button type="button" role="radio" data-k="set-${key}-${v}" data-setseg="${key}|${v}" aria-checked="${cur===v}" tabindex="${cur===v?0:-1}">${l}</button>`).join('')}</div></div>`;
 const sel=(key,label,opts,cur,help)=>`<div class="field"><label for="set-f-${key}">${label}</label><select id="set-f-${key}" data-k="set-${key}" data-set="${key}"${help?` aria-describedby="set-h-${key}"`:''}>${opts.map(([v,l,off])=>`<option value="${esc(v)}"${String(cur)===String(v)?' selected':''}${off?' disabled':''}>${esc(l)}</option>`).join('')}</select>${help?`<span class="help" id="set-h-${key}">${help}</span>`:''}</div>`;
 const propOpts=hub?[['last','Last used']].concat(hubPropChoices()):[];
 const u=me();
 const org=typeof settingsOrgHTML==='function'?settingsOrgHTML(head):'';
 return `<div id="settingsView" class="page-narrow">${back||(typeof backBtn==='function'?backBtn():'')}<h1 style="margin-top:12px">Settings</h1>${pageCoSub()}
 <p class="muted set-status" id="setStatus" role="status">${ic(settingsSaved?'circle-check':'info',15)}<span>${settingsSaved?'All changes saved on this device.':'Changes save as you make them. Nothing is sent.'}</span> <span class="sample">Preview</span></p>
 <div class="card">${head('appearance','Appearance and defaults')}
  ${seg('theme','Theme',[['light','Light'],['dark','Dark'],['system','System']],themeMode)}
  ${seg('density','App tiles',[['comfortable','Comfortable'],['compact','Compact']],s.density)}
  ${hub?sel('defaultView','Show first when the Hub opens',HUB_VIEWS,s.defaultView)+sel('defaultProp','Building when you sign in',propOpts,propOpts.some(o=>o[0]===s.defaultProp)?s.defaultProp:'last'):''}</div>
 <div class="card">${head('region','Language and region')}
  ${sel('lang','Display language',[['en','English'],['vi','Tiếng Việt (coming soon)',1],['es','Español (coming soon)',1]],s.lang,'Only English is available in this preview.')}
  ${sel('dateFormat','Date format',DATE_FORMATS,s.dateFormat)}
  ${sel('timeZone','Time zone',TIME_ZONES,s.timeZone,`Now: ${esc(fmtTime(Date.now()))}`)}</div>
 <div class="card">${head('notifications','Notifications')}<p class="small muted" style="margin:4px 0 8px">In app shows under the bell. Email is a preview: nothing is sent.</p>
  <div class="list">${NOTIF_TYPES.map(([k,l,h])=>`<div class="li set-n"><div class="grow"><b style="font-weight:600">${l}</b><span class="small muted" style="display:block">${h}</span></div><div class="set-n-sw">${[['inapp','In app'],['email','Email']].map(([ch,cl])=>`<button type="button" class="sw" role="switch" data-k="ns-${ch}-${k}" data-setsw="${ch}|${k}" aria-checked="${!!s.notif[ch][k]}" aria-label="${l}: ${cl}"><span class="trk"></span><span aria-hidden="true">${cl}</span></button>`).join('')}</div></div>`).join('')}</div></div>
 <div class="card">${head('security','Security and session')}
  <div class="list" style="margin-top:6px"><div class="li"><span class="muted pf-k">Signed in as</span><span class="pf-v" style="overflow-wrap:anywhere">${esc(u.email||u.name)}</span></div><div class="li"><span class="muted pf-k">This session</span><span class="pf-v">Started ${esc(fmtTime(sessionStart))}</span></div></div>
  ${sel('sessionMin','Sign me out when idle for',SESSION_MINUTES,s.sessionMin,'Applies to this Hub on this device.')}
  <div style="margin-top:14px"><button type="button" class="btn btn-outline" id="setSignoutAll" data-k="set-signout-all">${ic('log-out',16)}Sign out of every EasySpot app</button></div></div>
 ${org}
 <div style="margin-top:16px"><button type="button" class="btn btn-outline" id="setReset" data-k="set-reset">${ic('refresh-cw',16)}Reset my settings</button></div></div>`;
}

function settingsClick(t){
 if(t.dataset.setseg){
  const [key,val]=t.dataset.setseg.split('|');
  if(key==='theme'){if(!['light','dark','system'].includes(val))return true;setTheme(val);settingsSaved=true;render();live(val==='system'?'Theme follows this device.':val==='dark'?'Dark mode on.':'Light mode on.')}
  else if(key==='density'&&['comfortable','compact'].includes(val))settingsSave({density:val});
  const b=document.querySelector(`[data-k="set-${key}-${val}"]`);b&&b.focus();return true}
 if(t.dataset.setsw){const [ch,k]=t.dataset.setsw.split('|');if(!SETTINGS.notif[ch]||!(k in SETTINGS.notif[ch]))return true;SETTINGS.notif[ch][k]=!SETTINGS.notif[ch][k];settingsSave();return true}
 if(t.id==='setSignoutAll'){signOut();return true}
 if(t.id==='setReset'){
  const m=openModal('resetModal',`<h2 id="resetModal-t">Reset your settings?</h2><p class="lead">Theme, tiles, defaults, notifications and pinned apps go back to how they started. Your access to apps does not change.</p><div class="acts-col"><button type="button" class="btn btn-primary btn-block" id="rsYes">Reset my settings</button><button type="button" class="btn btn-outline btn-block" data-close>Cancel</button></div>`,{opener:t,focus:'[data-close]'});
  m.querySelector('#rsYes').onclick=()=>{SETTINGS=SETTINGS_DEFAULT();setTheme('system');closeModal({noReturn:true});settingsSave();live('Settings reset.');const b=$('#setReset');b&&b.focus()};
  return true}
 return false;
}
document.addEventListener('change',e=>{
 const t=e.target;if(!t||!t.dataset)return;
 if(t.dataset.set){
  const key=t.dataset.set,v=t.value,lists={defaultView:HUB_VIEWS,dateFormat:DATE_FORMATS,timeZone:TIME_ZONES};
  if(key==='sessionMin'){if(SESSION_MINUTES.some(x=>String(x[0])===v))settingsSave({sessionMin:+v})}
  else if(key==='defaultProp'){if(v==='last'||hubPropChoices().some(x=>x[0]===v))settingsSave({defaultProp:v})}
  else if(key==='lang'){if(v==='en')settingsSave({lang:v})}
  else if(lists[key]&&lists[key].some(x=>x[0]===v))settingsSave({[key]:v});
  return}
 if(t.dataset.setorg&&typeof settingsOrgChange==='function'){settingsOrgChange(t.dataset.setorg,t.value,t);return}
});
function openSettingsSection(key){go('settings');const h=$('#set-'+key);if(h){h.scrollIntoView({block:'start'});h.focus({preventScroll:true})}}
