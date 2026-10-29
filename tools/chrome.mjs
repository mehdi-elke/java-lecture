// Localise un Chrome/Chromium installé (surcharge possible via CHROME_PATH).
import { existsSync } from 'node:fs';

const CANDIDATES = [
  '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
  '/Applications/Chromium.app/Contents/MacOS/Chromium',
  '/usr/bin/google-chrome',
  '/usr/bin/chromium',
  '/usr/bin/chromium-browser',
  'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
];

export function chromePath() {
  const found = [process.env.CHROME_PATH, ...CANDIDATES].find((p) => p && existsSync(p));
  if (!found) {
    throw new Error('Chrome introuvable : définissez la variable CHROME_PATH.');
  }
  return found;
}
