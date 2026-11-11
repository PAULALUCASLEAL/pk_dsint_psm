# Diseño de la ampliacion de Creta

Estado: propuesta de diseño, pendiente de implementar y verificar.

## Requisitos del enunciado

- Incorporar Minos, Teseo, Ariadna, Minotauro, Icaro y Dedalo.
- Incorporar hilo, espada, alas y cuerno.
- Incorporar al heroe invitado Heracles.
- Desarrollar la historia de Teseo y del escape de Dedalo e Icaro.
- Permitir conexiones con el mito de Perseo mediante capacidades.
- En F2-2: Minotauro muerto por Teseo, Icaro muerto y Dedalo fuera.
- En F2-4: Perseo utiliza alas creadas despues del encierro de Dedalo.

## D01. Reutilizacion de las entidades

Los personajes nuevos seran individuos de las clases existentes.
No vamos a crear una clase Java por personaje.

## D02. Capacidad de vuelo comun

Las alas proporcionaran la capacidad Vuelo, ya existente.

Justificacion:
Las reglas de Medusa consultan capacidades, entonces las alas pueden sustituir a las sandalias sin duplicar el combate.

## D03. Situacion en el laberinto

Se representara por separado estar dentro del laberinto
y estar preso por un personaje.

Justificacion:
Estar libre no significa necesariamente estar fuera.
El escenario F2-2 exige comprobar que Dedalo sale del laberinto.

## D04. Estado de las alas

Vamos a representar explicitamente si las alas estan rotas.
Las alas rotas no deben permitir que se pueda volar.

Justificacion:
El enunciado permite recuperar y reparar las alas de Icaro.

## D05. Relaciones humanas

Vamos a mantener los favores y enojos divinos existentes.
Añadiremos relaciones separadas para ayuda y hostilidad humanas.

Justificacion:
El modelo actual restringe Favor y Enojo a dioses.
Ariadna y Minos no deben clasificarse como dioses para evitarlo.
Esto tiene sentido porque Ariadna es la princesa que ayuda a Teseo, y Minos es el rey de Creta.
Aunque Minos es hijo de Zeus, eso no implica clasificarlo como Dios.
Asi podemos representalro como Mortal y mantener separada las relacuiones humanas de los favores y enojos divinos.

## D06. Aparicion de las alas

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