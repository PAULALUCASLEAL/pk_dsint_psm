package dsi.mitologia.hechos;

import java.util.Objects;

import dsi.mitologia.modelo.EntidadMitologica;
import dsi.mitologia.modelo.Objeto;
import dsi.mitologia.modelo.Personaje;

/**
 * Hecho: un personaje ha localizado (descubierto donde esta) a otro personaje o a un objeto.
 *
 * <p>Relacion con la ontologia: corresponde a la ObjectProperty {@code localiza_a} (dominio
 * Personaje, rango la union Personaje U Objeto). No tiene inversa en el OWL.</p>
 *
 * <p>Papel en el mito: Perseo localiza a las Grayas gracias al favor de Hermes y Atenea; tras
 * robarles el OjoGrayas (con Invisibilidad) y devolverlo a cambio de informacion, localiza a las
 * Ninfas, que le entregan el ZurronMagico y las SandaliasAladas. Localizar es requisito previo
 * para interactuar con quien guarda un objeto.</p>
 *
 * <p>Uso en reglas: {@code Localizacion(localizador == $h, localizado.id == "Grayas")} como
 * antecedente de la regla de robo del ojo.</p>
 *
 * <p>Decision de diseno: hecho propio "desempaquetado" (notas K/L del profesor). El campo
 * localizado se tipa como EntidadMitologica para cubrir la union del rango, pero solo se exponen
 * constructores publicos para Personaje y para Objeto, de modo que no se pueda localizar una
 * Capacidad (que tambien es EntidadMitologica pero no pertenece al rango).</p>
 *
 * <p>Inmutable: si deja de ser cierto se retira ({@code delete}) y no se modifica. Igualdad por
 * valor (dos hechos con los mismos extremos son el mismo hecho).</p>
 */
public final class Localizacion {

    /** Quien localiza (sujeto de localiza_a). Ej.: Perseo. */
    private final Personaje localizador;
    /** Lo localizado (objeto de localiza_a): un Personaje o un Objeto. Ej.: Grayas, Ninfas. */
    private final EntidadMitologica localizado;

    /** Localizar a un personaje (p. ej. Perseo localiza a las Grayas). */
    public Localizacion(Personaje localizador, Personaje localizado) {
        this(localizador, (EntidadMitologica) localizado);
    }

    /** Localizar un objeto (p. ej. un objeto escondido que hay que encontrar antes de cogerlo). */
    public Localizacion(Personaje localizador, Objeto localizado) {
        this(localizador, (EntidadMitologica) localizado);
    }

    // Privado: el rango OWL es Personaje U Objeto, asi que no se admite una Capacidad.
    // Los dos constructores publicos delegan aqui; el cast a EntidadMitologica evita la ambiguedad.
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

    /** Coherente con equals. */
    @Override
    public int hashCode() {
        return Objects.hash(localizador, localizado);
    }

    /** Texto legible para trazas: localizador -> localizado. */
    @Override
    public String toString() {
        return "Localizacion(" + localizador.getId() + " -> " + localizado.getId() + ")";
    }
}
