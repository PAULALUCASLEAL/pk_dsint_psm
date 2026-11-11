package dsi.mitologia.modelo.enums;

/**
 * Subclases de {@code Objeto} declaradas en mitologia.owl.
 * No son disjuntas en la ontologia, asi que un objeto puede tener varias.
 */
public enum CategoriaObjeto {
 
    ARMA("Arma"),
    PROTECCION("Proteccion"),
    ARTEFACTO_MAGICO("ArtefactoMagico"),
    PARTE_ANATOMICA("ParteAnatomica");

    /** Nombre local de la clase OWL equivalente */
    private final String claseOwl;

    /** Asocia la constante con el nombre de su clase OWL. */
    CategoriaObjeto(String claseOwl) {
        this.claseOwl = claseOwl;
    }

    /** Nombre local de la clase OWL equivalente (p. ej. "ArtefactoMagico"). */
    public String getClaseOwl() {
        return claseOwl;
    }

    /**
     * Devuelve la categoria cuya clase OWL se llama asi, o {@code null} si no es una subclase de Objeto.
     */
    public static CategoriaObjeto desdeClaseOwl(String nombre) {
        for (CategoriaObjeto c : values()) {
            if (c.claseOwl.equals(nombre)) return c;
        }
        return null;
    }
}
