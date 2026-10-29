// Génère les deux supports de chaque séance :
//   - slides (Marp)       : cours/slides/*.md   -> outputs/slides/{html,pdf}
//   - supports détaillés  : cours/supports/*.md -> outputs/supports/{html,pdf}
// Usage : node build.mjs
import { readFileSync, readdirSync, mkdirSync, rmSync, writeFileSync, existsSync } from 'node:fs';
import { join, resolve, dirname, basename } from 'node:path';
import { fileURLToPath } from 'node:url';
import { marpCli } from '@marp-team/marp-cli';
import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js';
import puppeteer from 'puppeteer-core';
import { chromePath } from './chrome.mjs';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const out = join(root, 'outputs');
const theme = join(root, 'cours/theme/foodfast.css');
process.env.CHROME_PATH = chromePath();

const mdFiles = (dir) =>
  existsSync(dir) ? readdirSync(dir).filter((f) => f.endsWith('.md')).sort().map((f) => join(dir, f)) : [];

rmSync(out, { recursive: true, force: true });

// --- Slides ---------------------------------------------------------------
const slideFiles = [
  ...mdFiles(join(root, 'cours/slides')),
  ...mdFiles(join(root, 'qcms')).filter((f) => /^marp:\s*true/m.test(readFileSync(f, 'utf8'))),
];
for (const file of slideFiles) {
  const name = basename(file, '.md');
  console.log(`slides   ${name}`);
  const common = [file, '--theme-set', theme, '--html', '--allow-local-files', '--quiet', '--no-stdin'];
  for (const [fmt, ext] of [[[], 'html'], [['--pdf'], 'pdf']]) {
    const code = await marpCli([...common, ...fmt, '-o', join(out, 'slides', ext, `${name}.${ext}`)]);
    if (code !== 0) throw new Error(`Échec Marp pour ${file}`);
  }
}

// --- Supports détaillés -----------------------------------------------------
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: (str, lang) =>
    lang && hljs.getLanguage(lang)
      ? `<pre class="hljs"><code>${hljs.highlight(str, { language: lang, ignoreIllegals: true }).value}</code></pre>`
      : '',
});

const slug = (s) =>
  s.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');

// Ancres sur les titres h2/h3 et table des matières générée.
function renderSupport(src) {
  const tokens = md.parse(src, {});
  const toc = [];
  const used = new Map();
  tokens.forEach((t, i) => {
    if (t.type === 'heading_open' && (t.tag === 'h2' || t.tag === 'h3')) {
      const text = tokens[i + 1].content;
      let id = slug(text) || 'section';
      const n = used.get(id) ?? 0;
      used.set(id, n + 1);
      if (n) id += `-${n}`;
      t.attrSet('id', id);
      toc.push({ level: t.tag, text: md.renderInline(text), id });
    }
  });
  const body = md.renderer.render(tokens, md.options, {});
  const tocHtml = toc.length
    ? `<nav class="toc"><strong>Sommaire</strong><ul>${toc
        .map((h) => `<li class="${h.level}"><a href="#${h.id}">${h.text}</a></li>`)
        .join('')}</ul></nav>`
    : '';
  // Le sommaire s'insère juste après le titre principal.
  return body.replace(/(<\/h1>)/, `$1\n${tocHtml}`);
}

const supportCss = readFileSync(join(root, 'tools/support.css'), 'utf8');
const hljsCss = readFileSync(join(root, 'tools/node_modules/highlight.js/styles/github.css'), 'utf8');
const supportFiles = mdFiles(join(root, 'cours/supports'));
const browser = supportFiles.length
  ? await puppeteer.launch({ executablePath: process.env.CHROME_PATH, headless: true })
  : null;

try {
  for (const file of supportFiles) {
    const name = basename(file, '.md');
    console.log(`support  ${name}`);
    const src = readFileSync(file, 'utf8');
    const title = src.match(/^#\s+(.+)$/m)?.[1] ?? name;
    const html = `<!doctype html>
<html lang="fr"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${title}</title>
<style>${hljsCss}\n${supportCss}</style></head>
<body><main>${renderSupport(src)}</main></body></html>`;
    const htmlPath = join(out, 'supports', 'html', `${name}.html`);
    mkdirSync(dirname(htmlPath), { recursive: true });
    writeFileSync(htmlPath, html);

    const page = await browser.newPage();
    await page.setContent(html, { waitUntil: 'load' });
    const pdfPath = join(out, 'supports', 'pdf', `${name}.pdf`);
    mkdirSync(dirname(pdfPath), { recursive: true });
    await page.pdf({
      path: pdfPath,
      format: 'A4',
      printBackground: true,
      margin: { top: '18mm', bottom: '18mm', left: '16mm', right: '16mm' },
      displayHeaderFooter: true,
      headerTemplate: '<span></span>',
      footerTemplate: `<div style="font-size:8px;width:100%;text-align:center;color:#888">${title} — <span class="pageNumber"></span>/<span class="totalPages"></span></div>`,
    });
    await page.close();
  }
} finally {
  await browser?.close();
}

console.log(`\nTerminé : ${slideFiles.length} présentations, ${supportFiles.length} supports détaillés dans outputs/`);
