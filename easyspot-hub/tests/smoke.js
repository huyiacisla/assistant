/* End-to-end smoke test for the Hub. Run: node tests/smoke.js
   Serves this folder on localhost, opens admin + client + staff in one browser profile
   (same storage, like the three frames in preview.html) and walks the main flows. */
const http=require('http'),fs=require('fs'),path=require('path');
const {chromium}=require('playwright');
const ROOT=path.join(__dirname,'..');
const TYPES={'.html':'text/html','.js':'text/javascript','.css':'text/css'};
const server=http.createServer((q,r)=>{
 const f=path.join(ROOT,decodeURIComponent(q.url.split('?')[0]));
 if(!f.startsWith(ROOT)||!fs.existsSync(f)||fs.statSync(f).isDirectory()){r.writeHead(404);r.end();return}
 r.writeHead(200,{'content-type':TYPES[path.extname(f)]||'text/plain'});r.end(fs.readFileSync(f));
});
let pass=0,fail=0;const errors=[];
const ok=(c,name)=>{if(c){pass++;console.log('  ok  '+name)}else{fail++;console.log('FAIL  '+name)}};
const SHOT=process.env.SHOTS||'';

(async()=>{
 await new Promise(r=>server.listen(0,r));
 const base='http://localhost:'+server.address().port+'/';
 const browser=await chromium.launch();
 const ctx=await browser.newContext({viewport:{width:1280,height:900}});
 const open=async name=>{const p=await ctx.newPage();p.on('pageerror',e=>errors.push(name+': '+e.message));p.on('console',m=>{if(m.type()==='error')errors.push(name+' console: '+m.text())});await p.goto(base+name+'.html');return p};
 const state=(p,k)=>p.getAttribute(`[data-k="tile-${k}"]`,'data-state');
 const chip=(p,k)=>p.innerText(`#tc-${k}`);
 const bell=async p=>{const t=await p.getAttribute('#bellBtn','aria-label');const m=t.match(/(\d+) new/);return m?+m[1]:0};
 const settle=p=>p.waitForTimeout(450);
 const shot=async(p,n)=>{if(SHOT)await p.screenshot({path:path.join(SHOT,n+'.png')})};

 console.log('Registry and first paint');
 const admin=await open('admin');
 const client=await open('client');
 const staff=await open('staff');
 await client.click('#demoBtn');await client.click('#dSample');await settle(client);await settle(staff);
 ok(await client.locator('#grid .tile').count()===4,'client shows every registered app');
 ok(await state(client,'ops')==='open'&&await state(client,'home')==='new','client: ops open, home new');
 ok(await state(client,'evt')==='pending'&&(await chip(client,'evt')).includes('Asked EasySpot'),'client: seeded EasySpot request shows as pending');
 ok(await state(client,'fin')==='locked','client: fin locked until EasySpot turns it on');
 ok((await chip(client,'home')).includes('Coming soon'),'lifecycle badge: home is coming soon');
 ok(await state(staff,'ops')==='open','staff: granted app is open');
 ok(await state(staff,'home')==='request_access','staff: ungranted app asks for access');
 ok(await state(staff,'evt')==='locked','staff: app the company lacks is locked');
 await shot(client,'01-client-hub');

 console.log('Launch: building picker, context hand-off, return');
 await ctx.route('https://easyspotops.com/**',r=>r.fulfill({contentType:'text/html',body:'<title>stub</title>'}));
 const [tab]=await Promise.all([ctx.waitForEvent('page'),client.click('[data-k="tile-ops"]').then(()=>client.click('[data-pick="mc"]'))]);
 const ext=new URL(tab.url());
 ok(ext.origin==='https://easyspotops.com'&&ext.searchParams.get('hub')==='1'&&ext.searchParams.get('propertyId')==='prop_102'&&ext.searchParams.get('companyId')==='cli_001','registered app opens the real site in a new tab with the context');
 ok(!ext.searchParams.has('user')&&!ext.searchParams.has('company'),'names are not sent to the external site');
 ok(await client.isVisible('#grid')&&await client.inputValue('#propSel')==='mc','Hub stays open behind the new tab');
 await tab.close();
 /* The rest of the hand-off is checked against the bundled sample app (same site as the Hub). */
 await client.evaluate(()=>{appDef('ops').launch={type:'url',url:'apps/ops.html',target:'self',params:HubContract.FIELDS};setHubProp('all');render()});
 await client.click('[data-k="tile-ops"]');
 ok(await client.isVisible('#propModal'),'ops needs one building: picker opens when "All buildings"');
 await client.click('[data-pick="lp"]');
 await client.waitForURL(/apps\/ops\.html/);
 const u=new URL(client.url());
 ok(u.searchParams.get('hub')==='1'&&u.searchParams.get('companyId')==='cli_001'&&u.searchParams.get('propertyId')==='prop_101'&&u.searchParams.get('role')==='account_admin'&&!!u.searchParams.get('returnUrl'),'launch URL carries the contract fields');
 ok((await client.innerText('#appCtx')).includes('AMC LLC · Linden Park'),'app shows the company and building from the Hub');
 await shot(client,'02-ops-app');
 await client.click('#pingHub');
 await client.click('#backHub');
 await client.waitForURL(/client\.html$/);
 await settle(client);
 ok(await client.inputValue('#propSel')==='lp','back in the Hub with the building still selected');
 ok(await client.isVisible('[data-k="qRecent-ops"]'),'opened app is listed under Recent');
 ok(await bell(client)>=1,'app update reached the Hub bell');
 ok(await client.evaluate(()=>document.activeElement&&document.activeElement.dataset.k)==='tile-ops','focus returns to the app tile');

 console.log('Launch: other states');
 await client.click('[data-k="tile-home"]');
 ok(await client.isVisible('#amNotify'),'coming soon: preview with "tell me when ready"');
 await client.click('#amNotify');
 ok(await client.getAttribute('#amNotify','aria-checked')==='true','"tell me" switches on');
 await client.keyboard.press('Escape');
 await client.click('#demoBtn');await client.click('#dOffline');
 await client.click('[data-k="tile-ops"]');
 ok(await client.isVisible('#openErr'),'offline: says so instead of opening');
 await client.keyboard.press('Escape');
 await client.click('#demoBtn');await client.click('#dOffline');
 await client.click('#demoBtn');await client.click('#dFail');
 await client.click('[data-k="tile-ops"]');await client.waitForSelector('#openErr');
 ok(true,'failed open: error with Try again');
 await client.keyboard.press('Escape');
 await client.click('[data-k="pin-ops"]');
 ok(await client.isVisible('[data-k="qPinned-ops"]'),'pinning adds the app to Pinned');
 await client.click('[data-k="tab-pending"]');
 ok(await client.locator('#grid .tile').count()===1,'Pending filter shows only waiting apps');
 await client.focus('[data-k="tab-pending"]');await client.keyboard.press('ArrowLeft');
 ok(await client.getAttribute('[data-k="tab-ready"]','aria-checked')==='true','arrow keys move the filter');
 await client.click('[data-k="tab-all"]');

 console.log('Requests and notifications across roles');
 const sb=await bell(staff),cb=await bell(client);
 await staff.click('[data-k="tile-home"]');
 await staff.fill('#rqReason','I handle move-ins at this building');
 await staff.click('#rqSend');await settle(staff);await settle(client);
 ok(await state(staff,'home')==='pending'&&(await chip(staff,'home')).includes('Request pending'),'staff: sent request shows as pending');
 ok(await bell(client)===cb+1,'client bell rings for the new request');
 await client.click('[data-k="nav-access"]');await client.click('[data-k="at-requests"]');
 await client.click('[data-rqok]');await settle(staff);
 ok(await state(staff,'home')==='new','staff: approved request opens the app');
 ok(await bell(staff)>=sb+2,'staff bell rings for the approval');
 await client.click('[data-k="nav-hub"]');

 console.log('Ask EasySpot to turn an app on');
 const ab=await bell(admin);
 await client.click('[data-k="tile-fin"]');await client.click('#amAsk');
 await client.fill('#enReason','We need deposits tracking for both buildings');
 await client.click('#enSend');await settle(client);await settle(admin);
 ok(await state(client,'fin')==='pending','client: request to EasySpot shows as pending');
 ok(await bell(admin)===ab+1,'admin bell rings for the client request');
 await admin.click('[data-client="cli_001"]');
 ok((await admin.innerText('#main')).includes('deposits tracking'),'admin sees the request with its reason');
 const finRow=admin.locator('[data-req-row]',{hasText:'EasySpotFinance'});
 await finRow.locator('[data-rqok]').click();await settle(client);
 ok(await state(client,'fin')==='available','client: approved app can now be added');
 ok((await client.evaluate(()=>myNotes().map(n=>n.text).join('|'))).includes('EasySpot approved EasySpotFinance'),'client is told about the approval');

 console.log('System settings reach every Hub');
 await admin.click('#avBtn');await admin.click('#miSettings');
 await admin.selectOption('#sys-lc-ops','maintenance');await settle(client);await settle(staff);
 ok((await chip(client,'ops')).includes('Maintenance')&&(await chip(staff,'ops')).includes('Maintenance'),'maintenance shows on client and staff tiles');
 await staff.click('[data-k="tile-ops"]');
 ok(await staff.isVisible('#appModal')&&(await staff.innerText('#amWhy')).includes('maintenance'),'maintenance: tap explains instead of opening');
 await staff.keyboard.press('Escape');
 await admin.selectOption('#sys-lc-ops','live');await settle(staff);
 ok((await chip(staff,'ops')).includes('Ready to open'),'back to live everywhere');
 await shot(admin,'03-admin-system');

 console.log('Settings');
 await client.click('#avBtn');await client.click('#miSettings');
 await client.click('[data-k="set-density-compact"]');
 ok(await client.evaluate(()=>document.documentElement.classList.contains('dens-compact')),'compact tiles apply at once');
 await client.click('[data-k="set-theme-dark"]');
 ok(await client.evaluate(()=>document.documentElement.classList.contains('dark')),'dark theme applies at once');
 await client.selectOption('#set-f-dateFormat','iso');
 await client.selectOption('#set-f-defaultView','ready');
 await client.selectOption('#orgTtl','30');
 await client.fill('#org-b-mc','Maple Court East');await client.press('#org-b-mc','Tab');await settle(staff);
 await client.fill('#orgNew','Cedar Row');await client.click('[data-k="org-add"]');await settle(client);
 ok(await client.locator('#propSel option').count()===4,'new building appears in the building switcher');
 await client.fill('#orgNew','<img src=x onerror=alert(1)>');await client.click('[data-k="org-add"]');
 ok(await client.isVisible('#orgNewErr')&&await client.locator('#propSel option').count()===4,'building name with markup is refused');
 await client.fill('#orgNew','');
 await shot(client,'04-client-settings');
 await client.click('[data-k="ns-inapp-req"]');
 ok(await client.evaluate(()=>SETTINGS.notif.inapp.req)===false,'notification type can be turned off');
 await client.click('[data-k="ns-inapp-req"]');
 await client.evaluate(()=>sessionStorage.clear());
 await client.reload();await settle(client);
 ok(await client.evaluate(()=>document.documentElement.classList.contains('dark')&&document.documentElement.classList.contains('dens-compact')),'theme and density survive a reload');
 ok(await client.getAttribute('[data-k="tab-ready"]','aria-checked')==='true','Hub opens on the chosen default view');
 ok(await client.evaluate(()=>S.staff.some(u=>u.id==='u_002')&&reqTtl()===30&&PROPS.some(p=>p.name==='Cedar Row')),'organization settings survive a reload');
 await client.fill('#hdrSearch','time zone');
 ok((await client.innerText('#hdrSearchList')).includes('Language and region'),'search finds a setting by what it does');
 await client.fill('#hdrSearch','permits');
 ok((await client.innerText('#hdrSearchList')).includes('EasySpotOps'),'search finds an app by what it does');
 await client.fill('#hdrSearch','cedar');
 ok((await client.innerText('#hdrSearchList')).includes('Cedar Row'),'search finds a building');
 await client.fill('#hdrSearch','');
 await staff.click('#avBtn');await staff.click('#miSettings');
 ok(!(await staff.isVisible('#set-org')),'staff do not see organization settings');
 await staff.click('[data-k="back-hub"]');

 console.log('Session');
 await client.evaluate(()=>{lastActivity=Date.now()-31*6e4;sessionTick()});
 ok(await client.isVisible('#sessionEnded'),'idle session ends after the chosen time');
 await client.click('#signIn');
 const app=await ctx.newPage();app.on('pageerror',e=>errors.push('ops: '+e.message));
 await app.goto(base+'apps/ops.html');
 ok(await app.isVisible('#ctxMissing'),'app opened without the Hub refuses to guess a context');
 await app.goto(base+'apps/ops.html?hub=1&app=ops&companyId=cli_001&propertyId=prop_101&property=Linden+Park&role=viewer&userId=u_001&returnUrl='+encodeURIComponent('https://example.com/'));
 ok(await app.isVisible('#ctxMissing'),'app rejects a return address on another site');
 ok(await app.evaluate(()=>HubContract.safeUrl('https://hub.example.com/x',null,['https://hub.example.com'])==='https://hub.example.com/x'&&HubContract.safeUrl('https://evil.example.com/x',null,['https://hub.example.com'])===null),'app on another site returns only to Hub origins it lists');
 await client.click('#avBtn');await client.click('#miSignout');
 await app.goto(base+'apps/ops.html?hub=1&app=ops&companyId=cli_001&propertyId=prop_101&property=Linden+Park&role=viewer&userId=u_001&returnUrl='+encodeURIComponent(base+'client.html'));
 ok((await app.innerText('#main')).includes("You're signed out"),'signing out of the Hub closes the app too');
 await client.click('#signIn');

 console.log('Small screen');
 const phone=await browser.newContext({viewport:{width:360,height:740},storageState:await ctx.storageState()});
 for(const name of ['client','staff','admin']){
  const p=await phone.newPage();p.on('pageerror',e=>errors.push(name+'@360: '+e.message));
  await p.goto(base+name+'.html');await p.waitForSelector('#main');
  ok(await p.evaluate(()=>document.documentElement.scrollWidth<=window.innerWidth),name+': no sideways scroll at 360px');
  if(name==='client'){await p.click('[data-k="tab-all"]');const cols=await p.evaluate(()=>getComputedStyle(document.querySelector('#grid')).gridTemplateColumns.split(' ').length);ok(cols===2,'client: two-column grid on a phone');await shot(p,'05-client-phone')}
  if(name!=='admin'){await p.click('#avBtn');await p.click('#miSettings');ok(await p.evaluate(()=>document.documentElement.scrollWidth<=window.innerWidth),name+': settings fit at 360px')}
 }
 const prev=await ctx.newPage();prev.on('pageerror',e=>errors.push('preview: '+e.message));
 await prev.goto(base+'preview.html');await prev.waitForTimeout(600);
 ok(await prev.frameLocator('#adminFrame').locator('#main').isVisible(),'preview.html still frames the role pages');

 ok(errors.length===0,'no script errors'+(errors.length?': '+errors.slice(0,5).join(' || '):''));
 console.log(`\n${pass} passed, ${fail} failed`);
 await browser.close();server.close();process.exit(fail?1:0);
})().catch(e=>{console.error(e);server.close();process.exit(1)});
