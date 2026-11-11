package dsi.mitologia.modelo;

/**
 * Interfaz que define a cualquier individuo de la ontologia
 * con identificador y nombre: personajes, objetos y capacidades.
 */

public interface EntidadMitologica {

    /**
     * El nombre local del IRI en mitologia.owl.
     */
    String getId();

    /**
     * Nombre legible para la salida. Si no se indica, coincide con el id.
     */
    String getNombre();
}
