// Captura las paginas publicas de GitHub Actions del workflow de arquitectura.
// uso: node capturar-github.js <repo> <salida> <run-violacion> <run-revert>
const { chromium } = require('playwright');
const [repo, salida, runViolacion, runRevert] = process.argv.slice(2);
(async () => {
  const b = await chromium.launch();
  const p = await b.newPage({ viewport: { width: 1280, height: 900 }, deviceScaleFactor: 2 });
  const paginas = [
    [`https://github.com/${repo}/actions/workflows/arquitectura.yml`, 'github-historial-arquitectura.png'],
    [`https://github.com/${repo}/actions/runs/${runViolacion}`, 'github-run-violacion.png'],
    [`https://github.com/${repo}/actions/runs/${runRevert}`, 'github-run-revert.png'],
  ];
  for (const [url, archivo] of paginas) {
    await p.goto(url, { waitUntil: 'domcontentloaded' });
    await p.waitForTimeout(5000);
    await p.screenshot({ path: `${salida}/${archivo}`, fullPage: false });
    console.log('ok', archivo, url);
  }
  await b.close();
})();
