package com.localdownload.mobile;
import android.webkit.CookieManager;import java.io.*;import java.nio.charset.StandardCharsets;import java.util.*;
public final class Session {
    public static String cookies(String url){String domain=Links.cookieDomain(url);if(domain.isEmpty())return "";String value=CookieManager.getInstance().getCookie(url);return value==null?"":value;}
    public static File write(File folder,String url,String header)throws IOException{
        String domain=Links.cookieDomain(url);if(domain.isEmpty()||header==null||header.isEmpty())return null;
        File file=new File(folder,"session.txt");
        try(Writer writer=new OutputStreamWriter(new FileOutputStream(file),StandardCharsets.UTF_8)){
            writer.write("# Netscape HTTP Cookie File\n");
            for(String part:header.split(";")){int equal=part.indexOf('=');if(equal<1)continue;String name=part.substring(0,equal).trim(),value=part.substring(equal+1).trim();if((name+value).matches("(?s).*[\\r\\n\\t\\x00].*"))continue;writer.write("."+domain+"\tTRUE\t/\tTRUE\t0\t"+name+"\t"+value+"\n");}
        }return file;
    }
}
