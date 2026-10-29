// Compile et exécute les exemples Java complets des supports et des slides (JDK 25).
//
// Un bloc ```java est vérifié s'il commence par :   // Fichier : Nom.java
// Options sur cette même ligne :
//   (avec A.java, B.java)  compile aussi les blocs A.java et B.java du même document
//   (exception attendue)   le programme doit s'arrêter sur une exception (pas une erreur de compilation)
//   (compilation seule)    compile sans exécuter (ex. classe sans main, test JUnit)
// Un bloc contenant une classe *Test est compilé puis lancé avec JUnit.
// Le classpath contient tools/lib/*.jar (H2, JUnit).
//
// Usage : node check-java.mjs [fichiers.md...]   (par défaut : cours/slides et cours/supports)
import { readFileSync, readdirSync, mkdirSync, writeFileSync, rmSync, existsSync } from 'node:fs';
import { join, resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { spawnSync } from 'node:child_process';
import { tmpdir } from 'node:os';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const libDir = join(root, 'tools/lib');
const jars = existsSync(libDir) ? readdirSync(libDir).filter((f) => f.endsWith('.jar')).map((f) => join(libDir, f)) : [];
const junitJar = jars.find((j) => j.includes('junit-platform-console-standalone'));

const args = process.argv.slice(2);
const docs = args.length
  ? args.map((f) => resolve(f))
  : ['cours/slides', 'cours/supports'].flatMap((d) =>
      existsSync(join(root, d))
        ? readdirSync(join(root, d)).filter((f) => f.endsWith('.md')).sort().map((f) => join(root, d, f))
        : []);

const HEADER = /^\s*\/\/\s*Fichier\s*:\s*(\w+\.java)(.*)$/;
const work = join(tmpdir(), `check-java-${process.pid}`);
let failures = 0;
let checked = 0;

const run = (cmd, argv, cwd) =>
  spawnSync(cmd, argv, { cwd, encoding: 'utf8', timeout: 30_000, input: '' });

for (const doc of docs) {
  const src = readFileSync(doc, 'utf8');
  const blocks = [];
  const re = /^```java\s*\n([\s\S]*?)^```/gm;
  let m;
  while ((m = re.exec(src))) {
    const line = src.slice(0, m.index).split('\n').length;
    const first = m[1].split('\n')[0];
    const h = first.match(HEADER);
    if (h) blocks.push({ name: h[1], opts: h[2], code: m[1], line });
  }
  const byName = new Map(blocks.map((b) => [b.name, b]));
  const rel = doc.replace(root + '/', '');
  const errors = [];

  for (const [i, b] of blocks.entries()) {
    checked++;
    const dir = join(work, `${i}`);
    rmSync(dir, { recursive: true, force: true });
    mkdirSync(dir, { recursive: true });
    const deps = (b.opts.match(/avec\s+([^)]*)\)/)?.[1] ?? '')
      .split(/[,\s]+/).filter((n) => n.endsWith('.java'));
    const missing = deps.filter((d) => !byName.has(d));
    if (missing.length) {
      errors.push(`l.${b.line} ${b.name} : dépendance introuvable ${missing.join(', ')}`);
      continue;
    }
    const files = [b, ...deps.map((d) => byName.get(d))];
    files.forEach((f) => writeFileSync(join(dir, f.name), f.code));

    const isTest = /class\s+\w+Test\b/.test(b.code);
    const compileOnly = /compilation seule/.test(b.opts);
    const expectException = /exception attendue/.test(b.opts);
    const cp = [dir, ...jars].join(':');

    const javac = run('javac', ['-encoding', 'UTF-8', '-d', join(dir, 'out'), '-cp', cp, ...files.map((f) => f.name)], dir);
    if (javac.status !== 0) {
      errors.push(`l.${b.line} ${b.name} : ne compile pas\n${indent(javac.stderr)}`);
      continue;
    }
    if (compileOnly) continue;

    let exec;
    if (isTest) {
      if (!junitJar) { errors.push(`l.${b.line} ${b.name} : JUnit absent de tools/lib`); continue; }
      exec = run('java', ['-jar', junitJar, 'execute', '--disable-banner', '--details=summary',
        '-cp', [join(dir, 'out'), ...jars].join(':'), '--select-class', b.name.replace('.java', '')], dir);
    } else {
      // Lancement en mode « fichier source » : fonctionne aussi pour les compact source files.
      exec = run('java', ['-cp', jars.join(':') || '.', b.name], dir);
    }
    const crashed = exec.status !== 0;
    if (exec.error) errors.push(`l.${b.line} ${b.name} : ${exec.error.message} (boucle infinie ou lecture clavier ?)`);
    else if (expectException && !crashed) errors.push(`l.${b.line} ${b.name} : une exception était attendue, le programme s'est terminé normalement`);
    else if (!expectException && crashed) errors.push(`l.${b.line} ${b.name} : échec à l'exécution\n${indent(exec.stderr || exec.stdout)}`);
  }

  failures += errors.length;
  console.log(`\n${errors.length ? '✗' : '✓'} ${rel} — ${blocks.length} exemples vérifiés, ${errors.length} en échec`);
  errors.forEach((e) => console.log(`   ${e}`));
}

rmSync(work, { recursive: true, force: true });
console.log(`\nTotal : ${checked} exemples, ${failures} en échec`);
process.exit(failures ? 1 : 0);

function indent(s) {
  return s.trim().split('\n').slice(0, 12).map((l) => `      ${l}`).join('\n');
}
