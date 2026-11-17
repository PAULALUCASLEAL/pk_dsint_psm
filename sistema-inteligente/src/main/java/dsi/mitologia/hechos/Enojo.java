package dsi.mitologia.hechos;

import java.util.Objects;

import dsi.mitologia.modelo.Personaje;

/**
 * Hecho: un personaje tiene el enojo de otro ("Casiopea tiene el enojo de Poseidon").
 */
public final class Enojo {

    /** Quien sufre el enojo (sujeto de tiene_enojo)*/
    private final Personaje personaje;
    /** Quien esta enojado (objeto de tiene_enojo) */
    private final Personaje dios;

    /**
     * Crea el hecho de enojo. Solo se rechazan nulos.
     */
    public Enojo(Personaje personaje, Personaje dios) {
        if (personaje == null || dios == null) throw new IllegalArgumentException("Enojo necesita personaje y dios");
        this.personaje = personaje;
        this.dios = dios;
    }

    public Personaje getPersonaje() {
        return personaje;
    }

    public Personaje getDios() {
        return dios;
    }

    /** Igualdad por contenido (personaje, dios). */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Enojo)) return false;
        Enojo otro = (Enojo) o;
        return personaje.equals(otro.personaje) && dios.equals(otro.dios);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personaje, dios);
    }

    /** Texto legible para trazas.*/
    @Override
    public String toString() {
        return "Enojo(" + personaje.getId() + " -> " + dios.getId() + ")";
    }
}
