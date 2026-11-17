# Guía del proyecto DSINT

Este documento explica el sistema del mito de Perseo y Andrómeda (fase 1.b), que está terminado.
La ampliación al mito de Creta (fase 2) está en marcha: el diseño está en `DISENO_CRETA.md`,
el diario de cambios en `CAMBIOS_CRETA_SERGIO.txt` y las pruebas manuales en `PRUEBAS.md`.
Aquí solo se resume lo que ya existe de Creta (sección «Lo nuevo de Creta»).

## Estructura de la carpeta pk\_dsint\_psm

Todo lo importante está en `sistema-inteligente/`, que es un proyecto Maven normal: el código en `src/main`, las pruebas en `src/test`.

```
pk_dsint_psm/
├── ontologia/
│   ├── mitologia.owl           la ontología (personajes, objetos, capacidades, lugares)
│   └── mitologia.pprj          proyecto de Protégé 3.4.8 para abrir el OWL
├── documentacion/
│   ├── GUIA_PROYECTO.md        este documento
│   ├── REGLAS_DROOLS.md        documentación técnica de cada regla
│   ├── DISENO_CRETA.md         decisiones de diseño del mito de Creta
│   ├── CAMBIOS_CRETA_SERGIO.txt diario de cambios de Creta y lista de pendientes
│   └── PRUEBAS.md              pruebas manuales con su resultado
└── sistema-inteligente/
    ├── pom.xml                 dependencias (Drools 7.74.1, JUnit) y Java 8
    ├── escenarios/             ENTRADA: EscenarioF1-2.txt, EscenarioF2-1.txt...
    ├── soluciones/             SALIDA: RespuestaF1_2.txt, RespuestaF2_1.txt...
    └── src/
        ├── main/java/dsi/mitologia/
        │   ├── Principal.java      el "main": lee escenarios/, ejecuta y escribe en soluciones/
        │   ├── entrada/            el lector de los ficheros de escenario
        │   ├── modelo/             las COSAS del mito (Personaje, Objeto, Capacidad, Lugar)
        │   ├── hechos/             las RELACIONES entre cosas (quién tiene qué, quién mató a quién...)
        │   ├── escenario/          una partida: condiciones, estado y resultado
        │   ├── eventos/            el "diario" de lo que va pasando, un REGISTRO
        │   ├── motor/              el enchufe con Drools
        │   └── ontologia/          el lector del fichero .owl
        ├── main/resources/
        │   ├── META-INF/kmodule.xml  le dice a Drools dónde están las reglas
        │   └── reglas/             LAS REGLAS (7 ficheros .drl)
        └── test/
            ├── java/...            las pruebas automáticas (JUnit)
            └── resources/...       reglas que solo se usan en pruebas (ping → pong)
```

Para orientarse: si quieres saber **qué sabe** el programa, mira `modelo/` y `hechos/`; si quieres saber **qué razona**, mira `reglas/`.

## Las clases Java

**modelo/ — las cosas del mito**

| Clase | Qué es | Qué guarda |
| --- | --- | --- |
| `Personaje` | Cualquier ser: Perseo, Medusa, Atenea... | id, nombre, tipos (Dios, Héroe, Criatura...), si está vivo o muerto, si está libre o preso |
| `Objeto` | Una cosa: casco, hoz, zurrón... | id, nombre, categorías (Arma, Protección, Artefacto mágico, Parte anatómica) |
| `Capacidad` | Un poder: Vuelo, Invisibilidad, Reflejo, Petrificación, FuerzaExtraordinaria | id y nombre |
| `Lugar` | (Creta) Un sitio: LaberintoCreta, MundoExterior | id y nombre |
| `EntidadMitologica` | Interfaz común a las cuatro | solo «tener id y nombre» |
| `enums/` | Listas cerradas de valores | `EstadoVital` (vivo/muerto), `EstadoLibertad` (libre/preso), `TipoPersonaje`, `CategoriaObjeto`, `OrigenCapacidad` |

**hechos/ — las relaciones** (cada una es una ficha aparte para que las reglas las puedan buscar)

| Clase | Significa | Ejemplo |
| --- | --- | --- |
| `Posesion` | X tiene el objeto O (un objeto, un único dueño) | Hades tiene CascoHades |
| `ProporcionaCapacidad` | El objeto O da la capacidad C | CascoHades da Invisibilidad |
| `CapacidadDisponible` | X tiene la capacidad C gracias a una fuente | Perseo tiene Vuelo por SandaliasAladas |
| `Favor` | X tiene el favor de D (un dios o no) | Perseo – Atenea, Teseo – Ariadna |
| `Enojo` | X tiene el enojo de D (un dios o no) | Casiopea – Poseidón, Dédalo – Minos |
| `Cautiverio` | C tiene preso a P | Ceto tiene presa a Andrómeda |
| `Liberacion` | L ha liberado a P | Perseo libera a Andrómeda |
| `Muerte` | A ha matado a V | Perseo mata a Medusa |
| `Localizacion` | X ha encontrado a Y | Perseo localiza a las Grayas |
| `Filiacion` | H es hijo de P | Andrómeda es hija de Casiopea |
| `VarianteMito` | Activa una versión alternativa del mito | «Variante Pegaso» |
| `Ubicacion` | (Creta) X está en el lugar L; cambia con `modify` cuando se mueve | Teseo está en MundoExterior |
| `EstadoFisicoObjeto` | (Creta) El objeto O está «intacto» o «roto» | AlasIcaro está roto |

**escenario/ — una partida**

| Clase | Qué hace |
| --- | --- |
| `DominioMitologico` | Todo lo que dice el OWL, cargado una vez. No se modifica. |
| `EstadoAventura` | Una copia del dominio para una partida concreta: aquí sí cambian las cosas. |
| `EscenarioMitologico` | Una partida completa: pregunta, condiciones, estado, diario y resultado. |
| `Objetivo` / `TipoObjetivo` | La pregunta en forma de dato: «Perseo LIBERAR Andrómeda». Sabe comprobar si se ha cumplido. |
| `ResultadoMision` | PENDIENTE, CONSEGUIDO o NO\_CONSEGUIDO. |
| `PreparadorEscenario` | Carga el OWL y aplica las condiciones: «favor», «enojo», «tiene», «no tiene», «variante». |

**El resto**

| Clase | Qué hace |
| --- | --- |
| `entrada/LectorEscenario` | Lee un `EscenarioFx-y.txt`: la pregunta («Puede X liberar/matar a Y?»), la línea `Condiciones:` y cada condición («tiene el favor de», «tiene el enojo de», «tiene», «no tiene»). |
| `ontologia/CargadorOntologia` | Lee `mitologia.owl` (es XML, formato Protégé 3.4.8 o 5) y crea los personajes, objetos, lugares y relaciones. |
| `eventos/EventoMitologico` | Una línea del diario: quién, qué acción, sobre quién, con qué objeto y por qué. |
| `eventos/RegistroEventos` | El diario entero, numerado (E1, E2...). Las reglas escriben en él. |
| `eventos/TipoAccion` | Tipos de acción: OBTENER\_OBJETO, ROBAR\_OBJETO, MATAR, LIBERAR... |
| `motor/MotorReglas` | La única clase que habla con Drools: mete los hechos, dispara las reglas y recoge el resultado. |
| `Principal` | El `main`: lee todos los `escenarios/EscenarioF1-x.txt` y `EscenarioF2-x.txt`, los ejecuta y escribe cada respuesta en `soluciones/RespuestaFx_y.txt`. |

## Qué pasa cuando se ejecuta un escenario

Java prepara los datos y Drools hace todo el razonamiento (el paso 4 es el de Drools; los demás son Java, que solo mueve datos):

```
┌──────────────────────────────────────────────────────────────────────────┐
│ 1 · Leer la ontología                                                    │
│   CargadorOntologia lee mitologia.owl y crea personajes, objetos y       │
│   relaciones (quién tiene cada objeto, quién es hijo de quién)           │
│   en DominioMitologico.                                                  │
└──────────────────────────────────────────────────────────────────────────┘
                                     │
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│ 2 · Leer el escenario y preparar la partida                              │
│   LectorEscenario lee escenarios/EscenarioFx-y.txt; PreparadorEscenario  │
│   hace una copia (EstadoAventura) y aplica las condiciones: favores,     │
│   enojo, «Perseo tiene...», «Atenea no tiene...».                        │
└──────────────────────────────────────────────────────────────────────────┘
                                     │
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│ 3 · Meter las fichas en Drools                                           │
│   MotorReglas abre una sesión nueva de Drools e inserta todos los hechos │
│   y el Objetivo (la pregunta: Perseo LIBERAR Andrómeda).                 │
└──────────────────────────────────────────────────────────────────────────┘
                                     │
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│ 4 · Disparar las reglas  (DROOLS)                                        │◄─┐
│   Cada regla que encaja cambia fichas y apunta un evento en el diario;   │  │ en cadena
│   esos cambios despiertan otras reglas, hasta que ninguna puede actuar.  │──┘
└──────────────────────────────────────────────────────────────────────────┘
                                     │
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│ 5 · Recoger y responder                                                  │
│   MotorReglas copia el estado final y Objetivo decide si es Sí o No;     │
│   Principal escribe la respuesta con los eventos del diario en           │
│   soluciones/RespuestaFx_y.txt y también por pantalla.                   │
└──────────────────────────────────────────────────────────────────────────┘
```

El único paso que «piensa» es el 4: todo lo demás carga datos o los enseña. Por eso, si una respuesta sale mal, casi siempre hay que mirar una regla en `reglas/`.

## Drools para principiantes

Drools tiene una **memoria de trabajo**, que es una bolsa donde metemos todos los hechos (fichas). Cada regla mira la bolsa y, si encuentra hechos que encajan con su condición, se ejecuta. Al ejecutarse cambia hechos, y eso puede despertar a otras reglas. Así se encadena todo hasta que ninguna regla tiene nada que hacer.

Una regla en un fichero `.drl` tiene dos partes:

- **`when`** (cuándo): las fichas que tienen que existir. Los nombres con `$` son variables para recordar lo que se ha encontrado.
- **`then`** (entonces): lo que cambia. Solo usamos `modify` (cambiar una ficha), `insert` (añadir una), `delete` (quitar una) y apuntar el evento en el diario.

Ejemplo real, de `favores.drl`, con comentarios:

```
rule "R03-R04-R06 Quien da su favor entrega su objeto"
when
    $p   : Personaje(dios == false, estadoVital == VIVO, estadoLibertad == LIBRE)
                                  // alguien que no es dios, vivo y libre...
           Favor(personaje == $p, $d : dios)
                                  // ...que tiene el favor de un dios $d...
           Personaje(this == $d, estadoVital == VIVO)
                                  // ...que está vivo...
    $pos : Posesion(poseedor == $d, $o : objeto)
                                  // ...y ese dios tiene un objeto $o
           Objeto(this == $o, id != "EspadaIndestructible")
                                  // (la espada es una versión alternativa)
then
    modify($pos) { setPoseedor($p) }   // el objeto pasa a ser de $p
    registro.registrar(... "obtiene " + $o + " debido a tener el favor de " + $d ...);
end
```

Con el escenario 1, esta única regla se dispara tres veces: Atenea da el escudo, Hermes la hoz y Hades el casco. No pone «Perseo» en ningún sitio: funcionaría igual con Teseo. Y no entra en bucle: en cuanto el objeto cambia de dueño, el dios ya no lo tiene y la condición deja de cumplirse.

Dos ficheros más completan el montaje: `kmodule.xml` le dice a Drools que las reglas están en la carpeta `reglas`, y `MotorReglas` es quien mete las fichas y pulsa «arrancar» (`fireAllRules`).

## Las reglas que hemos usado

Partimos de nuestro catálogo de 48 reglas en lenguaje natural (R01–R48). Las que describen el mito se han convertido en reglas Drools, repartidas en 7 ficheros en `src/main/resources/reglas/` y en el orden en que pasan las cosas. Donde una regla general vale para varios casos, se ha juntado (por eso hay nombres como «R03-R04-R06»).

| Regla | Fichero | Qué hace (SI... ENTONCES...) |
| --- | --- | --- |
| R00 | `informacion_inicial.drl` | Al empezar, apunta el estado inicial (quién está vivo, quién tiene qué). Lo pide el profesor. |
| R01 | `inicio.drl` | SI un dios está enojado y tenía retenida a una criatura, ENTONCES la suelta (Poseidón suelta a Ceto). |
| R02 | `inicio.drl` | SI la criatura queda libre, ENTONCES apresa a la hija de quien enojó al dios (Andrómeda). |
| R03-R04-R06 | `favores.drl` | SI tienes el favor de un dios, ENTONCES te da su objeto (escudo, hoz, casco). |
| R05 | `favores.drl` | SI tienes el favor de Hefesto y el escudo de bronce, ENTONCES lo convierte en escudo-espejo. |
| R07 / R08 | `favores.drl` | Versiones alternativas: Hefesto da la espada o las bridas de oro. |
| R09-R12 | `capacidades.drl` | SI tienes un objeto que da una capacidad, ENTONCES tienes esa capacidad (casco o anillo → invisibilidad, etc.). |
| R20 / R20b / R21 | `capacidades.drl` | Versión Pegaso: con las bridas capturas a Pegaso y vuelas montándolo; en la versión clásica no se puede. |
| R13 | `grayas_ninfas.drl` | SI tienes el favor de Hermes y de Atenea, ENTONCES localizas a las Grayas. |
| R14-R15 | `grayas_ninfas.drl` | SI eres invisible y has localizado a las Grayas, ENTONCES les robas el ojo o el diente. |
| R16-R17 | `grayas_ninfas.drl` | SI tienes el ojo o el diente, ENTONCES lo devuelves a cambio de saber dónde están las Ninfas. |
| R18-R19 | `grayas_ninfas.drl` | SI has localizado a las Ninfas, ENTONCES te dan sus objetos (zurrón, sandalias). |
| R23-R24 | `medusa.drl` | SI tienes un arma, Reflejo, Invisibilidad y Vuelo, ENTONCES matas a Medusa. |
| R25 | `medusa.drl` | SI has matado a alguien, ENTONCES te quedas con su parte del cuerpo (la cabeza). |
| R26 | `medusa.drl` | SI llevas la cabeza y tienes el zurrón, ENTONCES sobrevives. |
| R27 | `medusa.drl` | SI llevas la cabeza y NO tienes el zurrón, ENTONCES mueres. |
| R28 | `rescate.drl` | SI puedes petrificar, ENTONCES matas a la criatura que tiene a alguien preso (Ceto). |
| R29 | `rescate.drl` | SI has matado al captor, ENTONCES liberas a su preso (Andrómeda). |
| R30 | `rescate.drl` | SI se ha liberado a quien pedía la pregunta, ENTONCES apunta «Sí, puede». |
| R31 | `rescate.drl` | SI al final no se ha conseguido, ENTONCES apunta «No» y qué faltó (vuelo, arma, el héroe murió...). |

Las reglas del catálogo que no aparecen en la tabla no son reglas del mito sino «normas de la casa», y están hechas de otra forma. Por ejemplo, «Atenea no tiene el escudo» lo aplica Java al preparar la partida, y «un muerto no actúa» está en todas las reglas como condición `estadoVital == VIVO`.

## Los trucos para que no se rompa

- **Capacidades «lógicas» (`insertLogical`).** La capacidad no se apunta para siempre: existe mientras exista lo que la justifica. Si Perseo pierde el anillo, Drools le quita la invisibilidad él solo; si muere, pierde todas. Y como hay una ficha por cada fuente, si tiene casco y anillo y pierde el casco, sigue siendo invisible.
- **Las reglas preguntan por capacidades, no por objetos.** Matar a Medusa pide «Vuelo», no «sandalias». Por eso el escenario 2 (anillo, espejo de mano y conjuro) funciona sin tocar ninguna regla.
- **Prioridades (`salience`).** Cuando hay varias reglas listas a la vez, Drools elige primero la de mayor prioridad. Las usamos en tres puntos: el combate va al final (primero el héroe recoge todo lo que puede); al coger la cabeza se decide antes que nada si muere o sobrevive; y el «No» final solo sale cuando ya no queda nada por hacer. Esto lo descubrimos por un fallo real: sin prioridad, a veces Perseo iba a por Medusa antes de recibir el zurrón y moría.
- **Sin bucles infinitos.** Cada regla, al ejecutarse, rompe su propia condición (el objeto cambia de dueño, el personaje deja de estar vivo o preso...). Drools tampoco repite una regla para las mismas fichas.
- **Variantes del mito.** La espada de Hefesto o Pegaso solo se activan si la partida incluye una ficha `VarianteMito`. Si no, se sigue la versión del enunciado.
- **Punto de partida.** El OWL ya trae a Andrómeda presa (que es el final de la historia). Para que el sistema lo deduzca, cada partida empieza un paso antes: Andrómeda libre y Ceto retenido por Poseidón.
- **«No lo sé» no es «no».** Que el OWL no diga algo no lo hace falso. Solo se quita algo cuando la partida lo dice expresamente («Atenea no tiene Escudo de Bronce»).
- **Partidas independientes.** Cada escenario usa su propia copia del estado y su propia sesión de Drools, así que lo que muere en una partida no sigue muerto en la siguiente.

## Cómo ejecutarlo y qué comprueban las pruebas

Hace falta **Java 8**. Con versiones nuevas de Java (por ejemplo, la 25) Drools 7 da el error `NoClassDefFoundError: java/lang/Compiler`. Desde la carpeta `sistema-inteligente/`:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)   # en Mac: usar Java 8
mvn clean test        # ejecuta las pruebas automáticas
mvn compile exec:java # lee escenarios/, imprime cada respuesta y la guarda en soluciones/
```

Hay 52 pruebas automáticas y pasan todas. Las del estado inicial de Creta están en `EstadoInicialCretaTest`, y las de favores y enojos entre personajes que no son dioses en `FavorYEnojoEntrePersonajesTest`;
las del mito de Perseo, en `ReglasPerseoAndromedaTest`:

| Prueba | Qué se comprueba | Resultado esperado |
| --- | --- | --- |
| Escenario 1 (enunciado) | Favores de Atenea, Hermes, Hades y Hefesto | Sí, y en el orden del mito |
| Escenario 2 (enunciado) | Sin escudo, casco ni sandalias; con anillo, espejo de mano y conjuro | Sí, usando los sustitutos |
| Sin zurrón | Perseo coge la cabeza sin zurrón | Muere una vez y ya no actúa |
| Sin forma de volar | Nada que dé Vuelo | Medusa sigue viva; No |
| Pegaso / espada | Versiones alternativas | Funcionan solo si se activan |
| Reinicio | Varias partidas seguidas | No se contaminan entre ellas |
| Bucles y orden | 25 órdenes de entrada al azar, límite de 1000 disparos | Mismo resultado y siempre termina |

La regla de prueba antigua `ping → pong` solo comprobaba que Drools arrancaba. Ahora está en los recursos de test y no se carga con las reglas del mito.

## Lo nuevo de Creta (fase 2, en construcción)

Ya existe:

- **Ontología:** Teseo (Héroe y Mortal), Ariadna, Dédalo, Ícaro y Minos (Mortal), Minotauro (Criatura),
  Heracles (Héroe y Semidiós, con FuerzaExtraordinaria y el favor de Zeus). Objetos: HiloAriadna,
  EspadaTeseo, AlasIcaro y AlasDedalo (dan Vuelo, sin dueño al empezar), CuernoMinotauro (parte anatómica
  y arma), CeraAbejas, PlumasAve, CabezaMinotauro y CabezaCeto. Lugares: LaberintoCreta y MundoExterior.
  Propiedades: `esta_en_lugar` y `estado_fisico` («intacto»/«roto»). `tiene_favor` y `tiene_enojo` admiten ahora
  cualquier personaje, no solo dioses (ver «Relaciones humanas» en `DISENO_CRETA.md`).
- **Java:** las clases `Lugar`, `Ubicacion` y `EstadoFisicoObjeto`.
  Los cuatro hechos se cargan del OWL, se copian por escenario, se meten en Drools y se recuperan al acabar.
  Al empezar: el Minotauro está en LaberintoCreta; Teseo, Ariadna, Dédalo e Ícaro en MundoExterior;
  las dos alas están «intacto». La ayuda de Ariadna se escribe en el escenario («Teseo tiene el favor de Ariadna»)
  y el enojo de Minos lo creará una regla o el escenario («Dedalo tiene el enojo de Minos»).
- **Efecto en Perseo:** como Ceto ahora posee CabezaCeto, la regla R25 se la da a Perseo al matarlo.
  Las respuestas F1 tienen una línea más, pero el resultado no cambia.

Todavía no existe ninguna regla de Creta. La lista completa de pendientes está al final de
`CAMBIOS_CRETA_SERGIO.txt`. Fallos que había y ya están arreglados:

- [x] Favor y Enojo valen para cualquier personaje; se quitan `ayuda_a`, `hostil_con`, `AyudaHumana` y `HostilidadHumana`.
- [x] `EstadoAventura.sincronizarDesde` recupera las `Ubicacion` al acabar Drools (antes se perdían).
- [x] En el OWL el Minotauro empieza en LaberintoCreta.
- [x] AlasIcaro y AlasDedalo tienen `estado_fisico` «intacto».

## Lo que falta todavía (Perseo)

- [x] Leer las preguntas desde los ficheros `escenarios/EscenarioF1-x.txt` (hecho: `LectorEscenario`).
- [x] Escribir la respuesta en `soluciones/RespuestaF1_x.txt` (hecho: `Principal`).
- [ ] Nombres bonitos («Casco de Hades» en lugar de `CascoHades`): el OWL no los tiene.
- [ ] Decidir entre todos el punto de partida (Andrómeda libre al empezar) y si añadimos al OWL el anillo, el espejo de mano y el conjuro, que ahora añade Java.
