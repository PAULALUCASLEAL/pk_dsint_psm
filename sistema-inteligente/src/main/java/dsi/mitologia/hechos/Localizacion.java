package dsi.mitologia.hechos;

import java.util.Objects;

import dsi.mitologia.modelo.EntidadMitologica;
import dsi.mitologia.modelo.Objeto;
import dsi.mitologia.modelo.Personaje;

/**
 * Hecho: un personaje ha localizado (descubierto donde esta) a otro personaje o a un objeto.
 */
public final class Localizacion {

    /** Quien localiza (sujeto de localiza_a)*/
    private final Personaje localizador;
    /** Lo localizado (objeto de localiza_a): un Personaje o un Objeto*/
    private final EntidadMitologica localizado;

    /** Localizar a un personaje */
    public Localizacion(Personaje localizador, Personaje localizado) {
        this(localizador, (EntidadMitologica) localizado);
    }

    /** Localizar un objeto */
    public Localizacion(Personaje localizador, Objeto localizado) {
        this(localizador, (EntidadMitologica) localizado);
    }

    private Localizacion(Personaje localizador, EntidadMitologica localizado) {
        if (localizador == null || localizado == null) throw new IllegalArgumentException("Localizacion necesita localizador y localizado");
        this.localizador = localizador;
        this.localizado = localizado;
    }

    /** Personaje que localiza. */
    public Personaje getLocalizador() {
        return localizador;
    }

    /** Personaje u objeto localizado; las reglas suelen filtrar por {@code localizado.id}. */
    public EntidadMitologica getLocalizado() {
        return localizado;
    }

    /** Igualdad por contenido (localizador, localizado). */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Localizacion)) return false;
        Localizacion otro = (Localizacion) o;
        return localizador.equals(otro.localizador) && localizado.equals(otro.localizado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localizador, localizado);
    }

    /** Texto legible para trazas*/
    @Override
    public String toString() {
        return "Localizacion(" + localizador.getId() + " -> " + localizado.getId() + ")";
    }
}
