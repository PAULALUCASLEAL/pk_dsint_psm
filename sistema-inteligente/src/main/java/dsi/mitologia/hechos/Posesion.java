package dsi.mitologia.hechos;

import dsi.mitologia.modelo.Objeto;
import dsi.mitologia.modelo.Personaje;

/**
 * Hecho dinamico: quien posee actualmente un objeto.
 */
public class Posesion {

    /** Poseedor actual (sujeto de tiene_objeto). Mutable: cambia en cada transferencia. Nunca null. */
    private Personaje poseedor;
    /** Objeto poseido. Fijo: identifica al hecho. */
    private final Objeto objeto;

    /**
     * Crea la posesion inicial (desde el OWL o las condiciones de entrada) o la de un objeto que
     * aparece en la aventura
     */
    public Posesion(Personaje poseedor, Objeto objeto) {
        if (objeto == null) throw new IllegalArgumentException("Posesion necesita objeto");
        setPoseedor(poseedor);
        this.objeto = objeto;
    }

    public Personaje getPoseedor() {
        return poseedor;
    }

    public void setPoseedor(Personaje poseedor) {
        if (poseedor == null) {
            throw new IllegalArgumentException("Para que nadie posea el objeto se retira la Posesion");
        }
        this.poseedor = poseedor;
    }

    /** Objeto poseido; no cambia durante la vida del hecho. */
    public Objeto getObjeto() {
        return objeto;
    }

    /** Texto legible para trazas*/
    @Override
    public String toString() {
        return "Posesion(" + poseedor.getId() + " tiene " + objeto.getId() + ")";
    }
}
