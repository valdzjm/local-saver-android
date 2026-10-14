"""CLI helpers for the dedicated emulator only. Never targets a connected phone."""
import subprocess,sys,xml.etree.ElementTree as ET,json,html
from pathlib import Path
root=Path(__file__).resolve().parent; out=root/'test-results';out.mkdir(exist_ok=True)
sys.stdout.reconfigure(encoding='utf-8')
adb=str(root/'.tools/sdk/platform-tools/adb.exe')
def run(*args,binary=False):
    result=subprocess.run([adb,'-s','emulator-5580',*args],capture_output=True,timeout=60)
    if result.returncode:raise RuntimeError(result.stderr.decode(errors='replace'))
    return result.stdout if binary else result.stdout.decode(errors='replace')
def ui():
    run('shell','uiautomator','dump','/sdcard/localsaver-test-ui.xml')
    xml=run('shell','cat','/sdcard/localsaver-test-ui.xml');(out/'ui.xml').write_text(xml,encoding='utf-8')
    return ET.fromstring(xml)
def history():
    xml=run('exec-out','run-as','com.localdownload.mobile','cat','shared_prefs/history.xml')
    data=ET.fromstring(xml).find("string[@name='jobs']")
    return json.loads(data.text) if data is not None else []
if __name__=='__main__':
    cmd=sys.argv[1]
    if cmd=='install':print(run('install','-r',str(root/'app/build/outputs/apk/debug/app-debug.apk')))
    elif cmd=='share':print(run('shell','am','start','-a','android.intent.action.SEND','-t','text/plain','--es','android.intent.extra.TEXT',sys.argv[2],'-n','com.localdownload.mobile/.MainActivity'))
    elif cmd=='ui':
        for node in ui().iter('node'):
            if node.get('text') or node.get('content-desc'):print(node.get('text'),node.get('content-desc'),node.get('bounds'))
    elif cmd=='tap':
        match=next(n for n in ui().iter('node') if n.get('text')==sys.argv[2]);import re
        x1,y1,x2,y2=map(int,re.findall(r'\d+',match.get('bounds')));print(run('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2)))
    elif cmd=='screenshot':
        path=out/(sys.argv[2] if len(sys.argv)>2 else 'screen.png');path.write_bytes(run('exec-out','screencap','-p',binary=True));print(path)
    elif cmd=='history':print(json.dumps(history(),ensure_ascii=False,indent=2))
    elif cmd=='shell':print(run('shell',*sys.argv[2:]))
    elif cmd=='logs':print(run('logcat','-d','-t','500','AndroidRuntime:E','*:S'))
