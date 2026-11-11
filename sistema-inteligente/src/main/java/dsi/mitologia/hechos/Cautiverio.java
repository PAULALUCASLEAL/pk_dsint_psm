package dsi.mitologia.hechos;

import java.util.Objects;

import dsi.mitologia.modelo.Personaje;

/**
 * Hecho dinamico: el captor tiene preso al personaje.
 */
public final class Cautiverio {

    /** Quien mantiene preso (sujeto de apresa_a; objeto de esta_preso_por)*/
    private final Personaje captor;
    /** Quien esta preso (sujeto de esta_preso_por; objeto de apresa_a)*/
    private final Personaje preso;

    /**
     * Crea el hecho de cautiverio. Se rechazan extremos nulos porque una ObjectProperty siempre
     * relaciona dos individuos. La coherencia con estado_libertad la garantiza EstadoAventura.
     */
    public Cautiverio(Personaje captor, Personaje preso) {
        if (captor == null || preso == null) throw new IllegalArgumentException("Cautiverio necesita captor y preso");
        this.captor = captor;
        this.preso = preso;
    }

    /** Captor (apresa_a). */
    public Personaje getCaptor() {
        return captor;
    }

    /** Preso (esta_preso_por). */
    public Personaje getPreso() {
        return preso;
    }

    /** Igualdad por contenido (captor, preso); Personaje compara por id. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cautiverio)) return false;
        Cautiverio otro = (Cautiverio) o;
        return captor.equals(otro.captor) && preso.equals(otro.preso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(captor, preso);
    }

    /** Texto legible para trazas */
    @Override
    public String toString() {
        return "Cautiverio(" + captor.getId() + " -> " + preso.getId() + ")";
    }
}
