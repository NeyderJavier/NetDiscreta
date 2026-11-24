// =====================================================================
// NetDiscreta (versión web) — dibujos en SVG
// Equivalen a los lienzos Graphics2D del programa en Java.
// =====================================================================

export const C = {
  azul: '#1F3A5F', naranja: '#E67E22', gris: '#A0AAB9', grisClaro: '#E1E5EB',
  texto: '#333F4F', suave: '#6B7686', blanco: '#FFFFFF', fondoNaranja: 'rgba(230,126,34,0.35)',
};

export const esc = s => String(s).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

const texto = (x, y, t, extra = '') =>
  `<text x="${x}" y="${y}" font-family="Inter, Arial, sans-serif" ${extra}>${esc(t)}</text>`;

// ---------------------------------------------------------------------
// Grafo de la red (los nodos se arrastran desde app.js)
// ---------------------------------------------------------------------
export function dibujarGrafo(svg, g, camino = []) {
  if (!g.nodos.length) {
    svg.innerHTML = texto(20, 32, 'Agrega equipos o carga la red de ejemplo.', `font-size="15" fill="${C.suave}"`);
    return;
  }
  const enCamino = ar => camino.some((n, i) => i + 1 < camino.length &&
    ((ar.a === n && ar.b === camino[i + 1]) || (ar.b === n && ar.a === camino[i + 1])));
  let s = '';
  for (const ar of g.aristas) {
    const res = enCamino(ar);
    const mx = (ar.a.x + ar.b.x) / 2, my = (ar.a.y + ar.b.y) / 2;
    const w = String(ar.peso).length * 8 + 12;
    s += `<line x1="${ar.a.x}" y1="${ar.a.y}" x2="${ar.b.x}" y2="${ar.b.y}" stroke="${res ? C.naranja : C.gris}" stroke-width="${res ? 5 : 2.5}"/>`;
    s += `<rect x="${mx - w / 2}" y="${my - 11}" width="${w}" height="22" rx="8" fill="${C.blanco}"/>`;
    s += texto(mx, my + 5, ar.peso, `text-anchor="middle" font-size="14" fill="${res ? C.naranja : C.texto}" font-weight="${res ? 700 : 400}"`);
  }
  g.nodos.forEach((n, i) => {
    const res = camino.includes(n);
    s += `<g class="nodo" data-i="${i}" style="cursor:grab">` +
      `<circle cx="${n.x}" cy="${n.y}" r="26" fill="${res ? C.naranja : C.azul}" stroke="${C.blanco}" stroke-width="2.5"/>` +
      texto(n.x, n.y + 5, n.nombre, `text-anchor="middle" font-size="${n.nombre.length > 4 ? 11 : 13}" font-weight="700" fill="${C.blanco}" pointer-events="none"`) +
      `</g>`;
  });
  svg.innerHTML = s;
}

// ---------------------------------------------------------------------
// Diagrama de Venn: dos conjuntos dentro del universo U
// ---------------------------------------------------------------------
const REGIONES = {
  union: ['soloA', 'ambos', 'soloB'], inter: ['ambos'], difAB: ['soloA'], difBA: ['soloB'],
  sim: ['soloA', 'soloB'], compA: ['soloB', 'fuera'], compB: ['soloA', 'fuera'],
};

export function dibujarVenn(svg, a, b, nodos, opId) {
  if (!a || !b) {
    svg.innerHTML = texto(20, 32, 'Crea dos grupos y elige una operación.', `font-size="15" fill="${C.suave}"`);
    return;
  }
  const W = 600, H = 400, m = 24, r = 118, cy = 200, cxA = 230, cxB = 370;
  const U = `x="${m}" y="${m}" width="${W - 2 * m}" height="${H - 2 * m}"`;
  const circA = `cx="${cxA}" cy="${cy}" r="${r}"`, circB = `cx="${cxB}" cy="${cy}" r="${r}"`;
  let s = `<defs>
    <clipPath id="v-clipB"><circle ${circB}/></clipPath>
    <mask id="v-sinB"><rect ${U} fill="white"/><circle ${circB} fill="black"/></mask>
    <mask id="v-sinA"><rect ${U} fill="white"/><circle ${circA} fill="black"/></mask>
    <mask id="v-fuera"><rect ${U} fill="white"/><circle ${circA} fill="black"/><circle ${circB} fill="black"/></mask>
  </defs>`;
  const f = C.fondoNaranja;
  for (const reg of REGIONES[opId] || []) {
    if (reg === 'ambos') s += `<circle ${circA} fill="${f}" clip-path="url(#v-clipB)"/>`;
    if (reg === 'soloA') s += `<circle ${circA} fill="${f}" mask="url(#v-sinB)"/>`;
    if (reg === 'soloB') s += `<circle ${circB} fill="${f}" mask="url(#v-sinA)"/>`;
    if (reg === 'fuera') s += `<rect ${U} fill="${f}" mask="url(#v-fuera)"/>`;
  }
  s += `<rect ${U} fill="none" stroke="${C.gris}" stroke-width="2"/>`;
  s += `<circle ${circA} fill="none" stroke="${C.azul}" stroke-width="2.5"/><circle ${circB} fill="none" stroke="${C.azul}" stroke-width="2.5"/>`;
  s += texto(m + 10, m + 24, 'U', `font-size="17" font-weight="700" fill="${C.texto}"`);
  s += texto(cxA - r * 0.75, cy - r * 0.82, a.nombre, `font-size="16" font-weight="700" fill="${C.azul}" text-anchor="middle"`);
  s += texto(cxB + r * 0.75, cy - r * 0.82, b.nombre, `font-size="16" font-weight="700" fill="${C.azul}" text-anchor="middle"`);

  const soloA = [], ambos = [], soloB = [], fuera = [];
  for (const n of nodos) {
    const enA = a.elementos.has(n), enB = b.elementos.has(n);
    (enA && enB ? ambos : enA ? soloA : enB ? soloB : fuera).push(n.nombre);
  }
  const columna = (items, cx) => items.map((t, i) =>
    texto(cx, cy + (i - (items.length - 1) / 2) * 20 + 5, t, `text-anchor="middle" font-size="14" fill="${C.texto}"`)).join('');
  s += columna(soloA, cxA - r * 0.48) + columna(ambos, (cxA + cxB) / 2) + columna(soloB, cxB + r * 0.48);
  if (fuera.length) s += texto(W / 2, H - m - 12, fuera.join('   '), `text-anchor="middle" font-size="14" fill="${C.texto}"`);
  svg.innerHTML = s;
}

// ---------------------------------------------------------------------
// Plano cartesiano de A × B
// ---------------------------------------------------------------------
export function dibujarPlano(svg, a, b, listaA, listaB, pares) {
  if (!a || !b || !listaA.length || !listaB.length) {
    svg.innerHTML = texto(20, 32, 'Elige dos grupos no vacíos y genera A × B.', `font-size="15" fill="${C.suave}"`);
    return;
  }
  const W = 600, H = 420, izq = 80, der = 24, arriba = 50, abajo = 60;
  const ox = izq, oy = H - abajo;
  const px = (W - izq - der) / (listaA.length + 1), py = (H - abajo - arriba) / (listaB.length + 1);
  let s = '';
  listaA.forEach((n, i) => {
    const x = ox + (i + 1) * px;
    s += `<line x1="${x}" y1="${oy}" x2="${x}" y2="${arriba}" stroke="${C.grisClaro}" stroke-dasharray="4 4"/>`;
    s += texto(x, oy + 20, n.nombre, `text-anchor="middle" font-size="13" fill="${C.texto}"`);
  });
  listaB.forEach((n, j) => {
    const y = oy - (j + 1) * py;
    s += `<line x1="${ox}" y1="${y}" x2="${W - der}" y2="${y}" stroke="${C.grisClaro}" stroke-dasharray="4 4"/>`;
    s += texto(ox - 10, y + 4, n.nombre, `text-anchor="end" font-size="13" fill="${C.texto}"`);
  });
  s += `<line x1="${ox}" y1="${oy}" x2="${W - der}" y2="${oy}" stroke="${C.azul}" stroke-width="2"/>`;
  s += `<line x1="${ox}" y1="${oy}" x2="${ox}" y2="${arriba - 10}" stroke="${C.azul}" stroke-width="2"/>`;
  s += texto(W - der, oy + 44, a.nombre + ' (eje X)', `text-anchor="end" font-size="13" font-weight="700" fill="${C.azul}"`);
  s += texto(10, arriba - 22, b.nombre + ' (eje Y)', `font-size="13" font-weight="700" fill="${C.azul}"`);
  for (const p of pares) {
    const x = ox + (listaA.indexOf(p.a) + 1) * px, y = oy - (listaB.indexOf(p.b) + 1) * py;
    s += p.enRelacion
      ? `<circle cx="${x}" cy="${y}" r="8" fill="${C.naranja}"><title>(${esc(p.a.nombre)}, ${esc(p.b.nombre)}) ∈ R</title></circle>`
      : `<circle cx="${x}" cy="${y}" r="7" fill="${C.blanco}" stroke="${C.gris}" stroke-width="2"><title>(${esc(p.a.nombre)}, ${esc(p.b.nombre)}) ∉ R</title></circle>`;
  }
  const lx = W - der - 200, ly = 14;
  s += `<circle cx="${lx + 6}" cy="${ly + 6}" r="6" fill="${C.naranja}"/>` + texto(lx + 18, ly + 11, '(a, b) ∈ R: enlace directo', `font-size="12" fill="${C.texto}"`);
  s += `<circle cx="${lx + 6}" cy="${ly + 24}" r="6" fill="${C.blanco}" stroke="${C.gris}" stroke-width="2"/>` + texto(lx + 18, ly + 29, '(a, b) ∉ R', `font-size="12" fill="${C.texto}"`);
  svg.innerHTML = s;
}

// ---------------------------------------------------------------------
// Distribución binomial en barras (+ simulación como rombos)
// ---------------------------------------------------------------------
export function dibujarBarras(svg, b, ev, k, simulada) {
  const W = 640, H = 300, izq = 58, der = 16, arriba = 44, abajo = 46;
  const ancho = W - izq - der, alto = H - arriba - abajo, oy = arriba + alto, n = b.n;
  let max = 0;
  for (let i = 0; i <= n; i++) max = Math.max(max, b.probabilidad(i));
  if (simulada) for (const v of simulada) max = Math.max(max, v);
  max = Math.min(1, Math.ceil(max * 10 + 1e-9) / 10) || 0.1;

  let s = texto(izq, 22, `X ~ B(n = ${n}, p = ${b.p.toFixed(2)})  ·  P(X = k)`, `font-size="14" font-weight="700" fill="${C.azul}"`);
  for (let i = 0; i <= 5; i++) {
    const v = max * i / 5, y = oy - alto * v / max;
    s += `<line x1="${izq}" y1="${y}" x2="${izq + ancho}" y2="${y}" stroke="${C.grisClaro}"/>`;
    s += texto(izq - 8, y + 4, v.toFixed(2), `text-anchor="end" font-size="11" fill="${C.suave}"`);
  }
  const paso = ancho / (n + 1), ab = Math.max(4, paso * 0.6);
  for (let i = 0; i <= n; i++) {
    const v = b.probabilidad(i), cx = izq + paso * (i + 0.5), h = alto * v / max;
    const enEv = ev && ev.ocurre(i, k);
    s += `<rect x="${cx - ab / 2}" y="${oy - h}" width="${ab}" height="${h}" fill="${enEv ? C.naranja : '#A0B4D2'}"><title>P(X = ${i}) = ${v.toFixed(6)}</title></rect>`;
    let yt = oy - h - 5;
    if (simulada) yt = Math.min(yt, oy - alto * simulada[i] / max - 10);
    if (n <= 12 && v >= 0.0005) s += texto(cx, yt, v.toFixed(3), `text-anchor="middle" font-size="11" fill="${C.texto}"`);
    if (n <= 25 || i % 5 === 0) s += texto(cx, oy + 16, i, `text-anchor="middle" font-size="11" fill="${C.texto}"`);
    if (simulada) {
      const sy = oy - alto * simulada[i] / max;
      s += `<path d="M${cx} ${sy - 6}L${cx + 6} ${sy}L${cx} ${sy + 6}L${cx - 6} ${sy}Z" fill="${C.texto}"><title>Simulada: ${simulada[i].toFixed(4)}</title></path>`;
    }
  }
  s += `<line x1="${izq}" y1="${oy}" x2="${izq + ancho}" y2="${oy}" stroke="${C.azul}" stroke-width="1.5"/>`;
  s += texto(izq + ancho / 2, oy + 36, 'k = número de bits con error', `text-anchor="middle" font-size="12" font-weight="700" fill="${C.azul}"`);
  const lx = izq + ancho - 250;
  if (ev) s += `<rect x="${lx}" y="12" width="12" height="10" fill="${C.naranja}"/>` + texto(lx + 18, 22, 'Evento ' + ev.cond.replace('k', k), `font-size="11" fill="${C.texto}"`);
  if (simulada) s += `<path d="M${lx + 140} 11L${lx + 146} 17L${lx + 140} 23L${lx + 134} 17Z" fill="${C.texto}"/>` + texto(lx + 152, 22, 'Simulación', `font-size="11" fill="${C.texto}"`);
  svg.innerHTML = s;
}

// ---------------------------------------------------------------------
// Circuito de paridad: cadena de compuertas XOR
// ---------------------------------------------------------------------
export function dibujarCircuito(svg, bits, titulo, salida) {
  const W = 640, H = 320;
  let s = texto(14, 24, titulo, `font-size="14" font-weight="700" fill="${C.azul}"`);
  if (!bits) { svg.innerHTML = s + texto(14, 54, 'Escribe una cadena binaria para ver el circuito.', `font-size="13" fill="${C.suave}"`); return; }
  if (bits.length > 16) { svg.innerHTML = s + texto(14, 54, 'El circuito se dibuja para tramas de hasta 16 bits.', `font-size="13" fill="${C.suave}"`); return; }

  const n = bits.length, x0 = 66, arriba = 44, abajo = 16;
  const dy = (H - arriba - abajo) / n;
  const dx = Math.min(110, (W - x0 - 110) / Math.max(1, n - 1));
  const gw = Math.min(42, dx * 0.6);
  const pref = salida === 'S' ? 'r' : 'b';
  const y = Array.from({ length: n }, (_, i) => arriba + dy * (i + 0.5));
  const bit = i => (bits[i] === '1' ? 1 : 0);
  const cable = (x1, y1, x2, y2, v) => `<line x1="${x1}" y1="${y1}" x2="${x2}" y2="${y2}" stroke="${v ? C.naranja : C.gris}" stroke-width="${v ? 2.6 : 1.6}"/>`;
  const etiqueta = (x, yy, v) => texto(x, yy + 6, salida + ' = ' + v, `font-size="17" font-weight="700" fill="${v ? C.naranja : C.azul}"`);

  for (let i = 0; i < n; i++) s += texto(6, y[i] + 4, pref + (i + 1) + ' = ' + bit(i), `font-size="${n > 10 ? 10.5 : 12}" fill="${C.texto}"`);
  if (n === 1) { svg.innerHTML = s + cable(x0, y[0], W - 90, y[0], bit(0)) + etiqueta(W - 84, y[0], bit(0)); return; }

  let ySal = y[0], val = bit(0), xSal = x0;
  for (let i = 1; i < n; i++) {
    const gx = x0 + 20 + (i - 1) * dx, yTop = ySal, yBot = y[i], cy = (yTop + yBot) / 2, h = (yBot - yTop) + 18;
    const nuevo = val ^ bit(i);
    s += cable(xSal, yTop, gx + 4, yTop, val) + cable(x0, yBot, gx + 4, yBot, bit(i));
    const yy = cy - h / 2;
    s += `<path d="M${gx} ${yy} Q${gx + gw * 0.55} ${yy} ${gx + gw} ${cy} Q${gx + gw * 0.55} ${yy + h} ${gx} ${yy + h} Q${gx + gw * 0.3} ${cy} ${gx} ${yy}Z" fill="${C.blanco}" stroke="${C.azul}" stroke-width="2"/>`;
    s += `<path d="M${gx - 7} ${yy} Q${gx - 7 + gw * 0.3} ${cy} ${gx - 7} ${yy + h}" fill="none" stroke="${C.azul}" stroke-width="2"/>`;
    s += texto(gx + gw * 0.42, cy + 4, '⊕', `text-anchor="middle" font-size="12" font-weight="700" fill="${C.azul}"`);
    s += texto(gx + gw + 4, cy - 5, nuevo, `font-size="12" fill="${nuevo ? C.naranja : C.suave}"`);
    val = nuevo; ySal = cy; xSal = gx + gw;
  }
  const xFin = Math.min(W - 84, xSal + 40);
  s += cable(xSal, ySal, xFin, ySal, val) + etiqueta(xFin + 4, ySal, val);
  svg.innerHTML = s;
}
