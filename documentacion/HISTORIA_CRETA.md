# Historia de Creta (escenario F2-2)

Esta es la versión del mito que hemos decidido contar con nuestras reglas.

## Escenario y respuesta: qué es cada cosa

- El **escenario** (`escenarios/EscenarioF2-2.txt`) es la entrada. Lo escribimos nosotros a mano: una pregunta y las condiciones de partida. El enunciado pide que cada grupo cree los suyos.
- La **respuesta** (`soluciones/RespuestaF2_2.txt`) es la salida. No se escribe a mano: la genera el programa al ejecutarlo (`mvn compile exec:java`). Dice si se puede o no y cuenta la historia paso a paso.

Lo que hay en medio es Drools: lee la ontología (lo que hay al empezar), le añade las condiciones del escenario y va disparando reglas hasta que no queda nada por hacer.

El escenario F2-2 es este:

```
Puede Teseo matar a Minotauro?
Condiciones:
Teseo tiene el favor de Ariadna
```

Con esa condición basta, el resto se encadena solo. El enunciado dice que en F2-2 la pregunta da igual; lo importante es que la historia acabe con el Minotauro muerto por Teseo, Ícaro muerto y Dédalo fuera del laberinto.

## Cómo empieza

El laberinto ya está construido y el Minotauro está dentro. Construirlo no aporta nada a la historia, así que empezamos a partir de ahí. Teseo, Ariadna, Dédalo e Ícaro están fuera, vivos y libres. Ariadna tiene el hilo y Teseo su espada. Las alas existen y están intactas, pero no son de nadie todavía, igual que la cera y las plumas.

## Qué pasa

1. Ariadna ayuda a Teseo y le da el hilo.
2. Con el hilo y la espada, Teseo entra en el laberinto.
3. Mata al Minotauro y se queda su cabeza y su cuerno.
4. Sigue el hilo y sale.
5. Minos se entera de que alguien ha entrado y salido, y se enoja con Dédalo.
6. Encierra en el laberinto a Dédalo y a su hijo Ícaro.
7. Dentro encuentran cera y plumas, y Dédalo fabrica dos pares de alas.
8. Con las alas consiguen Vuelo y escapan.
9. Ícaro muere en la huida y sus alas se quedan rotas donde cae.

## Reglas que usa

Hemos reutilizado todas las reglas de Perseo que hemos podido. Como son genéricas ("quien te da su favor te da su objeto", "quien mata a alguien se queda sus partes", "quien tiene un objeto que da Vuelo, vuela"), sirven también para Creta sin tocarlas.

Reutilizadas de Perseo:

- **R03-R04-R06** (`favores.drl`): Ariadna da el hilo a Teseo porque él tiene su favor.
- **R25** (`medusa.drl`): Teseo se queda la cabeza y el cuerno del Minotauro.
- **R09-R12** (`capacidades.drl`): las alas dan Vuelo a quien las tiene.

Nuevas, en `creta.drl`:

- **C01**: un héroe con el hilo y un arma entra en el laberinto.
- **C02**: un héroe con arma mata a la criatura que está con él en el laberinto.
- **C03**: el héroe sale siguiendo el hilo después de matarla.
- **C04**: Minos se enoja con Dédalo cuando alguien ha entrado, matado al Minotauro y salido.
- **C05**: Minos encierra en el laberinto a quien le enoja.
- **C06**: Minos encierra también a sus hijos.
- **C07**: quien está encerrado encuentra la cera y las plumas.
- **C08**: Dédalo fabrica sus alas.
- **C09**: Dédalo fabrica las alas de su hijo.
- **C10**: quien está encerrado y tiene Vuelo escapa.
- **C11**: el hijo que escapa volando con su padre muere y sus alas se rompen.

## Decisiones que hemos tomado

- Favor y Enojo valen para cualquier personaje, no solo para dioses. Así Ariadna da el hilo con la regla de siempre y el enfado de Minos es un Enojo normal. Además el enunciado pide no añadir casi nada a la ontología (ver DISENO_CRETA.md).
- El favor de Ariadna va en el escenario y no en la ontología. Si estuviera en la ontología, Ariadna daría el hilo a Teseo en todas las historias, también en las de Perseo.
- Las reglas son generales siempre que se puede: C01, C02 y C03 valen para cualquier héroe con el hilo, y C05 y C06 para cualquiera que enoje a Minos.
- Solo nombramos a Minos y a Dédalo donde la ontología no da otra forma de saberlo (quién construyó el laberinto, quién sabe hacer alas).
- No podemos representar el sol, así que Ícaro muere por ser el hijo que escapa volando con su padre.
- Al escapar se guarda que la persona "se ha liberado". Así Minos no la vuelve a encerrar; sin esto había un bucle infinito.
- Las alas de Ícaro quedan rotas y sin recoger. Así otro héroe puede encontrarlas y arreglarlas con cera y plumas (lo usaremos en F2-4). Por eso la cera y las plumas no se gastan al fabricar.
- La regla de capacidades (R09) ahora solo reacciona a si el personaje está vivo o muerto. Antes, al escapar (pasar de preso a libre), el Vuelo aparecía dos veces en la historia.

## Escenarios que mezclan los dos mitos

**F2-3. Teseo libera a Andrómeda.** No hace falta ninguna regla nueva: Teseo sigue el mismo camino que Perseo porque las reglas son genéricas. Basta con que tenga el favor de Atenea, Hermes, Hades y Hefesto. Si falta cualquiera de los cuatro, no lo consigue. El enojo de Poseidón no hace falta escribirlo porque ya está en la ontología.

**F2-4. Perseo vuela con las alas de Dédalo.** Tampoco hace falta ninguna regla nueva. Las condiciones son:

```
Teseo tiene el favor de Ariadna
Perseo tiene el favor de Atenea
Perseo tiene el favor de Hermes
Perseo tiene el favor de Hades
Perseo tiene el favor de Hefesto
Perseo tiene el favor de Dedalo
Ninfas no tiene SandaliasAladas
```

- El favor de Ariadna pone en marcha la historia de Creta. Así muere el Minotauro y Dédalo acaba fabricando las alas, como pide el enunciado.
- El favor de Dédalo es lo que une los dos mitos. En los mitos originales Perseo y Dédalo no se conocen, pero el enunciado solo deja poner en el escenario favores, enojos, desposesiones y apresamientos, y pide usar la imaginación para mezclarlos. Lo interpretamos así: Dédalo, una vez libre, ayuda a Perseo prestándole sus alas, igual que los dioses le ayudan con sus objetos. Se usa la misma regla de favores (R03), sin reglas nuevas.
- Quitamos las sandalias a las Ninfas para que Perseo solo pueda volar con las alas.

Hemos comprobado que las siete condiciones son necesarias: quitando cualquiera, Perseo no lo consigue o vuela con las sandalias.

Otra opción, más cercana a la idea del enunciado, sería que Perseo recogiera las alas rotas de Ícaro y las arreglara con cera y plumas. Necesita tres reglas nuevas y la dejamos como mejora.

## Pruebas

Las pruebas automáticas de Creta están en `ReglasCretaTeseoTest`, `ReglasCretaDedaloTest` y `ReglasCretaHuidaTest`. Comprueban el final que pide el enunciado, que los pasos salen en orden y que sin el favor de Ariadna no pasa nada de Creta. La historia de Perseo sigue funcionando igual.
