// Vérifie que chaque slide Marp tient dans le cadre 1280x720.
// Usage : node check-slides.mjs [fichiers...]   (par défaut : ../cours/slides/*.md)
// Code de sortie 1 si au moins une slide déborde.
import { readFileSync, readdirSync } from 'node:fs';
import { join, resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { Marp } from '@marp-team/marp-core';
import puppeteer from 'puppeteer-core';
import { chromePath } from './chrome.mjs';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const themeCss = readFileSync(join(root, 'cours/theme/foodfast.css'), 'utf8');

const args = process.argv.slice(2);
const files = args.length
  ? args.map((f) => resolve(f))
  : readdirSync(join(root, 'cours/slides'))
      .filter((f) => f.endsWith('.md'))
      .sort()
      .map((f) => join(root, 'cours/slides', f));

const TOLERANCE = 1; // px

const browser = await puppeteer.launch({ executablePath: chromePath(), headless: true });
let failures = 0;

try {
  for (const file of files) {
    const marp = new Marp({ html: true, inlineSVG: false, math: false });
    marp.themeSet.add(themeCss);
    const { html, css } = marp.render(readFileSync(file, 'utf8'));

    const page = await browser.newPage();
    await page.setContent(`<!doctype html><meta charset="utf-8"><style>${css}</style>${html}`, {
      waitUntil: 'load',
    });
    await page.evaluateHandle('document.fonts.ready');

    const report = await page.evaluate((tol) => {
      return [...document.querySelectorAll('div.marpit > section')].map((s, i) => {
        const title = (s.querySelector('h1, h2, h3')?.textContent ?? '(sans titre)').trim();
        const problems = [];
        const dy = s.scrollHeight - s.clientHeight;
        if (dy > tol) problems.push(`déborde en bas de ${dy}px`);
        const dx = s.scrollWidth - s.clientWidth;
        if (dx > tol) problems.push(`déborde à droite de ${dx}px`);
        s.querySelectorAll('pre, table').forEach((el) => {
          const w = el.scrollWidth - el.clientWidth;
          if (w > tol) problems.push(`<${el.tagName.toLowerCase()}> trop large de ${w}px`);
        });
        const codeLines = [...s.querySelectorAll('pre')].reduce(
          (n, p) => n + p.textContent.replace(/\n$/, '').split('\n').length, 0);
        return { index: i + 1, title, problems, codeLines };
      });
    }, TOLERANCE);
    await page.close();

    const bad = report.filter((r) => r.problems.length);
    failures += bad.length;
    const rel = file.replace(root + '/', '');
    console.log(`\n${bad.length ? '✗' : '✓'} ${rel} — ${report.length} slides, ${bad.length} en débordement`);
    for (const r of bad) {
      console.log(`   slide ${r.index} « ${r.title} » : ${r.problems.join(', ')}`);
    }
  }
} finally {
  await browser.close();
}

process.exit(failures ? 1 : 0);
