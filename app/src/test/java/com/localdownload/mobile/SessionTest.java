package com.localdownload.mobile;
import org.junit.Test;
import java.nio.file.*;
import java.io.File;
import static org.junit.Assert.*;
public class SessionTest {
    @Test public void writesOnlySelectedSiteSession() throws Exception {
        Path dir=Files.createTempDirectory("localsaver-session-test");
        try {File cookie=Session.write(dir.toFile(),"https://m.facebook.com/reel/123","c_user=test; token=value=with=equals");String text=new String(Files.readAllBytes(cookie.toPath()),java.nio.charset.StandardCharsets.UTF_8);assertTrue(text.contains(".facebook.com\tTRUE\t/\tTRUE\t0\tc_user\ttest"));assertTrue(text.contains("token\tvalue=with=equals"));assertFalse(text.contains("instagram.com"));}
        finally {Files.deleteIfExists(dir.resolve("session.txt"));Files.delete(dir);}
    }
    @Test public void rejectsCookieLineInjection() throws Exception {
        Path dir=Files.createTempDirectory("localsaver-session-test");
        try {File cookie=Session.write(dir.toFile(),"https://www.instagram.com/reel/123","safe=value; bad=evil\n.example.org\tTRUE\t/\tFALSE\t0\tx\ty");String text=new String(Files.readAllBytes(cookie.toPath()),java.nio.charset.StandardCharsets.UTF_8);assertTrue(text.contains("safe\tvalue"));assertFalse(text.contains("example.org"));assertFalse(text.contains("bad\t"));}
        finally {Files.deleteIfExists(dir.resolve("session.txt"));Files.delete(dir);}
    }
    @Test public void noCookieExportForInsecureOrAnonymousRequest() throws Exception {File dir=new File(System.getProperty("java.io.tmpdir"));assertNull(Session.write(dir,"http://facebook.com/video","secret=test"));assertNull(Session.write(dir,"https://facebook.com/video",""));}
}
