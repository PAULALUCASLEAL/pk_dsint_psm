package dsi.mitologia.modelo.enums;

/**
 * Subclases de {@code Objeto} declaradas en mitologia.owl.
 * Tampoco son disjuntas en la ontologia, asi que un objeto puede tener varias.
 *
 * <p>Relacion con la ontologia: cada constante corresponde a una clase OWL hija de {@code Objeto}
 * ({@code Arma}, {@code Proteccion}, {@code ArtefactoMagico}, {@code ParteAnatomica}); el campo
 * {@code claseOwl} guarda su nombre local exacto.</p>
 *
 * <p>Papel en el mito: permite reglas genericas por categoria. Matar a Medusa exige "un arma"
 * (HozAcero o cualquier otra), el EscudoEspejo se fabrica a partir de "una proteccion" (EscudoBronce),
 * y las partes anatomicas (OjoGrayas, CabezaMedusa) son las que se roban, intercambian o cortan.</p>
 *
 * <p>Decision de diseno: enum y no clase con instancias, porque las subclases forman parte del esquema
 * de la ontologia (fase 1.a) y no de los datos de cada escenario; ademas, en Drools un enum se compara
 * con {@code ==} y permite getters booleanos en {@code Objeto}. Alternativa descartada: subclases Java
 * de {@code Objeto}, que impedirian que un objeto tuviera varias categorias.</p>
 */
public enum CategoriaObjeto {
    /** Clase OWL {@code Arma} (p. ej. HozAcero). */
    ARMA("Arma"),
    /** Clase OWL {@code Proteccion} (p. ej. EscudoBronce, EscudoEspejo, CascoHades). */
    PROTECCION("Proteccion"),
    /** Clase OWL {@code ArtefactoMagico} (p. ej. SandaliasAladas, ZurronMagico). */
    ARTEFACTO_MAGICO("ArtefactoMagico"),
    /** Clase OWL {@code ParteAnatomica} (p. ej. OjoGrayas, CabezaMedusa). */
    PARTE_ANATOMICA("ParteAnatomica");

    /** Nombre local de la clase OWL equivalente, tal como aparece en el IRI de mitologia.owl. */
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
     *
     * <p>Se usa al leer los tipos de un individuo: devolver {@code null} (en vez de lanzar excepcion)
     * permite ignorar sin error otras clases que no son categorias, como el propio {@code Objeto}.</p>
     */
    public static CategoriaObjeto desdeClaseOwl(String nombre) {
        // Busqueda lineal: solo hay cuatro constantes, no compensa un mapa.
        for (CategoriaObjeto c : values()) {
            if (c.claseOwl.equals(nombre)) return c;
        }
        return null;
    }
}
