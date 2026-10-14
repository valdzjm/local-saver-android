(function inspectFacebookStory() {
  const state=window.__localDownloadStoryCapture;
  if(!state||!location.pathname.startsWith('/stories/'))return null;
  const key=decodeURIComponent(location.pathname.split('/').filter(Boolean).pop());
  let decoded='';try{decoded=atob(key.replace(/-/g,'+').replace(/_/g,'/'));}catch{}
  const ids=new Set([key,decoded,decoded.split(':').pop()].filter(Boolean));
  let records=state.records.filter(r=>Date.now()-r.time<5*60*1000);
  for(const script of document.querySelectorAll('script[type="application/json"]')){
    if(script.textContent.length>4*1024*1024)continue;
    try{records.push(...state.scan(JSON.parse(script.textContent)));}catch{}
  }
  const matched=records.filter(r=>r.ids.some(id=>ids.has(id)));
  const chosen=matched.find(r=>r.hd)||matched.find(r=>r.sd);
  if(chosen)return {url:chosen.hd||chosen.sd,kind:'file',matched:true};
  const players=Array.from(document.querySelectorAll('video')).filter(v=>{const r=v.getBoundingClientRect();return !v.paused&&r.width>100&&r.height>80&&r.bottom>0&&r.top<innerHeight;}).sort((a,b)=>b.clientWidth*b.clientHeight-a.clientWidth*a.clientHeight);
  for(const player of players){const url=state.mediaUrl(player.currentSrc||player.src);if(url)return {url,kind:'file',matched:true};}
  const video=matched.find(r=>r.videoId);
  if(video)return {url:'https://www.facebook.com/watch/?v='+video.videoId,kind:'page',matched:true};
  return null;
})();