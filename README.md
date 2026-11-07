# NetDiscreta

Simulador de envío de mensajes en una red de computadores, desarrollado en **Java (Swing / JFrame)** con **NetBeans** para la asignatura **Matemáticas Discretas** – Ingeniería de Sistemas, Universidad Simón Bolívar.

El usuario construye una red, la organiza en grupos, escribe un mensaje binario, lo envía por la ruta más corta, calcula la probabilidad de error del canal y verifica si el mensaje llegó bien. En el proceso se usan todos los temas del curso.

![Módulo Red](docs/img/modulo-red.png)

## Módulos

| # | Módulo | Tema | Estado |
|---|---|---|---|
| 1 | Red | Grafos: vértices, aristas, grados, matriz de adyacencia, conexidad, Dijkstra | ✅ Listo |
| 2 | Grupos | Conjuntos: unión, intersección, diferencias, complemento, subconjuntos, diagrama de Venn | ✅ Listo |
| 3 | Conexiones | Producto cartesiano A × B en el plano y relación R ⊆ A × B | ✅ Listo |
| 4 | Mensajes | Alfabetos, cadenas (longitud, inversa, concatenación, potencia, prefijos, sufijos), Σⁿ y lenguajes | ✅ Listo |
| 5 | Canal | Espacio muestral, eventos, variable aleatoria, distribución binomial | ⏳ Fase 3 |
| 6 | Verificador | Álgebra booleana: bit de paridad (XOR), tablas de verdad | ⏳ Fase 3 |

## Capturas

| Grupos (conjuntos) | Conexiones (A × B) |
|---|---|
| ![Grupos](docs/img/modulo-grupos.png) | ![Conexiones](docs/img/modulo-conexiones.png) |

| Mensajes (cadenas y lenguajes) |
|---|
| ![Mensajes](docs/img/modulo-mensajes.png) |

**Demo rápida:** en *Red* pulsa *Red de ejemplo*; en *Grupos* pulsa *Grupos de ejemplo*; en *Conexiones* pulsa *Generar A × B*; en *Mensajes* valida `1011001` contra el lenguaje *Paridad par*.

## Cómo ejecutarlo

**En NetBeans:**
1. *File → Open Project* y selecciona la carpeta `NetDiscreta` (NetBeans la reconoce como proyecto Maven).
2. Clic en **Run** (F6).

**Por consola** (requiere JDK 17+ y Maven):
```bash
mvn package
java -jar target/NetDiscreta-1.0.jar
```

## Estructura

```
src/main/java/
├── main/            → NetDiscreta (punto de entrada)
├── modelo/          → Nodo, Arista, Grafo, Conjunto, Estado
├── logica/          → Dijkstra, OperacionesConjuntos, ProductoCartesiano, Cadenas, Lenguaje
└── vista/           → VentanaPrincipal, PanelRed, PanelGrupos, PanelConexiones, PanelMensajes
    └── graficos/    → LienzoGrafo, LienzoVenn, LienzoPlano (dibujo con Graphics2D)
docs/                → Propuesta del proyecto e imágenes
```

La lógica matemática (`modelo`, `logica`) está separada de la interfaz (`vista`). Todas las pestañas comparten un mismo `Estado` (red, grupos y mensaje) y se actualizan al entrar a ellas.

## Autor

**Neyder Javier Rojas Lasso** – [GitHub](https://github.com/NeyderJavier) · [LinkedIn](https://www.linkedin.com/in/neyder-javier-rojas-lasso-952a36196)
