package dsi.mitologia.hechos;

import java.util.Objects;

import dsi.mitologia.modelo.Personaje;

/**
 * Hecho estatico de parentesco: "Perseo es_hijo_de Zeus".

 */
public final class Filiacion {

    /** Hijo (sujeto de es_hijo_de; objeto de tiene_hijo).*/
    private final Personaje hijo;
    /** Progenitor (sujeto de tiene_hijo; objeto de es_hijo_de).*/
    private final Personaje progenitor;

    /** Crea el hecho de filiacion; se rechazan extremos nulos. */
    public Filiacion(Personaje hijo, Personaje progenitor) {
        if (hijo == null || progenitor == null) throw new IllegalArgumentException("Filiacion necesita hijo y progenitor");
        this.hijo = hijo;
        this.progenitor = progenitor;
    }

    /** Hijo (es_hijo_de). */
    public Personaje getHijo() {
        return hijo;
    }

    /** Progenitor (tiene_hijo). */
    public Personaje getProgenitor() {
        return progenitor;
    }

    /** Igualdad por contenido (hijo, progenitor). */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Filiacion)) return false;
        Filiacion otro = (Filiacion) o;
        return hijo.equals(otro.hijo) && progenitor.equals(otro.progenitor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hijo, progenitor);
    }

    /** Texto legible para trazas*/
    @Override
    public String toString() {
        return "Filiacion(" + hijo.getId() + " -> " + progenitor.getId() + ")";
    }
}
