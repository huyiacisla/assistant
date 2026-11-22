/* The outside of EasySpot Applicator: what a person sees before they sign in.
   Only the account-admin page (client.html) loads this; other pages keep the plain signed-out card. */
function landingHTML(){
 const u=me(),ini=u.name.split(' ').map(x=>x[0]).join(''),first=u.name.split(' ')[0],solo=!!window.EA_STANDALONE;
 const points=['One sign-in for every EasySpot app','Your building follows you into each app','Ask for access in two taps. Admins answer in one'];
 const steps=[['Sign in once','Your account opens the Hub and every app you have been given.'],['Pick your building','Choose it in the Hub. Each app starts on that building.'],['Open the app','Tap it. No second sign-in, no picking the building again.']];
 return `<main class="landing" id="main" tabindex="-1"><div id="landing">
 <header class="ld-nav"><span class="ld-brand"><span class="sb-logo">${ic('layers',18)}</span><b>${esc(BRAND)}</b></span><button type="button" class="btn btn-icon btn-ghost" id="themeBtn" data-k="theme" aria-label="${theme==='dark'?'Switch to light mode':'Switch to dark mode'}">${ic(theme==='dark'?'sun':'moon',20)}</button></header>
 <section class="ld-hero">
  <div class="ld-copy"><p class="ld-eyebrow">For property teams</p><h1>One front door to every EasySpot app.</h1>
   <p class="ld-lead">Parking, resident requests, events and finance open from one place, already on the right building.</p>
   <ul class="ld-points">${points.map(p=>`<li>${ic('circle-check',18)}<span>${p}</span></li>`).join('')}</ul></div>
  <div class="ld-sign card" id="signedOut"><h2>Sign in</h2><p class="small muted" style="margin:4px 0 0">Choose your account to open the Hub.</p>
   <div class="ld-acct"><span class="av av-lg" aria-hidden="true">${ini}</span><div><b>${esc(u.name)}</b><span class="small muted">${ROLES[u.role]||'Not set up'} · ${esc(CLIENT_COMPANY)}</span></div><span class="sample">Sample</span></div>
   <button type="button" class="btn btn-primary btn-block" id="signIn" data-k="sign-in">Continue as ${esc(first)}</button>
   <p class="small muted" style="margin:10px 0 0">Demo sign-in. No password needed.</p>
   ${solo?'':`<div class="ld-other"><span class="small muted">Not ${esc(first)}?</span><a href="staff.html">Staff sign-in</a><a href="admin.html">EasySpot team</a></div>`}</div>
 </section>
 <section class="ld-sec" aria-labelledby="ldApps"><h2 id="ldApps">What opens from your Hub</h2>
  <div class="ld-apps">${APP_REGISTRY.map(a=>{const lc=appLifecycle(a.id),live=lc==='live'||lc==='beta';return `<div class="ld-app"><span class="mini-ic ic-${a.id}">${ic(a.icon,20)}</span><div class="ld-app-t"><b>${a.name}</b><span class="small muted">${a.category}</span></div><span class="chip ${live?'c-open':'c-pending'}">${LIFECYCLE[lc].label}</span><p>${a.description}</p></div>`}).join('')}</div></section>
 <section class="ld-sec" aria-labelledby="ldHow"><h2 id="ldHow">How it works</h2>
  <ol class="ld-steps">${steps.map(([t,d])=>`<li><b>${t}</b><span>${d}</span></li>`).join('')}</ol></section>
 <footer class="ld-foot"><span>${esc(BRAND)}</span><span>Preview build · sample data</span></footer>
 </div></main>`;
}
