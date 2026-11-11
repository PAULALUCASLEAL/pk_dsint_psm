package dsi.mitologia.hechos;

import java.util.Objects;

import dsi.mitologia.modelo.Capacidad;
import dsi.mitologia.modelo.Objeto;

/**
 * Hecho de conocimiento estatico: un objeto proporciona una capacidad a quien lo posee.
 */
public final class ProporcionaCapacidad {

    /** Objeto que da la capacidad (sujeto de proporciona_capacidad). Ej.: CascoHades. */
    private final Objeto objeto;
    /** Capacidad proporcionada (objeto de proporciona_capacidad). Ej.: Invisibilidad. */
    private final Capacidad capacidad;

    /** Crea el hecho; se rechazan nulos porque la propiedad siempre une un objeto y una capacidad. */
    public ProporcionaCapacidad(Objeto objeto, Capacidad capacidad) {
        if (objeto == null || capacidad == null) {
            throw new IllegalArgumentException("ProporcionaCapacidad necesita objeto y capacidad");
        }
        this.objeto = objeto;
        this.capacidad = capacidad;
    }

    /** Objeto que proporciona la capacidad. */
    public Objeto getObjeto() {
        return objeto;
    }

    /** Capacidad proporcionada. */
    public Capacidad getCapacidad() {
        return capacidad;
    }

    /** Igualdad por contenido (objeto, capacidad). */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProporcionaCapacidad)) return false;
        ProporcionaCapacidad otro = (ProporcionaCapacidad) o;
        return objeto.equals(otro.objeto) && capacidad.equals(otro.capacidad);
    }

    /** Coherente con equals. */
    @Override
    public int hashCode() {
        return Objects.hash(objeto, capacidad);
    }

    /** Texto legible para trazas: objeto -> capacidad. */
    @Override
    public String toString() {
        return "ProporcionaCapacidad(" + objeto.getId() + " -> " + capacidad.getId() + ")";
    }
}
