package dsi.mitologia.modelo.enums;

/**
 * Valores de la propiedad de datos {@code estado_libertad} (rango oneOf {"libre", "preso"}, cardinalidad 1).
 *
 * <p>Relacion con la ontologia: DatatypeProperty {@code estado_libertad} con dominio {@code Personaje},
 * rango enumerado {"libre", "preso"} y restriccion de cardinalidad exactamente 1. Se complementa con las
 * ObjectProperties {@code apresa_a} / {@code esta_preso_por} y {@code libera_a} / {@code es_liberado_por},
 * que dicen quien apresa o libera; este enum solo dice en que situacion esta el personaje.</p>
 *
 * <p>Papel en el mito: Ceto pasa a "libre" por el enojo de Poseidon hacia Casiopea; Andromeda pasa a
 * "preso" encadenada para Ceto; la pregunta final ("Puede Perseo liberar a Andromeda?") se responde
 * comprobando si Andromeda vuelve a "libre".</p>
 *
 * <p>Decision de diseno: enum con un unico valor por personaje (cardinalidad 1), de modo que estar libre
 * y preso a la vez es irrepresentable. Alternativa descartada: un booleano {@code preso}, que perderia
 * el vinculo directo con los literales del OWL y no admitiria ampliar el rango en la fase 2.</p>
 */
public enum EstadoLibertad {
    /** Literal OWL "libre". */
    LIBRE("libre"),
    /** Literal OWL "preso". */
    PRESO("preso");

    /** Literal exacto del oneOf de la ontologia. */
    private final String valorOwl;

    /** Asocia la constante con su literal OWL. */
    EstadoLibertad(String valorOwl) {
        this.valorOwl = valorOwl;
    }

    /** Literal OWL ("libre" / "preso"), usado tambien al mostrar el estado. */
    public String getValorOwl() {
        return valorOwl;
    }

    /**
     * Convierte el literal del OWL; lanza excepcion si no esta en el oneOf.
     *
     * <p>A diferencia de {@code TipoPersonaje.desdeClaseOwl}, aqui un valor desconocido es un error de los
     * datos de entrada (violaria el rango de la ontologia), por eso se lanza excepcion en vez de devolver
     * {@code null}.</p>
     */
    public static EstadoLibertad desdeOwl(String valor) {
        for (EstadoLibertad e : values()) {
            if (e.valorOwl.equals(valor)) return e;
        }
        throw new IllegalArgumentException("estado_libertad desconocido: " + valor);
    }
}
