package dsi.mitologia.modelo;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import dsi.mitologia.modelo.enums.EstadoLibertad;
import dsi.mitologia.modelo.enums.EstadoVital;
import dsi.mitologia.modelo.enums.TipoPersonaje;

/**
 * Clase OWL {@code Personaje} y sus subclases (Dios, Semidios, Heroe, Mortal, Criatura).
 *
 * <p>Relacion con la ontologia: cada instancia es un individuo de {@code Personaje}. Sus clases OWL
 * concretas se guardan en {@code tipos} y sus DatatypeProperties {@code estado_vital} y
 * {@code estado_libertad} en los dos campos de estado. Las ObjectProperties ({@code tiene_objeto},
 * {@code tiene_favor}, {@code tiene_enojo}, {@code apresa_a}, {@code mata_a}, {@code libera_a},
 * {@code localiza_a}, {@code es_hijo_de}, {@code tiene_capacidad}) son hechos aparte.</p>
 *
 * <p>Papel en el mito: Perseo (Heroe y Semidios) es el protagonista; Atenea, Hermes, Hefesto, Hades y
 * Poseidon (Dios) conceden favores o enojos; Andromeda y Casiopea (Mortal) son la victima y la causante
 * del castigo; Medusa, Ceto y Pegaso (Criatura) son los obstaculos o la montura. Los estados cambian a lo
 * largo de la historia: Ceto pasa a libre por el enojo de Poseidon, Andromeda pasa a presa, Medusa pasa
 * a muerta y, al final, Andromeda vuelve a libre.</p>
 *
 * <p>Decision de diseno (tipos): los tipos son un conjunto porque OWL permite que un individuo
 * pertenezca a varias clases (Perseo es Heroe y Semidios) y Java no tiene herencia multiple de clases.
 * Alternativa descartada: subclases Java {@code Dios}, {@code Heroe}... Obligaria a elegir una sola
 * clase por individuo. Ademas, Drools filtra igual de bien con getters booleanos: {@code Personaje(heroe)}.</p>
 *
 * <p>Decision de diseno (estado): solo {@code estadoVital} y {@code estadoLibertad} son mutables, y en
 * el OWL son propiedades de datos con cardinalidad exactamente 1. Un enum por propiedad hace imposible
 * estar vivo y muerto a la vez o no tener estado. Las posesiones, favores, cautiverios, etc. NO se
 * guardan aqui: son hechos aparte (paquete {@code hechos}), para tener una sola fuente de verdad y para
 * que las reglas accedan a ellos directamente (hechos "desempaquetados", como pide el profesor).</p>
 *
 * <p>Uso en Drools: el estado se cambia en el consecuente con
 * {@code modify($p) { setEstadoVital(EstadoVital.MUERTO) }}, para que el motor reevalue las reglas que
 * dependen de el. La igualdad es por identificador: el id es el nombre local del IRI y no cambia
 * aunque cambie el estado, de modo que {@code equals}/{@code hashCode} son estables dentro de la sesion.</p>
 *
 * <p>No es {@code final} (a diferencia de {@code Objeto} y {@code Capacidad}) para no cerrar la
 * puerta a extensiones en la fase 2; aun asi, las reglas no deben depender de subclases.</p>
 */
public class Personaje implements EntidadMitologica {

    /** Nombre local del IRI del individuo en mitologia.owl (p. ej. "Perseo"). Clave de igualdad. */
    private final String id;
    /** Nombre legible para la historia; por defecto igual al id. */
    private final String nombre;
    /**
     * Subclases OWL de {@code Personaje} a las que pertenece (p. ej. {HEROE, SEMIDIOS} para Perseo).
     * Inmutable: un personaje no cambia de clase durante la historia, lo que hace seguros los getters
     * booleanos en los patrones de Drools.
     */
    private final Set<TipoPersonaje> tipos;
    /** DatatypeProperty {@code estado_vital} ("vivo" / "muerto"). Cambia, p. ej., al morir Medusa. */
    private EstadoVital estadoVital;
    /** DatatypeProperty {@code estado_libertad} ("libre" / "preso"). Cambia al apresar o liberar. */
    private EstadoLibertad estadoLibertad;

    /**
     * Crea un personaje vivo y libre (estado por defecto).
     *
     * <p>Es el caso mas habitual en el estado inicial (Perseo, los dioses, Andromeda antes del
     * castigo). Los escenarios que partan de otro estado (p. ej. Ceto presa) usan el otro constructor.</p>
     */
    public Personaje(String id, String nombre, Set<TipoPersonaje> tipos) {
        this(id, nombre, tipos, EstadoVital.VIVO, EstadoLibertad.LIBRE);
    }

    /**
     * Crea un personaje con un estado inicial explicito.
     *
     * @param id             nombre local del IRI en el OWL; obligatorio
     * @param nombre         nombre para mostrar; si es {@code null} se usa el id
     * @param tipos          subclases OWL; {@code null} o vacio equivale a un {@code Personaje} generico
     * @param estadoVital    valor de {@code estado_vital}; no puede ser {@code null} (cardinalidad 1)
     * @param estadoLibertad valor de {@code estado_libertad}; no puede ser {@code null} (cardinalidad 1)
     * @throws IllegalArgumentException si falta el id o alguno de los estados
     */
    public Personaje(String id, String nombre, Set<TipoPersonaje> tipos,
                     EstadoVital estadoVital, EstadoLibertad estadoLibertad) {
        // El id es la clave de igualdad y el enlace con la ontologia: no puede faltar.
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("El personaje necesita id");
        this.id = id;
        this.nombre = nombre == null ? id : nombre;
        // Copia defensiva e inmodificable: los tipos no deben cambiar despues de insertar el hecho.
        // EnumSet.copyOf falla con colecciones vacias, por eso el caso vacio usa emptySet().
        this.tipos = tipos == null || tipos.isEmpty()
                ? Collections.<TipoPersonaje>emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(tipos));
        // Se pasa por los setters para reutilizar la validacion de cardinalidad 1 (no nulos).
        setEstadoVital(estadoVital);
        setEstadoLibertad(estadoLibertad);
    }

    /**
     * Copia independiente (mismo id y tipos, mismo estado) para otro escenario.
     *
     * <p>Existe porque el estado es mutable: cada escenario debe partir de su propio estado inicial y
     * los {@code modify} de una sesion no pueden afectar a otra. Los tipos se comparten sin riesgo
     * porque son inmodificables.</p>
     */
    public Personaje copia() {
        return new Personaje(id, nombre, tipos, estadoVital, estadoLibertad);
    }

    /** {@inheritDoc} */
    @Override
    public String getId() {
        return id;
    }

    /** {@inheritDoc} */
    @Override
    public String getNombre() {
        return nombre;
    }

    /** Subclases OWL del personaje (vista inmodificable). */
    public Set<TipoPersonaje> getTipos() {
        return tipos;
    }

    /**
     * Indica si el personaje pertenece a la subclase OWL dada. Version generica de los getters
     * booleanos, util cuando el tipo llega como dato.
     */
    public boolean esDeTipo(TipoPersonaje tipo) {
        return tipos.contains(tipo);
    }

    // Getters booleanos para patrones Drools sencillos: Personaje(heroe), Personaje(dios)...
    // Los tipos no cambian, asi que estos getters son seguros con la reactividad de Drools.

    /** Clase OWL {@code Dios} (p. ej. Atenea, Hermes, Hades). Solo un dios concede favor o enojo. */
    public boolean isDios() {
        return esDeTipo(TipoPersonaje.DIOS);
    }

    /** Clase OWL {@code Semidios} (p. ej. Perseo, hijo de Zeus y Danae). */
    public boolean isSemidios() {
        return esDeTipo(TipoPersonaje.SEMIDIOS);
    }

    /** Clase OWL {@code Heroe} (p. ej. Perseo). */
    public boolean isHeroe() {
        return esDeTipo(TipoPersonaje.HEROE);
    }

    /** Clase OWL {@code Mortal} (p. ej. Andromeda, Casiopea, Danae). */
    public boolean isMortal() {
        return esDeTipo(TipoPersonaje.MORTAL);
    }

    /** Clase OWL {@code Criatura} (p. ej. Medusa, Ceto, Pegaso). */
    public boolean isCriatura() {
        return esDeTipo(TipoPersonaje.CRIATURA);
    }

    /** Valor actual de {@code estado_vital}; nunca es {@code null}. */
    public EstadoVital getEstadoVital() {
        return estadoVital;
    }

    /**
     * Cambia {@code estado_vital}. En las reglas debe llamarse dentro de {@code modify} para que Drools
     * se entere del cambio (p. ej. al matar a Medusa).
     *
     * @throws IllegalArgumentException si el valor es {@code null}
     */
    public void setEstadoVital(EstadoVital estadoVital) {
        // Cardinalidad exactamente 1 en la ontologia: no se admite "sin estado".
        if (estadoVital == null) throw new IllegalArgumentException("estado_vital tiene cardinalidad 1");
        this.estadoVital = estadoVital;
    }

    /** Valor actual de {@code estado_libertad}; nunca es {@code null}. */
    public EstadoLibertad getEstadoLibertad() {
        return estadoLibertad;
    }

    /**
     * Cambia {@code estado_libertad}. En las reglas debe llamarse dentro de {@code modify}
     * (p. ej. cuando Andromeda queda presa o cuando Perseo la libera).
     *
     * @throws IllegalArgumentException si el valor es {@code null}
     */
    public void setEstadoLibertad(EstadoLibertad estadoLibertad) {
        // Cardinalidad exactamente 1 en la ontologia: no se admite "sin estado".
        if (estadoLibertad == null) throw new IllegalArgumentException("estado_libertad tiene cardinalidad 1");
        this.estadoLibertad = estadoLibertad;
    }

    /**
     * Igualdad por id (no por estado): Medusa viva y Medusa muerta son el mismo individuo. Asi los
     * hechos que apuntan a un personaje siguen siendo validos tras un {@code modify}.
     */
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Personaje && id.equals(((Personaje) o).id));
    }

    /** Coherente con {@link #equals(Object)}: solo usa el id, que es inmutable, nunca el estado mutable. */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /** Nombre, tipos y estados con sus literales OWL, p. ej. "Perseo[SEMIDIOS, HEROE](vivo, libre)". */
    @Override
    public String toString() {
        return nombre + tipos + "(" + estadoVital.getValorOwl() + ", " + estadoLibertad.getValorOwl() + ")";
    }
}
