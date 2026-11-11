package dsi.mitologia.hechos;

import java.util.Objects;

import dsi.mitologia.modelo.Personaje;

/**
 * Hecho: un personaje tiene el favor de un dios.
 */
public final class Favor {

    /** Quien recibe el favor (sujeto de tiene_favor)*/
    private final Personaje personaje;
    /** Dios que concede el favor */
    private final Personaje dios;

    /**
     * Crea el hecho de favor. Solo se rechazan nulos; el tipo Dios lo valida EstadoAventura para
     * mantener este hecho como simple portador de datos.
     */
    public Favor(Personaje personaje, Personaje dios) {
        if (personaje == null || dios == null) throw new IllegalArgumentException("Favor necesita personaje y dios");
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
        if (!(o instanceof Favor)) return false;
        Favor otro = (Favor) o;
        return personaje.equals(otro.personaje) && dios.equals(otro.dios);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personaje, dios);
    }

    /** Texto legible para trazas */
    @Override
    public String toString() {
        return "Favor(" + personaje.getId() + " -> " + dios.getId() + ")";
    }
}
