package dsi.mitologia.hechos;

import java.util.Objects;

import dsi.mitologia.modelo.Personaje;

/**
 * Hecho historico: un personaje ha matado a otro. Se conserva tras la muerte (es historia).
 *
 * <p>Relacion con la ontologia: corresponde al par de ObjectProperties inversas
 * {@code mata_a} (asesino -> victima) / {@code es_asesinado_por} (victima -> asesino). Va
 * acompanado del cambio de la DatatypeProperty {@code estado_vital} (cardinalidad 1) de la
 * victima a muerto, que hace EstadoAventura.matar. Una muerte sin autor (p. ej. el heroe que toca
 * la CabezaMedusa sin ZurronMagico) cambia estado_vital pero no crea este hecho.</p>
 *
 * <p>Papel en el mito: Perseo mata a Medusa (con arma, Reflejo, Invisibilidad y Vuelo), y de ahi
 * obtiene la CabezaMedusa. Segun la variante, la derrota de Ceto se explica por petrificacion.</p>
 *
 * <p>Uso en reglas: {@code Muerte(asesino == $h, victima.id == "Medusa")} como antecedente de la
 * regla que entrega la CabezaMedusa a $h.</p>
 *
 * <p>Decision de diseno: hecho propio "desempaquetado" (notas K/L del profesor); estado_vital
 * solo dice que alguien esta muerto, este hecho dice quien lo mato, lo que hace falta para la
 * historia causal. Se guarda una sola direccion del par inverso (asesino, victima).</p>
 *
 * <p>Inmutable: si deja de ser cierto se retira ({@code delete}) y no se modifica. Igualdad por
 * valor (dos hechos con los mismos extremos son el mismo hecho).</p>
 */
public final class Muerte {

    /** Quien mata (sujeto de mata_a). Ej.: Perseo. */
    private final Personaje asesino;
    /** Quien muere (sujeto de es_asesinado_por). Ej.: Medusa. */
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

    /** Coherente con equals. */
    @Override
    public int hashCode() {
        return Objects.hash(asesino, victima);
    }

    /** Texto legible para trazas: asesino -> victima. */
    @Override
    public String toString() {
        return "Muerte(" + asesino.getId() + " -> " + victima.getId() + ")";
    }
}
