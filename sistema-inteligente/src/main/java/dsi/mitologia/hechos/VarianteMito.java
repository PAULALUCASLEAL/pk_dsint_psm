package dsi.mitologia.hechos;

/**
 * Hecho de control: una variante narrativa del mito que el escenario activa de forma explicita.
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
