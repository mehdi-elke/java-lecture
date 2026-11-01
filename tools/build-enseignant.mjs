// Génère les supports enseignant, si le dépôt privé prive/ est présent :
//   - slides de la séance, avec les slides privées (QCM, corrigé) insérées là où une note
//     de présentation contient « enseignant: chemin.md … »   -> prive/outputs/slides/{html,pdf}
//   - conducteur de séance : prive/enseignant/*.md          -> prive/outputs/conducteur/{html,pdf}
// Usage : node build-enseignant.mjs
import { readFileSync, readdirSync, mkdirSync, rmSync, writeFileSync, existsSync } from 'node:fs';
import { join, resolve, dirname, basename } from 'node:path';
import { fileURLToPath } from 'node:url';
import { marpCli } from '@marp-team/marp-cli';
import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js';
import puppeteer from 'puppeteer-core';
import { chromePath } from './chrome.mjs';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const prive = join(root, 'prive');
if (!existsSync(prive)) {
  console.log('prive/ absent : supports enseignant non générés');
  process.exit(0);
}
const out = join(prive, 'outputs');
const theme = join(root, 'cours/theme/foodfast.css');
process.env.CHROME_PATH = chromePath();

const mdFiles = (dir) =>
  existsSync(dir) ? readdirSync(dir).filter((f) => f.endsWith('.md')).sort().map((f) => join(dir, f)) : [];
const stripFrontMatter = (src) => src.replace(/^---\n[\s\S]*?\n---\n/, '').trim();

rmSync(out, { recursive: true, force: true });

// --- Slides enseignant ------------------------------------------------------
// Le marqueur doit être dans la dernière note de la slide : les slides insérées suivent la note.
const MARKER = /<!--((?:(?!-->)[\s\S])*?)^enseignant:[ \t]*(.+)$((?:(?!-->)[\s\S])*?)-->/gm;
let decks = 0;
for (const file of mdFiles(join(root, 'cours/slides'))) {
  const src = readFileSync(file, 'utf8');
  if (!src.match(MARKER)) continue;
  const name = basename(file, '.md');
  console.log(`enseignant slides  ${name}`);
  const merged = src
    .replace(/^(title:\s*"?)(.*?)("?)$/m, '$1$2 (enseignant)$3')
    .replace(MARKER, (comment, _before, paths) =>
      comment +
      paths
        .trim()
        .split(/\s+/)
        .map((p) => `\n\n---\n\n${stripFrontMatter(readFileSync(join(root, p), 'utf8'))}`)
        .join(''));
  const tmp = join(out, '.src', `${name}.md`);
  mkdirSync(dirname(tmp), { recursive: true });
  writeFileSync(tmp, merged);
  const common = [tmp, '--theme-set', theme, '--html', '--allow-local-files', '--quiet', '--no-stdin'];
  for (const [fmt, ext] of [[[], 'html'], [['--pdf'], 'pdf']]) {
    const code = await marpCli([...common, ...fmt, '-o', join(out, 'slides', ext, `${name}-enseignant.${ext}`)]);
    if (code !== 0) throw new Error(`Échec Marp pour ${file}`);
  }
  decks++;
}
rmSync(join(out, '.src'), { recursive: true, force: true });

// --- Conducteurs ------------------------------------------------------------
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: (str, lang) =>
    lang && hljs.getLanguage(lang)
      ? `<pre class="hljs"><code>${hljs.highlight(str, { language: lang, ignoreIllegals: true }).value}</code></pre>`
      : '',
});
const css = readFileSync(join(root, 'tools/support.css'), 'utf8');
const hljsCss = readFileSync(join(root, 'tools/node_modules/highlight.js/styles/github.css'), 'utf8');
const conducteurs = mdFiles(join(prive, 'enseignant'));
const browser = conducteurs.length
  ? await puppeteer.launch({ executablePath: process.env.CHROME_PATH, headless: true })
  : null;
try {
  for (const file of conducteurs) {
    const name = basename(file, '.md');
    console.log(`enseignant support ${name}`);
    const src = readFileSync(file, 'utf8');
    const title = src.match(/^#\s+(.+)$/m)?.[1] ?? name;
    const html = `<!doctype html>
<html lang="fr"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${title}</title>
<style>${hljsCss}\n${css}</style></head>
<body><main>${md.render(src)}</main></body></html>`;
    for (const ext of ['html', 'pdf']) mkdirSync(join(out, 'conducteur', ext), { recursive: true });
    writeFileSync(join(out, 'conducteur', 'html', `${name}.html`), html);
    const page = await browser.newPage();
    await page.setContent(html, { waitUntil: 'load' });
    await page.pdf({
      path: join(out, 'conducteur', 'pdf', `${name}.pdf`),
      format: 'A4',
      landscape: true,
      printBackground: true,
      margin: { top: '12mm', bottom: '12mm', left: '12mm', right: '12mm' },
    });
    await page.close();
  }
} finally {
  await browser?.close();
}

console.log(`\nTerminé : ${decks} présentations enseignant, ${conducteurs.length} conducteurs dans prive/outputs/`);
