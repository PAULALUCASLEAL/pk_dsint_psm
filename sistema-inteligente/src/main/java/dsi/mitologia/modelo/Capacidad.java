package dsi.mitologia.modelo;

/**
 * Clase OWL {@code Capacidad}. Sus individuos actuales son Vuelo, Invisibilidad, Reflejo y Petrificacion.
 *
 * <p>Relacion con la ontologia: cada instancia es un individuo de la clase {@code Capacidad}. Es el
 * rango de {@code tiene_capacidad} (Personaje -&gt; Capacidad) y de {@code proporciona_capacidad}
 * (Objeto -&gt; Capacidad). En el OWL {@code Capacidad} es disjunta con {@code Personaje} y
 * {@code Objeto}, por eso es una clase Java separada y no un tipo de objeto.</p>
 *
 * <p>Papel en el mito: las capacidades son el eje de las reglas genericas que pide el profesor. Las
 * reglas no preguntan "tiene Perseo el CascoHades", sino "tiene Perseo Invisibilidad". Asi, para robar
 * el OjoGrayas basta Invisibilidad (venga del CascoHades o de un Anillo de Invisibilidad), para matar a
 * Medusa se exige Reflejo, Invisibilidad y Vuelo (SandaliasAladas, Conjuro de Vuelo o Pegaso con la
 * BridaDorada), y la Petrificacion de la CabezaMedusa permite vencer a Ceto.</p>
 *
 * <p>Decision de diseno: es una clase con instancias (y no un enum) para poder incorporar capacidades
 * nuevas en la fase 2 (p. ej. la fuerza de Heracles) sin tocar el codigo; basta con declararlas en los
 * datos. Inmutable y compartida entre escenarios: una capacidad no cambia nunca, lo que cambia es quien
 * la tiene (hecho {@code CapacidadDisponible}). Independiente de los objetos: la misma capacidad puede
 * venir de fuentes distintas.</p>
 *
 * <p>Alternativa descartada: enum {@code Capacidad}. Mas comodo en los patrones, pero cerraria el
 * conjunto de capacidades en tiempo de compilacion.</p>
 */
public final class Capacidad implements EntidadMitologica {

    /** Nombre local del IRI del individuo en mitologia.owl (p. ej. "Invisibilidad"). Clave de igualdad. */
    private final String id;
    /** Nombre legible para la historia generada; por defecto igual al id. */
    private final String nombre;

    /**
     * Crea una capacidad.
     *
     * @param id     nombre local del IRI en el OWL; obligatorio porque es la clave de igualdad
     * @param nombre nombre para mostrar; si es {@code null} se usa el id
     * @throws IllegalArgumentException si el id es nulo o vacio
     */
    public Capacidad(String id, String nombre) {
        // Sin id no se podria comparar ni cruzar con la ontologia: se rechaza en la construccion.
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("La capacidad necesita id");
        this.id = id;
        // Valor por defecto: el propio id, para que la salida nunca muestre "null".
        this.nombre = nombre == null ? id : nombre;
    }

    /** {@inheritDoc} */
    @Override
    public String getId() {
        return id;
    }

    /** {@inheritDoc} */
    @Override
    public String getNombre() {
        return nombre;
    }

    /**
     * Igualdad por id, igual que en la ontologia (un individuo = un IRI). Permite que dos instancias
     * creadas por separado para "Vuelo" se consideren la misma capacidad en colecciones y en Drools.
     */
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Capacidad && id.equals(((Capacidad) o).id));
    }

    /** Coherente con {@link #equals(Object)}: depende solo del id, que es inmutable. */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /** Devuelve el nombre legible, pensado para escribir la historia de la respuesta. */
    @Override
    public String toString() {
        return nombre;
    }
}
