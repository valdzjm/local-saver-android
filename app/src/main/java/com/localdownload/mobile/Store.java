package com.localdownload.mobile;
import android.content.*;import org.json.*;import java.util.*;
public final class Store {
    public static class Job {
        public volatile String id=UUID.randomUUID().toString(),url="",source="",site="",title="Video",state="Queued",error="",uri="";public volatile float progress=0;public volatile long bytes=0;
        public JSONObject json(){JSONObject o=new JSONObject();try{o.put("id",id);o.put("url",url);o.put("source",source);o.put("site",site);o.put("title",title);o.put("state",state);o.put("error",error);o.put("uri",uri);o.put("bytes",bytes);}catch(Exception ignored){}return o;}
        public static Job from(JSONObject o){Job j=new Job();j.id=o.optString("id",j.id);j.url=o.optString("url");j.source=o.optString("source",j.url);j.site=o.optString("site");j.title=o.optString("title","Video");j.state=o.optString("state","Stopped");if(!j.state.equals("Complete")&&!j.state.equals("Error")&&!j.state.equals("Stopped"))j.state="Stopped";j.error=o.optString("error");j.uri=o.optString("uri");j.bytes=o.optLong("bytes");return j;}
    }
    public static synchronized List<Job> history(Context c){List<Job> result=new ArrayList<>();try{JSONArray a=new JSONArray(c.getSharedPreferences("history",0).getString("jobs","[]"));for(int i=0;i<a.length();i++)result.add(Job.from(a.getJSONObject(i)));}catch(Exception ignored){}return result;}
    public static synchronized void save(Context c,Job job){List<Job> entries=history(c);entries.removeIf(j->j.id.equals(job.id));entries.add(0,job);JSONArray a=new JSONArray();for(int i=0;i<Math.min(entries.size(),100);i++)a.put(entries.get(i).json());c.getSharedPreferences("history",0).edit().putString("jobs",a.toString()).apply();}
    public static synchronized void remove(Context c,String id){JSONArray a=new JSONArray();for(Job j:history(c))if(!j.id.equals(id))a.put(j.json());c.getSharedPreferences("history",0).edit().putString("jobs",a.toString()).apply();}
}
