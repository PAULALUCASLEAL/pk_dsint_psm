package dsi.mitologia.hechos;

import java.util.Objects;

import dsi.mitologia.modelo.Personaje;

/**
 * Hecho estatico de parentesco: "Perseo es_hijo_de Zeus". No cambia durante la aventura.
 *
 * <p>Relacion con la ontologia: corresponde al par de ObjectProperties inversas
 * {@code es_hijo_de} (hijo -> progenitor) / {@code tiene_hijo} (progenitor -> hijo).</p>
 *
 * <p>Papel en el mito: en el OWL, Perseo es_hijo_de Zeus y de Danae (lo que lo hace Semidios),
 * y Andromeda es_hijo_de Casiopea. Esta ultima filiacion es la que conecta el enojo de Poseidon
 * hacia Casiopea con el cautiverio de Andromeda por Ceto.</p>
 *
 * <p>Uso en reglas: {@code Filiacion($hija : hijo, progenitor == $madre)} junto con
 * {@code Enojo(personaje == $madre, dios.id == "Poseidon")}.</p>
 *
 * <p>Decision de diseno: hecho propio "desempaquetado" (notas K/L del profesor) para que el
 * parentesco sea un patron simple. Se guarda una sola direccion del par inverso (hijo,
 * progenitor): un unico hecho representa es_hijo_de y tiene_hijo; la regla elige que extremo
 * enlazar. Un hijo con dos progenitores (Perseo) da lugar a dos hechos.</p>
 *
 * <p>Inmutable: si deja de ser cierto se retira ({@code delete}) y no se modifica (en la practica
 * nunca se retira). Igualdad por valor (dos hechos con los mismos extremos son el mismo hecho).</p>
 */
public final class Filiacion {

    /** Hijo (sujeto de es_hijo_de; objeto de tiene_hijo). Ej.: Andromeda. */
    private final Personaje hijo;
    /** Progenitor (sujeto de tiene_hijo; objeto de es_hijo_de). Ej.: Casiopea. */
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

    /** Coherente con equals. */
    @Override
    public int hashCode() {
        return Objects.hash(hijo, progenitor);
    }

    /** Texto legible para trazas: hijo -> progenitor. */
    @Override
    public String toString() {
        return "Filiacion(" + hijo.getId() + " -> " + progenitor.getId() + ")";
    }
}
