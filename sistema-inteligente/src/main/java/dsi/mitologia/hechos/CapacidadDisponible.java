package dsi.mitologia.hechos;

import java.util.Objects;

import dsi.mitologia.modelo.Capacidad;
import dsi.mitologia.modelo.EntidadMitologica;
import dsi.mitologia.modelo.Objeto;
import dsi.mitologia.modelo.Personaje;
import dsi.mitologia.modelo.enums.OrigenCapacidad;

/**
 * Hecho: un personaje dispone de una capacidad gracias a una fuente concreta.
 *
 * <p>Relacion con la ontologia: corresponde a la ObjectProperty {@code tiene_capacidad}
 * (dominio Personaje, rango Capacidad, con restriccion allValuesFrom Capacidad en Personaje).
 * No tiene inversa en el OWL. Se amplia con dos datos que la ontologia no recoge: el origen
 * ({@link OrigenCapacidad}) y la fuente (objeto, conjuro o montura que la proporciona).</p>
 *
 * <p>Papel en el mito: Perseo obtiene Invisibilidad por el CascoHades (favor de Hades), Reflejo
 * por el EscudoEspejo (favor de Hefesto sobre el EscudoBronce de Atenea), Vuelo por las
 * SandaliasAladas de las Ninfas y Petrificacion por la CabezaMedusa. Con Invisibilidad roba el
 * OjoGrayas; con arma + Reflejo + Invisibilidad + Vuelo mata a Medusa; con Petrificacion vence
 * a Ceto y libera a Andromeda.</p>
 *
 * <p>Decision de diseno: un hecho por cada fuente. Si Perseo tiene casco y anillo, hay dos hechos
 * de Invisibilidad. Al perder el casco solo desaparece el suyo y la capacidad sigue disponible por
 * el anillo. Las reglas genericas preguntan por la capacidad sin mirar la fuente, asi una misma
 * regla sirve para todos los mitos alternativos (vuelo por SandaliasAladas, por un conjuro de
 * vuelo o por montar a Pegaso con la BridaDorada):
 * {@code CapacidadDisponible(personaje == $p, capacidad.id == "Vuelo")}.</p>
 *
 * <p>Se guarda origen y fuente porque la respuesta debe explicar la causa ("Perseo puede volar
 * debido a tener SandaliasAladas") y porque permite retirar la capacidad exacta cuando desaparece
 * su fuente. Alternativa descartada: un unico hecho Personaje-Capacidad sin fuente; obligaria a
 * recalcular si queda otra fuente al perder un objeto y no permitiria explicar el "debido a".</p>
 *
 * <p>Uso recomendado en Drools: {@code insertLogical(CapacidadDisponible.deObjeto($p, $c, $o))}
 * en una regla que exija {@code Posesion(poseedor == $p, objeto == $o)} y
 * {@code ProporcionaCapacidad(objeto == $o, capacidad == $c)}; cuando la posesion se retira o
 * cambia de poseedor, Drools retira solo esa capacidad (mantenimiento de la verdad). El
 * consecuente se limita a insertar, sin logica programada.</p>
 *
 * <p>Inmutable: un hecho deducido no cambia; si deja de ser cierto se retira. Igualdad por valor
 * (personaje, capacidad, origen, fuente): dos derivaciones de la misma capacidad por la misma
 * fuente son el mismo hecho, lo que evita duplicados en memoria de trabajo y en EstadoAventura.</p>
 */
public final class CapacidadDisponible {

    /** Quien dispone de la capacidad (sujeto de tiene_capacidad). Nunca null. */
    private final Personaje personaje;
    /** Capacidad disponible (objeto de tiene_capacidad): Invisibilidad, Vuelo, Reflejo... */
    private final Capacidad capacidad;
    /** Tipo de fuente: PROPIA, OBJETO, CONJURO o MONTURA. Extension del modelo, no esta en el OWL. */
    private final OrigenCapacidad origen;
    /**
     * Entidad que proporciona la capacidad: un Objeto (OBJETO, CONJURO) o un Personaje (MONTURA).
     * Es {@code null} solo si el origen es PROPIA. Se tipa como EntidadMitologica para admitir ambos.
     */
    private final EntidadMitologica fuente;

    /**
     * Constructor privado: solo se crean instancias mediante los metodos de fabrica, que fijan el
     * origen y el tipo de la fuente de forma coherente (no se puede crear una montura que sea un Objeto).
     */
    private CapacidadDisponible(Personaje personaje, Capacidad capacidad,
                                OrigenCapacidad origen, EntidadMitologica fuente) {
        // Sin personaje, capacidad u origen el hecho no tiene sentido en la ontologia.
        if (personaje == null || capacidad == null || origen == null) {
            throw new IllegalArgumentException("CapacidadDisponible necesita personaje, capacidad y origen");
        }
        // Coherencia origen/fuente: PROPIA exige fuente nula y cualquier otro origen exige fuente.
        if ((origen == OrigenCapacidad.PROPIA) != (fuente == null)) {
            throw new IllegalArgumentException("Solo las capacidades propias carecen de fuente");
        }
        this.personaje = personaje;
        this.capacidad = capacidad;
        this.origen = origen;
        this.fuente = fuente;
    }

    /**
     * Capacidad propia del personaje, sin fuente externa (p. ej. una capacidad innata declarada
     * en el dominio). Existe para los casos en que tiene_capacidad se afirma directamente.
     */
    public static CapacidadDisponible propia(Personaje personaje, Capacidad capacidad) {
        return new CapacidadDisponible(personaje, capacidad, OrigenCapacidad.PROPIA, null);
    }

    /**
     * Capacidad dada por un objeto poseido, segun proporciona_capacidad
     * (p. ej. Perseo + CascoHades -> Invisibilidad). Caso principal del mito.
     */
    public static CapacidadDisponible deObjeto(Personaje personaje, Capacidad capacidad, Objeto objeto) {
        return new CapacidadDisponible(personaje, capacidad, OrigenCapacidad.OBJETO, objeto);
    }

    /**
     * Capacidad dada por un conjuro poseido (el conjuro se modela como Objeto). Existe para el
     * mito alternativo del conjuro de vuelo, distinguible en la explicacion del de un objeto fisico.
     */
    public static CapacidadDisponible deConjuro(Personaje personaje, Capacidad capacidad, Objeto conjuro) {
        return new CapacidadDisponible(personaje, capacidad, OrigenCapacidad.CONJURO, conjuro);
    }

    /**
     * Capacidad dada por una criatura capturada que hace de montura (Pegaso domado con la
     * BridaDorada -> Vuelo). La fuente es un Personaje porque Pegaso es una Criatura en el OWL.
     */
    public static CapacidadDisponible deMontura(Personaje personaje, Capacidad capacidad, Personaje montura) {
        return new CapacidadDisponible(personaje, capacidad, OrigenCapacidad.MONTURA, montura);
    }

    /** Personaje que dispone de la capacidad. */
    public Personaje getPersonaje() {
        return personaje;
    }

    /** Capacidad disponible. */
    public Capacidad getCapacidad() {
        return capacidad;
    }

    /** Tipo de fuente de la capacidad; util en reglas de explicacion de la historia. */
    public OrigenCapacidad getOrigen() {
        return origen;
    }

    /** Objeto, conjuro o montura que da la capacidad; {@code null} si es propia. */
    public EntidadMitologica getFuente() {
        return fuente;
    }

    /**
     * Nombre de la fuente para la explicacion ("CascoHades", "ConjuroVuelo", "Pegaso") o
     * "capacidad propia" si no tiene fuente. Lo usan los consecuentes al registrar eventos, para no
     * tener que comprobar en el DRL si la fuente es nula (las capacidades propias no la tienen).
     */
    public String getNombreFuente() {
        return fuente == null ? "capacidad propia" : fuente.getNombre();
    }

    /**
     * Igualdad por contenido: se comparan los cuatro campos. La fuente usa Objects.equals porque
     * puede ser null (capacidad propia).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CapacidadDisponible)) return false;
        CapacidadDisponible otro = (CapacidadDisponible) o;
        return personaje.equals(otro.personaje) && capacidad.equals(otro.capacidad)
                && origen == otro.origen && Objects.equals(fuente, otro.fuente);
    }

    /** Coherente con equals: mismos cuatro campos. */
    @Override
    public int hashCode() {
        return Objects.hash(personaje, capacidad, origen, fuente);
    }

    /** Texto legible para trazas y para la historia, indicando la fuente ("por CascoHades"). */
    @Override
    public String toString() {
        return "CapacidadDisponible(" + personaje.getId() + " tiene " + capacidad.getId()
                + (fuente == null ? " propia" : " por " + fuente.getId()) + ")";
    }
}
