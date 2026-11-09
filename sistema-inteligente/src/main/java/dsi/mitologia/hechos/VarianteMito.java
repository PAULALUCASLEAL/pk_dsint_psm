package dsi.mitologia.hechos;

/**
 * Hecho de control: una variante narrativa del mito que el escenario activa de forma explicita.
 *
 * <p>Relacion con la ontologia: no existe en {@code mitologia.owl}. Es una decision tecnica para poder
 * ejecutar las versiones alternativas que cita el enunciado (pag. 10-11) sin mezclarlas con la version
 * principal: la espada indestructible de Hefesto en lugar de la hoz de Hermes, volar montando a Pegaso
 * con las bridas de oro de Hefesto, o la version clasica en la que Pegaso nace de Medusa.</p>
 *
 * <p>Decision de diseno: un hecho por variante, con igualdad por nombre. Las reglas que solo valen en una
 * variante la exigen en su antecedente ({@code VarianteMito(nombre == VarianteMito.PEGASO)}) y las que la
 * excluyen usan {@code not}. Si el escenario no activa ninguna variante, se ejecuta la version del
 * enunciado. Alternativa descartada: anadir objetos o posesiones al OWL segun la variante, lo que obligaria
 * a Java a decidir que ocurre (nota O del enunciado).</p>
 */
public final class VarianteMito {

    /** Hefesto entrega la EspadaIndestructible (R07); sustituye a la HozAcero como arma (R24). */
    public static final String ESPADA_HEFESTO = "EspadaHefesto";
    /** Hefesto entrega la BridaDorada y el heroe puede capturar a Pegaso para volar (R08, R20). */
    public static final String PEGASO = "Pegaso";
    /** Version clasica: Pegaso nace de Medusa, asi que no puede servir para llegar hasta ella (R21). */
    public static final String PEGASO_CLASICO = "PegasoClasico";

    private final String nombre;

    public VarianteMito(String nombre) {
        if (nombre == null || nombre.isEmpty()) throw new IllegalArgumentException("La variante necesita nombre");
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof VarianteMito && nombre.equals(((VarianteMito) o).nombre));
    }

    @Override
    public int hashCode() {
        return nombre.hashCode();
    }

    @Override
    public String toString() {
        return "VarianteMito(" + nombre + ")";
    }
}
