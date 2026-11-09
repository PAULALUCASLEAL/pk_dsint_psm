package dsi.mitologia.hechos;

import java.util.Objects;

import dsi.mitologia.modelo.Personaje;

/**
 * Hecho: un personaje tiene el favor de un dios ("Perseo tiene el favor de Atenea").
 *
 * <p>Relacion con la ontologia: corresponde a la ObjectProperty {@code tiene_favor} (dominio
 * Personaje, rango Dios), con la restriccion allValuesFrom Dios sobre Personaje. No tiene inversa
 * en el OWL. La restriccion de rango no se comprueba en este constructor sino en
 * {@code EstadoAventura.agregarFavor}, que rechaza un "dios" que no sea de tipo Dios.</p>
 *
 * <p>Papel en el mito: los favores son las condiciones de entrada que habilitan cada paso de
 * Perseo: favor de Atenea -> EscudoBronce; favor de Hermes -> HozAcero; favor de Hefesto (con el
 * escudo) -> EscudoEspejo; favor de Hades -> CascoHades; favor de Hermes y Atenea -> localizar a
 * las Grayas. Si el fichero de entrada no da un favor, la regla correspondiente no se dispara y
 * la historia se corta (o se busca un objeto alternativo).</p>
 *
 * <p>Uso en reglas: {@code Favor(personaje == $h, dios.id == "Atenea")} junto con
 * {@code Posesion(poseedor.id == "Atenea", $o : objeto)} para deducir la entrega del objeto.</p>
 *
 * <p>Decision de diseno: hecho propio "desempaquetado" (notas K/L del profesor) en lugar de una
 * lista de favores dentro de Personaje: el antecedente es un patron directo, sin navegar
 * referencias, y retirar un favor es un simple delete.</p>
 *
 * <p>Inmutable: si deja de ser cierto se retira ({@code delete}) y no se modifica. Igualdad por
 * valor (dos hechos con los mismos extremos son el mismo hecho), asi no se duplican favores.</p>
 */
public final class Favor {

    /** Quien recibe el favor (sujeto de tiene_favor). Ej.: Perseo. */
    private final Personaje personaje;
    /** Dios que concede el favor (objeto de tiene_favor; Dios por allValuesFrom). Ej.: Atenea. */
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

    /** Personaje favorecido. */
    public Personaje getPersonaje() {
        return personaje;
    }

    /** Dios que concede el favor. */
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

    /** Coherente con equals. */
    @Override
    public int hashCode() {
        return Objects.hash(personaje, dios);
    }

    /** Texto legible para trazas: personaje -> dios. */
    @Override
    public String toString() {
        return "Favor(" + personaje.getId() + " -> " + dios.getId() + ")";
    }
}
