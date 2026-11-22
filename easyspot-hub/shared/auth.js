/* Sign-in for the preview. One door for everyone: the account decides which workspace opens.
   This is a design mock: the accounts below live in the page, there is no server and no password is stored.
   A real build replaces authenticate() with a call to the identity service. */
const EaAuth=(function(){
 const ACCOUNTS=[
  {username:'client',email:'daniel.okafor@amcllc.sample',password:'1234',userId:'u_001',name:'Daniel Okafor',role:'Account admin',company:'AMC LLC',page:'client'},
  {username:'staff',email:'maria.lopez@amcllc.sample',password:'1234',userId:'u_002',name:'Maria Lopez',role:'Property manager',company:'AMC LLC',page:'staff'},
  {username:'admin',email:'priya.shah@easyspot.sample',password:'1234',userId:'u_010',name:'Priya Shah',role:'Internal admin',company:'EasySpot',page:'admin'}];
 const SIGNUPS='ea-auth-signups',LAST='ea-auth-last',REMEMBER='ea-auth-remember',TRIES='ea-auth-tries';
 const MAX_TRIES=5,LOCK_MS=30000;
 const get=(k,d)=>{try{const v=JSON.parse(localStorage.getItem(k)||'null');return v==null?d:v}catch(e){return d}};
 const set=(k,v)=>{try{localStorage.setItem(k,JSON.stringify(v))}catch(e){}};
 const norm=s=>String(s||'').trim().toLowerCase();
 const find=id=>ACCOUNTS.find(a=>a.username===norm(id)||a.email===norm(id));
 const signups=()=>{const a=get(SIGNUPS,[]);return Array.isArray(a)?a.filter(x=>x&&typeof x.email==='string'):[]};
 const pending=id=>signups().find(x=>x.email===norm(id));
 function lockedFor(){const t=get(TRIES,{n:0,until:0});return Math.max(0,Math.ceil(((t.until||0)-Date.now())/1000))}
 /* -> {ok:true, account} | {ok:false, reason:'locked'|'unverified'|'invalid', seconds?, email?} */
 function authenticate(id,password){
  const wait=lockedFor();if(wait)return {ok:false,reason:'locked',seconds:wait};
  const a=find(id);
  if(a&&String(password)===a.password){set(TRIES,{n:0,until:0});return {ok:true,account:a}}
  const p=pending(id);if(p)return {ok:false,reason:'unverified',email:p.email};
  const t=get(TRIES,{n:0,until:0}),n=(t.n||0)+1;
  if(n>=MAX_TRIES){set(TRIES,{n:0,until:Date.now()+LOCK_MS});return {ok:false,reason:'locked',seconds:LOCK_MS/1000}}
  set(TRIES,{n,until:0});return {ok:false,reason:'invalid',left:MAX_TRIES-n};
 }
 function start(account,remember){HubContract.markSession(account.userId,'in');set(LAST,account.username);if(remember)set(REMEMBER,account.username);else{try{localStorage.removeItem(REMEMBER)}catch(e){}}}
 /* The account this browser last used, if it is still signed in. */
 function current(){const a=find(get(LAST,''));if(!a)return null;let rec=null;try{rec=localStorage.getItem(HubContract.sessionKey(a.userId))}catch(e){}return rec&&!HubContract.signedOut(a.userId)?a:null}
 function register(d){
  const email=norm(d.email);
  if(find(email)||pending(email))return {ok:false,reason:'exists'};
  set(SIGNUPS,signups().concat({name:String(d.name).trim().slice(0,80),email,company:String(d.company).trim().slice(0,80),size:String(d.size).slice(0,12),at:Date.now()}).slice(-20));
  return {ok:true,email};
 }
 /* 0-4, plus what is still missing. */
 function strength(p){p=String(p||'');const miss=[];if(p.length<8)miss.push('8 characters');if(!/[0-9]/.test(p))miss.push('a number');if(!/[a-z]/i.test(p))miss.push('a letter');let s=0;if(p.length>=8)s++;if(p.length>=12)s++;if(/[0-9]/.test(p)&&/[a-z]/i.test(p))s++;if(/[^a-z0-9]/i.test(p)||(/[a-z]/.test(p)&&/[A-Z]/.test(p)))s++;return {score:p?Math.max(1,s):0,miss}}
 return {authenticate,start,current,register,strength,lockedFor,remembered:()=>get(REMEMBER,''),ACCOUNTS_COUNT:ACCOUNTS.length};
})();
