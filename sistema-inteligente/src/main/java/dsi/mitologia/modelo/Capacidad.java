package dsi.mitologia.modelo;

/**
 * Clase OWL {@code Capacidad}
 */

public final class Capacidad implements EntidadMitologica {

    private final String id;
    private final String nombre;

    /**
     * Crea una capacidad.
     *
     * @param id     nombre local del IRI en el OWL
     * @param nombre nombre para mostrar
     * @throws IllegalArgumentException si el id es nulo o vacio
     */
    public Capacidad(String id, String nombre) {
        // Sin id no se podria comparar ni cruzar con la ontologia.
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("La capacidad necesita id");
        this.id = id;
        // Valor por defecto: el propio id.
        this.nombre = nombre == null ? id : nombre;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    /**
     * Igualdad por id
     */
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Capacidad && id.equals(((Capacidad) o).id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /** Devuelve el nombre legible*/
    @Override
    public String toString() {
        return nombre;
    }
}
