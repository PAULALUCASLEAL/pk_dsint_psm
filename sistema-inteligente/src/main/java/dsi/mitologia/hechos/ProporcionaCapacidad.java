package dsi.mitologia.hechos;

import java.util.Objects;

import dsi.mitologia.modelo.Capacidad;
import dsi.mitologia.modelo.Objeto;

/**
 * Hecho de conocimiento estatico: un objeto proporciona una capacidad a quien lo posee.
 *
 * <p>Relacion con la ontologia: corresponde a la ObjectProperty {@code proporciona_capacidad}
 * (dominio Objeto, rango Capacidad, con restriccion allValuesFrom Capacidad sobre Objeto). No
 * tiene inversa en el OWL.</p>
 *
 * <p>Papel en el mito: en el OWL, CascoHades -> Invisibilidad, SandaliasAladas -> Vuelo,
 * EscudoEspejo -> Reflejo y CabezaMedusa -> Petrificacion. Varias fuentes pueden dar la misma
 * capacidad (AnilloInvisibilidad -> Invisibilidad, un conjuro de vuelo -> Vuelo) sin crear clases
 * nuevas: basta con anadir otro hecho.</p>
 *
 * <p>Uso en reglas: es la pieza que hace genericas las reglas de capacidades (nota del profesor):
 * una sola regla
 * {@code Posesion($p : poseedor, $o : objeto) ProporcionaCapacidad(objeto == $o, $c : capacidad)}
 * -> {@code insertLogical(CapacidadDisponible.deObjeto($p, $c, $o))} sirve para todos los objetos,
 * en lugar de una regla por objeto.</p>
 *
 * <p>Decision de diseno: se modela como hecho en memoria de trabajo y no como campo de Objeto
 * para que el antecedente sea un patron simple (notas K/L del profesor) y para que el
 * conocimiento del dominio venga de la ontologia y no quede programado en Java.</p>
 *
 * <p>Inmutable (no cambia durante la aventura). Igualdad por valor (objeto, capacidad).</p>
 */
public final class ProporcionaCapacidad {

    /** Objeto que da la capacidad (sujeto de proporciona_capacidad). Ej.: CascoHades. */
    private final Objeto objeto;
    /** Capacidad proporcionada (objeto de proporciona_capacidad). Ej.: Invisibilidad. */
    private final Capacidad capacidad;

    /** Crea el hecho; se rechazan nulos porque la propiedad siempre une un objeto y una capacidad. */
    public ProporcionaCapacidad(Objeto objeto, Capacidad capacidad) {
        if (objeto == null || capacidad == null) {
            throw new IllegalArgumentException("ProporcionaCapacidad necesita objeto y capacidad");
        }
        this.objeto = objeto;
        this.capacidad = capacidad;
    }

    /** Objeto que proporciona la capacidad. */
    public Objeto getObjeto() {
        return objeto;
    }

    /** Capacidad proporcionada. */
    public Capacidad getCapacidad() {
        return capacidad;
    }

    /** Igualdad por contenido (objeto, capacidad). */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProporcionaCapacidad)) return false;
        ProporcionaCapacidad otro = (ProporcionaCapacidad) o;
        return objeto.equals(otro.objeto) && capacidad.equals(otro.capacidad);
    }

    /** Coherente con equals. */
    @Override
    public int hashCode() {
        return Objects.hash(objeto, capacidad);
    }

    /** Texto legible para trazas: objeto -> capacidad. */
    @Override
    public String toString() {
        return "ProporcionaCapacidad(" + objeto.getId() + " -> " + capacidad.getId() + ")";
    }
}
