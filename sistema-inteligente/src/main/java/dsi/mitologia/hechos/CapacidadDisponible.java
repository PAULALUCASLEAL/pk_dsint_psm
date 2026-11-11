package dsi.mitologia.hechos;

import java.util.Objects;

import dsi.mitologia.modelo.Capacidad;
import dsi.mitologia.modelo.EntidadMitologica;
import dsi.mitologia.modelo.Objeto;
import dsi.mitologia.modelo.Personaje;
import dsi.mitologia.modelo.enums.OrigenCapacidad;

/**
 * Hecho: un personaje dispone de una capacidad gracias a una fuente concreta.
 * Corresponde a la ObjectProperty {@code tiene_capacidad}
 */
public final class CapacidadDisponible {

    private final Personaje personaje; // No puede ser null
    private final Capacidad capacidad;
    /** Tipo de fuente: PROPIA, OBJETO, CONJURO o MONTURA. Extension del modelo, no esta en el OWL. */
    private final OrigenCapacidad origen;
    private final EntidadMitologica fuente; // si viene de un objeto o personaje, NULL si es PROPIA

    /**
     * Constructor privado: solo se crean instancias mediante los metodos de fabrica, que fijan el
     * origen y el tipo de la fuente de forma coherente (no se puede crear una montura que sea un Objeto).
     */
    private CapacidadDisponible(Personaje personaje, Capacidad capacidad,
                                OrigenCapacidad origen, EntidadMitologica fuente) {
        // Sin personaje, capacidad u origen el hecho no tiene sentido en la ontologia.
        if (personaje == null || capacidad == null || origen == null) {
            throw new IllegalArgumentException("CapacidadDisponible necesita personaje, capacidad y origen");
        }
        // Coherencia origen/fuente: PROPIA exige fuente nula y cualquier otro origen exige fuente.
        if ((origen == OrigenCapacidad.PROPIA) != (fuente == null)) {
            throw new IllegalArgumentException("Solo las capacidades propias carecen de fuente");
        }
        this.personaje = personaje;
        this.capacidad = capacidad;
        this.origen = origen;
        this.fuente = fuente;
    }

    /**
     * Capacidad propia del personaje. Existe para los casos en que tiene_capacidad se afirma directamente.
     */
    public static CapacidadDisponible propia(Personaje personaje, Capacidad capacidad) {
        return new CapacidadDisponible(personaje, capacidad, OrigenCapacidad.PROPIA, null);
    }

    /**
     * Capacidad dada por un objeto poseido, segun proporciona_capacidad
     */
    public static CapacidadDisponible deObjeto(Personaje personaje, Capacidad capacidad, Objeto objeto) {
        return new CapacidadDisponible(personaje, capacidad, OrigenCapacidad.OBJETO, objeto);
    }

    /**
     * Capacidad dada por un conjuro poseido.
     */
    public static CapacidadDisponible deConjuro(Personaje personaje, Capacidad capacidad, Objeto conjuro) {
        return new CapacidadDisponible(personaje, capacidad, OrigenCapacidad.CONJURO, conjuro);
    }

    /**
     * Capacidad dada por una criatura capturada que hace de montura.
     */
    public static CapacidadDisponible deMontura(Personaje personaje, Capacidad capacidad, Personaje montura) {
        return new CapacidadDisponible(personaje, capacidad, OrigenCapacidad.MONTURA, montura);
    }

    /** Personaje que dispone de la capacidad. */
    public Personaje getPersonaje() {
        return personaje;
    }

    /** Capacidad disponible. */
    public Capacidad getCapacidad() {
        return capacidad;
    }

    /** Tipo de fuente de la capacidad; util en reglas de explicacion de la historia. */
    public OrigenCapacidad getOrigen() {
        return origen;
    }

    /** Objeto, conjuro o montura que da la capacidad; {@code null} si es propia. */
    public EntidadMitologica getFuente() {
        return fuente;
    }

    /**
     * Nombre de la fuente para la explicacion ("CascoHades", "ConjuroVuelo", "Pegaso") o
     * "capacidad propia" si no tiene fuente. Lo usan los consecuentes al registrar eventos, para no
     * tener que comprobar en el DRL si la fuente es nula (las capacidades propias no la tienen).
     */
    public String getNombreFuente() {
        return fuente == null ? "capacidad propia" : fuente.getNombre();
    }

    /**
     * Igualdad por contenido: se comparan los cuatro campos. La fuente usa Objects.equals porque
     * puede ser null (capacidad propia).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CapacidadDisponible)) return false;
        CapacidadDisponible otro = (CapacidadDisponible) o;
        return personaje.equals(otro.personaje) && capacidad.equals(otro.capacidad)
                && origen == otro.origen && Objects.equals(fuente, otro.fuente);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personaje, capacidad, origen, fuente);
    }

    /** Texto legible para trazas */
    @Override
    public String toString() {
        return "CapacidadDisponible(" + personaje.getId() + " tiene " + capacidad.getId()
                + (fuente == null ? " propia" : " por " + fuente.getId()) + ")";
    }
}
