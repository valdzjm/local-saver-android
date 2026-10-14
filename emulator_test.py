"""Start only this project's hidden test emulator, on dedicated port 5580."""
import json, os, subprocess, time
from pathlib import Path
root=Path(__file__).resolve().parent; tools=root/'.tools'; sdk=tools/'sdk'
adb_dir=sdk/'platform-tools'
if not (adb_dir/'adb.exe').is_file():raise RuntimeError('Run setup_tools.py to prepare project-local Android tools first.')
adb=str(adb_dir/'adb.exe'); env=os.environ.copy()
env.update(ANDROID_HOME=str(sdk),ANDROID_AVD_HOME=str(tools/'avd'),ANDROID_USER_HOME=str(tools/'android-home'))
(tools/'android-home').mkdir(exist_ok=True)
log=(tools/'emulator.log').open('wb')
args=[str(sdk/'emulator/emulator.exe'),'-avd','LocalSaver','-port','5580','-no-window','-no-audio','-gpu','swiftshader_indirect','-no-boot-anim','-no-snapshot']
process=subprocess.Popen(args,env=env,stdout=log,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW)
(tools/'emulator-pid.txt').write_text(str(process.pid));print('Test emulator PID',process.pid,flush=True)
for i in range(120):
    if process.poll() is not None:raise RuntimeError('Emulator exited: '+str(process.returncode))
    result=subprocess.run([adb,'-s','emulator-5580','shell','getprop','sys.boot_completed'],capture_output=True,text=True,timeout=10)
    if result.stdout.strip()=='1':print('Android test emulator booted.',flush=True);break
    if i%6==0:print('Waiting for Android boot...',flush=True)
    time.sleep(5)
else:raise TimeoutError('Emulator boot timeout')
