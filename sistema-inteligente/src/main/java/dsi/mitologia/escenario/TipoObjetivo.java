package dsi.mitologia.escenario;

/**
 * Misiones que puede preguntar un escenario (enunciado, apartado 8.a, mas localizar).
 *
 * <p>El enunciado enumera cinco misiones: matar, apresar o liberar a un personaje, obtener un
 * objeto y conseguir una capacidad. Se anade LOCALIZAR porque en el mito es un paso intermedio
 * con entidad propia (Perseo localiza a las Grayas y a las Ninfas) y la ontologia tiene la
 * propiedad {@code localiza_a}; asi tambien se puede preguntar por el.</p>
 *
 * <p>Relacion con la ontologia: cada valor corresponde a una ObjectProperty cuyo hecho se busca
 * en el estado final (ver {@link Objetivo#seCumpleEn(EstadoAventura)}).</p>
 */
public enum TipoObjetivo {
    /** "Puede X matar a Y?": propiedad {@code mata_a} (hecho Muerte). Ej.: Perseo mata a Medusa. */
    MATAR,
    /** "Puede X apresar a Y?": propiedad {@code apresa_a} (hecho Cautiverio). Ej.: Perseo apresa a Pegaso. */
    APRESAR,
    /** "Puede X liberar a Y?": propiedad {@code libera_a} (hecho Liberacion). Ej.: Perseo libera a Andromeda. */
    LIBERAR,
    /** "Puede X obtener O?": propiedad {@code tiene_objeto} (hecho Posesion). Ej.: Perseo y CabezaMedusa. */
    OBTENER_OBJETO,
    /** "Puede X conseguir C?": propiedad {@code tiene_capacidad} (hecho CapacidadDisponible). Ej.: Vuelo. */
    OBTENER_CAPACIDAD,
    /** "Puede X localizar a Y?": propiedad {@code localiza_a} (hecho Localizacion). Ej.: Perseo y Grayas. */
    LOCALIZAR
}
