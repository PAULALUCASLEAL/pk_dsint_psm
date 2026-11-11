package dsi.mitologia.modelo.enums;

/**
 * Valores de la propiedad de datos {@code estado_vital} (rango oneOf {"vivo", "muerto"}, cardinalidad 1).
 */
public enum EstadoVital {
    VIVO("vivo"), // Estado por defecto de un personaje nuevo.
    MUERTO("muerto");

    /** Literal exacto del oneOf de la ontologia. */
    private final String valorOwl;

    /** Asocia la constante con su literal OWL. */
    EstadoVital(String valorOwl) {
        this.valorOwl = valorOwl;
    }

    /** Literal OWL ("vivo" / "muerto"), usado tambien al mostrar el estado. */
    public String getValorOwl() {
        return valorOwl;
    }

    /**
     * Convierte el literal del OWL; lanza excepcion si no esta en el oneOf.
     */
    public static EstadoVital desdeOwl(String valor) {
        for (EstadoVital e : values()) {
            if (e.valorOwl.equals(valor)) return e;
        }
        throw new IllegalArgumentException("estado_vital desconocido: " + valor);
    }
}
