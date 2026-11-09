package dsi.mitologia.hechos;

import java.util.Objects;

import dsi.mitologia.modelo.Personaje;

/**
 * Hecho dinamico: el captor tiene preso al personaje.
 *
 * <p>Relacion con la ontologia: corresponde al par de ObjectProperties inversas
 * {@code apresa_a} (captor -> preso) / {@code esta_preso_por} (preso -> captor). Se mantiene
 * sincronizado con la DatatypeProperty {@code estado_libertad} del preso (cardinalidad 1),
 * que en Java es {@code Personaje.estadoLibertad}: mientras exista el hecho, el preso esta PRESO.</p>
 *
 * <p>Papel en el mito: en el OWL, Ceto apresa_a Andromeda (y Andromeda esta_preso_por Ceto) como
 * consecuencia del enojo de Poseidon hacia Casiopea. El objetivo de la pregunta
 * "Puede Perseo liberar a Andromeda?" es precisamente romper este hecho. En el mito alternativo,
 * Perseo captura a Pegaso, y ese Cautiverio es el que habilita la capacidad de montura.</p>
 *
 * <p>Uso en reglas: {@code Cautiverio(captor == $c, preso.id == "Andromeda")} junto con
 * {@code Muerte(victima == $c)} o con la petrificacion del captor permite deducir la liberacion.</p>
 *
 * <p>Decision de diseno: la relacion es un hecho propio "desempaquetado" (notas K/L del profesor)
 * para que los antecedentes sean patrones simples sobre memoria de trabajo, sin navegar listas
 * dentro de Personaje. Se guarda una sola direccion del par inverso (captor, preso): un unico hecho
 * representa apresa_a y esta_preso_por; guardar ambas obligaria a mantenerlas sincronizadas y
 * duplicaria activaciones de reglas.</p>
 *
 * <p>Inmutable: si deja de ser cierto (el preso es liberado) se retira ({@code delete}) y no se
 * modifica. Igualdad por valor (dos hechos con los mismos extremos son el mismo hecho), lo que
 * permite comprobar existencia con {@code contains} y evita duplicados.</p>
 */
public final class Cautiverio {

    /** Quien mantiene preso (sujeto de apresa_a; objeto de esta_preso_por). Ej.: Ceto. */
    private final Personaje captor;
    /** Quien esta preso (sujeto de esta_preso_por; objeto de apresa_a). Ej.: Andromeda. */
    private final Personaje preso;

    /**
     * Crea el hecho de cautiverio. Se rechazan extremos nulos porque una ObjectProperty siempre
     * relaciona dos individuos. La coherencia con estado_libertad la garantiza EstadoAventura.
     */
    public Cautiverio(Personaje captor, Personaje preso) {
        if (captor == null || preso == null) throw new IllegalArgumentException("Cautiverio necesita captor y preso");
        this.captor = captor;
        this.preso = preso;
    }

    /** Captor (apresa_a). */
    public Personaje getCaptor() {
        return captor;
    }

    /** Preso (esta_preso_por). */
    public Personaje getPreso() {
        return preso;
    }

    /** Igualdad por contenido (captor, preso); Personaje compara por id. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cautiverio)) return false;
        Cautiverio otro = (Cautiverio) o;
        return captor.equals(otro.captor) && preso.equals(otro.preso);
    }

    /** Coherente con equals. */
    @Override
    public int hashCode() {
        return Objects.hash(captor, preso);
    }

    /** Texto legible para trazas: captor -> preso. */
    @Override
    public String toString() {
        return "Cautiverio(" + captor.getId() + " -> " + preso.getId() + ")";
    }
}
