import {installFacebookCapture,inspectFacebookStory} from './tools/facebook-story.js';
import {mkdirSync,writeFileSync} from 'node:fs';import {dirname,join} from 'node:path';import {fileURLToPath} from 'node:url';
const assets=join(dirname(fileURLToPath(import.meta.url)),'app/src/main/assets');mkdirSync(assets,{recursive:true});
writeFileSync(join(assets,'story-install.js'),'('+installFacebookCapture.toString()+')();');
writeFileSync(join(assets,'story-inspect.js'),'('+inspectFacebookStory.toString()+')();');
console.log('Prepared mobile Facebook story browser assets.');
