/* Hub Context Contract v1.
   The one file a child app needs. No dependencies; safe to load in the Hub and in any app.
   Hub side:   HubContract.build(launch, ctx)      -> launch URL carrying the context
               HubContract.parseReturn(hash)       -> what the app said when it came back
   App side:   HubContract.read(location.search)   -> {ok, ctx, errors}
               HubContract.backUrl(ctx, info)      -> where "Back to Hub" goes
               HubContract.notifyHub(ctx, note)    -> put a line under the Hub bell
               HubContract.signedOut(userId)       -> true once the Hub signed the user out */
(function(root){
 const VERSION=1;
 const FIELDS=['app','companyId','company','propertyId','propertyKey','property','role','userId','user','theme','locale','dateFormat','timeZone','returnUrl'];
 const REQUIRED=['app','companyId','role','userId','returnUrl'];
 const RETURN_MARK='#hub-return=';
 const CHANNEL='ea-store';

 /* Only ever go back to a page on this origin: no open redirects, no script URLs. */
 function safeUrl(u,base,hubOrigins){
  try{
   const here=new URL(base||root.location.href),x=new URL(String(u),here);
   if(!/^(https?|file):$/.test(x.protocol))return null;
   if(x.protocol===here.protocol&&x.host===here.host)return x.href;
   /* An app on another site lists the Hub origins it trusts, e.g. ['https://hub.example.com']. */
   return x.protocol==='https:'&&Array.isArray(hubOrigins)&&hubOrigins.indexOf(x.origin)>=0?x.href:null;
  }catch(e){return null}
 }

 function build(launch,ctx,base){
  const u=new URL(launch.url,base||root.location.href);
  u.searchParams.set('hub',String(VERSION));
  (launch.params||FIELDS).forEach(k=>{if(ctx[k]!=null&&ctx[k]!=='')u.searchParams.set(k,String(ctx[k]))});
  return u.href;
 }

 function read(search,opts){
  const o=opts||{},q=new URLSearchParams(search==null?root.location.search:search),ctx={},errors=[];
  if(q.get('hub')!==String(VERSION))errors.push('hub');
  FIELDS.forEach(k=>{const v=q.get(k);if(v!=null&&v!=='')ctx[k]=v});
  REQUIRED.concat(o.require||[]).forEach(k=>{if(!ctx[k])errors.push(k)});
  if(ctx.returnUrl){const s=safeUrl(ctx.returnUrl,o.base,o.hubOrigins);if(s)ctx.returnUrl=s;else{delete ctx.returnUrl;if(errors.indexOf('returnUrl')<0)errors.push('returnUrl')}}
  if(ctx.theme!=='dark'&&ctx.theme!=='light')delete ctx.theme;
  if(o.app&&ctx.app&&ctx.app!==o.app)errors.push('app');
  return {ok:!errors.length,ctx,errors};
 }

 function backUrl(ctx,info,opts){
  const base=ctx&&ctx.returnUrl?safeUrl(ctx.returnUrl,null,opts&&opts.hubOrigins):null;
  if(!base)return null;
  const payload=Object.assign({app:ctx.app},info||{});
  return base.split('#')[0]+RETURN_MARK+encodeURIComponent(JSON.stringify(payload));
 }

 function parseReturn(hash){
  const h=String(hash==null?root.location.hash:hash);
  if(h.indexOf(RETURN_MARK)!==0)return null;
  try{
   const o=JSON.parse(decodeURIComponent(h.slice(RETURN_MARK.length)));
   if(!o||typeof o!=='object'||typeof o.app!=='string')return null;
   return {app:o.app.slice(0,40),event:typeof o.event==='string'?o.event.slice(0,40):'',message:typeof o.message==='string'?o.message.slice(0,200):''};
  }catch(e){return null}
 }

 /* Notes live in the same store the Hub syncs: ea:notes:<topic>:<sender>. */
 function notifyHub(ctx,note){
  if(!ctx||!ctx.userId||!ctx.app||!note||!note.text)return false;
  const key='ea:notes:'+ctx.userId+':app_'+ctx.app,t=Date.now();
  let items=[];
  try{const cur=JSON.parse(root.localStorage.getItem(key)||'null');if(cur&&cur.v===1&&Array.isArray(cur.items))items=cur.items}catch(e){}
  items=[{id:'app_'+ctx.app+'-'+t,type:note.type||'apps',text:String(note.text).slice(0,200),app:ctx.app}].concat(items).slice(0,20);
  const val={v:1,items,_t:t};
  try{root.localStorage.setItem(key,JSON.stringify(val))}catch(e){return false}
  try{const ch=new BroadcastChannel(CHANNEL);ch.postMessage({from:'app',key,val});ch.close()}catch(e){}
  return true;
 }

 function sessionKey(userId){return 'ea-session:'+userId}
 function signedOut(userId){
  try{const o=JSON.parse(root.localStorage.getItem(sessionKey(userId))||'null');return !!(o&&o.out&&(!o.in||o.out>o.in))}catch(e){return false}
 }
 function markSession(userId,what){
  try{const k=sessionKey(userId),o=JSON.parse(root.localStorage.getItem(k)||'null')||{};o[what]=Date.now();root.localStorage.setItem(k,JSON.stringify(o))}catch(e){}
 }

 const PAGES=['index','client','staff','admin'];
 function go(page,hash){
  if(PAGES.indexOf(page)<0)return;
  if(root.EA_FRAMED&&root.parent!==root){root.parent.postMessage({eaNav:page,hash:hash||''},'*');return}
  root.location.assign(page+'.html'+(hash?'#'+hash:''));
 }
 root.HubContract={go,PAGES,VERSION,FIELDS,REQUIRED,build,read,backUrl,parseReturn,notifyHub,safeUrl,sessionKey,signedOut,markSession};
})(window);
