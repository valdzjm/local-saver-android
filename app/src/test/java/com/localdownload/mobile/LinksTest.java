package com.localdownload.mobile;
import org.junit.Test;
import static org.junit.Assert.*;
public class LinksTest {
    @Test public void shareCaptionExtractsRealLink(){assertEquals("https://www.youtube.com/shorts/TlRlDNHdmvk",Links.extract("Watch this: https://www.youtube.com/shorts/TlRlDNHdmvk)."));}
    @Test public void acceptsSocialLinkQuery(){assertEquals("https://www.instagram.com/reel/abc/?igsh=123",Links.extract("https://www.instagram.com/reel/abc/?igsh=123"));}
    @Test public void rejectsCredentialsAndMalformedLinks(){assertEquals("",Links.extract("https://user:pass@facebook.com/video"));assertEquals("",Links.extract("not a link"));assertEquals("",Links.extract(null));}
    @Test public void rejectsSpoofedSocialDomains(){assertEquals("Website",Links.site("https://facebook.com.evil.example/video"));assertEquals("Website",Links.site("https://notinstagram.com/reels/abc"));assertFalse(Links.facebookStory("https://facebook.com.evil.example/stories/123/"));}
    @Test public void detectsSocialShareDomains(){assertEquals("YouTube",Links.site("https://youtu.be/abc"));assertEquals("Facebook",Links.site("https://fb.watch/abc/"));assertEquals("Instagram",Links.site("https://www.instagram.com/reels/abc/"));assertTrue(Links.facebookStory("https://m.facebook.com/stories/123/456/"));}
    @Test public void sessionScopeRequiresHttps(){assertEquals("",Links.cookieDomain("http://www.facebook.com/video"));assertEquals("facebook.com",Links.cookieDomain("https://m.facebook.com/reel/123"));assertEquals("facebook.com.evil.example",Links.cookieDomain("https://facebook.com.evil.example/video"));}
}
