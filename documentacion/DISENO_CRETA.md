# Diseño de la ampliacion de Creta

Propuesta de diseño, falta verificar y comprobar.

## Requisitos del enunciado

- Incorporar Minos, Teseo, Ariadna, Minotauro, Icaro y Dedalo.
- Incorporar hilo, espada, alas y cuerno.
- Incorporar al heroe invitado Heracles.
- Desarrollar la historia de Teseo y del escape de Dedalo e Icaro.
- Permitir conexiones con el mito de Perseo mediante capacidades.
- En F2-2: Minotauro muerto por Teseo, Icaro muerto y Dedalo fuera.
- En F2-4: Perseo utiliza alas creadas despues del encierro de Dedalo.

## Reutilizacion de las entidades

Los personajes nuevos seran individuos de las clases existentes.
No vamos a crear una clase Java por personaje.

## Clasificacion multiple de personajes

Heroe representa un papel narrativo, mientras que Mortal y
Semidios describen la naturaleza o ascendencia del personaje.
Estas categorias no se consideran mutuamente excluyentes.

Se mantiene Perseo como Heroe y Semidios. Heracles recibe
los mismos tipos. Teseo se representa como Heroe y Mortal,
adoptando una interpretacion humana para el alcance del proyecto.

El tipo Heroe permite reutilizar las reglas actuales de combate.
El segundo tipo aporta informacion descriptiva y no es una
condicion tecnica necesaria para combatir.

Ariadna se mantiene como Mortal. No se le asigna Heroe.

## Capacidad de vuelo comun

Las alas proporcionaran la capacidad Vuelo que ya existía.

Justificacion:
Las reglas de Medusa consultan capacidades, entonces las alas pueden sustituir a las sandalias sin duplicar el combate.

## Situacion en el laberinto

Vamos a representar como cosas distintas estar dentro del laberinto
y estar preso por un personaje.

Justificacion:
Estar libre no significa necesariamente estar fuera.
El escenario F2-2 exige comprobar que Dedalo sale del laberinto.

Como se ha hecho:
Primero se probo un booleano en_laberinto, pero se ha cambiado por algo
mas general: la clase Lugar (individuos LaberintoCreta y MundoExterior),
la propiedad de objeto esta_en_lugar y el hecho Java Ubicacion(personaje, lugar).
Moverse es hacer modify de la Ubicacion. Asi se pueden añadir mas lugares
sin tocar el modelo.

El Minotauro empieza en LaberintoCreta; Teseo, Ariadna, Dedalo e Icaro en MundoExterior.

## Estado de las alas

Vamos a representar explicitamente si las alas estan rotas.
Las alas rotas no deben permitir que se pueda volar.

Justificacion:
El enunciado permite recuperar y reparar las alas de Icaro.

Como se ha hecho:
Primero se probo un booleano roto, pero se ha cambiado por la propiedad
de datos estado_fisico con valores "intacto" o "roto" y el hecho Java
EstadoFisicoObjeto(objeto, condicion). Romperse o repararse es hacer modify
de la condicion.

Las dos alas empiezan "intacto". Pendiente: la regla de capacidades aun no
comprueba que las alas esten intactas.

## Relaciones humanas

Decision final: reutilizamos Favor y Enojo para cualquier personaje, no solo dioses.
Ariadna y Minos siguen siendo Mortal (Minos es hijo de Zeus, pero no es un dios).

Primero se probo otra opcion: propiedades nuevas ayuda_a y hostil_con, con sus hechos
Java AyudaHumana y HostilidadHumana. Se descarto porque duplicaba Favor y Enojo.

Justificacion:
- El enunciado pide no añadir practicamente nada a la ontologia. Asi no se añade
  ninguna propiedad: solo se amplia el rango de tiene_favor y tiene_enojo de Dios a Personaje.
- El enunciado dice que en los ficheros de escenario solo se ponen favores, enojos,
  desposesiones y apresamientos. Con Enojo se puede escribir "Casiopea tiene el enojo de Minos"
  y el lector ya lo entiende. Con un hecho aparte no habria forma de escribirlo.
- La regla R03 (quien da su favor entrega su objeto) ya sirve para Ariadna:
  con "Teseo tiene el favor de Ariadna", Ariadna le da el HiloAriadna sin regla nueva.

Como se ha hecho:
- OWL: rango de tiene_favor y tiene_enojo = Personaje. Se quitan ayuda_a y hostil_con.
- Java: se quita la comprobacion "tiene que ser un Dios" y las clases AyudaHumana y HostilidadHumana.
- Reglas: R03 se llama ahora "Quien da su favor entrega su objeto". R01 (un dios enojado suelta
  a la criatura) exige ahora que quien se enoja sea un dios, para que el enojo de Minos no suelte criaturas.
- El favor de Ariadna NO se pone en el OWL: se pone en el escenario ("Teseo tiene el favor de Ariadna").
  Si estuviera en el OWL, Ariadna daria el hilo a Teseo en todas las historias, tambien en las de Perseo,
  y nadie mas podria conseguirlo. El enunciado tambien dice que por defecto nadie tiene favores.

## Aparicion de las alas

Vamos a establecer que las alas existan en el catalogo de objetos, pero no tendran
poseedor inicial. Las reglas de fabricacion les daran poseedor.

Justificacion:
Conocer un objeto no significa que este disponible.
El escenario F2-4 exige que su fabricacion tenga una causa.

## Pruebas pendientes

- Carga de los nuevos individuos.
- Historia completa de Creta.
- Alas rotas sin vuelo.
- Reparacion de alas.
- Perseo utiliza alas como fuente de vuelo.
- Los escenarios originales de Perseo siguen funcionando.