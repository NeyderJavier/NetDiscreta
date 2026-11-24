// =====================================================================
// NetDiscreta (versión web) — lógica matemática
// Es la misma lógica del programa en Java (paquetes modelo y logica),
// escrita en JavaScript. No depende de la interfaz.
// =====================================================================

// ---------------------------------------------------------------------
// Grafo: la red de computadores G = (V, E), no dirigido y ponderado
// ---------------------------------------------------------------------
export class Grafo {
  constructor() {
    this.nodos = [];    // { nombre, x, y }
    this.aristas = [];  // { a, b, peso }  (a y b son objetos nodo)
  }

  buscarNodo(nombre) {
    const n = nombre.trim().toLowerCase();
    return this.nodos.find(x => x.nombre.toLowerCase() === n) || null;
  }

  agregarNodo(nombre, x, y) {
    nombre = nombre.trim();
    if (!nombre) throw new Error('Escribe un nombre para el equipo.');
    if (this.buscarNodo(nombre)) throw new Error('Ya existe un equipo llamado ' + nombre);
    const nodo = { nombre, x, y };
    this.nodos.push(nodo);
    return nodo;
  }

  eliminarNodo(nodo) {
    this.aristas = this.aristas.filter(ar => ar.a !== nodo && ar.b !== nodo);
    this.nodos = this.nodos.filter(n => n !== nodo);
  }

  buscarArista(a, b) {
    return this.aristas.find(ar => (ar.a === a && ar.b === b) || (ar.a === b && ar.b === a)) || null;
  }

  agregarArista(a, b, peso) {
    if (a === b) throw new Error('No se permiten lazos (un nodo consigo mismo).');
    if (!(peso > 0)) throw new Error('El peso debe ser mayor que 0.');
    const existente = this.buscarArista(a, b);
    if (existente) existente.peso = peso;      // si ya existe, se actualiza el peso
    else this.aristas.push({ a, b, peso });
  }

  eliminarArista(a, b) {
    this.aristas = this.aristas.filter(ar => !((ar.a === a && ar.b === b) || (ar.a === b && ar.b === a)));
  }

  vecinos(n) {
    const v = [];
    for (const ar of this.aristas) {
      if (ar.a === n) v.push(ar.b);
      else if (ar.b === n) v.push(ar.a);
    }
    return v;
  }

  grado(n) { return this.vecinos(n).length; }

  sumaGrados() { return this.nodos.reduce((s, n) => s + this.grado(n), 0); }

  /** M[i][j] = 1 si hay arista entre el nodo i y el j. */
  matrizAdyacencia() {
    const k = this.nodos.length;
    const m = Array.from({ length: k }, () => Array(k).fill(0));
    for (const ar of this.aristas) {
      const i = this.nodos.indexOf(ar.a), j = this.nodos.indexOf(ar.b);
      m[i][j] = 1;
      m[j][i] = 1;
    }
    return m;
  }

  /** Conexo: desde un nodo se alcanzan todos (recorrido en anchura, BFS). */
  esConexo() {
    if (this.nodos.length === 0) return true;
    const visitados = new Set([this.nodos[0]]);
    const cola = [this.nodos[0]];
    while (cola.length) {
      const actual = cola.shift();
      for (const v of this.vecinos(actual)) {
        if (!visitados.has(v)) { visitados.add(v); cola.push(v); }
      }
    }
    return visitados.size === this.nodos.length;
  }

  limpiar() { this.nodos = []; this.aristas = []; }

  /** Red de 6 equipos para pruebas y demostraciones. */
  cargarEjemplo() {
    this.limpiar();
    const pc1 = this.agregarNodo('PC1', 90, 120);
    const pc2 = this.agregarNodo('PC2', 90, 330);
    const r1 = this.agregarNodo('R1', 250, 220);
    const r2 = this.agregarNodo('R2', 400, 110);
    const r3 = this.agregarNodo('R3', 400, 340);
    const srv = this.agregarNodo('SRV', 540, 220);
    this.agregarArista(pc1, r1, 4);
    this.agregarArista(pc2, r1, 3);
    this.agregarArista(r1, r2, 7);
    this.agregarArista(r1, r3, 2);
    this.agregarArista(r3, r2, 3);
    this.agregarArista(r2, srv, 4);
    this.agregarArista(r3, srv, 9);
  }
}

// ---------------------------------------------------------------------
// Dijkstra: camino de menor latencia entre dos nodos
// ---------------------------------------------------------------------
export function dijkstra(g, origen, destino) {
  const dist = new Map(), previo = new Map(), visitados = new Set();
  for (const n of g.nodos) dist.set(n, Infinity);
  dist.set(origen, 0);

  while (visitados.size < g.nodos.length) {
    // 1. El nodo no visitado con menor distancia
    let u = null;
    for (const n of g.nodos) {
      if (!visitados.has(n) && (u === null || dist.get(n) < dist.get(u))) u = n;
    }
    if (u === null || dist.get(u) === Infinity) break;   // el resto es inalcanzable
    visitados.add(u);
    if (u === destino) break;

    // 2. Relajar las aristas que salen de u
    for (const ar of g.aristas) {
      if (ar.a !== u && ar.b !== u) continue;
      const v = ar.a === u ? ar.b : ar.a;
      if (visitados.has(v)) continue;
      const nueva = dist.get(u) + ar.peso;
      if (nueva < dist.get(v)) { dist.set(v, nueva); previo.set(v, u); }
    }
  }

  if (dist.get(destino) === Infinity) return { camino: [], distancia: -1 };
  // 3. Reconstruir el camino desde el destino hacia atrás
  const camino = [];
  for (let n = destino; n; n = previo.get(n)) camino.unshift(n);
  return { camino, distancia: dist.get(destino) };
}

// ---------------------------------------------------------------------
// Conjuntos (los grupos guardan nodos)
// ---------------------------------------------------------------------
export const OPERACIONES = [
  { id: 'union', simbolo: 'A ∪ B', nombre: 'Unión', def: '{x | x ∈ A ∨ x ∈ B}' },
  { id: 'inter', simbolo: 'A ∩ B', nombre: 'Intersección', def: '{x | x ∈ A ∧ x ∈ B}' },
  { id: 'difAB', simbolo: 'A − B', nombre: 'Diferencia A − B', def: '{x | x ∈ A ∧ x ∉ B}' },
  { id: 'difBA', simbolo: 'B − A', nombre: 'Diferencia B − A', def: '{x | x ∈ B ∧ x ∉ A}' },
  { id: 'sim', simbolo: 'A Δ B', nombre: 'Diferencia simétrica', def: '(A − B) ∪ (B − A)' },
  { id: 'compA', simbolo: "A'", nombre: 'Complemento de A', def: '{x ∈ U | x ∉ A}' },
  { id: 'compB', simbolo: "B'", nombre: 'Complemento de B', def: '{x ∈ U | x ∉ B}' },
];

export const conj = {
  union: (a, b) => new Set([...a, ...b]),
  inter: (a, b) => new Set([...a].filter(x => b.has(x))),
  dif: (a, b) => new Set([...a].filter(x => !b.has(x))),
  sim: (a, b) => new Set([...conj.dif(a, b), ...conj.dif(b, a)]),
  comp: (a, u) => conj.dif(u, a),
  subconjunto: (a, b) => [...a].every(x => b.has(x)),
};

export function aplicarOperacion(id, a, b, u) {
  switch (id) {
    case 'union': return conj.union(a, b);
    case 'inter': return conj.inter(a, b);
    case 'difAB': return conj.dif(a, b);
    case 'difBA': return conj.dif(b, a);
    case 'sim': return conj.sim(a, b);
    case 'compA': return conj.comp(a, u);
    case 'compB': return conj.comp(b, u);
  }
  return new Set();
}

/** {PC1, PC2} o ∅, en el orden de la red. */
export function extension(conjunto, ordenNodos) {
  const lista = ordenNodos.filter(n => conjunto.has(n));
  return lista.length ? '{' + lista.map(n => n.nombre).join(', ') + '}' : '∅';
}

// ---------------------------------------------------------------------
// Producto cartesiano A × B y relación R (enlace directo)
// ---------------------------------------------------------------------
export function productoCartesiano(listaA, listaB, g) {
  const pares = [];
  for (const x of listaA) {
    for (const y of listaB) {
      pares.push({ a: x, b: y, enRelacion: x !== y && g.buscarArista(x, y) !== null });
    }
  }
  return pares;
}

// ---------------------------------------------------------------------
// Alfabetos, cadenas y lenguajes
// ---------------------------------------------------------------------
export const LAMBDA = 'λ';
export const cad = {
  leerAlfabeto(texto) {
    const sigma = [];
    for (const parte of texto.split(',')) {
      const s = parte.trim();
      if ([...s].length !== 1) throw new Error('Cada símbolo del alfabeto debe ser un solo carácter: "' + s + '"');
      if (!sigma.includes(s)) sigma.push(s);
    }
    if (!sigma.length) throw new Error('El alfabeto no puede ser vacío.');
    return sigma;
  },
  mostrarAlfabeto: sigma => 'Σ = {' + sigma.join(', ') + '}',
  simboloInvalido: (w, sigma) => [...w].find(c => !sigma.includes(c)) ?? null,
  mostrar: w => (w === '' ? LAMBDA : w),
  inversa: w => [...w].reverse().join(''),
  potencia: (w, n) => w.repeat(Math.max(0, n)),
  contar: (w, c) => [...w].filter(x => x === c).length,
  esPalindromo: w => w === [...w].reverse().join(''),
  prefijos: w => Array.from({ length: w.length + 1 }, (_, i) => cad.mostrar(w.slice(0, i))),
  sufijos: w => Array.from({ length: w.length + 1 }, (_, i) => cad.mostrar(w.slice(i))),
  /** Σⁿ: todas las cadenas de longitud n sobre Σ. */
  sigmaN(sigma, n) {
    let actual = [''];
    for (let i = 0; i < n; i++) {
      const sig = [];
      for (const w of actual) for (const c of sigma) sig.push(w + c);
      actual = sig;
    }
    return actual;
  },
};

/** Cada lenguaje sabe decir si una cadena le pertenece y por qué. */
export const LENGUAJES = {
  longitud: n => ({
    nombre: 'Longitud fija (|w| = ' + n + ')',
    definicion: 'L = {w ∈ Σ* | |w| = ' + n + '}',
    validar: w => ({ pertenece: w.length === n, explicacion: '|w| = ' + w.length + (w.length === n ? ' = ' : ' ≠ ') + n }),
  }),
  paridad: () => ({
    nombre: 'Paridad par (cantidad par de 1)',
    definicion: 'L = {w ∈ Σ* | la cantidad de 1 en w es par}',
    validar: w => {
      const unos = cad.contar(w, '1');
      return { pertenece: unos % 2 === 0, explicacion: 'w tiene ' + unos + ' unos → ' + (unos % 2 === 0 ? 'par' : 'impar') };
    },
  }),
  cabecera: (p, s) => ({
    nombre: 'Empieza con "' + p + '" y termina con "' + s + '"',
    definicion: 'L = {w ∈ Σ* | w = ' + p + '·x·' + s + ', x ∈ Σ*}',
    validar: w => {
      const ini = w.startsWith(p), fin = w.endsWith(s);
      return {
        pertenece: ini && fin && w.length >= p.length + s.length,
        explicacion: '¿Empieza con ' + p + '? ' + (ini ? 'sí' : 'no') + ' · ¿Termina con ' + s + '? ' + (fin ? 'sí' : 'no'),
      };
    },
  }),
  palindromo: () => ({
    nombre: 'Palíndromos (w = wᴿ)',
    definicion: 'L = {w ∈ Σ* | w = wᴿ}',
    validar: w => {
      const r = cad.inversa(w);
      return { pertenece: w === r, explicacion: 'wᴿ = ' + cad.mostrar(r) + (w === r ? ' = w' : ' ≠ w') };
    },
  }),
};

// ---------------------------------------------------------------------
// Probabilidad: binomial, eventos, espacio muestral y simulación
// ---------------------------------------------------------------------
export function combinaciones(n, k) {
  if (k < 0 || k > n) return 0;
  k = Math.min(k, n - k);
  let r = 1;
  for (let i = 1; i <= k; i++) r = r * (n - k + i) / i;   // sin factoriales completos
  return Math.round(r);
}

export class Binomial {
  constructor(n, p) {
    if (n < 0) throw new Error('n debe ser mayor o igual a 0.');
    if (p < 0 || p > 1) throw new Error('p debe estar entre 0 y 1.');
    this.n = n;
    this.p = p;
  }
  probabilidad(k) {   // P(X = k) = C(n, k) · pᵏ · (1 − p)ⁿ⁻ᵏ
    if (k < 0 || k > this.n) return 0;
    return combinaciones(this.n, k) * Math.pow(this.p, k) * Math.pow(1 - this.p, this.n - k);
  }
  acumulada(k) { let s = 0; for (let i = 0; i <= Math.min(k, this.n); i++) s += this.probabilidad(i); return s; }
  media() { return this.n * this.p; }
  varianza() { return this.n * this.p * (1 - this.p); }
  desviacion() { return Math.sqrt(this.varianza()); }
  /** P(X par y X ≥ 2): errores que el bit de paridad NO detecta. */
  erroresParesNoDetectados() { let s = 0; for (let k = 2; k <= this.n; k += 2) s += this.probabilidad(k); return s; }
}

export const EVENTOS = [
  { id: 'cero', nombre: 'Llega sin errores', cond: 'X = 0', usaK: false, ocurre: (x) => x === 0 },
  { id: 'exacto', nombre: 'Exactamente k errores', cond: 'X = k', usaK: true, ocurre: (x, k) => x === k },
  { id: 'max', nombre: 'A lo sumo k errores', cond: 'X ≤ k', usaK: true, ocurre: (x, k) => x <= k },
  { id: 'min', nombre: 'Al menos k errores', cond: 'X ≥ k', usaK: true, ocurre: (x, k) => x >= k },
  { id: 'alguno', nombre: 'Al menos un error', cond: 'X ≥ 1', usaK: false, ocurre: (x) => x >= 1 },
];
export const condicion = (ev, k) => ev.cond.replace('k', String(k));
export function probEvento(ev, b, k) {
  let s = 0;
  for (let x = 0; x <= b.n; x++) if (ev.ocurre(x, k)) s += b.probabilidad(x);
  return s;
}

/** Ω = {C, E}ⁿ con X(ω) y P(ω) = pˣ (1 − p)ⁿ⁻ˣ. */
export function espacioMuestral(n, p) {
  return cad.sigmaN(['C', 'E'], n).map(w => {
    const x = cad.contar(w, 'E');
    return { valor: w, errores: x, probabilidad: Math.pow(p, x) * Math.pow(1 - p, n - x) };
  });
}

/** Cada bit se invierte con probabilidad p. */
export function transmitir(trama, p, azar = Math.random) {
  let recibida = '';
  const posiciones = [];
  for (let i = 0; i < trama.length; i++) {
    const b = trama[i];
    if (azar() < p) { recibida += b === '0' ? '1' : '0'; posiciones.push(i); }
    else recibida += b;
  }
  return { enviada: trama, recibida, posiciones, errores: posiciones.length };
}

/** frec[k] = número de envíos con exactamente k errores. */
export function simular(n, p, envios, azar = Math.random) {
  const frec = Array(n + 1).fill(0);
  for (let e = 0; e < envios; e++) {
    let x = 0;
    for (let i = 0; i < n; i++) if (azar() < p) x++;
    frec[x]++;
  }
  return frec;
}

// ---------------------------------------------------------------------
// Paridad con XOR (⊕)
// ---------------------------------------------------------------------
export const paridad = {
  xorTotal: bits => [...bits].reduce((r, c) => r ^ (c === '1' ? 1 : 0), 0),
  agregar: m => m + paridad.xorTotal(m),
  expresion: bits => (bits ? [...bits].join(' ⊕ ') + ' = ' + paridad.xorTotal(bits) : '0'),
  pasoAPaso(bits) {
    if (!bits) return '  (vacío) = 0\n';
    let acum = bits[0] === '1' ? 1 : 0;
    let s = '  inicio: ' + acum + '\n';
    for (let i = 1; i < bits.length; i++) {
      const b = bits[i] === '1' ? 1 : 0, nuevo = acum ^ b;
      s += '  ' + acum + ' ⊕ ' + b + ' = ' + nuevo + '\n';
      acum = nuevo;
    }
    return s;
  },
};

// ---------------------------------------------------------------------
// Expresiones booleanas (descenso recursivo)
//   NOT: A'  !A  ¬A   ·   AND: A·B  A*B  A&B  AB   ·   XOR: A⊕B  A^B   ·   OR: A+B  A|B
// ---------------------------------------------------------------------
export class ExpresionBooleana {
  constructor(texto) {
    this.texto = texto.trim();
    this.s = this.texto.replace(/\s+/g, '').toUpperCase();
    if (!this.s) throw new Error('Escribe una expresión.');
    this.pos = 0;
    this.vistas = new Set();
    this.raiz = this.disyuncion();
    if (this.pos < this.s.length) {
      throw new Error("Símbolo inesperado '" + this.s[this.pos] + "' en la posición " + (this.pos + 1) + '.');
    }
    this.variables = [...this.vistas].sort();
  }
  actual() { return this.pos < this.s.length ? this.s[this.pos] : '\0'; }
  disyuncion() {
    let izq = this.xor();
    while ('+|∨'.includes(this.actual()) && this.actual() !== '\0') {
      this.pos++;
      const a = izq, b = this.xor();
      izq = v => a(v) || b(v);
    }
    return izq;
  }
  xor() {
    let izq = this.conjuncion();
    while ('^⊕'.includes(this.actual()) && this.actual() !== '\0') {
      this.pos++;
      const a = izq, b = this.conjuncion();
      izq = v => a(v) !== b(v);
    }
    return izq;
  }
  conjuncion() {
    let izq = this.unario();
    for (;;) {
      const c = this.actual();
      if ('·*&∧'.includes(c) && c !== '\0') this.pos++;
      else if (!this.iniciaFactor(c)) break;      // AND implícito: AB = A·B
      const a = izq, b = this.unario();
      izq = v => a(v) && b(v);
    }
    return izq;
  }
  iniciaFactor(c) { return /[A-Z01(!¬]/.test(c) && c !== '\0'; }
  unario() {
    if (this.actual() === '!' || this.actual() === '¬') {
      this.pos++;
      const a = this.unario();
      return v => !a(v);
    }
    let n = this.primario();
    while (this.actual() === "'" || this.actual() === '’' || this.actual() === '′') {
      this.pos++;
      const a = n;
      n = v => !a(v);
    }
    return n;
  }
  primario() {
    const c = this.actual();
    if (c === '(') {
      this.pos++;
      const n = this.disyuncion();
      if (this.actual() !== ')') throw new Error('Falta cerrar un paréntesis.');
      this.pos++;
      return n;
    }
    if (c === '0' || c === '1') { this.pos++; const val = c === '1'; return () => val; }
    if (/[A-Z]/.test(c)) { this.pos++; this.vistas.add(c); return v => !!v[c]; }
    if (c === '\0') throw new Error('La expresión termina de forma incompleta.');
    throw new Error("Símbolo no válido '" + c + "'.");
  }
  evaluar(valores) { return this.raiz(valores); }

  /** Valores de las variables en la fila i (la primera variable es el bit más significativo). */
  static fila(vars, i) {
    const m = {};
    vars.forEach((v, j) => { m[v] = ((i >> (vars.length - 1 - j)) & 1) === 1; });
    return m;
  }
  minterminos() {
    const r = [];
    for (let i = 0; i < (1 << this.variables.length); i++) if (this.evaluar(ExpresionBooleana.fila(this.variables, i))) r.push(i);
    return r;
  }
  sumaDeProductos() {
    const m = this.minterminos();
    if (!m.length) return '0';
    if (m.length === (1 << this.variables.length)) return '1';
    return m.map(i => {
      const f = ExpresionBooleana.fila(this.variables, i);
      return this.variables.map(v => v + (f[v] ? '' : "'")).join('');
    }).join(' + ');
  }
  static equivalentes(e1, e2) {
    const vars = [...new Set([...e1.variables, ...e2.variables])].sort();
    for (let i = 0; i < (1 << vars.length); i++) {
      const f = ExpresionBooleana.fila(vars, i);
      if (e1.evaluar(f) !== e2.evaluar(f)) return false;
    }
    return true;
  }
}
