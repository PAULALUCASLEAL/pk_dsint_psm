# Diseño de la ampliacion de Creta (Fase 2)

## 1. Principales decisiones de diseño

### Reutilizacion de las entidades y Clasificacion múltiple
Los personajes nuevos seran individuos de las clases existentes. No vamos a crear una clase Java por personaje. 
Heroe representa un papel narrativo, mientras que Mortal y Semidios describen la naturaleza o ascendencia del personaje. Estas categorias no se consideran mutuamente excluyentes. Se mantiene Perseo como Heroe y Semidios. Heracles recibe los mismos tipos. Teseo se representa como Heroe y Mortal.

### Actualización del LectorEscenario (Parser)
El parser original solo admitía misiones de "matar" o "liberar". Se modificó la expresión regular (`^\\¿?Puede (.+?) (liberar a|matar a|Obtener|tener la capacidad de) (.+?)\\?$`) y el bloque de instanciación para soportar misiones de `OBTENER_OBJETO` y `OBTENER_CAPACIDAD`. 
*Justificación:* Era estrictamente necesario para poder procesar los escenarios F2-7 y F2-8 siguiendo el formato literal exigido por el enunciado (ej. "¿Puede Perseo Obtener Brida de Oro?").

### Generalización de roles de combate (Empoderamiento)
Se modificaron las reglas de combate y rescate (ej. R23-R24, R28) cambiando la precondición `Personaje(heroe == true)` por `Personaje(dios == false, criatura == false)`. 
*Justificación:* Esto permite que personajes mortales (como Dánae) puedan combatir sin estar etiquetados rígidamente como héroes. Cumple la directriz de generalizar al máximo y da solución al Escenario F2-6 ("Las chicas se empoderan").

### Mantenimiento de la verdad y Saqueo (Regla R33)
Se introdujo una regla (`salience 50`) que dicta que el vencedor de un combate hereda automáticamente todos los objetos no anatómicos de su víctima.
*Justificación:* Drools utiliza `insertLogical` para las capacidades. Al saquear dinámicamente un objeto (como las Sandalias o el Casco), el vencedor adquiere automáticamente la capacidad de Vuelo o Invisibilidad. Esto es vital para encadenar misiones en el modo "Battle Royale" sin programar lógicas específicas por personaje.

### Situacion en el laberinto y Estado de las alas
Vamos a representar como cosas distintas estar dentro del laberinto y estar preso por un personaje. Estar libre no significa estar fuera. Se usa la clase Lugar y el hecho Java `Ubicacion(personaje, lugar)`.
Las alas tendrán estado explícito (`EstadoFisicoObjeto`) "intacto" o "roto". Las alas rotas impiden el vuelo, cumpliendo el requisito de que se puedan reparar a posteriori con cera y plumas.

### Relaciones humanas
Reutilizamos Favor y Enojo para cualquier personaje, no solo dioses. 
*Justificación:* El enunciado pide no añadir apenas conceptos a la ontología. Ampliando el rango de `tiene_favor` y `tiene_enojo` a Personaje, podemos definir "Teseo tiene el favor de Ariadna" directamente en el fichero de texto, y la regla R03 hace que Ariadna le entregue el hilo de forma genérica.

---

## 2. Explicación de Escenarios 1-7 (y 8)

*   **F2-1 a F2-4 (Mitos clásicos e hibridación de vuelo):** Los escenarios iniciales funcionan encadenando las reglas genéricas. En F2-4, el favor de Dédalo hacia Perseo transfiere automáticamente las alas recién construidas a Perseo, permitiéndole volar hacia Medusa sin reglas específicas.
*   **Escenario F2-5 (Héroe apresado por el Minotauro):** Se resolvió añadiendo la regla `C05b`. Si un personaje es encerrado por Minos y está en el laberinto sin el Hilo de Ariadna, el cautiverio se transfiere al Minotauro. Esto congela al héroe apresado (Perseo) hasta que otro (Teseo) mata a la bestia y se dispara la regla de rescate `R29`.
*   **Escenario F2-6 (Las chicas se empoderan):** Se resuelve gracias a la generalización de héroes. Aprovechando que Zeus enoja a Poseidón (provocando el encierro de los varones), Dánae asume el rol principal, se equipa con los objetos divinos, mata a Medusa y rescata a todos.
*   **Escenario F2-7 (Solo puede quedar uno):** Se implementó la regla `R32` (Battle Royale), que permite a cualquier héroe armado asesinar a otro que posea un trofeo (cabeza). Para evitar el *overfitting* y que héroes inactivos interfirieran masacrando a otros, se añadió la restricción de que los participantes del combate deben tener un `Favor` activo en la sesión. Heracles termina asesinando a los demás, saqueando sus objetos (R100) y finalizando la odisea.
*   **Escenario F2-8 (Mito propio):** Se diseñó una historia donde Heracles busca obtener la capacidad de Vuelo. Se explotan todas las mecánicas: Minos encierra a Dédalo, Perseo recibe las alas, y finalmente Heracles asesina a Perseo para robarle las alas de Dédalo, demostrando la herencia dinámica de capacidades de Drools.

---

## 3. Actualizaciones de Ontología

*   **Rango de Favores:** Se modificó el rango de las propiedades `tiene_favor` y `tiene_enojo` para que apunten a `Personaje` en lugar de solo a `Dios`.
*   **Ubicación inicial (`MundoExterior`):** Se añadió explícitamente la propiedad `esta_en_lugar` apuntando a `MundoExterior` a los héroes principales de la Fase 1 (Perseo, Heracles, etc.). 
    * *Justificación:* Sin este aserto en la ontología, los personajes clásicos carecían de ubicación, lo que provocaba que las reglas de Creta (que filtran explícitamente por `MundoExterior` o `LaberintoCreta`) los ignoraran por completo, rompiendo la hibridación de escenarios como el F2-5.

---

## 4. Pruebas pendientes verificadas

- [x] Carga de los nuevos individuos.
- [x] Historia completa de Creta (F2-2).
- [x] Alas rotas sin vuelo.
- [x] Perseo utiliza alas como fuente de vuelo (F2-4).
- [x] Rescate cruzado en laberinto (F2-5).
- [x] Combate cruzado y saqueo (F2-7).
- [x] Los escenarios originales de Perseo siguen funcionando con las nuevas reglas.