PRUEBA: Perseo sin el favor de Hades

Objetivo:
Comprobar que el sistema no consigue el rescate cuando falta
una capacidad necesaria y no existe una alternativa.

Entrada:
EscenarioF2-1.txt, eliminando únicamente la condición
"Perseo tiene el favor de Hades".

Resultado esperado:
Perseo no recibe CascoHades y no obtiene Invisibilidad.
No puede robar a las Grayas ni completar el rescate de Andromeda.

Resultado observado:
Escenario F2-1: Puede Perseo liberar a Andromeda?
Condiciones:
  Perseo tiene el favor de Atenea
  Perseo tiene el favor de Hermes
  Perseo tiene el favor de Hefesto
  Casiopea tiene el enojo de Poseidon
No, no se puede demostrar que Perseo pueda liberar a Andromeda. Lo que ocurre es:
  Perseo localiza a las Grayas debido a tener el favor de Hermes y Atenea.
  Ceto queda libre debido al enojo de Poseidon hacia Casiopea.
  Andromeda queda presa debido a estar Ceto libre.
  Perseo obtiene HozAcero debido a tener el favor de Hermes.
  Perseo obtiene EscudoBronce debido a tener el favor de Atenea.
  Perseo obtiene EscudoEspejo debido a tener el favor de Hefesto y el EscudoBronce.
  Perseo obtiene Capacidad Reflejo debido a tener EscudoEspejo.
  Obstaculo: Perseo no tiene Capacidad Vuelo y no puede enfrentarse a Medusa.
  Obstaculo: Perseo no tiene Capacidad Invisibilidad y no puede enfrentarse a Medusa.

Conclusión:
Coincide con lo que esperabamos.