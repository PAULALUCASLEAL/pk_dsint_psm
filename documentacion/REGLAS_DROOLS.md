# Reglas Drools del mito de Perseo y Andromeda (fase 1.b)

Implementacion en Drools 7.74.1.Final del catalogo de 48 reglas (R01-R48) del documento
*Catalogo de reglas — Mito de Perseo y liberacion de Andromeda*. Responde a la pregunta
**«¿Puede Perseo liberar a Andromeda?»** encadenando reglas hacia delante sobre los hechos Java del
modelo (`dsi.mitologia.*`), cargados de `ontologia/mitologia.owl` y de las condiciones de cada escenario.

## 1. Ficheros

```text
sistema-inteligente/src/main/resources/
├── META-INF/kmodule.xml              (sin cambios: kbase mitologiaKB, paquete "reglas")
└── reglas/
    ├── informacion_inicial.drl       R00 + agenda "Informacion Inicial"
    ├── inicio.drl                    R01, R02
    ├── favores.drl                   R03-R04-R06, R05, R07, R08
    ├── capacidades.drl               R09-R12, R20, R20b, R21
    ├── grayas_ninfas.drl             R13, R14-R15, R16-R17, R18-R19
    ├── medusa.drl                    R23-R24, R25, R26, R27
    └── rescate.drl                   R28, R29, R30, R31 (x4)

sistema-inteligente/src/test/resources/dsi/mitologia/pruebas/
    ├── prueba-integracion.drl        regla temporal ping -> pong (trasladada aqui, solo test)
    └── prueba-tecnica-modelo.drl     (existente)
```

Todas las reglas de la historia estan en la agenda MAIN; solo las de informacion inicial estan en el
agenda-group `"Informacion Inicial"`, que R00 pone en foco al empezar (nota 10 del enunciado).

## 2. Como se conectan con las clases Java

| Concepto del mito | Hecho Java | Como lo cambian las reglas |
|---|---|---|
| Personaje y su estado vital/libertad | `modelo.Personaje` | `modify($p) { setEstadoVital(..) }`, `setEstadoLibertad(..)` |
| tiene_objeto / esta_en_posesion_de | `hechos.Posesion` (un poseedor por objeto) | `modify($pos) { setPoseedor($h) }`, `insert`, `delete` |
| proporciona_capacidad (estatico) | `hechos.ProporcionaCapacidad` | solo se lee |
| tiene_capacidad | `hechos.CapacidadDisponible` (origen + fuente) | `insertLogical(CapacidadDisponible.deObjeto/deMontura(..))` |
| tiene_favor / tiene_enojo | `hechos.Favor`, `hechos.Enojo` | solo se leen (entrada del escenario) |
| apresa_a / esta_preso_por | `hechos.Cautiverio` | `insert` al apresar, `delete` al liberar |
| libera_a | `hechos.Liberacion` | `insert` |
| mata_a | `hechos.Muerte` | `insert` |
| localiza_a | `hechos.Localizacion` | `insert` |
| es_hijo_de | `hechos.Filiacion` | se lee (R02); `insert` en R21 |
| Pregunta | `escenario.Objetivo` | se lee (R00, R30, R31) |
| Variante narrativa | `hechos.VarianteMito` (**nuevo**) | se lee |
| Explicacion | global `RegistroEventos registro` + `EventoMitologico` | `registro.registrar(..)` en cada consecuente |

Flujo: `PreparadorEscenario` (nuevo) carga el OWL, aplica el estado previo al mito y las condiciones →
`MotorReglas.ejecutar(sesion, escenario)` inserta `escenario.getHechosParaDrools()`, fija el global
`registro`, llama a `fireAllRules()`, copia el estado final con `EstadoAventura.sincronizarDesde(..)` y
`Objetivo.seCumpleEn(..)` decide CONSEGUIDO / NO_CONSEGUIDO. Java no infiere nada (nota O).

Estilo (notas H-O): consecuentes formados solo por `insert`/`modify`/`delete`/`insertLogical` y el
registro del evento; antecedentes con patrones planos (`Posesion(poseedor == $h, objeto == $o)`), sin
navegar referencias; reglas generales para cualquier heroe/personaje, nunca con «Perseo» en el DRL.

## 3. Prioridades (salience) y control de bucles

| salience | Reglas | Motivo |
|---|---|---|
| 1000 | R00 | Primera en dispararse: pasa el foco a «Informacion Inicial». |
| 100 | R26, R27 | Al obtener la cabeza, decidir antes que nada si el heroe sobrevive o muere. |
| 10 | R16-R17 | El intercambio va antes que un segundo robo (ojo y diente). |
| 0 | resto | — |
| -10 | R23-R24, R28 | Estrategia «prepararse antes de combatir» (ver R23). |
| -500 | R30 | La conclusion se registra al final de la historia. |
| -1000 | R31 | Diagnosticos: solo cuando no queda ninguna otra accion. |

No hay bucles porque cada consecuente invalida su propio antecedente (el objeto cambia de poseedor, el
personaje deja de estar vivo/preso, se inserta la `Localizacion` que la guarda `not` excluye) y Drools no
vuelve a disparar una regla para la misma combinacion de hechos (refraccion). Las reglas que solo
explican (R26, R30, R31) no modifican hechos. Las pruebas lo comprueban con `fireAllRules(1000)` y con
una agenda vacia al terminar. No ha hecho falta `no-loop` ni `lock-on-active`.

## 4. Reglas DRL

Procedencia: **E** = exigida por el enunciado (texto o salida esperada); **V** = variante citada en el
relato; **T** = decision tecnica del equipo (control, no regla mitologica).

### R00 · Mostrar la informacion inicial antes de la historia — `informacion_inicial.drl` · T
- **Condiciones:** existe un `Objetivo`.
- **Consecuencias:** `drools.setFocus("Informacion Inicial")`.
- **Hechos:** `Objetivo`.
- **Enunciado:** nota 10 (uso de agendas: mostrar el estado inicial antes de las misiones).
- **Justificacion:** el foco se declara en el DRL (decision recogida en `MotorReglas`), no en Java.
- **Ejemplo:** primera regla disparada en todos los escenarios.

### Informacion inicial (7 reglas) — `informacion_inicial.drl` · E (nota 10)
- **Condiciones:** `Personaje`, `Posesion`, `Favor`, `Enojo`, `Cautiverio`, `CapacidadDisponible` propia o
  `VarianteMito` presentes al empezar. Agenda-group `"Informacion Inicial"`.
- **Consecuencias:** evento `INFORMACION` («Perseo esta vivo y libre», «Atenea tiene EscudoBronce»...).
  No modifican hechos.
- **Justificacion:** depurar y mostrar las condiciones de partida; los eventos `INFORMACION` se filtran de
  la respuesta final.

### R01 · Un dios enojado libera a la criatura que retenia — `inicio.drl` · E
- **Condiciones:** `Enojo(personaje $m, dios $d)`; `$d` vivo; `Cautiverio(captor $d, preso $cr)`; `$cr`
  criatura viva y presa.
- **Consecuencias:** `delete(Cautiverio)`; `$cr` libre; `insert(Liberacion($d, $cr))`; evento `QUEDAR_LIBRE`.
- **Hechos:** `Enojo`, `Personaje`, `Cautiverio`, `Liberacion`.
- **Enunciado:** «Ceto queda libre debido al enojo de Poseidon hacia Casiopea».
- **Justificacion:** general (cualquier dios y criatura). El punto de partida tiene a Ceto retenido por
  Poseidon (ver §6, decision D2). La version de Doris no se aplica.
- **Ejemplo:** E1: *Ceto queda libre debido al enojo de Poseidon hacia Casiopea.*

### R02 · La criatura liberada apresa al hijo de quien enojo al dios — `inicio.drl` · E
- **Condiciones:** `Liberacion($d, $cr)`; `Enojo($m, $d)`; `Filiacion(hijo $h, progenitor $m)`; `$cr` criatura
  viva y libre; `$h` viva y libre.
- **Consecuencias:** `$h` presa; `insert(Cautiverio($cr, $h))`; evento `APRESAR`.
- **Hechos:** `Liberacion`, `Enojo`, `Filiacion` (Andromeda es_hijo_de Casiopea, del OWL), `Personaje`, `Cautiverio`.
- **Enunciado:** «Andromeda queda presa debido a estar Ceto libre».
- **Justificacion:** no se apresa dos veces (exige libre). Usa la filiacion del OWL en vez de nombrar a Andromeda.
- **Ejemplo:** *Andromeda queda presa debido a estar Ceto libre.*

### R03-R04-R06 · Un dios entrega su objeto a quien tiene su favor — `favores.drl` · E
- **Condiciones:** `$p` no dios, vivo y libre; `Favor($p, $d)`; `$d` vivo; `Posesion(poseedor $d, objeto $o)`;
  `$o` distinto de `EspadaIndestructible`.
- **Consecuencias:** `modify(Posesion){ setPoseedor($p) }`; evento `OBTENER_OBJETO`.
- **Hechos:** `Personaje`, `Favor`, `Posesion`, `Objeto`.
- **Enunciado:** «Perseo obtiene Escudo de Bronce / Hoz de Acero / Casco de Hades debido a tener el favor
  de Atenea / Hermes / Hades».
- **Justificacion:** una regla para los tres dioses: quien entrega que lo dice el OWL (tiene_objeto). La
  espada de Hefesto se excluye porque el enunciado la presenta como version alternativa (R07). Un hecho
  negativo («Atenea no tiene EscudoBronce») elimina la `Posesion` y la regla no puede dispararse (R32-R33).
- **Ejemplo:** *Perseo obtiene HozAcero debido a tener el favor de Hermes.*

### R05 · Hefesto convierte el escudo de bronce en escudo-espejo — `favores.drl` · E
- **Condiciones:** `Favor($p, Hefesto)`; `$p` posee `EscudoBronce`; nadie posee `EscudoEspejo`.
- **Consecuencias:** `delete(Posesion EscudoBronce)`; `insert(Posesion($p, EscudoEspejo))`; evento `TRANSFORMAR_OBJETO`.
- **Enunciado:** «Perseo obtiene Escudo-Espejo debido a tener el favor de Hefesto y el Escudo de Bronce».
- **Justificacion:** transformacion (el escudo de bronce deja de existir), opcion preferida en el catalogo.
- **Ejemplo:** *Perseo obtiene EscudoEspejo debido a tener el favor de Hefesto y el EscudoBronce.*

### R07 · Hefesto entrega la espada indestructible (variante) — `favores.drl` · V
- **Condiciones:** `VarianteMito(ESPADA_HEFESTO)`; `Favor($p, Hefesto)`; Hefesto posee `EspadaIndestructible`.
- **Consecuencias:** transfiere la espada; evento `OBTENER_OBJETO`.
- **Enunciado:** «en otras versiones es una espada indestructible que le da Hefesto».
- **Ejemplo:** prueba F: con `Hermes no tiene HozAcero` + variante, Perseo mata a Medusa con la espada.

### R08 · Hefesto entrega la brida dorada (variante Pegaso) — `favores.drl` · V
- **Condiciones:** `VarianteMito(PEGASO)`; `Favor($p, Hefesto)`; nadie posee `BridaDorada` (en el OWL no tiene dueno).
- **Consecuencias:** `insert(Posesion($p, BridaDorada))`; evento.
- **Enunciado:** «atrapar a Pegaso antes con unas bridas de oro que le entrega Hefestos».

### R09-R12 · Capacidad proporcionada por un objeto poseido — `capacidades.drl` · E (R09 T de modelado)
- **Condiciones:** `Posesion($p, $o)`; `$p` vivo, ni dios ni criatura; `ProporcionaCapacidad($o, $c)`.
- **Consecuencias:** `insertLogical(CapacidadDisponible.deObjeto($p, $c, $o))`; evento `OBTENER_CAPACIDAD`.
- **Hechos:** `Posesion`, `Personaje`, `ProporcionaCapacidad`, `CapacidadDisponible`.
- **Enunciado:** «Perseo obtiene Capacidad Espejo/Invisibilidad/Vuelo debido a tener Escudo-Espejo /
  Casco de Hades / Sandalias Aladas» y, en el escenario 2, por Anillo, Espejo de Mano y Conjuro de Vuelo.
- **Justificacion:** una sola regla cubre R09, R10 (conjuro), R11 (anillo) y R12 (espejo de mano).
  `insertLogical` hace que la capacidad desaparezca sola si se pierde el objeto o muere el personaje.
  «Capacidad Espejo» del enunciado = `Reflejo` del OWL. El conjuro es un `Objeto` (decision D6), asi que
  su origen queda como OBJETO (no hay en el OWL una clase Conjuro con la que distinguirlo).
- **Ejemplo:** *Perseo obtiene Capacidad Vuelo debido a tener ConjuroVuelo.*

### R20 · Capturar una criatura voladora con la brida dorada — `capacidades.drl` · V
- **Condiciones:** no `VarianteMito(PEGASO_CLASICO)`; heroe vivo y libre con `BridaDorada`; criatura viva y
  libre con `CapacidadDisponible` propia de `Vuelo` (Pegaso, dato anadido por `PreparadorEscenario`).
- **Consecuencias:** criatura presa; `insert(Cautiverio($h, $m))`; evento `APRESAR`.
- **Justificacion:** las condiciones exactas de captura son decision de diseno (catalogo).

### R20b · Vuelo por montar una criatura capturada — `capacidades.drl` · V
- **Condiciones:** `Cautiverio($h, $m)`; `$h` heroe vivo; `$m` viva con Vuelo propio.
- **Consecuencias:** `insertLogical(CapacidadDisponible.deMontura($h, Vuelo, $m))`; evento.
- **Ejemplo:** prueba E: *Perseo mata a Medusa ... y Vuelo (Pegaso).*

### R21 · Version clasica: Pegaso nace de Medusa — `capacidades.drl` · V/T
- **Condiciones:** `VarianteMito(PEGASO_CLASICO)`; Medusa muerta; no existe `Filiacion(Pegaso, Medusa)`.
- **Consecuencias:** `insert(Filiacion(Pegaso, Medusa))`; evento.
- **Justificacion:** junto al `not` de R20 evita usar a Pegaso para llegar a Medusa antes de que nazca.

### R13 · Localizar a las Grayas con el favor de Hermes y Atenea — `grayas_ninfas.drl` · E
- **Condiciones:** `$h` no dios, vivo y libre; `Favor($h, Hermes)` y `Favor($h, Atenea)`; Grayas vivas; no localizadas aun.
- **Consecuencias:** `insert(Localizacion($h, Grayas))`; evento `LOCALIZAR`.
- **Ejemplo:** *Perseo localiza a las Grayas debido a tener el favor de Hermes y Atenea.*

### R14-R15 · Robar el ojo o el diente de las Grayas gracias a la invisibilidad — `grayas_ninfas.drl` · E
- **Condiciones:** `$h` vivo y libre; `Localizacion($h, Grayas)`; aun no conoce a las Ninfas;
  `CapacidadDisponible($h, Invisibilidad)` de cualquier fuente; Grayas poseen `OjoGrayas` o `DienteGrayas`.
- **Consecuencias:** `modify(Posesion){ setPoseedor($h) }`; evento `ROBAR_OBJETO` con la fuente de la invisibilidad.
- **Justificacion:** generaliza R37 (casco o anillo). Basta con uno: tras el intercambio la guarda impide un segundo robo.
- **Ejemplo:** *Perseo obtiene (roba) OjoGrayas de las Grayas debido a tener Invisibilidad (AnilloInvisibilidad) y las Grayas localizadas.*

### R16-R17 · Intercambiar el ojo o el diente por la localizacion de las Ninfas — `grayas_ninfas.drl` · E
- **Condiciones:** `$h` vivo y libre; Grayas localizadas; aun no conoce a las Ninfas; `$h` posee el ojo o el diente.
- **Consecuencias:** devuelve el objeto a las Grayas (`modify`), `insert(Localizacion($h, Ninfas))`; eventos
  `INTERCAMBIAR_OBJETO` y `LOCALIZAR`. salience 10.
- **Ejemplo:** *Perseo localiza a las Ninfas debido a intercambio de OjoGrayas con las Grayas.*

### R18-R19 · Las Ninfas entregan sus objetos a quien las localiza — `grayas_ninfas.drl` · E
- **Condiciones:** `$h` vivo y libre; `Localizacion($h, Ninfas)`; `Posesion(poseedor Ninfas, $o)`.
- **Consecuencias:** transfiere `$o` (ZurronMagico, SandaliasAladas); evento `OBTENER_OBJETO`.
- **Justificacion:** si el escenario niega las sandalias (R34) no hay `Posesion` que transferir.
- **Ejemplo:** *Perseo obtiene ZurronMagico debido a las Ninfas localizadas.*

### R23-R24 · Matar a Medusa con arma, Reflejo, Invisibilidad y Vuelo — `medusa.drl` · E (+R39 T)
- **Condiciones:** `$h` heroe vivo y libre; Medusa viva; `$h` posee un `Objeto` de clase `Arma`;
  `CapacidadDisponible` de `Reflejo`, `Invisibilidad` y `Vuelo` (cualquier fuente).
- **Consecuencias:** Medusa muerta; `insert(Muerte($h, Medusa))`; evento `MATAR` con el arma y la fuente de cada capacidad. salience -10.
- **Enunciado:** «Perseo mata a Medusa debido a tener Hoz de Acero, Espejo (Escudo-Espejo), Invisibilidad (Casco de Hades) y Vuelo (Sandalias Aladas)».
- **Justificacion:** comprueba capacidades, no objetos concretos (R36-R39): sandalias, conjuro o Pegaso
  sirven igual. R22 (llegar hasta Medusa) queda implicita en la exigencia de Vuelo. La salience -10 es una
  **decision tecnica**: el encadenamiento hacia delante no planifica y, sin ella, una ejecucion podia
  atacar a Medusa con el zurron aun por recibir y morir (lo detecto una prueba). Ahora el combate solo se
  elige cuando ya no queda nada que obtener; una prueba lo verifica con 25 ordenes de insercion aleatorios.
- **Ejemplo:** *Perseo mata a Medusa debido a tener HozAcero, Espejo (EspejoMano), Invisibilidad (AnilloInvisibilidad) y Vuelo (ConjuroVuelo).*

### R25 · El vencedor toma la cabeza de su victima — `medusa.drl` · E
- **Condiciones:** `Muerte($h, $v)`; `$h` vivo; `$v` posee una `ParteAnatomica`.
- **Consecuencias:** transfiere la parte (CabezaMedusa); evento `OBTENER_OBJETO`.
- **Ejemplo:** *Perseo obtiene CabezaMedusa debido a matar a Medusa.*

### R26 · El zurron magico protege a quien lleva la cabeza de Medusa — `medusa.drl` · E
- **Condiciones:** `$h` vivo, ni dios ni criatura; posee una parte anatomica que proporciona `Petrificacion`; posee `ZurronMagico`.
- **Consecuencias:** evento explicativo `INFORMACION`; el estado ya es correcto (sigue vivo). salience 100.
- **Ejemplo:** *Perseo no muere al obtener CabezaMedusa por tener ZurronMagico.*

### R27 · Muere quien toma la cabeza de Medusa sin zurron magico — `medusa.drl` · E
- **Condiciones:** las de R26 pero `not Posesion($h, ZurronMagico)`.
- **Consecuencias:** `$h` muerto; evento `MORIR`. salience 100 (antes de que pueda usar la cabeza).
- **Justificacion:** «si no lo tuviera Perseo debe morir». La falta de zurron es ausencia en la memoria de
  trabajo del escenario (mundo cerrado del escenario), no una negacion OWL (R46). Las criaturas (Medusa) son
  inmunes. No hay hecho `Muerte` porque no hay asesino. No puede morir dos veces (exige vivo).
- **Ejemplo:** prueba C: *Perseo muere al obtener CabezaMedusa por no tener ZurronMagico.*

### R28 · Petrificar a la criatura que tiene preso a alguien — `rescate.drl` · E
- **Condiciones:** `$h` heroe vivo y libre con `CapacidadDisponible(Petrificacion)`; `Cautiverio($c, preso != $h)`; `$c` criatura viva.
- **Consecuencias:** `$c` muerta; `insert(Muerte($h, $c))`; evento `MATAR`. salience -10.
- **Ejemplo:** *Perseo mata a Ceto debido a tener CabezaMedusa.*

### R29 · Liberar al preso de la criatura muerta — `rescate.drl` · E
- **Condiciones:** `Muerte($h, $c)`; `$h` vivo; `Cautiverio($c, $p)`; `$p` viva y presa.
- **Consecuencias:** `delete(Cautiverio)`; `$p` libre; `insert(Liberacion($h, $p))`; evento `LIBERAR`.
- **Justificacion:** la liberacion elimina el cautiverio activo (no queda `esta_preso_por`).
- **Ejemplo:** *Perseo libera a Andromeda debido a matar a Ceto.*

### R30 · Mision cumplida — `rescate.drl` · T
- **Condiciones:** `Objetivo(LIBERAR, P, D)`; P vivo; D libre; `Liberacion(P, D)`.
- **Consecuencias:** evento `CUMPLIR_OBJETIVO` («Si, Perseo puede liberar a Andromeda»). El resultado lo
  comprueba `Objetivo.seCumpleEn` en Java.

### R31 · Mision no demostrada y obstaculos (4 reglas) — `rescate.drl` · T
- **Condiciones:** `Objetivo(LIBERAR)` sin la `Liberacion` pedida; y, segun la regla, protagonista muerto, sin
  `Vuelo`/`Invisibilidad`/`Reflejo` con Medusa viva, o sin ningun arma. salience -1000.
- **Consecuencias:** eventos `INFORMACION` («No, no se puede demostrar...», «Obstaculo: Perseo no tiene Capacidad Vuelo...»).
- **Justificacion:** con la prioridad minima solo se disparan cuando no queda otra accion; como no
  modifican nada, describen el estado final.

## 5. Reglas del catalogo que no son una regla DRL propia

| Id | Donde se implementa |
|---|---|
| R10, R11, R12 | R09-R12 (regla general por objeto). |
| R22 | Implicita en R23-R24 (el Vuelo permite llegar). |
| R32, R33, R34 | `PreparadorEscenario.noTiene(..)`: retira la `Posesion` por defecto. |
| R35 | `PreparadorEscenario.tiene(..)`: hechos de entrada del heroe. |
| R36, R37, R38 | Las reglas de accion piden `CapacidadDisponible`, nunca el objeto. |
| R39 | R23-R24 pide un `Objeto` de clase `Arma`. |
| R40 | Siempre `modify` de la unica `Posesion` del objeto (prueba de poseedor unico). |
| R41 | Guardas `not`/estado + refraccion; prueba de agenda vacia y limite de disparos. |
| R42 | Todos los actores exigen `estadoVital == VIVO`; prueba de que un muerto no actua. |
| R43 | Los actores exigen `estadoLibertad == LIBRE`. |
| R44 | Cada consecuente registra un `EventoMitologico` (actor, destinatario, objeto, capacidad, justificacion, regla). |
| R45 | Orden parcial por los antecedentes; las pruebas comprueban cada precedencia del enunciado. |
| R46 | Solo se niega lo que el escenario niega explicitamente (`noTiene`). |
| R47 | `PreparadorEscenario`: el escenario prevalece sobre el OWL. |
| R48 | Una `KieSession` y una copia de `EstadoAventura` por escenario (prueba G). |

## 6. Decisiones de diseno

1. **Estado previo al mito (D2).** El OWL tiene a Andromeda ya presa de Ceto (resultado del mito). Para que
   R01-R02 se deduzcan y no se informe de algo cargado como si se hubiera inferido, `PreparadorEscenario`
   parte de Andromeda libre y Ceto retenido por Poseidon («fue liberada por Poseidon»). El
   `Casiopea tiene_enojo Poseidon` del OWL se conserva como conocimiento por defecto.
2. **Objetos que faltan en el OWL (D5).** `AnilloInvisibilidad` (ArtefactoMagico) → Invisibilidad,
   `EspejoMano` (Proteccion) → Reflejo, `ConjuroVuelo` (ArtefactoMagico) → Vuelo, y Vuelo propio de
   Pegaso, se anaden como datos en `PreparadorEscenario.ampliarDominio`. El OWL no se modifica.
3. **Variantes** con el hecho `VarianteMito` en lugar de cambiar posesiones desde Java.
4. **Prioridad de combate** (-10) como estrategia, ver R23-R24.
5. **Sin causas numericas** en los eventos: la justificacion textual nombra los hechos habilitantes.
   Rellenar `causas` exigiria buscar eventos previos en el consecuente («programar» en la regla).

## 7. Cambios en las clases Java

| Clase | Cambio | Motivo |
|---|---|---|
| `hechos/VarianteMito` | **Nueva** | Hecho de control para las variantes (R07, R08, R20, R21). |
| `escenario/PreparadorEscenario` | **Nueva** | Carga del dominio y aplicacion de condiciones (R32-R35, R46-R48). |
| `escenario/EstadoAventura` | `+ retirarCautiverio(captor, preso)` | Quitar el cautiverio del OWL sin registrar una liberacion falsa. |
| `escenario/EscenarioMitologico` | `+ agregarVariante`, `getVariantes`; las variantes se insertan en Drools | Variantes por escenario. |
| `hechos/CapacidadDisponible` | `+ getNombreFuente()` | Texto de la fuente en las explicaciones sin `if` en el DRL. |
| `motor/MotorReglas` | `+ ejecutar(sesion, escenario, maxDisparos)` | Probar que las reglas terminan. |
| `Principal` | Ejecuta los escenarios 1 y 2 y muestra la respuesta | La prueba `ping` ya no esta en la KieBase. |
| `reglas/prueba-integracion.drl` | Movida a `src/test/resources/dsi/mitologia/pruebas/` | No cargarla con las reglas del mito. |

## 8. Pruebas

`ReglasPerseoAndromedaTest` (14 pruebas) y `PruebaIntegracionDroolsTest` (2):

| Prueba | Comprueba |
|---|---|
| Escenario 1 | CONSEGUIDO; 19 eventos del enunciado en orden parcial correcto; textos de Ceto y Medusa; estado final. |
| Escenario 2 | CONSEGUIDO con anillo, espejo de mano y conjuro; los hechos negativos impiden escudo, casco y sandalias. |
| Escenario 3 (C) | Sin zurron: Perseo muere una vez, Ceto vivo, Andromeda presa, ningun acto de Perseo despues de morir. |
| Escenario 4 (D) | Sin vuelo: Medusa viva, NO_CONSEGUIDO, diagnostico «no tiene Capacidad Vuelo». |
| Pegaso (E) y version clasica | Vuelo por montura; en la version clasica no se captura. |
| Espada (F) | Sustituye a la hoz solo con la variante. |
| Reinicio (G) | A, B, C, A seguidos: resultados y eventos de A identicos; estado nuevo limpio. |
| Control | Agenda vacia y < 1000 disparos; poseedor unico; nadie muere dos veces; capacidad retirada al perder el anillo; sin enojo no hay captura; 25 ordenes de insercion aleatorios. |
| Integracion | `ping -> pong` desde recursos de test; la KieBase real no contiene esa regla. |

Ejecucion (Java 8 obligatorio: con JDK 25 Drools 7/MVEL falla con `NoClassDefFoundError: java/lang/Compiler`):

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
mvn clean test
```

## 9. Pendiente para completar la fase 1.b

1. Lector de `escenarios/EscenarioF1-x.txt` que traduzca cada linea a `PreparadorEscenario`.
2. Escritor de `soluciones/RespuestaF1_x.txt` (ahora `Principal.respuesta` solo imprime).
3. Nombres legibles («Casco de Hades») para la narracion: el OWL no tiene `rdfs:label`.
4. Decidir en equipo el estado previo al mito (decision 1) y si se refleja en el OWL.
5. Revisar con el profesor las prioridades de combate y la exclusion de la espada en R03-R04-R06.
