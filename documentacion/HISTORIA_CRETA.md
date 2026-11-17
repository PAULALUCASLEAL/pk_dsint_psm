# Historia de Creta (escenario F2-2)

Esta es la versión del mito que hemos decidido contar con nuestras reglas.

## Cómo empieza

El laberinto ya está construido y el Minotauro está dentro. Construirlo no aporta nada a la historia, así que empezamos a partir de ahí. Teseo, Ariadna, Dédalo e Ícaro están fuera, vivos y libres. Ariadna tiene el hilo y Teseo su espada. Las alas existen y están intactas, pero no son de nadie todavía, igual que la cera y las plumas.

## Qué pasa

1. Ariadna ayuda a Teseo y le da el hilo.
2. Con el hilo y la espada, Teseo entra en el laberinto.
3. Mata al Minotauro y se queda su cabeza y su cuerno.
4. Sigue el hilo y sale.
5. Minos se entera de que alguien ha entrado y salido, y se enoja con Dédalo.
6. Encierra en el laberinto a Dédalo y a su hijo Ícaro.
7. Dentro, Dédalo encuentra cera y plumas y fabrica dos pares de alas.
8. Con las alas consiguen Vuelo y escapan.
9. Ícaro muere en la huida y sus alas se quedan rotas donde cae.

Así se cumple lo que pide el enunciado: el Minotauro muere a manos de Teseo, Ícaro muere y Dédalo acaba fuera del laberinto.

## El fichero de entrada

```
Puede Teseo matar a Minotauro?
Condiciones:
Teseo tiene el favor de Ariadna
```

Con esa condición basta, el resto se encadena solo.

## Decisiones que hemos tomado

- Ariadna da el hilo por su favor, con la misma regla que usan los dioses con Perseo. Por eso Favor y Enojo valen para cualquier personaje (ver DISENO_CRETA.md).
- El enojo de Minos es el mismo Enojo de Poseidón. Cualquiera que enoje a Minos acaba en el laberinto junto a sus hijos, no solo Dédalo.
- La cera y las plumas las encuentra quien está encerrado en el laberinto.
- Las alas las fabrica Dédalo. La ontología no dice quién sabe hacerlas y el enunciado pide no ampliarla, así que la regla lo nombra.
- Para escapar basta con estar encerrado y tener Vuelo, venga de donde venga.
- No podemos representar el sol, así que Ícaro muere por ser el hijo que escapa volando con su padre.
- Las alas de Ícaro quedan rotas y sin recoger. Así otro héroe puede encontrarlas y arreglarlas con cera y plumas (lo usaremos en F2-4).

## Estado

Los pasos 1 a 6 ya funcionan (reglas C01 a C06 en `creta.drl`). Faltan los pasos 7 a 9.
