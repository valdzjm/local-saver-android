package com.localdownload.mobile;
import android.app.*;import android.content.*;import android.content.pm.ServiceInfo;import android.net.Uri;import android.os.*;import android.provider.MediaStore;import android.media.MediaMetadataRetriever;
import com.yausername.youtubedl_android.*;import com.yausername.ffmpeg.FFmpeg;import kotlin.Unit;
import java.io.*;import java.text.SimpleDateFormat;import java.util.*;import java.util.concurrent.*;

public class DownloadService extends Service {
    public static volatile Store.Job current;public static volatile boolean busy=false;private static volatile boolean cancelled=false;
    private final ExecutorService executor=Executors.newSingleThreadExecutor();private static final int NOTICE=1204;private static final String CHANNEL="downloads";
    public static void begin(Context c,String url,String source,boolean session){
        String link=Links.extract(url);if(link.isEmpty())throw new IllegalArgumentException("Paste a valid video link.");
        if(busy)throw new IllegalStateException("A download is already running. Stop it or wait for it to finish.");
        String cookie=session?Session.cookies(link):"";
        Intent intent=new Intent(c,DownloadService.class).setAction("download").putExtra("url",link).putExtra("source",source).putExtra("cookie",cookie);
        c.startForegroundService(intent);
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        if(intent==null){stopSelf();return START_NOT_STICKY;}
        if("cancel".equals(intent.getAction())){if(current!=null&&(intent.getStringExtra("id")==null||current.id.equals(intent.getStringExtra("id")))){cancelled=true;current.state="Stopping";final String processId=current.id;new Thread(()->{while(busy&&cancelled&&current!=null&&current.id.equals(processId)){if(YoutubeDL.INSTANCE.destroyProcessById(processId))break;try{Thread.sleep(150);}catch(InterruptedException ignored){break;}}}).start();}return START_NOT_STICKY;}
        if(busy)return START_NOT_STICKY;
        busy=true;cancelled=false;Store.Job job=new Store.Job();job.url=intent.getStringExtra("url");job.source=intent.getStringExtra("source");if(job.source==null)job.source=job.url;job.site=Links.site(job.source);job.title=job.site+" video";job.state="Preparing";current=job;Store.save(this,job);
        NotificationManager manager=getSystemService(NotificationManager.class);manager.createNotificationChannel(new NotificationChannel(CHANNEL,"Video downloads",NotificationManager.IMPORTANCE_LOW));
        startForeground(NOTICE,notification(job,true),ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        String cookie=intent.getStringExtra("cookie");intent.removeExtra("cookie");executor.submit(()->download(job,cookie,startId));return START_NOT_STICKY;
    }
    private void download(Store.Job job,String cookie,int startId){
        File work=new File(getFilesDir(),"downloads/"+job.id);File session=null;Uri pending=null;
        try{
            if(!work.mkdirs()&&!work.isDirectory())throw new IOException("Could not create the download folder.");
            job.state="Preparing video engine";notifyProgress(job);
            YoutubeDL.INSTANCE.init(this);FFmpeg.INSTANCE.init(this);checkCancelled();
            long last=getSharedPreferences("engine",0).getLong("updated",0);
            if(System.currentTimeMillis()-last>24*60*60*1000L){
                try{job.state="Updating video engine";notifyProgress(job);YoutubeDL.INSTANCE.updateYoutubeDL(this,YoutubeDL.UpdateChannel._STABLE);getSharedPreferences("engine",0).edit().putLong("updated",System.currentTimeMillis()).apply();}catch(Exception ignored){}
            }
            checkCancelled();session=Session.write(work,job.url,cookie);
            YoutubeDLRequest request=new YoutubeDLRequest(job.url);
            request.addOption("--ignore-config");request.addOption("--no-playlist");request.addOption("--playlist-items","1");request.addOption("--no-simulate");request.addOption("--newline");request.addOption("--no-color");request.addOption("--socket-timeout","25");request.addOption("--retries","2");
            request.addOption("-f","bv*[vcodec^=avc1][ext=mp4]+ba[ext=m4a]/b[ext=mp4]/bv*[ext=mp4]+ba[ext=m4a]/bv*+ba/bv*/b");
            request.addOption("--merge-output-format","mp4");request.addOption("--remux-video","mp4");request.addOption("--progress");request.addOption("--print","before_dl:LOCAL_TITLE:%(title)s");request.addOption("-o",new File(work,"video.%(ext)s").getAbsolutePath());
            if(session!=null)request.addOption("--cookies",session.getAbsolutePath());
            job.state="Downloading";notifyProgress(job);final long[] lastNotice={0};
            YoutubeDLResponse response=YoutubeDL.INSTANCE.execute(request,job.id,false,(progress,eta,line)->{
                job.progress=Math.max(0,Math.min(100,progress));job.state=line.contains("[Merger]")||line.contains("[VideoRemuxer]")?"Combining video and audio":"Downloading";
                if(System.currentTimeMillis()-lastNotice[0]>900){lastNotice[0]=System.currentTimeMillis();notifyProgress(job);}return Unit.INSTANCE;
            });
            checkCancelled();for(String line:response.getOut().split("\n"))if(line.startsWith("LOCAL_TITLE:")){String title=line.substring(12).trim();if(!title.isEmpty())job.title=title;}
            File video=new File(work,"video.mp4");if(!video.isFile()||video.length()==0)throw new IOException("No completed MP4 was produced.");
            MediaMetadataRetriever metadata=new MediaMetadataRetriever();try{metadata.setDataSource(video.getAbsolutePath());if(!"yes".equals(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)))throw new IOException("The downloaded file contains audio only, not a video.");}finally{metadata.release();}
            checkCancelled();job.state="Saving to your phone";notifyProgress(job);
            ContentValues values=new ContentValues();String title=job.title.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]","_");if(title.length()>100)title=title.substring(0,100);
            values.put(MediaStore.Video.Media.DISPLAY_NAME,title+"_"+new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(new Date())+".mp4");values.put(MediaStore.Video.Media.MIME_TYPE,"video/mp4");values.put(MediaStore.Video.Media.RELATIVE_PATH,Environment.DIRECTORY_MOVIES+"/LocalSaver");values.put(MediaStore.Video.Media.IS_PENDING,1);
            pending=getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,values);if(pending==null)throw new IOException("Could not save to the phone's video library.");
            try(InputStream input=new FileInputStream(video);OutputStream output=getContentResolver().openOutputStream(pending)){if(output==null)throw new IOException("Could not open the saved video.");byte[] buffer=new byte[65536];int count;while((count=input.read(buffer))!=-1){checkCancelled();output.write(buffer,0,count);}}
            checkCancelled();values.clear();values.put(MediaStore.Video.Media.IS_PENDING,0);getContentResolver().update(pending,values,null,null);job.uri=pending.toString();job.bytes=video.length();job.progress=100;job.state="Complete";pending=null;
        }catch(Exception error){job.state=cancelled||error instanceof YoutubeDL.CanceledException?"Stopped":"Error";job.error=job.state.equals("Stopped")?"Download cancelled. Share or paste the link again to retry.":friendly(error.getMessage());}
        finally{
            if(pending!=null)try{getContentResolver().delete(pending,null,null);}catch(Exception ignored){}
            if(session!=null)session.delete();clearWork(work);Store.save(this,job);stopForeground(STOP_FOREGROUND_REMOVE);getSystemService(NotificationManager.class).notify(NOTICE,notification(job,false));busy=false;stopSelfResult(startId);
        }
    }
    private void checkCancelled()throws InterruptedException{if(cancelled)throw new InterruptedException("Cancelled");}
    private void notifyProgress(Store.Job job){getSystemService(NotificationManager.class).notify(NOTICE,notification(job,true));}
    private Notification notification(Store.Job job,boolean ongoing){
        PendingIntent open=PendingIntent.getActivity(this,1,new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder=new Notification.Builder(this,CHANNEL).setSmallIcon(R.drawable.ic_notification).setContentTitle(job.state.equals("Complete")?"Video saved":job.title).setContentText(job.state.equals("Complete")?"Movies / LocalSaver":job.state).setContentIntent(open).setOnlyAlertOnce(true).setOngoing(ongoing).setAutoCancel(!ongoing);
        if(ongoing){builder.setProgress(100,(int)job.progress,job.progress==0);PendingIntent stop=PendingIntent.getService(this,2,new Intent(this,DownloadService.class).setAction("cancel").putExtra("id",job.id),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);builder.addAction(new Notification.Action.Builder(null,"Cancel",stop).build());}
        return builder.build();
    }
    private static String friendly(String message){String text=message==null?"Download failed.":message;if(text.length()>2500)text=text.substring(text.length()-2500);String lower=text.toLowerCase(Locale.US);if(lower.contains("login")||lower.contains("log in")||lower.contains("cookies"))return "This video needs a session. Open it in the in-app browser, sign in on the official site, enable Use in-app signed-in session, and retry.\n\n"+text;if(lower.contains("unsupported url"))return "This link is not supported. Open the individual video or reel rather than its feed/profile. Facebook stories need the in-app browser.\n\n"+text;return text;}
    private void clearWork(File folder){try{if(!folder.getCanonicalPath().startsWith(new File(getFilesDir(),"downloads").getCanonicalPath()+File.separator))return;File[] children=folder.listFiles();if(children!=null)for(File child:children){if(child.isDirectory())clearWork(child);else child.delete();}folder.delete();}catch(IOException ignored){}}
    @Override public void onTimeout(int startId,int type){cancelled=true;if(current!=null)YoutubeDL.INSTANCE.destroyProcessById(current.id);stopForeground(STOP_FOREGROUND_REMOVE);stopSelf();}
    @Override public void onDestroy(){super.onDestroy();executor.shutdown();}
    @Override public IBinder onBind(Intent intent){return null;}
}
