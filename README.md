# NetDiscreta

Simulador de envío de mensajes en una red de computadores, desarrollado en **Java (Swing / JFrame)** con **NetBeans** para la asignatura **Matemáticas Discretas** – Ingeniería de Sistemas, Universidad Simón Bolívar.

El usuario construye una red, la organiza en grupos, escribe un mensaje binario, lo envía por la ruta más corta, calcula la probabilidad de error del canal y verifica si el mensaje llegó bien. En el proceso se usan todos los temas del curso.

![Módulo Red](docs/img/modulo-red.png)

## Módulos

| # | Módulo | Tema | Estado |
|---|---|---|---|
| 1 | Red | Grafos: vértices, aristas, grados, matriz de adyacencia, conexidad, Dijkstra | ✅ Listo |
| 2 | Grupos | Conjuntos: unión, intersección, diferencia, complemento, Venn | ⏳ Fase 2 |
| 3 | Conexiones | Producto cartesiano A × B y plano cartesiano | ⏳ Fase 2 |
| 4 | Mensajes | Alfabetos, cadenas y lenguajes sobre Σ = {0, 1} | ⏳ Fase 2 |
| 5 | Canal | Espacio muestral, eventos, variable aleatoria, distribución binomial | ⏳ Fase 3 |
| 6 | Verificador | Álgebra booleana: bit de paridad (XOR), tablas de verdad | ⏳ Fase 3 |

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
├── modelo/          → Nodo, Arista, Grafo
├── logica/          → Dijkstra
└── vista/           → VentanaPrincipal, PanelRed, PanelPendiente
    └── graficos/    → LienzoGrafo (dibujo con Graphics2D)
docs/                → Propuesta del proyecto e imágenes
```

La lógica matemática (`modelo`, `logica`) está separada de la interfaz (`vista`), y todas las pestañas comparten el mismo objeto `Grafo`.

## Autor

**Neyder Javier Rojas Lasso** – [GitHub](https://github.com/NeyderJavier) · [LinkedIn](https://www.linkedin.com/in/neyder-javier-rojas-lasso-952a36196)
