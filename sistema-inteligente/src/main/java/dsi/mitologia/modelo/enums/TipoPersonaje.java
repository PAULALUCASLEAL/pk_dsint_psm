package dsi.mitologia.modelo.enums;

/**
 * Subclases de {@code Personaje} declaradas en mitologia.owl.
 *
 * No son disjuntas en la ontologia: un personaje puede tener varios tipos a la vez
 * (Perseo es Heroe y Semidios). Por eso {@code Personaje} guarda un conjunto de tipos
 * en lugar de usar una jerarquia de clases Java.
 *
 * <p>Relacion con la ontologia: cada constante corresponde a una clase OWL hija de {@code Personaje}
 * ({@code Dios}, {@code Semidios}, {@code Heroe}, {@code Mortal}, {@code Criatura}); {@code claseOwl}
 * guarda su nombre local exacto. Las restricciones allValuesFrom {@code Dios} de {@code tiene_favor} y
 * {@code tiene_enojo} se comprueban en las reglas con {@code Personaje(dios)}.</p>
 *
 * <p>Papel en el mito: Atenea, Hermes, Hefesto, Hades y Poseidon (DIOS) conceden favores o enojos;
 * Perseo (HEROE y SEMIDIOS) protagoniza la mision; Andromeda y Casiopea (MORTAL) sufren el castigo;
 * Medusa, Ceto y Pegaso (CRIATURA) son obstaculos o montura.</p>
 *
 * <p>Decision de diseno: se guarda en un {@code EnumSet} dentro de {@code Personaje}. Un enum es
 * compacto, se compara con {@code ==} en Drools y refleja que las subclases son parte fija del esquema.
 * Alternativa descartada: subclases Java, por la falta de herencia multiple.</p>
 */
public enum TipoPersonaje {
    /** Clase OWL {@code Dios} (p. ej. Atenea, Hermes, Hefesto, Hades, Poseidon, Zeus). */
    DIOS("Dios"),
    /** Clase OWL {@code Semidios} (p. ej. Perseo). */
    SEMIDIOS("Semidios"),
    /** Clase OWL {@code Heroe} (p. ej. Perseo). */
    HEROE("Heroe"),
    /** Clase OWL {@code Mortal} (p. ej. Andromeda, Casiopea). */
    MORTAL("Mortal"),
    /** Clase OWL {@code Criatura} (p. ej. Medusa, Ceto, Pegaso). */
    CRIATURA("Criatura");

    /** Nombre local de la clase OWL equivalente, tal como aparece en el IRI de mitologia.owl. */
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
     *
     * <p>Se devuelve {@code null} (y no excepcion) para poder recorrer todas las clases de un individuo
     * e ignorar las que no son subclases de Personaje (p. ej. el propio {@code Personaje}).</p>
     */
    public static TipoPersonaje desdeClaseOwl(String nombre) {
        // Busqueda lineal: solo hay cinco constantes, no compensa un mapa.
        for (TipoPersonaje t : values()) {
            if (t.claseOwl.equals(nombre)) return t;
        }
        return null;
    }
}
