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


PRUEBA: Segundo ejemplo de Perseo con medios alternativos

Objetivo:
Comprobar que el lector interpreta condiciones de posesion y
desposesion, y que las reglas utilizan capacidades obtenidas
mediante objetos alternativos.

Archivo de entrada:
escenarios/EscenarioF1-2.txt

Configuracion:
Atenea no tiene EscudoBronce.
Hades no tiene CascoHades.
Las Ninfas no tienen SandaliasAladas.
Perseo tiene AnilloInvisibilidad, EspejoMano y ConjuroVuelo.

Resultado esperado:
Perseo obtiene Invisibilidad mediante AnilloInvisibilidad,
Reflejo mediante EspejoMano y Vuelo mediante ConjuroVuelo.
Consigue el ZurronMagico, mata a Medusa, sobrevive al obtener
su cabeza, mata a Ceto y libera a Andromeda.

Resultado observado:
Escenario F1-2: Puede Perseo liberar a Andromeda?
Condiciones:
  Perseo tiene el favor de Atenea
  Perseo tiene el favor de Hermes
  Perseo tiene el favor de Hefesto
  Casiopea tiene el enojo de Poseidon
  Atenea no tiene EscudoBronce
  Hades no tiene CascoHades
  Ninfas no tiene SandaliasAladas
  Perseo tiene AnilloInvisibilidad
  Perseo tiene EspejoMano
  Perseo tiene ConjuroVuelo
Si, Perseo puede liberar a Andromeda debido a que:
  Perseo localiza a las Grayas debido a tener el favor de Hermes y Atenea.
  Ceto queda libre debido al enojo de Poseidon hacia Casiopea.
  Andromeda queda presa debido a estar Ceto libre.
  Perseo obtiene HozAcero debido a tener el favor de Hermes.
  Perseo obtiene Capacidad Vuelo debido a tener ConjuroVuelo.
  Perseo obtiene Capacidad Reflejo debido a tener EspejoMano.
  Perseo obtiene Capacidad Invisibilidad debido a tener AnilloInvisibilidad.
  Perseo obtiene (roba) OjoGrayas de las Grayas debido a tener Invisibilidad (AnilloInvisibilidad) y las Grayas localizadas.
  Perseo devuelve OjoGrayas a las Grayas a cambio de saber donde estan las Ninfas.
  Perseo localiza a las Ninfas debido a intercambio de OjoGrayas con las Grayas.
  Perseo obtiene ZurronMagico debido a las Ninfas localizadas.
  Perseo mata a Medusa debido a tener HozAcero, Espejo (EspejoMano), Invisibilidad (AnilloInvisibilidad) y Vuelo (ConjuroVuelo).
  Perseo obtiene CabezaMedusa debido a matar a Medusa.
  Perseo no muere al obtener CabezaMedusa por tener ZurronMagico.
  Perseo obtiene Capacidad Petrificacion debido a tener CabezaMedusa.
  Perseo mata a Ceto debido a tener CabezaMedusa.
  Perseo libera a Andromeda debido a matar a Ceto.

Evidencia:
soluciones/RespuestaF1_2.txt

Conclusion:
El resultado coincide con lo esperado.


PRUEBA: Pregunta de matar a Medusa

Objetivo:
Comprobar que el lector reconoce preguntas de matar y
que la respuesta utiliza el verbo correspondiente.

Entrada:
Se utiliza EscenarioF1-1.txt con sus condiciones originales,
cambiando temporalmente la pregunta por:
Puede Perseo matar a Medusa?

Resultado esperado:
El lector construye un objetivo de tipo MATAR.
Drools deduce la muerte de Medusa.
La respuesta indica que Perseo puede matar a Medusa.

Resultado observado:
Escenario F1-1: Puede Perseo matar a Medusa?
Condiciones:
  Perseo tiene el favor de Atenea
  Perseo tiene el favor de Hermes
  Perseo tiene el favor de Hades
  Perseo tiene el favor de Hefesto
  Casiopea tiene el enojo de Poseidon
Si, Perseo puede matar a Medusa debido a que:
  Perseo localiza a las Grayas debido a tener el favor de Hermes y Atenea.
  Ceto queda libre debido al enojo de Poseidon hacia Casiopea.
  Andromeda queda presa debido a estar Ceto libre.
  Perseo obtiene CascoHades debido a tener el favor de Hades.
  Perseo obtiene HozAcero debido a tener el favor de Hermes.
  Perseo obtiene EscudoBronce debido a tener el favor de Atenea.
  Perseo obtiene EscudoEspejo debido a tener el favor de Hefesto y el EscudoBronce.
  Perseo obtiene Capacidad Reflejo debido a tener EscudoEspejo.
  Perseo obtiene Capacidad Invisibilidad debido a tener CascoHades.
  Perseo obtiene (roba) OjoGrayas de las Grayas debido a tener Invisibilidad (CascoHades) y las Grayas localizadas.
  Perseo devuelve OjoGrayas a las Grayas a cambio de saber donde estan las Ninfas.
  Perseo localiza a las Ninfas debido a intercambio de OjoGrayas con las Grayas.
  Perseo obtiene SandaliasAladas debido a las Ninfas localizadas.
  Perseo obtiene ZurronMagico debido a las Ninfas localizadas.
  Perseo obtiene Capacidad Vuelo debido a tener SandaliasAladas.
  Perseo mata a Medusa debido a tener HozAcero, Espejo (EscudoEspejo), Invisibilidad (CascoHades) y Vuelo (SandaliasAladas).
  Perseo obtiene CabezaMedusa debido a matar a Medusa.
  Perseo no muere al obtener CabezaMedusa por tener ZurronMagico.
  Perseo obtiene Capacidad Petrificacion debido a tener CabezaMedusa.
  Perseo mata a Ceto debido a tener CabezaMedusa.
  Perseo libera a Andromeda debido a matar a Ceto.

Evidencia:
documentacion/prueba-matar-medusa.txt

Conclusion:
Funciona perfecto. Hemos ampliado el reconocimiento de preguntas y la presentacion del resultado sin modificar las reglas Drools.