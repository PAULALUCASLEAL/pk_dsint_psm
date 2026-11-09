package dsi.mitologia.modelo.enums;

/**
 * Valores de la propiedad de datos {@code estado_vital} (rango oneOf {"vivo", "muerto"}, cardinalidad 1).
 *
 * <p>Relacion con la ontologia: DatatypeProperty {@code estado_vital} con dominio {@code Personaje},
 * rango enumerado {"vivo", "muerto"} y restriccion de cardinalidad exactamente 1 en la clase
 * {@code Personaje}. Se complementa con {@code mata_a} / {@code es_asesinado_por}, que dicen quien mata
 * a quien; este enum solo dice si el personaje vive.</p>
 *
 * <p>Papel en el mito: Medusa pasa a "muerto" cuando Perseo la mata, y Ceto cuando la CabezaMedusa lo
 * petrifica. Tambien sirve como precondicion: solo un personaje "vivo" puede actuar, y si Perseo
 * cortara la cabeza sin ZurronMagico pasaria a "muerto" y la historia no podria continuar.</p>
 *
 * <p>Decision de diseno: enum con un unico valor por personaje (cardinalidad 1): estar vivo y muerto a
 * la vez es irrepresentable. Alternativa descartada: un booleano {@code vivo}, que pierde el vinculo
 * con los literales OWL.</p>
 */
public enum EstadoVital {
    /** Literal OWL "vivo". Estado por defecto de un personaje nuevo. */
    VIVO("vivo"),
    /** Literal OWL "muerto". */
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
     *
     * <p>Un valor desconocido violaria el rango de la ontologia, por eso es un error de los datos de
     * entrada y no se devuelve {@code null}.</p>
     */
    public static EstadoVital desdeOwl(String valor) {
        for (EstadoVital e : values()) {
            if (e.valorOwl.equals(valor)) return e;
        }
        throw new IllegalArgumentException("estado_vital desconocido: " + valor);
    }
}
