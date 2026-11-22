const ICONS={"settings":"<path d=\"M9.671 4.136a2.34 2.34 0 0 1 4.659 0 2.34 2.34 0 0 0 3.319 1.915 2.34 2.34 0 0 1 2.33 4.033 2.34 2.34 0 0 0 0 3.831 2.34 2.34 0 0 1-2.33 4.033 2.34 2.34 0 0 0-3.319 1.915 2.34 2.34 0 0 1-4.659 0 2.34 2.34 0 0 0-3.32-1.915 2.34 2.34 0 0 1-2.33-4.033 2.34 2.34 0 0 0 0-3.831A2.34 2.34 0 0 1 6.35 6.051a2.34 2.34 0 0 0 3.319-1.915\"/><circle cx=\"12\" cy=\"12\" r=\"3\"/>","chevron-down":"<path d=\"m6 9 6 6 6-6\"/>","arrow-left":"<path d=\"m12 19-7-7 7-7\"/><path d=\"M19 12H5\"/>","square-parking":"<rect width=\"18\" height=\"18\" x=\"3\" y=\"3\" rx=\"2\"/><path d=\"M9 17V7h4a3 3 0 0 1 0 6H9\"/>","house":"<path d=\"M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8\"/><path d=\"M3 10a2 2 0 0 1 .709-1.528l7-6a2 2 0 0 1 2.582 0l7 6A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z\"/>","landmark":"<path d=\"M10 18v-7\"/><path d=\"M11.119 2.205a2 2 0 0 1 1.762 0l7.84 3.846A.5.5 0 0 1 20.5 7h-17a.5.5 0 0 1-.22-.949z\"/><path d=\"M14 18v-7\"/><path d=\"M18 18v-7\"/><path d=\"M3 22h18\"/><path d=\"M6 18v-7\"/>","calendar-heart":"<path d=\"M12.127 21H5a2 2 0 01-2-2V5a2 2 0 012-2h14a2 2 0 012 2v5.125\"/><path d=\"M14.62 17.8A2.25 2.25 0 1118 14.836a2.25 2.25 0 113.38 2.966l-2.626 2.856a.998.998 0 01-1.507 0z\"/><path d=\"M16 2v3\"/><path d=\"M3 9h18\"/><path d=\"M8 2v3\"/>","layers":"<path d=\"M12.83 2.18a2 2 0 0 0-1.66 0L2.6 6.08a1 1 0 0 0 0 1.83l8.58 3.91a2 2 0 0 0 1.66 0l8.58-3.9a1 1 0 0 0 0-1.83z\"/><path d=\"M2 12a1 1 0 0 0 .58.91l8.6 3.91a2 2 0 0 0 1.65 0l8.58-3.9A1 1 0 0 0 22 12\"/><path d=\"M2 17a1 1 0 0 0 .58.91l8.6 3.91a2 2 0 0 0 1.65 0l8.58-3.9A1 1 0 0 0 22 17\"/>","plus":"<path d=\"M5 12h14\"/><path d=\"M12 5v14\"/>","search":"<path d=\"m21 21-4.34-4.34\"/><circle cx=\"11\" cy=\"11\" r=\"8\"/>","x":"<path d=\"M18 6 6 18\"/><path d=\"m6 6 12 12\"/>","lock":"<rect width=\"18\" height=\"11\" x=\"3\" y=\"11\" rx=\"2\" ry=\"2\"/><path d=\"M7 11V7a5 5 0 0 1 10 0v4\"/>","clock":"<circle cx=\"12\" cy=\"12\" r=\"10\"/><path d=\"M12 6v6l4 2\"/>","check":"<path d=\"M20 6 9 17l-5-5\"/>","circle-check":"<circle cx=\"12\" cy=\"12\" r=\"10\"/><path d=\"m16 9-5.5 5.5L8 12\"/>","circle-alert":"<circle cx=\"12\" cy=\"12\" r=\"10\"/><line x1=\"12\" x2=\"12\" y1=\"8\" y2=\"12\"/><line x1=\"12\" x2=\"12.01\" y1=\"16\" y2=\"16\"/>","circle-x":"<circle cx=\"12\" cy=\"12\" r=\"10\"/><path d=\"m15 9-6 6\"/><path d=\"m9 9 6 6\"/>","users":"<path d=\"M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2\"/><path d=\"M16 3.128a4 4 0 0 1 0 7.744\"/><path d=\"M22 21v-2a4 4 0 0 0-3-3.87\"/><circle cx=\"9\" cy=\"7\" r=\"4\"/>","shield-check":"<path d=\"M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z\"/><path d=\"m9 12 2 2 4-4\"/>","key-round":"<path d=\"M2.586 17.414A2 2 0 0 0 2 18.828V21a1 1 0 0 0 1 1h3a1 1 0 0 0 1-1v-1a1 1 0 0 1 1-1h1a1 1 0 0 0 1-1v-1a1 1 0 0 1 1-1h.172a2 2 0 0 0 1.414-.586l.814-.814a6.5 6.5 0 1 0-4-4z\"/><circle cx=\"16.5\" cy=\"7.5\" r=\".5\" fill=\"currentColor\"/>","user-round":"<circle cx=\"12\" cy=\"8\" r=\"5\"/><path d=\"M20 21a8 8 0 0 0-16 0\"/>","log-out":"<path d=\"M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4\"/><polyline points=\"16 17 21 12 16 7\"/><line x1=\"21\" x2=\"9\" y1=\"12\" y2=\"12\"/>","chevron-up":"<path d=\"m18 15-6-6-6 6\"/>","chevron-left":"<path d=\"m15 18-6-6 6-6\"/>","menu":"<path d=\"M4 5h16\"/><path d=\"M4 12h16\"/><path d=\"M4 19h16\"/>","moon":"<path d=\"M20.985 12.486a9 9 0 1 1-9.473-9.472c.405-.022.617.46.402.803a6 6 0 0 0 8.268 8.268c.344-.215.825-.004.803.401\"/>","sun":"<circle cx=\"12\" cy=\"12\" r=\"4\"/><path d=\"M12 2v2\"/><path d=\"M12 20v2\"/><path d=\"m4.93 4.93 1.41 1.41\"/><path d=\"m17.66 17.66 1.41 1.41\"/><path d=\"M2 12h2\"/><path d=\"M20 12h2\"/><path d=\"m6.34 17.66-1.41 1.41\"/><path d=\"m19.07 4.93-1.41 1.41\"/>","refresh-cw":"<path d=\"M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8\"/><path d=\"M21 3v5h-5\"/><path d=\"M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16\"/><path d=\"M8 16H3v5\"/>","info":"<circle cx=\"12\" cy=\"12\" r=\"10\"/><path d=\"M12 16v-4\"/><path d=\"M12 8h.01\"/>","triangle-alert":"<path d=\"m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3\"/><path d=\"M12 9v4\"/><path d=\"M12 17h.01\"/>","sparkles":"<path d=\"M11.017 2.814a1 1 0 0 1 1.966 0l1.051 5.558a2 2 0 0 0 1.594 1.594l5.558 1.051a1 1 0 0 1 0 1.966l-5.558 1.051a2 2 0 0 0-1.594 1.594l-1.051 5.558a1 1 0 0 1-1.966 0l-1.051-5.558a2 2 0 0 0-1.594-1.594l-5.558-1.051a1 1 0 0 1 0-1.966l5.558-1.051a2 2 0 0 0 1.594-1.594z\"/><path d=\"M20 2v4\"/><path d=\"M22 4h-4\"/><circle cx=\"4\" cy=\"20\" r=\"2\"/>","bell":"<path d=\"M10.268 21a2 2 0 0 0 3.464 0\"/><path d=\"M3.262 15.326A1 1 0 0 0 4 17h16a1 1 0 0 0 .74-1.673C19.41 13.956 18 12.499 18 8A6 6 0 0 0 6 8c0 4.499-1.411 5.956-2.738 7.326\"/>"};
const ic=(n,s=18)=>`<svg class="ic" width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">${ICONS[n]||''}</svg>`;
const esc=x=>String(x).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const $=s=>document.querySelector(s),$$=s=>[...document.querySelectorAll(s)];
const BASE=Date.UTC(2026,10,14);
const day=d=>fmtDate(new Date(BASE+d*864e5));
const live=t=>{const el=$('#toast');el.textContent='';setTimeout(()=>{el.textContent=t},30);clearTimeout(live.t);live.t=setTimeout(()=>{el.textContent=''},4000)};
let themeMode=readThemeMode();
let theme=document.documentElement.classList.contains('dark')?'dark':'light';
/* mode: light | dark | system. `theme` is always the one actually showing. */
function setTheme(mode){themeMode=mode==='dark'||mode==='light'?mode:'system';theme=themeMode==='system'?(sysDark()?'dark':'light'):themeMode;document.documentElement.classList.toggle('dark',theme==='dark');document.documentElement.classList.toggle('light',theme!=='dark');try{localStorage.setItem(THEME_KEY,themeMode)}catch(e){}}
const COMPANY=typeof GLOBAL_COMPANY==='string'?GLOBAL_COMPANY:'';
const AMC_ID='cli_001';
const LEGACY_STORE='ea-hub-state';
const STORE_CH='ea-store';
let storeChan=null,applyingShared=false,sharedBound=false,storeSnapState='',pushingShared=false;
function emptyEnt(){return {ops:{st:'none'},home:{st:'none'},evt:{st:'none'},fin:{st:'none'}}}
function defaultApps(){return {ops:true,home:true,evt:false,fin:false}}
function emptyApps(){return {ops:false,home:false,evt:false,fin:false}}
function validApps(a){if(!a||typeof a!=='object')return null;const o={};let n=0;APP_KEYS.forEach(k=>{if(typeof a[k]==='boolean'){o[k]=a[k];n++}});return n?Object.assign({ops:false,home:false,evt:false,fin:false},o):null}
function storeGet(key){try{const r=localStorage.getItem(key);if(!r)return null;return JSON.parse(r)}catch(e){return null}}
function storeAllowed(key,list){return (list||[]).some(x=>x.endsWith(':*')?key.startsWith(x.slice(0,-1)):x===key)}
let storeClock=0,storeBatch=null;
function storeNow(){const t=Date.now();storeClock=t>storeClock?t:storeClock+1;return storeClock}
function storeT(v){return v&&typeof v==='object'&&typeof v._t==='number'?v._t:0}
function storeRole(){return typeof STORE_ROLE==='string'?STORE_ROLE:''}
function storePost(msg){if(!storeChan)return;try{storeChan.postMessage(Object.assign({from:storeRole()},msg))}catch(e){}}
function storeSet(key,val){if(applyingShared)return;const w=typeof STORE_WRITE==='function'?STORE_WRITE():[];if(!storeAllowed(key,w))return;if(val&&typeof val==='object'&&!Array.isArray(val))val=Object.assign({},val,{_t:storeNow()});try{localStorage.setItem(key,JSON.stringify(val))}catch(e){}if(pushingShared&&storeBatch)storeBatch.push({key,val});else storePost({key,val})}
function storeFromOk(key,from){const m=typeof STORE_FROM==='function'?STORE_FROM():{};return Object.keys(m).some(p=>storeAllowed(key,[p])&&(m[p]||[]).indexOf(from)>=0)}
function storeAccept(key,val,from){if(typeof key!=='string'||key.indexOf('ea:')!==0||val===undefined||val===null)return false;const r=typeof STORE_READ==='function'?STORE_READ():[];if(!storeAllowed(key,r)||!storeFromOk(key,from))return false;let raw=null,loc=null;try{raw=localStorage.getItem(key)}catch(e){}const next=JSON.stringify(val);if(raw===next)return false;if(raw!=null){try{loc=JSON.parse(raw)}catch(e){loc=null}}if(loc&&storeT(val)<=storeT(loc))return false;try{localStorage.setItem(key,next)}catch(e){return false}return true}
function storeOwnItems(){const w=typeof STORE_WRITE==='function'?STORE_WRITE():[];const out=[];try{for(let i=0;i<localStorage.length;i++){const k=localStorage.key(i);if(k&&k.indexOf('ea:')===0&&storeAllowed(k,w)){const v=storeGet(k);if(v!=null)out.push({key:k,val:v})}}}catch(e){}return out}
function storeOnMessage(e){const d=e&&e.data;if(!d||typeof d!=='object'||applyingShared)return;if(d.hello){const items=storeOwnItems();if(items.length)storePost({sync:true,items});return}if(pushingShared)return;const items=Array.isArray(d.items)?d.items:(d.key?[{key:d.key,val:d.val}]:[]);let any=false;items.forEach(it=>{if(it&&storeAccept(it.key,it.val,d.from))any=true});if(any||d.flush||d.sync)ingestShared();else if(d.key)storeOnChange(d.key)}
function storeOnChange(key){if(pushingShared||applyingShared)return;const r=typeof STORE_READ==='function'?STORE_READ():[];if(key&&!storeAllowed(key,r))return;ingestShared()}
function storeSnap(){const r=typeof STORE_READ==='function'?STORE_READ():[];const parts=[];try{for(let i=0;i<localStorage.length;i++){const k=localStorage.key(i);if(k&&k.indexOf('ea:')===0&&storeAllowed(k,r))parts.push(k+'\0'+(localStorage.getItem(k)||''))}}catch(e){}parts.sort();return parts.join('\n')}
function memStoreSig(){try{const st=window.__ea;if(!st)return '';return JSON.stringify({cat:st.catalog||null,ent:st.ent||null,grants:st.grants||null,reqs:st.reqs||null,clients:st.clients||null})}catch(e){return ''}}
function ingestShared(opts){const force=opts&&opts.force;const before=memStoreSig();applyingShared=true;try{if(typeof applyShared==='function')applyShared()}finally{applyingShared=false}const memChanged=memStoreSig()!==before;let n='';try{n=storeSnap()}catch(e){}const snapChanged=n!==storeSnapState;storeSnapState=n;if(sharedBound&&typeof render==='function'&&(force||snapChanged||memChanged))render()}
function ingestIfChanged(){let n='';try{n=storeSnap()}catch(e){return}if(n===storeSnapState)return;ingestShared()}
function shareReady(){sharedBound=true;ingestShared({force:true})}
function bindShared(){try{Object.defineProperty(window,'__ea',{get:()=>S,configurable:true})}catch(e){}try{localStorage.removeItem(LEGACY_STORE)}catch(e){}if(typeof seedStores==='function')seedStores();if(typeof applyShared==='function')applyShared();try{storeChan=new BroadcastChannel(STORE_CH);storeChan.onmessage=storeOnMessage;storePost({hello:true})}catch(e){storeChan=null}addEventListener('storage',e=>{if(e.key==null||(e.key&&e.key.indexOf('ea:')===0))storeOnChange(e.key)});addEventListener('focus',ingestIfChanged);addEventListener('visibilitychange',()=>{if(document.visibilityState==='visible')ingestIfChanged()});storeSnapState=storeSnap();setInterval(ingestIfChanged,200)}
function pushShared(){if(applyingShared||pushingShared)return;pushingShared=true;storeBatch=[];let items=[];try{if(typeof collectShared==='function')collectShared()}finally{pushingShared=false;items=storeBatch||[];storeBatch=null}storePost({flush:true,items})}
function reqTtl(r){const n=typeof REQ_TTL==='function'?REQ_TTL(r):14;return n>0?n:14}
function reqExpired(r){return r&&r.st==='pending'&&S.now-r.day>=reqTtl(r)}
function reqViewSt(r){return reqExpired(r)?'expired':r.st}
function nextReqId(reqs){let n=0;(reqs||[]).forEach(r=>{const m=String(r.id||'').match(/^req_(\d+)$/);if(m)n=Math.max(n,+m[1])});return 'req_'+String(n+1).padStart(3,'0')}
function pageCoSub(){if(!COMPANY)return '';return `<p class="sub co-sub"><span>${esc(COMPANY)}</span><span class="sample">Sample</span></p>`}


let searchQ='',searchOpen=false,searchIx=-1,searchHits=[];
function searchSettingsItems(){return SETTINGS_SECTIONS.filter(([k])=>k!=='org'||typeof settingsOrgHTML==='function').map(([key,label,terms])=>({group:'settings',key,label,terms,run:()=>openSettingsSection(key)}))}
function hdrSearchFits(text){
 const inp=$('#hdrSearch');if(!inp)return false;
 const cs=getComputedStyle(inp);
 const pad=(parseFloat(cs.paddingLeft)||0)+(parseFloat(cs.paddingRight)||0);
 const avail=inp.clientWidth-pad;
 const c=document.createElement('span');
 c.style.cssText='position:absolute;left:-9999px;white-space:nowrap;visibility:hidden;font:'+cs.font;
 c.textContent=text;document.body.appendChild(c);const w=c.offsetWidth;c.remove();
 return w<=avail+0.5;
}
function hdrSearchPlaceholder(){return hdrSearchFits('Search everything')?'Search everything':'Search'}
function filterSearch(q){
 const n=String(q||'').trim().toLowerCase();
 const idx=typeof searchIndex==='function'?searchIndex():{apps:[],buildings:[],people:[],clients:[],pages:[],settings:[]};
 const order=['apps','buildings','people','clients','pages','settings'];
 const labels={apps:'Apps',buildings:'Buildings',people:'People',clients:'Clients',pages:'Pages',settings:'Settings'};
 const leak=new RegExp(['htt'+'ps?:\\/\\/','w'+'ww\\.','local'+'host','/'+'v1\\b','\\bap'+'i\\.','\\btok'+'en\\b','\\bdom'+'ain\\b','u_\\d','cli_\\d','inv-','req_','ea:'].join('|'),'i');
 const out=[];
 if(!n)return out;
 order.forEach(g=>{(idx[g]||[]).forEach(item=>{
  const label=String(item.label||'');
  if(!label||leak.test(label))return;
  const terms=(label+' '+(g==='settings'?'settings':'')+' '+(item.terms||'')).toLowerCase();
  if(terms.includes(n))out.push({group:g,groupLabel:labels[g],label,run:item.run});
 })});
 return out;
}
function searchHTML(){
 if(typeof badRole==='function'&&badRole())return '';
 return `<div class="hdr-search" role="search"><label class="sr-only" for="hdrSearch">Search everything</label><span class="hdr-search-ic" aria-hidden="true">${ic('search',16)}</span><input id="hdrSearch" data-k="hdr-search" type="search" role="combobox" placeholder="Search" value="${esc(searchQ)}" autocomplete="off" aria-autocomplete="list" aria-expanded="false" aria-controls="hdrSearchList"><div id="hdrSearchLive" class="sr-only" aria-live="polite" aria-atomic="true"></div><div class="hdr-search-pop" id="hdrSearchPop" hidden><div id="hdrSearchList" role="listbox" aria-label="Search results"></div></div></div>`;
}
function announceSearch(n){
 const live=$('#hdrSearchLive');if(!live)return;
 live.textContent='';
 const msg=!String(searchQ||'').trim()?'':(n?`${n} result${n===1?'':'s'}`:'No results');
 setTimeout(()=>{const el=$('#hdrSearchLive');if(el)el.textContent=msg},30);
}
function paintSearch(){
 const inp=$('#hdrSearch'),list=$('#hdrSearchList'),pop=$('#hdrSearchPop');
 if(!inp||!list||!pop)return;
 const ph=hdrSearchPlaceholder();
 if(inp.placeholder!==ph)inp.placeholder=ph;
 if(document.activeElement===inp&&inp.value!==searchQ){searchQ=inp.value;searchIx=-1;if(String(searchQ).trim())searchOpen=true}
 else if(inp.value!==searchQ)inp.value=searchQ;
 searchHits=filterSearch(searchQ);
 if(!searchOpen||!String(searchQ).trim()){
  pop.hidden=true;inp.setAttribute('aria-expanded','false');inp.removeAttribute('aria-activedescendant');list.innerHTML='';
  if(!String(searchQ).trim()){const live=$('#hdrSearchLive');if(live)live.textContent=''}else announceSearch(0);
  return;
 }
 pop.hidden=false;inp.setAttribute('aria-expanded','true');
 if(!searchHits.length){
  list.innerHTML='<div class="hdr-search-empty" id="hdrSearchEmpty">No results</div>';
  inp.removeAttribute('aria-activedescendant');searchIx=-1;announceSearch(0);return;
 }
 const groups=[];
 searchHits.forEach((h,i)=>{if(!groups.length||groups[groups.length-1].g!==h.group)groups.push({g:h.group,label:h.groupLabel,items:[]});groups[groups.length-1].items.push({h,i})});
 list.innerHTML=groups.map(gr=>`<div role="group" aria-label="${esc(gr.label)}"><div class="hdr-search-h" aria-hidden="true">${esc(gr.label)}</div>${gr.items.map(({h,i})=>`<div role="option" class="hdr-search-opt" id="so-${i}" data-so="${i}" aria-selected="${i===searchIx?'true':'false'}">${esc(h.label)}</div>`).join('')}</div>`).join('');
 if(searchIx>=searchHits.length)searchIx=searchHits.length-1;
 if(searchIx>=0){inp.setAttribute('aria-activedescendant','so-'+searchIx);const el=$('#so-'+searchIx);el&&el.scrollIntoView({block:'nearest'})}
 else inp.removeAttribute('aria-activedescendant');
 announceSearch(searchHits.length);
}
function closeSearch(clear){
 searchOpen=false;searchIx=-1;
 if(clear){searchQ='';const inp=$('#hdrSearch');if(inp)inp.value=''}
 paintSearch();
}
function activateSearch(i){
 const hit=searchHits[i];if(!hit)return;
 closeSearch(true);
 if(typeof hit.run==='function')hit.run();
}
function bindSearch(){
 if(bindSearch.ok)return;bindSearch.ok=true;
 const onSearchInput=e=>{if(!e.target||e.target.id!=='hdrSearch')return;searchQ=e.target.value;searchOpen=true;searchIx=-1;paintSearch()};
 document.addEventListener('input',onSearchInput);
 document.addEventListener('change',onSearchInput);
 document.addEventListener('search',onSearchInput);
 document.addEventListener('focusin',e=>{if(e.target.id==='hdrSearch'&&String(searchQ).trim()){searchOpen=true;paintSearch()}});
 document.addEventListener('keydown',e=>{
  const inp=$('#hdrSearch');if(!inp)return;
  const inField=document.activeElement===inp||(e.target&&e.target.closest&&e.target.closest('#hdrSearchPop'));
  if(!inField)return;
  if(e.key==='ArrowDown'||e.key==='ArrowUp'){
   if(!searchOpen){searchOpen=true;paintSearch()}
   if(!searchHits.length)return;
   e.preventDefault();
   if(searchIx<0)searchIx=e.key==='ArrowDown'?0:searchHits.length-1;
   else searchIx=e.key==='ArrowDown'?(searchIx+1)%searchHits.length:(searchIx-1+searchHits.length)%searchHits.length;
   paintSearch();inp.focus();return;
  }
  if(e.key==='Enter'){if(searchOpen&&searchHits.length){e.preventDefault();activateSearch(searchIx<0?0:searchIx)}return}
  if(e.key==='Escape'){
   if(searchOpen){e.preventDefault();e.stopPropagation();closeSearch(false);inp.focus();return}
   if(searchQ){e.preventDefault();e.stopPropagation();closeSearch(true);inp.focus();return}
  }
 },true);
 document.addEventListener('click',e=>{
  if(e.target.closest&&(e.target.closest('#hdrSearchPop')||e.target.id==='hdrSearch'||e.target.closest('.hdr-search'))){
   const opt=e.target.closest('[data-so]');if(opt){activateSearch(+opt.dataset.so);return}return;
  }
  if(searchOpen)closeSearch(false);
 });
 addEventListener('resize',()=>{if($('#hdrSearch'))paintSearch()});
}
function paintTopbar(){
 $('#topbar').innerHTML=topbarHTML();
 const inp=$('#hdrSearch');
 if(!inp)return;
 if(inp.value!==searchQ)inp.value=searchQ;
 paintSearch();
 if(document.activeElement&&document.activeElement.id==='hdrSearch'){
  const c=inp.selectionStart;inp.focus();
  if(c!=null&&inp.setSelectionRange)try{inp.setSelectionRange(c,c)}catch(e){}
 }
}

function brandSide(sub){
  const line=COMPANY||sub||'';
  return `<div class="sb-head"><span class="sb-logo">${ic('layers',18)}</span><div class="sb-brand"><b>${esc(BRAND)}</b>${line?`<span>${esc(line)}</span>`:''}</div><button type="button" class="btn btn-icon sb-close" id="sbClose" data-k="sb-close" aria-label="Close menu">${ic('x',18)}</button></div>`;
}

function profileView(back){
  const u=me();
  const row=(k,v)=>`<div class="li"><span class="muted pf-k">${k}</span><span class="pf-v">${v}</span></div>`;
  const company=COMPANY||u.company||BRAND;
  const extra=typeof profileExtraRows==='function'?profileExtraRows(u,row):row('Assigned buildings',(u.props||[]).map(p=>P(p).name).join(', ')||'None yet');
  const hideAsk=typeof hideProfileAsk==='function'?hideProfileAsk(u):false;
  const lead=typeof PROFILE_INTRO==='string'?PROFILE_INTRO:'Your details.';
  return `<div id="profileView" class="page-narrow">${back||(typeof backBtn==='function'?backBtn():'')}<h1 style="margin-top:12px">Account profile</h1><p class="muted" style="margin:4px 0 0">${lead} <span class="sample">Sample</span></p>
 <div class="card" style="margin-top:14px"><div class="row"><span class="av av-lg" aria-hidden="true">${u.name.split(' ').map(x=>x[0]).join('')}</span><div><h2 class="h3">${esc(u.name)}</h2><div class="small muted">${ROLES[u.role]||'Not set up'}</div></div></div>
 <div class="list" style="margin-top:12px">${row('Name',esc(u.name))}${row('Email',`<span style="overflow-wrap:anywhere">${esc(u.email)}</span>`)}${row('Role',ROLES[u.role]||'Not set up')}${row('Company',`<span style="overflow-wrap:anywhere">${esc(company)}</span>`)}${extra}</div></div>
 ${hideAsk?'':'<p class="small muted" style="margin-top:12px">To change these details, ask your account admin.</p>'}</div>`;
}

function blockerView(){return `<div class="blocker" id="blocker"><span class="blk-ic">${ic('user-round',28)}</span><h1>Account not set up</h1><p class="muted" style="margin:0">Your account isn't set up yet. Please contact your admin.</p><button type="button" class="btn btn-outline" id="blkSignout" data-k="blk-signout">Sign out</button></div>`}

function acctHTML(){const u=me(),blocked=badRole(),ini=u.name.split(' ').map(x=>x[0]).join('');
 const it=(id,k,icn,label)=>`<button type="button" role="menuitem" tabindex="-1" id="${id}" data-k="${k}">${ic(icn,16)}<span>${label}</span></button>`;
 const roleLabel=ROLES[u.role]||'Not set up';
 const vis=`${ini} ${u.name} ${roleLabel}`;
 const avLabel=COMPANY?`Account menu, ${vis}, ${COMPANY}`:`Account menu, ${vis}`;
 return `<div class="hdr-acct" id="hdrAcct"><button type="button" class="av-btn" id="avBtn" data-k="av-btn" aria-label="${esc(avLabel)}" aria-haspopup="menu" aria-expanded="false" aria-controls="avMenu"><span class="av" aria-hidden="true">${ini}</span> <span class="who" aria-hidden="true"><b>${esc(u.name)}</b> <span>${ROLES[u.role]||'Not set up'}</span></span><span class="chev" aria-hidden="true">${ic('chevron-down',16)}</span></button>
 <div class="av-pop" id="avPop" hidden><div class="am-hd" aria-hidden="true"><span class="av">${ini}</span><div><b>${esc(u.name)}</b><span>${ROLES[u.role]||'Not set up'}</span>${COMPANY?`<span class="am-co">${esc(COMPANY)}</span>`:''}</div></div><div class="av-menu" id="avMenu" role="menu" aria-label="Account">${blocked?'':it('miProfile','mi-profile','user-round','Account profile')+it('miSettings','mi-settings','settings','Settings')}${it('demoBtn','demo-btn','sparkles','Preview options')}${it('miSignout','mi-signout','log-out','Sign out')}</div></div></div>`}

function topbarHTML(){
 const n=myNotes().filter(x=>!x.read).length;
 return `<button type="button" class="btn btn-icon btn-ghost menu-btn" id="menuBtn" data-k="menu-btn" aria-label="Open menu" aria-expanded="false" aria-controls="sidebar">${ic('menu',20)}</button>${searchHTML()}${badRole()?'':`<button type="button" class="btn btn-icon btn-ghost bell" id="bellBtn" data-k="bell" aria-label="Notifications${n?`, ${n} new`:''}">${ic('bell',20)}${n?`<span class="dotc" aria-hidden="true">${n}</span>`:''}</button>`}<button type="button" class="btn btn-icon btn-ghost" id="helpBtn" data-k="help" aria-label="Help and contact">${ic('info',20)}</button><button type="button" class="btn btn-icon btn-ghost" id="themeBtn" data-k="theme" aria-label="${theme==='dark'?'Switch to light mode':'Switch to dark mode'}">${ic(theme==='dark'?'sun':'moon',20)}</button>${acctHTML()}`;
}

function sessionHTML(){
 const out=S.session==='out';
  const signed=typeof SIGNOUT_LINE==='string'?SIGNOUT_LINE:"You're signed out of every EasySpot app.";
 return `<main class="signedout" id="main" tabindex="-1"><div class="card" id="${out?'signedOut':'sessionEnded'}"><span class="sb-logo" style="margin:0 auto 12px;width:48px;height:48px">${ic(out?'log-out':'clock',22)}</span><h1>${out?"You're signed out":'Your session ended'}</h1><p class="muted">${out?signed:'Sign in again to pick up where you left off.'}</p><button type="button" class="btn btn-primary btn-block" id="signIn" data-k="sign-in">${out?'Log in':'Log in again'}</button></div></main>`;
}

function ensureShell(){
 const root=$('#app');
 if(!$('#shell')){root.innerHTML=`<div class="shell" id="shell"><aside class="sidebar" id="sidebar" aria-label="Menu"></aside><div class="sb-scrim" id="sbScrim" hidden></div><div class="main"><header class="topbar" id="topbar"></header><div class="offline" id="offline" role="status" hidden>${ic('triangle-alert',16)}<span>You're offline. Apps will open again when you're back online.</span></div><main id="main" tabindex="-1"></main></div></div>`}
}

let modalStack=[];
function focusables(c){return [...c.querySelectorAll('button,select,input,textarea,a[href],[tabindex]')].filter(e=>!e.disabled&&e.tabIndex>=0&&!e.closest('[hidden]')&&e.getClientRects().length)}
function setInert(on){['#app','#demoStrip'].forEach(s=>{const e=$(s);if(e){if(on)e.setAttribute('inert','');else e.removeAttribute('inert')}})}
function openModal(id,html,opts={}){
 const opener=opts.opener||document.activeElement;
 const key=opener&&opener.dataset?opener.dataset.k:null;
 if(!(opener&&opener.closest&&opener.closest('#sidebar')))closeDrawer(false);
 const wrap=document.createElement('div');wrap.className='scrim';wrap.id=id;
 wrap.innerHTML=`<div class="dialog" role="dialog" aria-modal="true" aria-labelledby="${id}-t">${html}<button type="button" class="btn btn-icon btn-ghost x" data-close aria-label="Close">${ic('x',18)}</button></div>`;
 document.body.appendChild(wrap);
 modalStack.push({id,key,opener});setInert(true);
 wrap.addEventListener('mousedown',e=>{if(e.target===wrap)closeModal()});
 const dlg=wrap.querySelector('.dialog'),foot=dlg.querySelector('.sticky-acts');
 if(foot){const pad=()=>{dlg.style.scrollPaddingBottom=(foot.offsetHeight+8)+'px'};pad();dlg.addEventListener('focusin',e=>{if(e.target.matches&&e.target.matches('input,textarea,select')){pad();e.target.scrollIntoView({block:'nearest'})}})}
 const f=opts.focus?wrap.querySelector(opts.focus):focusables(wrap)[0];(f||dlg).focus();
 return wrap;}
function closeModal(opts={}){
 const m=modalStack.pop();if(!m)return;const el=$('#'+m.id);if(el)el.remove();
 if(!modalStack.length)setInert(false);
 render();
 if(opts.noReturn)return;
 let t=m.opener&&m.opener.isConnected?m.opener:(m.key?document.querySelector(`[data-k="${m.key}"]`):null);
 if(!t&&modalStack.length)t=focusables($('#'+modalStack.at(-1).id))[0];
 if(!t)t=$('#main');
 if(t)t.focus();}
function closeAll(){while(modalStack.length){const m=modalStack.pop();const el=$('#'+m.id);if(el)el.remove()}setInert(false)}

function openDrawer(){const sb=$('#sidebar');sb.classList.add('open');$('#sbScrim').hidden=false;$('#menuBtn').setAttribute('aria-expanded','true');['#topbar','#main','#demoStrip','#offline'].forEach(s=>$(s)&&$(s).setAttribute('inert',''));drawerOpen=true;const go=()=>{if(!drawerOpen)return;if(sb.contains(document.activeElement)&&document.activeElement!==sb)return;const f=$('#sbClose')||focusables(sb)[0];f&&f.focus()};go();requestAnimationFrame(()=>requestAnimationFrame(go));sb.addEventListener('transitionend',go,{once:true});setTimeout(go,260)}
let drawerOpen=false;
function closeDrawer(ret=true){if(!drawerOpen)return;drawerOpen=false;const sb=$('#sidebar');sb&&sb.classList.remove('open');$('#sbScrim')&&($('#sbScrim').hidden=true);['#topbar','#main','#demoStrip','#offline'].forEach(s=>$(s)&&$(s).removeAttribute('inert'));const mb=$('#menuBtn');if(mb){mb.setAttribute('aria-expanded','false');if(ret)mb.focus()}}
const isDesk=()=>matchMedia('(min-width:1024px)').matches;
function closeAv(ret){const mn=$('#avPop');if(mn&&!mn.hidden){mn.hidden=true;$('#avBtn').setAttribute('aria-expanded','false');if(ret)$('#avBtn').focus()}}
function openAv(last){const mn=$('#avPop');if(!mn)return;mn.hidden=false;$('#avBtn').setAttribute('aria-expanded','true');const it=[...$('#avMenu').querySelectorAll('[role=menuitem]')];(last?it.at(-1):it[0]).focus()}

function go(v){if(v!=='settings')settingsSaved=false;S.view=v;closeDrawer(false);render();$('#main').focus({preventScroll:true})}
function signOut(){closeAll();HubContract.markSession(me().id,'out');S.session='out';S.view=S.homeView||'hub';uiSave();HubContract.go('index','login')}

/* Notifications travel through the shared store: ea:notes:<topic>:<sender>.
   A page reads the topics it belongs to (NOTE_TOPICS) and writes one outbox per topic it talks to. */
function noteFrom(){return storeRole()}
function noteSelf(){return typeof NOTE_SELF==='string'?NOTE_SELF:me().id}
function notify(topic,type,text,extra){
 const key='ea:notes:'+topic+':'+noteFrom(),cur=storeGet(key);
 const items=cur&&cur.v===1&&Array.isArray(cur.items)?cur.items.slice():[];
 items.unshift(Object.assign({id:noteFrom()+'-'+storeNow(),type,text:String(text),day:S.now},extra||{}));
 storeSet(key,{v:1,items:items.slice(0,30)});
}
const note=(text,type)=>notify(noteSelf(),type||'appr',text);
function notesReadKey(){return 'ea-notes-read:'+me().id}
function notesRead(){try{const a=JSON.parse(localStorage.getItem(notesReadKey())||'[]');return Array.isArray(a)?a:[]}catch(e){return []}}
function myNotes(){
 const topics=typeof NOTE_TOPICS==='function'?NOTE_TOPICS():[noteSelf()],self=noteSelf(),from=noteFrom(),read=new Set(notesRead()),out=[];
 try{for(let i=0;i<localStorage.length;i++){
  const k=localStorage.key(i);if(!k||k.indexOf('ea:notes:')!==0)continue;
  const p=k.split(':'),topic=p[2],sender=p.slice(3).join(':');
  if(topics.indexOf(topic)<0||(sender===from&&topic!==self))continue;
  const v=storeGet(k);if(!v||v.v!==1||!Array.isArray(v.items))continue;
  v.items.forEach(n=>{if(!n||typeof n.id!=='string'||typeof n.text!=='string')return;
   const type=NOTIF_TYPES.some(x=>x[0]===n.type)?n.type:'apps';
   if(!notifOn(type)&&!(n.app&&n.ready&&SETTINGS.notifyReady[n.app]))return;
   out.push({id:n.id,type,text:n.text,day:typeof n.day==='number'?n.day:S.now,read:read.has(n.id),t:+(n.id.split('-').pop())||0})});
 }}catch(e){}
 return out.sort((a,b)=>b.t-a.t).slice(0,40);
}
function notesModal(){
 const ns=myNotes(),icn=n=>/not approved|turned off|removed|expired/i.test(n.text)?'circle-x':({req:'key-round',appr:'circle-check',apps:'layers',digest:'info'}[n.type]||'info');
 openModal('notesModal',`<h2 id="notesModal-t">Notifications</h2><div class="list" id="notesList" style="margin-top:10px">${ns.length?ns.map(n=>`<div class="li"><span>${ic(icn(n),16)}</span><div><div${n.read?'':' style="font-weight:600"'}>${esc(n.text)}</div><div class="small muted">${day(n.day)} (Sample)</div></div></div>`).join(''):'<p class="muted">Nothing new.</p>'}</div><div class="acts-col" style="margin-top:12px"><button type="button" class="btn btn-outline btn-block" id="notesSettings">Notification settings</button></div>`);
 try{localStorage.setItem(notesReadKey(),JSON.stringify(ns.map(n=>n.id).concat(notesRead()).filter((x,i,a)=>a.indexOf(x)===i).slice(0,200)))}catch(e){}
 $('#notesSettings').onclick=()=>{closeModal({noReturn:true});openSettingsSection('notifications')};
}

function helpModal(opener){
 const items=typeof helpItems==='function'?helpItems():[];
 const m=openModal('helpModal',`<h2 id="helpModal-t">Help and contact</h2><p class="lead">We're here to help you get the most from your EasySpot apps.</p>
 <div class="list" style="margin-top:8px">${items.map(([t,d])=>`<div class="li"><div><b>${t}</b><div class="small muted">${d}</div></div></div>`).join('')}</div>
 <div class="field"><label for="hpMsg">Message EasySpot support</label><textarea id="hpMsg" aria-describedby="hpHelp hpErr" placeholder="Tell us what you need help with"></textarea><span class="help" id="hpHelp">Demo only: nothing is sent (Sample).</span><div class="err" id="hpErr" hidden></div></div>
 <div class="acts-col sticky-acts"><button type="button" class="btn btn-primary btn-block" id="hpSend">Send message</button><button type="button" class="btn btn-outline btn-block" data-close>Close</button></div>`,{opener});
 m.querySelector('#hpSend').onclick=()=>{const v=m.querySelector('#hpMsg');if(v.value.trim().length<3){const e=m.querySelector('#hpErr');e.hidden=false;e.innerHTML=`${ic('circle-alert',15)}<span>Please write a short message first.</span>`;v.classList.add('is-err');v.setAttribute('aria-invalid','true');v.focus();return}closeModal();live('Message saved in this demo. Nothing was sent.')};
}

function handleChromeClick(t){
 if(t.matches('[data-close]')){closeModal();return true}
 if(t.id==='signIn'){HubContract.markSession(me().id,'out');HubContract.go('index','login');return true}
 if(t.id==='demoBtn'){closeAv(false);demoModal($('#avBtn'));return true}
 if(t.id==='menuBtn'){openDrawer();return true}
 if(t.id==='sbClose'||t.id==='sbScrim'){closeDrawer();return true}
 if(t.id==='themeBtn'){setTheme(theme==='dark'?'light':'dark');render();$('#themeBtn').focus();return true}
 if(t.id==='bellBtn'){notesModal();return true}
 if(t.id==='helpBtn'){helpModal(t);return true}
 if(t.id==='avBtn'){if($('#avPop').hidden)openAv();else closeAv(false);return true}
 if(t.id==='miProfile'){closeAv(false);go('profile');return true}
 if(t.id==='miSettings'){closeAv(false);go('settings');return true}
 if(settingsClick(t))return true;
 if(t.id==='blkSignout'){signOut();return true}
 if(t.id==='miSignout'){closeAv(false);closeDrawer(false);signOut();return true}
 if(t.dataset.go){go(t.dataset.go);return true}
 return false;
}

function bindChrome(){
 document.addEventListener('click',e=>{
  if(e.target.id==='sbScrim'){closeDrawer();return}
  if(!e.target.closest('#hdrAcct'))closeAv(false);
  const t=e.target.closest('button,[data-close]');
  if(!t)return;
  if(handleChromeClick(t))return;
  if(typeof handleRoleClick==='function')handleRoleClick(t);
 });
 document.addEventListener('keydown',e=>{
  if(e.key==='Escape'){if(modalStack.length){e.preventDefault();closeModal();return}const mn=$('#avPop');if(mn&&!mn.hidden){closeAv(true);return}if(drawerOpen){closeDrawer();return}}
  if(e.key==='Tab'){
   let c=modalStack.length?$('#'+modalStack.at(-1).id):(drawerOpen?$('#sidebar'):null);
   if(c){const f=focusables(c);if(!f.length)return;const i=f.indexOf(document.activeElement);
    if(e.shiftKey&&(i<=0)){e.preventDefault();f.at(-1).focus()}else if(!e.shiftKey&&(i===f.length-1||i===-1)){e.preventDefault();f[0].focus()}}
   const mn=$('#avPop');if(mn&&!mn.hidden&&!modalStack.length&&mn.contains(document.activeElement))closeAv(false)}
  const g=e.target.closest&&e.target.closest('.seg[role=radiogroup],[role=tablist]');
  if(g&&['ArrowLeft','ArrowRight','Home','End'].includes(e.key)){e.preventDefault();const bs=[...g.querySelectorAll('button')];const i=bs.indexOf(e.target);const n=e.key==='Home'?0:e.key==='End'?bs.length-1:(i+(e.key==='ArrowRight'?1:-1)+bs.length)%bs.length;bs[n].click();const nb=document.querySelector(`[data-k="${bs[n].dataset.k}"]`)||bs[n];nb.focus()}
  const mi=e.target.closest&&e.target.closest('#avMenu');
  if(mi&&['ArrowDown','ArrowUp','Home','End'].includes(e.key)){e.preventDefault();const bs=[...mi.querySelectorAll('[role=menuitem]')];const i=bs.indexOf(e.target);const n=e.key==='Home'?0:e.key==='End'?bs.length-1:(i+(e.key==='ArrowDown'?1:-1)+bs.length)%bs.length;bs[n].focus()}
  if(e.target.id==='avBtn'&&['ArrowDown','ArrowUp'].includes(e.key)){e.preventDefault();openAv(e.key==='ArrowUp')}
 });
 matchMedia('(min-width:1024px)').addEventListener('change',()=>{if(isDesk())closeDrawer(false)});
 addEventListener('offline',()=>{S.offline=true;render()});addEventListener('online',()=>{S.offline=false;render()});
 bindSearch();
}

/* What the person was looking at survives a trip into an app and back (per tab, per role). */
const UI_KEYS=['view','tab','prop','now','accessTab','staffPanel','matrixUser','client'];
function uiKey(){return 'ea-hub-ui:'+storeRole()}
function uiSave(){try{const o={};UI_KEYS.forEach(k=>{if(S[k]!==undefined&&S[k]!==null)o[k]=S[k]});sessionStorage.setItem(uiKey(),JSON.stringify(o))}catch(e){}}
function uiRestore(){let o=null;try{o=JSON.parse(sessionStorage.getItem(uiKey())||'null')}catch(e){}if(!o||typeof o!=='object')return false;UI_KEYS.forEach(k=>{if(o[k]!==undefined&&k in S&&(S[k]===null||typeof o[k]===typeof S[k]))S[k]=o[k]});if(S.view==='nf')S.view=S.homeView;return true}
/* Nobody gets a workspace without signing in at the front door (index.html). Returns false when leaving. */
function bootSession(){let rec=null;try{rec=localStorage.getItem(HubContract.sessionKey(me().id))}catch(e){}if(rec&&!HubContract.signedOut(me().id))return true;S.session='out';HubContract.go('index','login');return false}
