package dsi.mitologia.modelo.enums;

/**
 * Subclases de {@code Personaje} declaradas en mitologia.owl.
 */
public enum TipoPersonaje {
    /** Clase OWL {@code Dios}*/
    DIOS("Dios"),
    /** Clase OWL {@code Semidios}. */
    SEMIDIOS("Semidios"),
    /** Clase OWL {@code Heroe}*/
    HEROE("Heroe"),
    /** Clase OWL {@code Mortal}*/
    MORTAL("Mortal"),
    /** Clase OWL {@code Criatura}*/
    CRIATURA("Criatura");

    private final String claseOwl;

    /** Asocia la constante con el nombre de su clase OWL. */
    TipoPersonaje(String claseOwl) {
        this.claseOwl = claseOwl;
    }

    /** Nombre local de la clase OWL equivalente. */
    public String getClaseOwl() {
        return claseOwl;
    }

    /**
     * Devuelve el tipo cuya clase OWL se llama asi, o {@code null} si no es un tipo de personaje.
     */
    public static TipoPersonaje desdeClaseOwl(String nombre) {
        for (TipoPersonaje t : values()) {
            if (t.claseOwl.equals(nombre)) return t;
        }
        return null;
    }
}
