package dsi.mitologia.modelo.enums;

/**
 * Valores de la propiedad de datos {@code estado_libertad} (rango oneOf {"libre", "preso"}, cardinalidad 1).
 */
public enum EstadoLibertad {
    
	LIBRE("libre"),
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
     */
    public static EstadoLibertad desdeOwl(String valor) {
        for (EstadoLibertad e : values()) {
            if (e.valorOwl.equals(valor)) return e;
        }
        throw new IllegalArgumentException("estado_libertad desconocido: " + valor);
    }
}
