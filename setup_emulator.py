import json,shutil,urllib.request,xml.etree.ElementTree as ET,zipfile,hashlib
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
root=Path(__file__).resolve().parent;tools=root/'.tools';sdk=tools/'sdk'
def get(url):return urllib.request.urlopen(urllib.request.Request(url,headers={'User-Agent':'LocalSaver-tests/1.0'}),timeout=120)
def install(repo,package_id,folder,marker):
    with get(repo) as response:doc=ET.fromstring(response.read())
    packages=[p for p in doc if p.tag.endswith('remotePackage') and p.attrib.get('path')==package_id];package=packages[-1]
    archive=next(a for a in package.find('archives').findall('archive') if a.findtext('host-os') in (None,'windows'));complete=archive.find('complete');url=complete.findtext('url');checksum=complete.findtext('checksum')
    base=repo.rsplit('/',1)[0]+'/';path=tools/Path(url).name
    if not path.exists():
        print('Downloading emulator test dependency',path.name,flush=True)
        with get(base+url) as response,path.open('wb') as output:shutil.copyfileobj(response,output,1024*1024)
    digest=hashlib.sha1()
    with path.open('rb') as source:
        for chunk in iter(lambda:source.read(1024*1024),b''):digest.update(chunk)
    if digest.hexdigest()!=checksum:raise RuntimeError('Checksum mismatch: '+str(path))
    extracted=tools/('unpack-'+folder.replace('/','-'));extracted.mkdir(exist_ok=True)
    with zipfile.ZipFile(path) as z:
        for m in z.infolist():
            if not (extracted/m.filename).resolve().is_relative_to(extracted.resolve()):raise RuntimeError('Unsafe archive')
        z.extractall(extracted)
    source=next(extracted.rglob(marker)).parent;destination=sdk/folder;shutil.copytree(source,destination,dirs_exist_ok=True);print('Prepared',package_id,flush=True)
with ThreadPoolExecutor(max_workers=2) as pool:
    calls=[pool.submit(install,'https://dl.google.com/android/repository/repository2-1.xml','emulator','emulator','emulator.exe'),pool.submit(install,'https://dl.google.com/android/repository/sys-img/google_apis/sys-img2-1.xml','system-images;android-35;google_apis;x86_64','system-images/android-35/google_apis/x86_64','system.img')]
    for call in calls:call.result()
avd=tools/'avd/LocalSaver.avd';avd.mkdir(parents=True,exist_ok=True)
config={'avd.ini.encoding':'UTF-8','AvdId':'LocalSaver','avd.ini.displayname':'LocalSaver Test','hw.device.name':'pixel_6','hw.lcd.width':'1080','hw.lcd.height':'2400','hw.lcd.density':'420','hw.ramSize':'2048','vm.heapSize':'256','disk.dataPartition.size':'4G','hw.cpu.arch':'x86_64','hw.cpu.ncore':'2','hw.gpu.enabled':'yes','hw.gpu.mode':'swiftshader_indirect','image.sysdir.1':str(sdk/'system-images/android-35/google_apis/x86_64'),'tag.id':'google_apis','abi.type':'x86_64','hw.keyboard':'yes','showDeviceFrame':'no','fastboot.forceColdBoot':'yes','PlayStore.enabled':'false'}
(avd/'config.ini').write_text('\n'.join(k+'='+v for k,v in config.items())+'\n')
(tools/'avd/LocalSaver.ini').write_text('avd.ini.encoding=UTF-8\npath='+str(avd.resolve())+'\ntarget=android-35\n')
print('Project-local emulator prepared.')
