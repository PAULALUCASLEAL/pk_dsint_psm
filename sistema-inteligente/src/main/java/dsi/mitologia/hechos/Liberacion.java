package dsi.mitologia.hechos;

import java.util.Objects;

import dsi.mitologia.modelo.Personaje;

/**
 * Hecho historico: un personaje ha liberado a otro de su cautiverio.
 *
 * <p>Relacion con la ontologia: corresponde al par de ObjectProperties inversas
 * {@code libera_a} (libertador -> liberado) / {@code es_liberado_por} (liberado -> libertador).
 * Va acompanado del cambio de la DatatypeProperty {@code estado_libertad} del liberado a libre y
 * de la retirada del hecho {@link Cautiverio} correspondiente (lo hace EstadoAventura).</p>
 *
 * <p>Papel en el mito: es el paso final y la respuesta a la pregunta
 * "Puede Perseo liberar a Andromeda?": tras petrificar a Ceto con la CabezaMedusa, se inserta
 * Liberacion(Perseo, Andromeda). Si al terminar el encadenamiento existe este hecho, la respuesta
 * es afirmativa.</p>
 *
 * <p>Uso en reglas: objetivo de la consulta final,
 * {@code Liberacion(libertador.id == "Perseo", liberado.id == "Andromeda")}.</p>
 *
 * <p>Decision de diseno: se conserva como hecho propio aunque el Cautiverio ya se haya retirado,
 * porque la historia y la respuesta necesitan saber quien libero a quien; el estado_libertad por
 * si solo no lo dice. Se guarda una sola direccion del par inverso (libertador, liberado).</p>
 *
 * <p>Inmutable: si deja de ser cierto se retira ({@code delete}) y no se modifica. Igualdad por
 * valor (dos hechos con los mismos extremos son el mismo hecho).</p>
 */
public final class Liberacion {

    /** Quien libera (sujeto de libera_a). Ej.: Perseo. */
    private final Personaje libertador;
    /** Quien es liberado (sujeto de es_liberado_por). Ej.: Andromeda. */
    private final Personaje liberado;

    /** Crea el hecho de liberacion; se rechazan extremos nulos. */
    public Liberacion(Personaje libertador, Personaje liberado) {
        if (libertador == null || liberado == null) throw new IllegalArgumentException("Liberacion necesita libertador y liberado");
        this.libertador = libertador;
        this.liberado = liberado;
    }

    /** Libertador (libera_a). */
    public Personaje getLibertador() {
        return libertador;
    }

    /** Liberado (es_liberado_por). */
    public Personaje getLiberado() {
        return liberado;
    }

    /** Igualdad por contenido (libertador, liberado). */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Liberacion)) return false;
        Liberacion otro = (Liberacion) o;
        return libertador.equals(otro.libertador) && liberado.equals(otro.liberado);
    }

    /** Coherente con equals. */
    @Override
    public int hashCode() {
        return Objects.hash(libertador, liberado);
    }

    /** Texto legible para trazas: libertador -> liberado. */
    @Override
    public String toString() {
        return "Liberacion(" + libertador.getId() + " -> " + liberado.getId() + ")";
    }
}
