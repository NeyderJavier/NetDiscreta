// =====================================================================
// NetDiscreta (versión web) — controlador de la interfaz
// Cada pestaña equivale a un Panel del programa en Java. Todas comparten
// el mismo "estado" (red, grupos, mensaje y último envío).
// =====================================================================
import * as L from './logica.js';
import { dibujarGrafo, dibujarVenn, dibujarPlano, dibujarBarras, dibujarCircuito, esc } from './graficos.js';

// ---------------- Estado compartido ----------------
const estado = {
  red: new L.Grafo(),
  grupos: [],            // { nombre, elementos: Set<nodo> }
  mensaje: '1011001',
  p: 0.05,
  envio: null,           // { enviada, recibida, errores, posiciones }
};

// ---------------- Utilidades ----------------
const $ = id => document.getElementById(id);
const pre = t => `<pre>${esc(t)}</pre>`;
const f6 = x => x.toFixed(6);

let temporizador;
function aviso(msg) {
  const a = $('aviso');
  a.textContent = msg;
  a.hidden = false;
  clearTimeout(temporizador);
  temporizador = setTimeout(() => { a.hidden = true; }, 4200);
}

/** Llena un <select> con opciones { valor, texto }, conservando la selección si sigue existiendo. */
function llenar(select, opciones, preferido) {
  const previo = select.value;
  select.innerHTML = opciones.map(o => `<option value="${esc(o.valor)}">${esc(o.texto)}</option>`).join('');
  const valores = opciones.map(o => o.valor);
  if (valores.includes(previo)) select.value = previo;
  else if (preferido !== undefined && valores.includes(preferido)) select.value = preferido;
}
const opcionesNodos = () => estado.red.nodos.map(n => ({ valor: n.nombre, texto: n.nombre }));
const nodo = select => estado.red.buscarNodo(select.value || '') ;
const grupo = select => estado.grupos.find(g => g.nombre === select.value) || null;
const ordenar = set => estado.red.nodos.filter(n => set.has(n));
const ext = set => L.extension(set, estado.red.nodos);

/** Si se borran equipos en Red, se quitan de los grupos. */
function limpiarGrupos() {
  const vivos = new Set(estado.red.nodos);
  for (const g of estado.grupos) for (const n of [...g.elementos]) if (!vivos.has(n)) g.elementos.delete(n);
}

// =====================================================================
// Pestañas
// =====================================================================
const modulos = {};
document.querySelectorAll('.pestanas button').forEach(btn => {
  btn.addEventListener('click', () => abrir(btn.dataset.tab));
});
function abrir(tab) {
  document.querySelectorAll('.pestanas button').forEach(b => b.setAttribute('aria-selected', String(b.dataset.tab === tab)));
  document.querySelectorAll('.panel').forEach(p => { p.hidden = p.id !== 'tab-' + tab; });
  history.replaceState(null, '', '#' + tab);
  modulos[tab].refrescar();   // el módulo lee los cambios hechos en los demás
}

// =====================================================================
// 1. RED — grafos
// =====================================================================
modulos.red = (() => {
  const svg = $('svg-red');
  let camino = [];
  const dibujar = () => dibujarGrafo(svg, estado.red, camino);

  // Arrastrar equipos con el mouse o el dedo
  let arrastrado = null;
  const punto = e => {
    const pt = svg.createSVGPoint();
    pt.x = e.clientX; pt.y = e.clientY;
    return pt.matrixTransform(svg.getScreenCTM().inverse());
  };
  svg.addEventListener('pointerdown', e => {
    const g = e.target.closest('.nodo');
    if (!g) return;
    arrastrado = estado.red.nodos[+g.dataset.i];
    svg.setPointerCapture(e.pointerId);
  });
  svg.addEventListener('pointermove', e => {
    if (!arrastrado) return;
    const p = punto(e);
    arrastrado.x = Math.max(28, Math.min(612, p.x));
    arrastrado.y = Math.max(28, Math.min(432, p.y));
    dibujar();
  });
  const soltar = () => { arrastrado = null; };
  svg.addEventListener('pointerup', soltar);
  svg.addEventListener('pointercancel', soltar);

  function refrescar() {
    const ops = opcionesNodos();
    llenar($('red-a'), ops);
    llenar($('red-b'), ops, ops[1]?.valor);
    llenar($('red-origen'), ops);
    llenar($('red-destino'), ops, ops[ops.length - 1]?.valor);
    camino = [];
    dibujar();
  }

  function agregar() {
    try {
      const k = estado.red.nodos.length, ang = k * (2 * Math.PI / 8), r = Math.min(190, 150 + 30 * Math.floor(k / 8));
      estado.red.agregarNodo($('red-nombre').value, 320 + r * Math.cos(ang), 230 + r * Math.sin(ang) * 0.95);
      $('red-nombre').value = '';
      refrescar();
    } catch (err) { aviso(err.message); }
  }
  $('red-agregar').onclick = agregar;
  $('red-nombre').addEventListener('keydown', e => { if (e.key === 'Enter') agregar(); });
  $('red-eliminar').onclick = () => {
    const a = nodo($('red-a'));
    if (!a) return aviso('Selecciona el equipo A.');
    estado.red.eliminarNodo(a);
    limpiarGrupos();
    refrescar();
  };
  $('red-conectar').onclick = () => {
    const a = nodo($('red-a')), b = nodo($('red-b'));
    if (!a || !b) return aviso('Selecciona los equipos A y B.');
    try { estado.red.agregarArista(a, b, Number($('red-peso').value)); refrescar(); } catch (err) { aviso(err.message); }
  };
  $('red-quitar').onclick = () => {
    const a = nodo($('red-a')), b = nodo($('red-b'));
    if (a && b) { estado.red.eliminarArista(a, b); refrescar(); }
  };
  $('red-ruta').onclick = () => {
    const o = nodo($('red-origen')), d = nodo($('red-destino'));
    if (!o || !d) return aviso('Selecciona origen y destino.');
    const r = L.dijkstra(estado.red, o, d);
    if (r.distancia < 0) {
      camino = [];
      dibujar();
      $('salida-red').innerHTML = pre(`No existe camino entre ${o.nombre} y ${d.nombre}.\nEl grafo no es conexo.`);
      return;
    }
    camino = r.camino;
    dibujar();
    $('salida-red').innerHTML = pre(`Ruta más corta (Dijkstra):\n${r.camino.map(n => n.nombre).join(' → ')}\n\nLatencia total: ${r.distancia} ms\nSaltos: ${r.camino.length - 1}`);
  };
  $('red-grados').onclick = () => {
    const g = estado.red;
    let t = `|V| = ${g.nodos.length}   |E| = ${g.aristas.length}\n\nGrados:\n`;
    for (const n of g.nodos) t += `  gr(${n.nombre}) = ${g.grado(n)}\n`;
    t += `\nSuma de grados = ${g.sumaGrados()} = 2|E| ✔\n¿Es conexo? ${g.esConexo() ? 'Sí' : 'No'} (recorrido BFS)`;
    $('salida-red').innerHTML = pre(t);
  };
  $('red-matriz').onclick = () => {
    const ns = estado.red.nodos;
    if (!ns.length) return aviso('La red está vacía.');
    const m = estado.red.matrizAdyacencia();
    let h = '<p><b>Matriz de adyacencia</b>: M[i][j] = 1 si hay enlace entre i y j.</p><table><tr><th></th>' + ns.map(n => `<th>${esc(n.nombre)}</th>`).join('') + '</tr>';
    ns.forEach((n, i) => { h += `<tr><th>${esc(n.nombre)}</th>` + m[i].map(v => `<td class="${v ? 'marcado' : ''}">${v}</td>`).join('') + '</tr>'; });
    $('salida-red').innerHTML = h + '</table>';
  };
  $('red-ejemplo').onclick = () => {
    estado.red.cargarEjemplo();
    estado.grupos = [];
    estado.envio = null;
    refrescar();
    $('salida-red').innerHTML = pre('Red de ejemplo cargada: 6 equipos y 7 enlaces.\nPrueba la ruta de PC1 a SRV.');
  };
  $('red-limpiar').onclick = () => { estado.red.limpiar(); estado.grupos = []; refrescar(); $('salida-red').innerHTML = ''; };

  return { refrescar };
})();

// =====================================================================
// 2. GRUPOS — conjuntos
// =====================================================================
modulos.grupos = (() => {
  const svg = $('svg-grupos');
  llenar($('gr-op'), L.OPERACIONES.map(o => ({ valor: o.id, texto: `${o.simbolo}  (${o.nombre})` })));

  function refrescar() {
    limpiarGrupos();
    // Casillas de los equipos
    const marcados = new Set([...document.querySelectorAll('#gr-elementos input:checked')].map(c => c.value));
    $('gr-elementos').innerHTML = estado.red.nodos.map(n =>
      `<label class="check"><input type="checkbox" value="${esc(n.nombre)}" ${marcados.has(n.nombre) ? 'checked' : ''}> ${esc(n.nombre)}</label>`).join('');
    // Lista de grupos
    const actual = $('gr-nombre').value.trim().toLowerCase();
    $('gr-lista').innerHTML = estado.grupos.map((g, i) =>
      `<li data-i="${i}" class="${g.nombre.toLowerCase() === actual ? 'sel' : ''}">${esc(g.nombre)} = ${esc(ext(g.elementos))}</li>`).join('');
    const ops = estado.grupos.map(g => ({ valor: g.nombre, texto: g.nombre }));
    llenar($('gr-a'), ops);
    llenar($('gr-b'), ops, ops[1]?.valor);
    calcular();
  }

  function calcular() {
    const a = grupo($('gr-a')), b = grupo($('gr-b'));
    const op = L.OPERACIONES.find(o => o.id === $('gr-op').value);
    if (!a || !b) { dibujarVenn(svg, null, null); $('salida-grupos').innerHTML = estado.grupos.length ? '' : pre('Crea al menos un grupo (o pulsa «Grupos de ejemplo»).'); return; }
    const U = new Set(estado.red.nodos);
    const r = L.aplicarOperacion(op.id, a.elementos, b.elementos, U);
    dibujarVenn(svg, a, b, estado.red.nodos, op.id);
    const sim = op.simbolo.replace('A', '\u0001').replace('B', '\u0002').replace('\u0001', a.nombre).replace('\u0002', b.nombre);
    const inter = L.conj.inter(a.elementos, b.elementos), union = L.conj.union(a.elementos, b.elementos);
    $('salida-grupos').innerHTML = pre(
      `U = ${ext(U)}\n${a.nombre} = ${ext(a.elementos)}\n${b.nombre} = ${ext(b.elementos)}\n\n` +
      `${op.nombre}: ${op.def}\n${sim} = ${ext(r)}\n|${sim}| = ${r.size}\n\n` +
      `Propiedades:\n• ${a.nombre} ⊆ ${b.nombre}: ${L.conj.subconjunto(a.elementos, b.elementos) ? 'sí' : 'no'}\n` +
      `• ${b.nombre} ⊆ ${a.nombre}: ${L.conj.subconjunto(b.elementos, a.elementos) ? 'sí' : 'no'}\n` +
      `• Disjuntos (${a.nombre} ∩ ${b.nombre} = ∅): ${inter.size === 0 ? 'sí' : 'no'}\n` +
      `• |A ∪ B| = |A| + |B| − |A ∩ B| → ${union.size} = ${a.elementos.size} + ${b.elementos.size} − ${inter.size} ✔`);
  }

  $('gr-guardar').onclick = () => {
    const nombre = $('gr-nombre').value.trim();
    if (!nombre) return aviso('Escribe un nombre para el grupo (ej. A, Servidores).');
    const elegidos = [...document.querySelectorAll('#gr-elementos input:checked')].map(c => estado.red.buscarNodo(c.value));
    const existente = estado.grupos.find(g => g.nombre.toLowerCase() === nombre.toLowerCase());
    if (existente) existente.elementos = new Set(elegidos);
    else estado.grupos.push({ nombre, elementos: new Set(elegidos) });
    refrescar();
  };
  $('gr-eliminar').onclick = () => {
    const nombre = $('gr-nombre').value.trim().toLowerCase();
    const i = estado.grupos.findIndex(g => g.nombre.toLowerCase() === nombre);
    if (i < 0) return aviso('Selecciona un grupo de la lista para eliminarlo.');
    estado.grupos.splice(i, 1);
    $('gr-nombre').value = '';
    refrescar();
  };
  $('gr-lista').addEventListener('click', e => {
    const li = e.target.closest('li');
    if (!li) return;
    const g = estado.grupos[+li.dataset.i];
    $('gr-nombre').value = g.nombre;
    document.querySelectorAll('#gr-elementos input').forEach(c => { c.checked = g.elementos.has(estado.red.buscarNodo(c.value)); });
    refrescar();
  });
  ['gr-a', 'gr-b', 'gr-op'].forEach(id => $(id).addEventListener('change', calcular));
  $('gr-ejemplo').onclick = () => {
    if (!estado.red.nodos.length) estado.red.cargarEjemplo();
    const sel = (...ns) => new Set(ns.map(n => estado.red.buscarNodo(n)).filter(Boolean));
    estado.grupos = [{ nombre: 'Norte', elementos: sel('PC1', 'PC2', 'R1', 'R3') }, { nombre: 'Sur', elementos: sel('R2', 'R3', 'SRV') }];
    $('gr-a').innerHTML = ''; $('gr-b').innerHTML = '';
    $('gr-op').value = 'union';
    refrescar();
  };

  return { refrescar };
})();

// =====================================================================
// 3. CONEXIONES — producto cartesiano
// =====================================================================
modulos.conexiones = (() => {
  const svg = $('svg-conexiones');

  function refrescar() {
    limpiarGrupos();
    const ops = estado.grupos.map(g => ({ valor: g.nombre, texto: g.nombre }));
    llenar($('cx-a'), ops);
    llenar($('cx-b'), ops, ops[1]?.valor);
    generar();
  }

  function generar() {
    const a = grupo($('cx-a')), b = grupo($('cx-b'));
    if (!a || !b) {
      dibujarPlano(svg, null, null, [], [], []);
      $('cx-tabla').innerHTML = '';
      $('salida-conexiones').innerHTML = pre('Primero crea al menos un grupo en el módulo 2 (Grupos).');
      return;
    }
    const la = ordenar(a.elementos), lb = ordenar(b.elementos);
    const pares = L.productoCartesiano(la, lb, estado.red), rel = pares.filter(p => p.enRelacion);
    dibujarPlano(svg, a, b, la, lb, pares);
    $('cx-tabla').innerHTML = pares.length
      ? '<table><tr><th>#</th><th>Par (a, b)</th><th>¿Enlace?</th></tr>' + pares.map((p, i) =>
        `<tr class="${p.enRelacion ? 'rel' : ''}"><td>${i + 1}</td><td>(${esc(p.a.nombre)}, ${esc(p.b.nombre)})</td><td>${p.enRelacion ? 'Sí' : 'No'}</td></tr>`).join('') + '</table>'
      : '';
    const nA = a.nombre, nB = b.nombre;
    $('salida-conexiones').innerHTML = pre(
      `${nA} = ${ext(a.elementos)}\n${nB} = ${ext(b.elementos)}\n\n` +
      `${nA} × ${nB} = {(a, b) | a ∈ ${nA} ∧ b ∈ ${nB}}\n` +
      `|${nA} × ${nB}| = |${nA}| · |${nB}| = ${la.length} · ${lb.length} = ${pares.length}\n\n` +
      `Relación R ⊆ ${nA} × ${nB}: (a, b) ∈ R si a y b tienen enlace directo.\n` +
      `R = ${rel.length ? '{' + rel.map(p => `(${p.a.nombre}, ${p.b.nombre})`).join(', ') + '}' : '∅'}\n|R| = ${rel.length}`);
  }

  $('cx-generar').onclick = generar;
  ['cx-a', 'cx-b'].forEach(id => $(id).addEventListener('change', generar));
  $('cx-invertir').onclick = () => {
    const a = $('cx-a').value;
    $('cx-a').value = $('cx-b').value;
    $('cx-b').value = a;
    generar();
  };

  return { refrescar };
})();

// =====================================================================
// 4. MENSAJES — alfabetos, cadenas y lenguajes
// =====================================================================
modulos.mensajes = (() => {
  const veredicto = $('ms-veredicto');

  const lenguaje = () => {
    switch ($('ms-lenguaje').value) {
      case 'longitud': return L.LENGUAJES.longitud(Number($('ms-long').value));
      case 'paridad': return L.LENGUAJES.paridad();
      case 'cabecera': return L.LENGUAJES.cabecera($('ms-cab').value.trim(), $('ms-cola').value.trim());
      default: return L.LENGUAJES.palindromo();
    }
  };
  /** Lee Σ y w; devuelve null (y avisa) si w no es una cadena sobre Σ. */
  function leer() {
    try {
      const sigma = L.cad.leerAlfabeto($('ms-alfabeto').value);
      const w = $('ms-w').value.trim();
      const malo = L.cad.simboloInvalido(w, sigma);
      if (malo !== null) { aviso(`El símbolo '${malo}' no pertenece a ${L.cad.mostrarAlfabeto(sigma)}: w ∉ Σ*.`); return null; }
      return { sigma, w };
    } catch (err) { aviso(err.message); return null; }
  }

  $('ms-lenguaje').addEventListener('change', () => {
    const v = $('ms-lenguaje').value;
    $('ms-param-longitud').hidden = v !== 'longitud';
    $('ms-param-cabecera').hidden = v !== 'cabecera';
    veredicto.hidden = true;
  });

  $('ms-analizar').onclick = () => {
    const d = leer(); if (!d) return;
    const { sigma, w } = d, v = $('ms-v').value.trim(), n = Number($('ms-pot').value);
    let t = `${L.cad.mostrarAlfabeto(sigma)}\nw = ${L.cad.mostrar(w)}   (w ∈ Σ* ✔)\n\n`;
    t += `Longitud       |w| = ${w.length}\nInversa        wᴿ = ${L.cad.mostrar(L.cad.inversa(w))}\n`;
    t += L.cad.simboloInvalido(v, sigma) === null
      ? `Concatenación  w·v = ${L.cad.mostrar(w)} · ${L.cad.mostrar(v)} = ${L.cad.mostrar(w + v)}   (|w·v| = ${w.length + v.length})\n`
      : 'Concatenación: v no es una cadena sobre Σ\n';
    t += `Potencia       w^${n} = ${L.cad.mostrar(L.cad.potencia(w, n))}\n¿Palíndromo?   ${L.cad.esPalindromo(w) ? 'sí' : 'no'}\n\nApariciones de cada símbolo:\n`;
    for (const s of sigma) t += `   ${s}: ${L.cad.contar(w, s)}\n`;
    t += `\nPrefijos: ${L.cad.prefijos(w).join(', ')}\nSufijos:  ${L.cad.sufijos(w).join(', ')}`;
    $('salida-mensajes').innerHTML = pre(t);
  };

  $('ms-validar').onclick = () => {
    const d = leer(); if (!d) return;
    const l = lenguaje(), r = l.validar(d.w);
    veredicto.hidden = false;
    veredicto.className = 'veredicto ' + (r.pertenece ? 'ok' : 'mal');
    veredicto.textContent = r.pertenece ? 'w ∈ L ✔ mensaje válido' : 'w ∉ L ✘ mensaje rechazado';
    $('salida-mensajes').innerHTML = pre(`${L.cad.mostrarAlfabeto(d.sigma)}\n${l.definicion}\n\nw = ${L.cad.mostrar(d.w)}\n${r.explicacion}\n\nConclusión: w ${r.pertenece ? '∈' : '∉'} L`);
  };

  $('ms-usar').onclick = () => {
    const d = leer(); if (!d) return;
    if (!/^[01]+$/.test(d.w)) return aviso('El mensaje que se envía por la red debe ser binario y no vacío (solo 0 y 1).');
    if (d.w !== estado.mensaje) estado.envio = null;   // un mensaje nuevo invalida el último envío
    estado.mensaje = d.w;
    $('salida-mensajes').innerHTML = pre(`Mensaje guardado para el envío: ${d.w}\n\nSe usará en los módulos Canal (probabilidad de error)\ny Verificador (bit de paridad).`);
    aviso('Mensaje guardado: ' + d.w);
  };

  $('ms-sigma').onclick = () => {
    let sigma;
    try { sigma = L.cad.leerAlfabeto($('ms-alfabeto').value); } catch (err) { return aviso(err.message); }
    const n = Number($('ms-n').value), total = Math.pow(sigma.length, n);
    if (total > 4096) return aviso(`|Σⁿ| = ${sigma.length}^${n} = ${total} cadenas. Son demasiadas para mostrar; usa un n menor.`);
    const l = lenguaje(), todas = L.cad.sigmaN(sigma, n);
    let enL = 0;
    const filas = [];
    for (const w of todas) {
      const p = l.validar(w).pertenece;
      if (p) enL++;
      if (!$('ms-solo-l').checked || p) filas.push(L.cad.mostrar(w) + (p ? '  ∈ L' : ''));
    }
    $('salida-mensajes').innerHTML = pre(`${L.cad.mostrarAlfabeto(sigma)}\nΣ^${n}: cadenas de longitud ${n}\n|Σ^${n}| = |Σ|^${n} = ${sigma.length}^${n} = ${todas.length}\n${l.definicion}\n|L ∩ Σ^${n}| = ${enL}\n\n${filas.join('\n')}`);
  };

  return { refrescar() { if (!$('ms-w').value.trim()) $('ms-w').value = estado.mensaje; } };
})();

// =====================================================================
// 5. CANAL — espacio muestral, eventos, variable aleatoria y binomial
// =====================================================================
modulos.canal = (() => {
  const svg = $('svg-canal');
  let simulada = null;
  llenar($('cn-evento'), L.EVENTOS.map((e, i) => ({ valor: String(i), texto: `${e.nombre} (${e.cond})` })), '2');

  const n = () => Math.max(1, Math.min(60, Math.round(Number($('cn-n').value) || 1)));
  const p = () => Math.max(0, Math.min(1, Number($('cn-p').value) || 0));
  const ev = () => L.EVENTOS[Number($('cn-evento').value)];
  const k = () => Math.max(0, Math.min(n(), Math.round(Number($('cn-k').value) || 0)));

  function calcular(prefijo = '') {
    const b = new L.Binomial(n(), p()), e = ev(), kk = k();
    $('cn-k').disabled = !e.usaK;
    dibujarBarras(svg, b, e, kk, simulada);
    const pe = L.probEvento(e, b, kk);
    let t = prefijo;
    t += `X = número de bits con error al enviar n = ${b.n} bits\nX ~ B(n = ${b.n}, p = ${b.p.toFixed(2)})\nP(X = k) = C(n, k) · p^k · (1 − p)^(n − k)\n\n`;
    t += `Evento: ${e.nombre} → ${L.condicion(e, kk)}\nP(${L.condicion(e, kk)}) = ${f6(pe)}  (${(100 * pe).toFixed(2)} %)\n`;
    if (e.id === 'alguno') t += `   = 1 − P(X = 0) = 1 − ${f6(b.probabilidad(0))}\n`;
    t += `\nE[X] = n·p = ${b.media().toFixed(4)}   Var(X) = n·p·(1−p) = ${b.varianza().toFixed(4)}   σ = ${b.desviacion().toFixed(4)}\n\n`;
    t += '  k   C(n,k)          P(X = k)    P(X ≤ k)' + (simulada ? '    simulada' : '') + '\n';
    for (let i = 0; i <= b.n; i++) {
      t += `${String(i).padStart(3)}   ${String(L.combinaciones(b.n, i)).padEnd(14)}  ${f6(b.probabilidad(i))}    ${f6(b.acumulada(i))}` +
        (simulada ? `    ${f6(simulada[i])}` : '') + (e.ocurre(i, kk) ? '   ◀' : '') + '\n';
    }
    $('salida-canal').innerHTML = pre(t);
  }

  ['cn-n', 'cn-p'].forEach(id => $(id).addEventListener('input', () => { simulada = null; estado.p = p(); calcular(); }));
  ['cn-evento', 'cn-k'].forEach(id => $(id).addEventListener('input', () => calcular()));
  $('cn-usar-trama').onclick = () => { $('cn-n').value = estado.mensaje.length + 1; simulada = null; calcular(); };

  $('cn-omega').onclick = () => {
    const nn = n(), pp = p(), e = ev(), kk = k();
    if (nn > 5) {
      $('salida-canal').innerHTML = pre(`Ω = {C, E}^${nn}   (C = bit correcto, E = bit con error)\n|Ω| = 2^${nn} = ${Math.pow(2, nn)} resultados.\n\nSon demasiados para listarlos; baja n a 5 o menos para verlos todos.\n\nCada resultado ω con x errores tiene probabilidad p^x · (1 − p)^(n − x),\ny hay C(n, x) resultados con x errores. Por eso P(X = x) = C(n, x) · p^x · (1 − p)^(n − x).`);
      return;
    }
    const omega = L.espacioMuestral(nn, pp);
    let t = `Ω = {C, E}^${nn}   (C = correcto, E = error)   |Ω| = 2^${nn} = ${omega.length}\nEvento A: ${e.nombre} → ${L.condicion(e, kk)}\n\n  ω        X(ω)   P(ω)         ¿ω ∈ A?\n`;
    let suma = 0, enA = 0;
    for (const r of omega) {
      const o = e.ocurre(r.errores, kk);
      if (o) { suma += r.probabilidad; enA++; }
      t += `  ${r.valor.padEnd(8)} ${String(r.errores).padEnd(6)} ${f6(r.probabilidad)}     ${o ? 'sí' : ''}\n`;
    }
    t += `\n|A| = ${enA} resultados\nP(A) = suma de P(ω) con ω ∈ A = ${f6(suma)}`;
    $('salida-canal').innerHTML = pre(t);
  };

  $('cn-simular').onclick = () => {
    const envios = Math.max(10, Math.min(1000000, Math.round(Number($('cn-envios').value) || 1000)));
    const frec = L.simular(n(), p(), envios);
    simulada = frec.map(f => f / envios);
    const b = new L.Binomial(n(), p()), e = ev(), kk = k();
    let casos = 0, media = 0;
    frec.forEach((f, i) => { if (e.ocurre(i, kk)) casos += f; media += i * f; });
    media /= envios;
    calcular(`Simulación de ${envios} envíos\nFrecuencia relativa de ${L.condicion(e, kk)}: ${casos} / ${envios} = ${(casos / envios).toFixed(4)}   (teórica ${L.probEvento(e, b, kk).toFixed(4)})\n` +
      `Promedio de errores observado: ${media.toFixed(4)}   (E[X] = ${b.media().toFixed(4)})\nCon más envíos, la frecuencia se acerca a la probabilidad teórica.\n─────────────────────────────────────────────\n`);
  };

  $('cn-enviar').onclick = () => {
    const m = estado.mensaje, trama = L.paridad.agregar(m), pp = p();
    const t = L.transmitir(trama, pp);
    estado.envio = t;
    estado.p = pp;
    const marcas = [...trama].map((_, i) => (t.posiciones.includes(i) ? '^' : ' ')).join('');
    const b = new L.Binomial(trama.length, pp);
    $('salida-canal').innerHTML = pre(`Mensaje w        = ${m}\nBit de paridad P = ${L.paridad.xorTotal(m)}\n\nEnviada          = ${t.enviada}\nRecibida         = ${t.recibida}\n                   ${marcas}\n` +
      `Bits con error: ${t.errores}${t.errores ? ' (posiciones ' + t.posiciones.map(i => i + 1).join(', ') + ')' : ''}\n\n` +
      `Para una trama de n = ${trama.length} bits con p = ${pp.toFixed(2)}:\nP(X = ${t.errores}) = ${f6(b.probabilidad(t.errores))}\n\nVe al módulo 6 (Verificador) para comprobar si el receptor detecta el error.`);
  };

  return {
    refrescar() {
      $('cn-trama').textContent = `Trama: ${estado.mensaje} + P (${estado.mensaje.length + 1} bits)`;
      calcular();
    },
  };
})();

// =====================================================================
// 6. VERIFICADOR — álgebra booleana
// =====================================================================
modulos.verificador = (() => {
  const svg = $('svg-verificador'), res = $('vf-resultado');
  const LEYES = [
    ['Paridad de 3 bits', 'A ⊕ B ⊕ C', "A'B'C + A'BC' + AB'C' + ABC"],
    ['De Morgan (1)', "(A + B)'", "A'·B'"],
    ['De Morgan (2)', "(A·B)'", "A' + B'"],
    ['Distributiva', 'A·(B + C)', 'A·B + A·C'],
    ['Absorción', 'A + A·B', 'A'],
    ['XOR con AND/OR', 'A ⊕ B', "A·B' + A'·B"],
    ['Contraejemplo', 'A + B·C', '(A + B)·C'],
  ];
  llenar($('vf-ley'), LEYES.map((l, i) => ({ valor: String(i), texto: l[0] })));
  const ponerLey = () => { const l = LEYES[Number($('vf-ley').value)]; $('vf-e1').value = l[1]; $('vf-e2').value = l[2]; };
  $('vf-ley').addEventListener('change', ponerLey);
  ponerLey();

  const modo = () => document.querySelector('input[name="vf-modo"]:checked').value;
  const setModo = m => { document.querySelector(`input[name="vf-modo"][value="${m}"]`).checked = true; };
  const mostrar = (clase, texto) => { res.hidden = false; res.className = 'veredicto ' + clase; res.textContent = texto; };
  const binario = (id, nombre) => {
    const s = $(id).value.trim();
    if (!/^[01]+$/.test(s)) { aviso(nombre + ' debe ser una cadena binaria no vacía (solo 0 y 1).'); return null; }
    return s;
  };
  const unos = s => [...s].filter(c => c === '1').length;

  function dibujar() {
    if (modo() === 'receptor') {
      const r = $('vf-recibida').value.trim();
      dibujarCircuito(svg, /^[01]+$/.test(r) ? r : '', `Receptor: S = r1 ⊕ r2 ⊕ … ⊕ r${r.length}`, 'S');
    } else {
      const w = $('vf-mensaje').value.trim();
      dibujarCircuito(svg, /^[01]+$/.test(w) ? w : '', `Emisor: P = b1 ⊕ b2 ⊕ … ⊕ b${w.length}`, 'P');
    }
  }
  document.querySelectorAll('input[name="vf-modo"]').forEach(r => r.addEventListener('change', dibujar));
  $('vf-mensaje').addEventListener('input', () => { if (modo() === 'emisor') dibujar(); });
  $('vf-recibida').addEventListener('input', () => { if (modo() === 'receptor') dibujar(); });

  $('vf-paridad').onclick = () => {
    const w = binario('vf-mensaje', 'El mensaje'); if (!w) return;
    const pb = L.paridad.xorTotal(w), trama = w + pb;
    setModo('emisor'); dibujar();
    mostrar('info', `P = ${pb}  →  trama = ${trama}`);
    $('salida-verificador').innerHTML = pre(`EMISOR — paridad par\nP = b1 ⊕ b2 ⊕ … ⊕ bn\nP = ${L.paridad.expresion(w)}\n\nPaso a paso:\n${L.paridad.pasoAPaso(w)}\n` +
      `Cantidad de unos en w: ${unos(w)} → P = ${pb}${pb === 0 ? ' (ya era par)' : ' (se agrega un 1 para que sea par)'}\nTrama enviada = w · P = ${trama}  (tiene ${unos(trama)} unos: par)`);
  };

  $('vf-traer').onclick = () => {
    if (!estado.envio) return aviso('Todavía no hay envíos. Ve al módulo 5 (Canal) y pulsa «Enviar trama una vez».');
    $('vf-mensaje').value = estado.mensaje;
    $('vf-recibida').value = estado.envio.recibida;
    verificar();
  };

  function verificar() {
    const r = binario('vf-recibida', 'La trama recibida'); if (!r) return;
    const s = L.paridad.xorTotal(r);
    setModo('receptor'); dibujar();
    mostrar(s === 0 ? 'ok' : 'mal', s === 0 ? 'S = 0 ✔ no se detectan errores' : 'S = 1 ✘ ¡error detectado!');
    let t = `RECEPTOR — síndrome S = r1 ⊕ r2 ⊕ … ⊕ r(n+1)\nS = ${L.paridad.expresion(r)}\n`;
    t += s === 0 ? 'S = 0 → la trama tiene paridad par: se acepta.\n' : 'S = 1 → la paridad no coincide: hay un número impar de bits con error.\n';
    const e = estado.envio;
    if (e && r === e.recibida) {
      t += `\nLo que realmente ocurrió en el canal:\n  enviada  = ${e.enviada}\n  recibida = ${r}\n  errores reales = ${e.errores}\n`;
      if (e.errores === 0) t += '  ✔ Correcto: no hubo errores y no se detectó ninguno.\n';
      else if (e.errores % 2 === 1) t += `  ✔ Correcto: hubo ${e.errores} error(es), número impar, y se detectó.\n`;
      else t += `  ✘ Error NO detectado: hubo ${e.errores} errores (número par) y la paridad no cambia.\n`;
      const leido = r.slice(0, -1), original = e.enviada.slice(0, -1);
      t += `  Mensaje que lee el receptor: ${leido}${leido === original ? '  (igual al original)' : '  (distinto al original)'}\n`;
    }
    const b = new L.Binomial(r.length, estado.p);
    t += `\nLimitación del bit de paridad (n = ${r.length}, p = ${estado.p.toFixed(2)}):\n` +
      `  P(error no detectado) = P(X = 2) + P(X = 4) + … = ${f6(b.erroresParesNoDetectados())}\n` +
      `  P(error detectado)    = P(X impar)            = ${f6(1 - b.probabilidad(0) - b.erroresParesNoDetectados())}`;
    $('salida-verificador').innerHTML = pre(t);
  }
  $('vf-verificar').onclick = verificar;

  $('vf-tabla').onclick = () => {
    let e;
    try { e = new L.ExpresionBooleana($('vf-e1').value); } catch (err) { return aviso('Expresión 1: ' + err.message); }
    const vars = e.variables;
    if (vars.length > 6) return aviso('Usa máximo 6 variables para que la tabla quepa en pantalla.');
    let h = `<p><b>F = ${esc(e.texto)}</b></p><table><tr><th>fila</th>${vars.map(v => `<th>${v}</th>`).join('')}<th>F</th></tr>`;
    for (let i = 0; i < (1 << vars.length); i++) {
      const fila = L.ExpresionBooleana.fila(vars, i), val = e.evaluar(fila);
      h += `<tr><td>${val ? 'm' + i : i}</td>${vars.map(v => `<td>${fila[v] ? 1 : 0}</td>`).join('')}<td class="${val ? 'marcado' : ''}">${val ? 1 : 0}</td></tr>`;
    }
    h += `</table><pre>\nMinitérminos: Σm(${e.minterminos().join(', ')})\nSuma de productos canónica:\nF = ${esc(e.sumaDeProductos())}</pre>`;
    $('salida-verificador').innerHTML = h;
  };

  $('vf-equiv').onclick = () => {
    let e1, e2;
    try { e1 = new L.ExpresionBooleana($('vf-e1').value); } catch (err) { return aviso('Expresión 1: ' + err.message); }
    try { e2 = new L.ExpresionBooleana($('vf-e2').value); } catch (err) { return aviso('Expresión 2: ' + err.message); }
    const vars = [...new Set([...e1.variables, ...e2.variables])].sort();
    if (vars.length > 6) return aviso('Usa máximo 6 variables en total.');
    const eq = L.ExpresionBooleana.equivalentes(e1, e2);
    let h = `<p><b>F1 = ${esc(e1.texto)}</b><br><b>F2 = ${esc(e2.texto)}</b></p><table><tr>${vars.map(v => `<th>${v}</th>`).join('')}<th>F1</th><th>F2</th></tr>`;
    for (let i = 0; i < (1 << vars.length); i++) {
      const fila = L.ExpresionBooleana.fila(vars, i), a = e1.evaluar(fila), b = e2.evaluar(fila);
      h += `<tr>${vars.map(v => `<td>${fila[v] ? 1 : 0}</td>`).join('')}<td class="${a !== b ? 'marcado' : ''}">${a ? 1 : 0}</td><td class="${a !== b ? 'marcado' : ''}">${b ? 1 : 0}</td></tr>`;
    }
    h += `</table><pre>\n${eq ? 'F1 ≡ F2: coinciden en todas las filas, son equivalentes.' : 'F1 ≢ F2: las filas marcadas difieren, NO son equivalentes.'}</pre>`;
    mostrar(eq ? 'ok' : 'mal', eq ? 'F1 ≡ F2 ✔ equivalentes' : 'F1 ≢ F2 ✘ no equivalentes');
    $('salida-verificador').innerHTML = h;
  };

  return {
    refrescar() {
      $('vf-mensaje').value = estado.mensaje;
      if (estado.envio) $('vf-recibida').value = estado.envio.recibida;
      else if (!$('vf-recibida').value.trim().startsWith(estado.mensaje)) $('vf-recibida').value = L.paridad.agregar(estado.mensaje);
      dibujar();
    },
  };
})();

// ---------------- Inicio ----------------
estado.red.cargarEjemplo();   // la red de ejemplo aparece al abrir, para empezar a probar
const inicial = location.hash.slice(1);
abrir(modulos[inicial] ? inicial : 'red');
