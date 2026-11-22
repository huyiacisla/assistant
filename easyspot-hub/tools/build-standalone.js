/* Builds EasySpot Applicator (client.html + everything it loads) into one file that opens anywhere.
   node tools/build-standalone.js            -> dist/easyspot-applicator.html
   node tools/build-standalone.js --bare out -> same page without <html>/<head>/<body> wrappers, for hosts that add their own */
const fs=require('fs'),path=require('path');
const ROOT=path.join(__dirname,'..'),read=f=>fs.readFileSync(path.join(ROOT,f),'utf8');
let html=read('client.html');
html=html.replace('<link rel="stylesheet" href="shared/hub.css">',()=>'<style>\n'+read('shared/hub.css')+'</style>');
html=html.replace(/<script src="(shared\/[\w-]+\.js)"><\/script>/g,(m,f)=>{const js=read(f);if(/<\/script/i.test(js))throw new Error(f+' contains </script');return '<script>\n'+js+'</script>'});
if(/(src|href)="shared\//.test(html))throw new Error('a shared file was not inlined');
html=html.replace('<title>EasySpot Applicator · Hub</title>','<title>EasySpot Applicator</title>').replace('<script>\nconst BRAND=','<script>\nwindow.EA_STANDALONE=true;\nconst BRAND=');
if(!html.includes('window.EA_STANDALONE=true'))throw new Error('standalone flag not set');
fs.mkdirSync(path.join(ROOT,'dist'),{recursive:true});
fs.writeFileSync(path.join(ROOT,'dist','easyspot-applicator.html'),html);
const i=process.argv.indexOf('--bare');
if(i>0){
 const bare=html.replace(/<!doctype html>\s*/i,'').replace(/<\/?(html|head|body)[^>]*>\s*/gi,'').replace(/<meta (charset|name="viewport")[^>]*>\s*/g,'');
 fs.writeFileSync(process.argv[i+1],bare);
}
console.log('dist/easyspot-applicator.html',Math.round(html.length/1024)+' KB');
