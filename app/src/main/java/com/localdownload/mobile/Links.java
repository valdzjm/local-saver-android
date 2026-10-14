package com.localdownload.mobile;
import java.net.URI;import java.util.regex.*;
public final class Links {
    private static final Pattern URL=Pattern.compile("https?://[^\\s<>\\\"]+",Pattern.CASE_INSENSITIVE);
    public static String extract(String text){
        if(text==null)return "";Matcher m=URL.matcher(text);if(!m.find())return "";String candidate=m.group().replaceAll("[).,\\]}>]+$","");
        try{URI u=new URI(candidate);if(u.getHost()==null||u.getUserInfo()!=null||candidate.length()>16000)return "";return u.toASCIIString();}catch(Exception ignored){return "";}
    }
    public static String site(String url){
        try{String host=new URI(url).getHost().toLowerCase();if(host.equals("youtu.be")||belongs(host,"youtube.com"))return "YouTube";if(host.equals("fb.watch")||belongs(host,"facebook.com"))return "Facebook";if(belongs(host,"instagram.com"))return "Instagram";return "Website";}catch(Exception e){return "Website";}
    }
    public static boolean belongs(String host,String root){return host.equals(root)||host.endsWith("."+root);}
    public static String cookieDomain(String url){
        try{URI u=new URI(url);if(!"https".equalsIgnoreCase(u.getScheme()))return "";String host=u.getHost().toLowerCase();for(String domain:new String[]{"facebook.com","instagram.com","youtube.com"})if(belongs(host,domain))return domain;return host;}catch(Exception e){return "";}
    }
    public static boolean facebookStory(String url){try{return site(url).equals("Facebook")&&new URI(url).getPath().startsWith("/stories/");}catch(Exception e){return false;}}
}
