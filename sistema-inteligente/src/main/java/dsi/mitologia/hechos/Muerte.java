package dsi.mitologia.hechos;

import java.util.Objects;

import dsi.mitologia.modelo.Personaje;

/**
 * Hecho historico: un personaje ha matado a otro. Se conserva tras la muerte (es historia).
 */
public final class Muerte {

    /** Quien mata (sujeto de mata_a) */
    private final Personaje asesino;
    /** Quien muere (sujeto de es_asesinado_por) */
    private final Personaje victima;

    /** Crea el hecho de muerte; se rechazan extremos nulos (las muertes sin autor no son hecho). */
    public Muerte(Personaje asesino, Personaje victima) {
        if (asesino == null || victima == null) throw new IllegalArgumentException("Muerte necesita asesino y victima");
        this.asesino = asesino;
        this.victima = victima;
    }

    /** Asesino (mata_a). */
    public Personaje getAsesino() {
        return asesino;
    }

    /** Victima (es_asesinado_por). */
    public Personaje getVictima() {
        return victima;
    }

    /** Igualdad por contenido (asesino, victima). */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Muerte)) return false;
        Muerte otro = (Muerte) o;
        return asesino.equals(otro.asesino) && victima.equals(otro.victima);
    }

    @Override
    public int hashCode() {
        return Objects.hash(asesino, victima);
    }

    /** Texto legible para trazas */
    @Override
    public String toString() {
        return "Muerte(" + asesino.getId() + " -> " + victima.getId() + ")";
    }
}
