"""Project-local, checksum-verified Android build dependencies. No global install."""
import hashlib,json,os,shutil,urllib.request,xml.etree.ElementTree as ET,zipfile
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
ROOT=Path(__file__).resolve().parent;TOOLS=ROOT/'.tools';TOOLS.mkdir(exist_ok=True)
def get(url):
    return urllib.request.urlopen(urllib.request.Request(url,headers={'User-Agent':'LocalSaver-build/1.0'}),timeout=90)
def fetch(url,path,expected=None,algorithm='sha256'):
    path=Path(path)
    if not path.exists():
        print('Downloading',path.name,flush=True)
        with get(url) as response,path.open('wb') as output:shutil.copyfileobj(response,output,1024*1024)
    if expected:
        digest=hashlib.new(algorithm)
        with path.open('rb') as stream:
            for block in iter(lambda:stream.read(1024*1024),b''):digest.update(block)
        if digest.hexdigest().lower()!=expected.strip().lower():raise RuntimeError('Checksum mismatch: '+str(path))
    return path
def unpack(archive,destination):
    destination=Path(destination).resolve();destination.mkdir(parents=True,exist_ok=True)
    with zipfile.ZipFile(archive) as z:
        for member in z.infolist():
            target=(destination/member.filename).resolve()
            if not target.is_relative_to(destination):raise RuntimeError('Unsafe archive path')
        z.extractall(destination)
def java():
    with get('https://api.adoptium.net/v3/assets/latest/17/hotspot?architecture=x64&image_type=jdk&os=windows') as response:asset=json.load(response)[0]['binary']['package']
    archive=fetch(asset['link'],TOOLS/'jdk17.zip',asset['checksum']);unpack(archive,TOOLS/'jdk')
    return str(next((TOOLS/'jdk').glob('*/bin/java.exe')).parents[1])
def gradle():
    url='https://services.gradle.org/distributions/gradle-8.11.1-bin.zip'
    with get(url+'.sha256') as response:checksum=response.read().decode()
    archive=fetch(url,TOOLS/'gradle-8.11.1.zip',checksum);unpack(archive,TOOLS/'gradle');return str(TOOLS/'gradle/gradle-8.11.1')
def sdk():
    with get('https://dl.google.com/android/repository/repository2-1.xml') as response:doc=ET.fromstring(response.read())
    base=TOOLS/'sdk';base.mkdir(exist_ok=True)
    for package_id,dest in [('platforms;android-35','platforms/android-35-ext15'),('build-tools;35.0.0','build-tools/35.0.0'),('platform-tools','platform-tools')]:
        package=next(p for p in doc if p.tag.endswith('remotePackage') and p.attrib.get('path')==package_id)
        candidates=package.find('archives').findall('archive');archive=next(a for a in candidates if a.findtext('host-os') in (None,'windows'))
        complete=archive.find('complete');checksum=complete.findtext('checksum');url=complete.findtext('url')
        downloaded=fetch('https://dl.google.com/android/repository/'+url,TOOLS/Path(url).name,checksum,'sha1')
        temp=TOOLS/('extract-'+package_id.replace(';','-'));unpack(downloaded,temp)
        marker='android.jar' if package_id.startswith('platforms') else 'adb.exe' if package_id=='platform-tools' else 'aapt2.exe'
        source=next(temp.rglob(marker)).parent;target=base/dest
        shutil.copytree(source,target,dirs_exist_ok=True)
        print('Prepared',package_id,flush=True)
    return str(base)
with ThreadPoolExecutor(max_workers=3) as pool:
    futures=[pool.submit(fn) for fn in (java,gradle,sdk)];paths=[f.result() for f in futures]
(TOOLS/'paths.json').write_text(json.dumps(dict(zip(['java','gradle','sdk'],paths)),indent=2))
(ROOT/'local.properties').write_text('sdk.dir='+paths[2].replace('\\','/')+'\n')
print('Project-local build tools ready.',flush=True)
