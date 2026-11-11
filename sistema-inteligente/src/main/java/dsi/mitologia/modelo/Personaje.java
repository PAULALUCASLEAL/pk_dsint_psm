package dsi.mitologia.modelo;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import dsi.mitologia.modelo.enums.EstadoLibertad;
import dsi.mitologia.modelo.enums.EstadoVital;
import dsi.mitologia.modelo.enums.TipoPersonaje;

/**
 * Clase OWL {@code Personaje} y sus subclases (Dios, Semidios, Heroe, Mortal, Criatura).
 */
public class Personaje implements EntidadMitologica {

    /** Nombre local del IRI del individuo en mitologia.owl*/
    private final String id;
    /** Nombre legible para la historia*/
    private final String nombre;
    /**
     * Subclases OWL de {@code Personaje}
     */
    private final Set<TipoPersonaje> tipos;
    /** DatatypeProperty {@code estado_vital} ("vivo" / "muerto")*/
    private EstadoVital estadoVital;
    /** DatatypeProperty {@code estado_libertad} ("libre" / "preso")*/
    private EstadoLibertad estadoLibertad;

    /**
     * Crea un personaje vivo y libre (estado por defecto).
     */
    public Personaje(String id, String nombre, Set<TipoPersonaje> tipos) {
        this(id, nombre, tipos, EstadoVital.VIVO, EstadoLibertad.LIBRE);
    }

    /**
     * Crea un personaje con un estado inicial explicito.
     *
     * @param id             nombre local del IRI en el OWL
     * @param nombre         nombre para mostrar
     * @param tipos          subclases OWL
     * @param estadoVital    valor de {@code estado_vital} (cardinalidad 1)
     * @param estadoLibertad valor de {@code estado_libertad} (cardinalidad 1)
     * @throws IllegalArgumentException si falta el id o alguno de los estados
     */
    public Personaje(String id, String nombre, Set<TipoPersonaje> tipos,
                     EstadoVital estadoVital, EstadoLibertad estadoLibertad) {
        // El id es obligatorio
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("El personaje necesita id");
        this.id = id;
        this.nombre = nombre == null ? id : nombre;
        // Los tipos no deben cambiar despues de insertar el hecho.
        this.tipos = tipos == null || tipos.isEmpty()
                ? Collections.<TipoPersonaje>emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(tipos));
        // Se pasa por los setters para reutilizar la validacion de cardinalidad 1 (no nulos).
        setEstadoVital(estadoVital);
        setEstadoLibertad(estadoLibertad);
    }

    /**
     * Copia independiente (mismo id y tipos, mismo estado) para otro escenario.
	 * Cada escenario debe partir de su propio estado inicial.
	 * Los tipos se comparten sin riesgo porque son inmodificables.
     */
    public Personaje copia() {
        return new Personaje(id, nombre, tipos, estadoVital, estadoLibertad);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    /** Subclases OWL del personaje (vista inmodificable). */
    public Set<TipoPersonaje> getTipos() {
        return tipos;
    }

    /**
     * Indica si el personaje pertenece a la subclase OWL dada.
     */
    public boolean esDeTipo(TipoPersonaje tipo) {
        return tipos.contains(tipo);
    }

    /** Clase OWL {@code Dios}. Solo un dios concede favor o enojo. */
    public boolean isDios() {
        return esDeTipo(TipoPersonaje.DIOS);
    }

    /** Clase OWL {@code Semidios}*/
    public boolean isSemidios() {
        return esDeTipo(TipoPersonaje.SEMIDIOS);
    }

    /** Clase OWL {@code Heroe}. */
    public boolean isHeroe() {
        return esDeTipo(TipoPersonaje.HEROE);
    }

    /** Clase OWL {@code Mortal}. */
    public boolean isMortal() {
        return esDeTipo(TipoPersonaje.MORTAL);
    }

    /** Clase OWL {@code Criatura}. */
    public boolean isCriatura() {
        return esDeTipo(TipoPersonaje.CRIATURA);
    }

    /** Valor actual de {@code estado_vital}; nunca es {@code null}. */
    public EstadoVital getEstadoVital() {
        return estadoVital;
    }

    /**
     * Cambia {@code estado_vital}. En las reglas debe llamarse dentro de {@code modify} para que Drools
     * se entere del cambio.
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
     * Cambia {@code estado_libertad}. En las reglas debe llamarse dentro de {@code modify}.
     *
     * @throws IllegalArgumentException si el valor es {@code null}
     */
    public void setEstadoLibertad(EstadoLibertad estadoLibertad) {
        // Cardinalidad exactamente 1 en la ontologia: no se admite "sin estado".
        if (estadoLibertad == null) throw new IllegalArgumentException("estado_libertad tiene cardinalidad 1");
        this.estadoLibertad = estadoLibertad;
    }

    /**
     * Igualdad por id (no por estado): Medusa viva y Medusa muerta son el mismo individuo.
     */
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Personaje && id.equals(((Personaje) o).id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /** Nombre, tipos y estados con sus literales OWL*/
    @Override
    public String toString() {
        return nombre + tipos + "(" + estadoVital.getValorOwl() + ", " + estadoLibertad.getValorOwl() + ")";
    }
}
