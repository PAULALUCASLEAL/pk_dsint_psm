package dsi.mitologia.eventos;

/**
 * Acciones que puede narrar el sistema (verbos del enunciado: obtener, localizar, matar, liberar...).
 *
 * <p>Cada {@link EventoMitologico} lleva una de estas acciones y la salida la convierte en el verbo
 * de cada linea de la explicacion ("Perseo obtiene Escudo de Bronce debido a ...").</p>
 *
 * <p>Relacion con la ontologia: la mayoria de acciones narran el cambio de una propiedad OWL
 * (se indica en cada valor). Algunas no tienen propiedad propia porque son variantes de la misma
 * relacion (ENTREGAR, ROBAR, INTERCAMBIAR modifican {@code tiene_objeto}) o son de pura narracion
 * (CUMPLIR_OBJETIVO, INFORMACION).</p>
 *
 * <p>Decision de diseno: se distinguen ENTREGAR, ROBAR e INTERCAMBIAR aunque las tres cambien el
 * poseedor, porque la explicacion causal del mito los diferencia ("roba el Ojo de las Grayas", "lo
 * intercambia por la localizacion de las Ninfas") y el lector debe ver como se obtuvo cada objeto.</p>
 */
public enum TipoAccion {
    /** Alguien recibe un objeto (p. ej. de un dios por su favor): {@code tiene_objeto}. Ej.: HozAcero. */
    OBTENER_OBJETO,
    /** Un personaje da un objeto a otro: {@code tiene_objeto} cambia de sujeto. Ej.: las Ninfas a Perseo. */
    ENTREGAR_OBJETO,
    /** Se quita un objeto a su poseedor sin su voluntad: {@code tiene_objeto}. Ej.: el OjoGrayas. */
    ROBAR_OBJETO,
    /** Se cambia un objeto por otra cosa: {@code tiene_objeto}. Ej.: devolver el OjoGrayas a las Grayas. */
    INTERCAMBIAR_OBJETO,
    /** El poseedor deja de tener el objeto: se retira {@code tiene_objeto}. */
    PERDER_OBJETO,
    /** Un objeto se convierte en otro: {@code tiene_objeto}. Ej.: EscudoBronce pulido -> EscudoEspejo. */
    TRANSFORMAR_OBJETO,
    /** Se dispone de una capacidad: {@code tiene_capacidad}. Ej.: Reflejo, Invisibilidad, Vuelo. */
    OBTENER_CAPACIDAD,
    /** Se deja de disponer de una capacidad (se pierde su fuente): se retira {@code tiene_capacidad}. */
    PERDER_CAPACIDAD,
    /** Se encuentra a un personaje u objeto: {@code localiza_a}. Ej.: Perseo localiza a las Grayas. */
    LOCALIZAR,
    /** Un personaje mata a otro: {@code mata_a} y {@code estado_vital} = muerto. Ej.: Perseo y Medusa. */
    MATAR,
    /** Muerte sin autor: solo {@code estado_vital} = muerto (p. ej. tocar la cabeza de Medusa sin zurron). */
    MORIR,
    /** Un personaje apresa a otro: {@code apresa_a} y {@code estado_libertad} = preso. Ej.: Andromeda. */
    APRESAR,
    /** Un personaje libera a otro: {@code libera_a} y {@code estado_libertad} = libre. Ej.: Andromeda. */
    LIBERAR,
    /** Alguien queda libre sin libertador: solo {@code estado_libertad}. Ej.: Ceto por el enojo de Poseidon. */
    QUEDAR_LIBRE,
    /** Un dios concede su favor: {@code tiene_favor}. Ej.: Atenea a Perseo. */
    OTORGAR_FAVOR,
    /** Un dios se enoja con alguien: {@code tiene_enojo}. Ej.: Poseidon con Casiopea. */
    PROVOCAR_ENOJO,
    /** Cierre de la explicacion: se alcanza la mision preguntada (sin propiedad OWL propia). */
    CUMPLIR_OBJETIVO,
    /** Nota narrativa sin cambio de estado (p. ej. por que una regla no puede aplicarse). */
    INFORMACION
}
