package dsi.mitologia.hechos;

import java.util.Objects;

import dsi.mitologia.modelo.Personaje;

/**
 * Hecho: un personaje tiene el enojo de un dios ("Casiopea tiene el enojo de Poseidon").
 *
 * <p>Relacion con la ontologia: corresponde a la ObjectProperty {@code tiene_enojo} (dominio
 * Personaje, rango Dios), con la restriccion allValuesFrom Dios sobre Personaje. No tiene inversa
 * en el OWL. La restriccion de rango no se comprueba aqui sino en
 * {@code EstadoAventura.agregarEnojo}, que exige que el segundo extremo sea un Dios.</p>
 *
 * <p>Papel en el mito: es el detonante de la historia. En el OWL, Casiopea tiene_enojo Poseidon;
 * por ello Poseidon suelta a Ceto, que apresa a Andromeda (hija de Casiopea). Si la condicion de
 * entrada no incluye este enojo, Andromeda no llega a estar presa y no hay nada que liberar.</p>
 *
 * <p>Uso en reglas: {@code Enojo($m : personaje, dios.id == "Poseidon")} junto con
 * {@code Filiacion(progenitor == $m, $hija : hijo)} para deducir el Cautiverio de la hija.</p>
 *
 * <p>Decision de diseno: hecho propio "desempaquetado" (notas K/L del profesor) en lugar de una
 * lista de enojos dentro de Personaje, para que el antecedente sea un patron simple y para que
 * las condiciones del fichero de entrada se traduzcan directamente a insert de este hecho.</p>
 *
 * <p>Inmutable: si deja de ser cierto se retira ({@code delete}) y no se modifica. Igualdad por
 * valor (dos hechos con los mismos extremos son el mismo hecho), asi no se duplican enojos.</p>
 */
public final class Enojo {

    /** Quien sufre el enojo (sujeto de tiene_enojo). Ej.: Casiopea. */
    private final Personaje personaje;
    /** Dios enojado (objeto de tiene_enojo; debe ser Dios por allValuesFrom). Ej.: Poseidon. */
    private final Personaje dios;

    /**
     * Crea el hecho de enojo. Solo se rechazan nulos; el tipo Dios lo valida EstadoAventura para
     * mantener este hecho como simple portador de datos.
     */
    public Enojo(Personaje personaje, Personaje dios) {
        if (personaje == null || dios == null) throw new IllegalArgumentException("Enojo necesita personaje y dios");
        this.personaje = personaje;
        this.dios = dios;
    }

    /** Personaje que sufre el enojo. */
    public Personaje getPersonaje() {
        return personaje;
    }

    /** Dios enojado. */
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

    /** Coherente con equals. */
    @Override
    public int hashCode() {
        return Objects.hash(personaje, dios);
    }

    /** Texto legible para trazas: personaje -> dios. */
    @Override
    public String toString() {
        return "Enojo(" + personaje.getId() + " -> " + dios.getId() + ")";
    }
}
