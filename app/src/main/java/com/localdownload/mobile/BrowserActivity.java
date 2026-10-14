package com.localdownload.mobile;
import android.app.*;import android.content.*;import android.graphics.Color;import android.net.Uri;import android.net.http.SslError;import android.os.Bundle;import android.view.*;import android.webkit.*;import android.widget.*;import org.json.*;import java.io.*;import java.net.URI;

public class BrowserActivity extends Activity {
    private WebView web;private EditText address;private CheckBox session;private Button save;private String capture="",inspect="";
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().setStatusBarColor(Color.WHITE);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(10),dp(12),dp(10),dp(8));setContentView(root);
        root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(dp(10),insets.getSystemWindowInsetTop()+dp(8),dp(10),insets.getSystemWindowInsetBottom()+dp(8));return insets;});
        LinearLayout nav=new LinearLayout(this);Button back=new Button(this);back.setText("‹");back.setOnClickListener(v->{if(web.canGoBack())web.goBack();else finish();});address=new EditText(this);address.setSingleLine();address.setTextSize(13);address.setInputType(17);Button go=new Button(this);go.setText("Go");nav.addView(back,new LinearLayout.LayoutParams(dp(48),dp(48)));nav.addView(address,new LinearLayout.LayoutParams(0,dp(48),1));nav.addView(go,new LinearLayout.LayoutParams(dp(56),dp(48)));root.addView(nav);
        web=new WebView(this);web.getSettings().setJavaScriptEnabled(true);web.getSettings().setDomStorageEnabled(true);web.getSettings().setAllowFileAccess(false);web.getSettings().setAllowContentAccess(false);web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);CookieManager.getInstance().setAcceptCookie(true);CookieManager.getInstance().setAcceptThirdPartyCookies(web,false);root.addView(web,new LinearLayout.LayoutParams(-1,0,1));
        session=new CheckBox(this);session.setText("Use this browser's signed-in session");session.setChecked(getSharedPreferences("settings",0).getBoolean("session",false));session.setOnCheckedChangeListener((button,checked)->getSharedPreferences("settings",0).edit().putBoolean("session",checked).apply());root.addView(session);
        save=new Button(this);save.setText("↓  Download this video");save.setAllCaps(false);save.setTextColor(Color.WHITE);save.setBackgroundColor(Color.rgb(36,107,253));root.addView(save,new LinearLayout.LayoutParams(-1,dp(52)));save.setOnClickListener(v->saveVideo());
        capture=asset("story-install.js");inspect=asset("story-inspect.js");
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){String url=request.getUrl().toString();if(Links.extract(url).isEmpty())return true;return !url.startsWith("https://");}
            @Override public void onPageStarted(WebView view,String url,android.graphics.Bitmap icon){address.setText(url);inject(url);}
            @Override public void onPageFinished(WebView view,String url){address.setText(url);CookieManager.getInstance().flush();inject(url);}
            @Override public void onReceivedSslError(WebView view,SslErrorHandler handler,SslError error){handler.cancel();toast("The site's certificate could not be verified.");}
        });
        go.setOnClickListener(v->{String url=Links.extract(address.getText().toString());if(url.startsWith("https://"))web.loadUrl(url);else toast("Enter an HTTPS video URL.");});
        String url=getIntent().getStringExtra("url");if(url==null||!url.startsWith("https://"))url="https://www.instagram.com/";web.loadUrl(url);
    }
    private void inject(String url){if(Links.site(url).equals("Facebook")&&!capture.isEmpty())web.evaluateJavascript(capture,null);}
    private void saveVideo(){String source=web.getUrl();if(source==null||Links.extract(source).isEmpty())return;if(Links.facebookStory(source)){
        save.setEnabled(false);web.evaluateJavascript(inspect,result->{save.setEnabled(true);try{Object value=new JSONTokener(result).nextValue();if(value instanceof String)value=new JSONTokener((String)value).nextValue();if(!(value instanceof JSONObject))throw new Exception("Play the story first, then retry. Photo-only or expired stories aren't supported.");String url=((JSONObject)value).optString("url");URI uri=new URI(url);boolean owned=uri.getHost()!=null&&(Links.belongs(uri.getHost(),"fbcdn.net")||Links.belongs(uri.getHost(),"fbsbx.com")||Links.belongs(uri.getHost(),"facebook.com"));if(!owned||!"https".equals(uri.getScheme())||uri.getUserInfo()!=null)throw new Exception("Invalid story video source.");begin(url,source);}catch(Exception error){toast(error.getMessage());}});
    }else begin(source,source);}
    private void begin(String url,String source){try{CookieManager.getInstance().flush();DownloadService.begin(this,url,source,session.isChecked());startActivity(new Intent(this,MainActivity.class).putExtra("url",source).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));finish();}catch(Exception error){new AlertDialog.Builder(this).setTitle("Cannot download").setMessage(error.getMessage()).setPositiveButton("OK",null).show();}}
    private String asset(String name){try(InputStream in=getAssets().open(name)){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)out.write(b,0,n);return out.toString("UTF-8");}catch(Exception e){return "null";}}
    private void toast(String message){Toast.makeText(this,message,Toast.LENGTH_LONG).show();}
    @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
    @Override protected void onDestroy(){web.stopLoading();web.destroy();super.onDestroy();}
}
