import argparse,json,os,subprocess
from pathlib import Path
root=Path(__file__).resolve().parent
parser=argparse.ArgumentParser();parser.add_argument('--abis',default='arm64-v8a,armeabi-v7a');parser.add_argument('tasks',nargs='*',default=['assembleDebug']);args=parser.parse_args()
paths=json.loads((root/'.tools/paths.json').read_text());env=os.environ.copy();env['JAVA_HOME']=paths['java'];env['ANDROID_HOME']=paths['sdk'];env['GRADLE_USER_HOME']=str(root/'.gradle');env['PATH']=str(Path(paths['java'])/'bin')+os.pathsep+env['PATH']
key=root/'.tools/localsaver.keystore'
if not key.exists():subprocess.run([str(Path(paths['java'])/'bin/keytool.exe'),'-genkeypair','-keystore',str(key),'-storepass','android','-keypass','android','-alias','localsaver','-keyalg','RSA','-keysize','2048','-validity','10000','-dname','CN=LocalSaver Personal,O=Personal,C=PH'],check=True,env=env)
command=[str(Path(paths['gradle'])/'bin/gradle.bat'),'--no-daemon','--console=plain',f'-PappAbis={args.abis}',*args.tasks]
raise SystemExit(subprocess.call(command,cwd=root,env=env))
