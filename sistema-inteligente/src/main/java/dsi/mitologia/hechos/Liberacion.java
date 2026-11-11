package dsi.mitologia.hechos;

import java.util.Objects;

import dsi.mitologia.modelo.Personaje;

/**
 * Hecho historico: un personaje ha liberado a otro de su cautiverio.
 */
public final class Liberacion {

    /** Quien libera (sujeto de libera_a)*/
    private final Personaje libertador;
    /** Quien es liberado (sujeto de es_liberado_por)*/
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
