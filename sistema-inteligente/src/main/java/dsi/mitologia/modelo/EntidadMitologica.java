package dsi.mitologia.modelo;

/**
 * Cualquier individuo de la ontologia con identificador y nombre: personajes, objetos y capacidades.
 *
 * <p>Relacion con la ontologia: no corresponde a ninguna clase OWL concreta. Es el supertipo comun de
 * las clases raiz {@code Personaje}, {@code Objeto} y {@code Capacidad} (disjuntas entre si en
 * mitologia.owl). Equivale a "cualquier individuo con nombre" ({@code owl:NamedIndividual}).</p>
 *
 * <p>Por que existe: hay hechos cuyo destino puede ser de mas de una clase. Por ejemplo
 * {@code localiza_a} tiene rango Personaje U Objeto (Perseo localiza a las Grayas, que son un
 * personaje, pero tambien podria localizar un objeto), y la fuente de una capacidad puede ser un
 * {@code Objeto} (CascoHades da Invisibilidad) o un {@code Personaje} usado como montura (Pegaso da
 * Vuelo). Con esta interfaz esos hechos guardan un unico campo tipado en vez de dos campos opcionales.</p>
 *
 * <p>Alternativa descartada: usar {@code Object} como tipo del destino. Se pierde la garantia de que
 * siempre hay un id y un nombre que mostrar en la historia generada.</p>
 */
public interface EntidadMitologica {

    /**
     * Identificador estable: el nombre local del IRI en mitologia.owl (p. ej. "CascoHades").
     *
     * <p>Decision de diseno: usar el nombre local del IRI permite cruzar sin ambiguedad los datos
     * del fichero de escenario, la ontologia y los hechos de Drools. Es la clave de
     * {@code equals}/{@code hashCode} en todas las implementaciones.</p>
     */
    String getId();

    /**
     * Nombre legible para la salida (p. ej. "Casco de Hades"). Si no se indica, coincide con el id.
     * Solo se usa para presentar resultados; las reglas comparan por identidad o por id, nunca por nombre.
     */
    String getNombre();
}
