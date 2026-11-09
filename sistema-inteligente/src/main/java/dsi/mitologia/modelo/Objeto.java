package dsi.mitologia.modelo;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import dsi.mitologia.modelo.enums.CategoriaObjeto;

/**
 * Clase OWL {@code Objeto} y sus subclases (Arma, Proteccion, ArtefactoMagico, ParteAnatomica).
 *
 * <p>Relacion con la ontologia: cada instancia es un individuo de {@code Objeto}; sus clases OWL
 * concretas se guardan en {@code categorias}. Las ObjectProperties que salen o llegan a un objeto
 * ({@code esta_en_posesion_de}, {@code proporciona_capacidad}) no son campos de esta clase, sino
 * hechos independientes.</p>
 *
 * <p>Papel en el mito: son los medios que Perseo va reuniendo. Ejemplos del OWL: EscudoBronce y
 * CascoHades (Proteccion), HozAcero (Arma), SandaliasAladas y ZurronMagico (ArtefactoMagico),
 * OjoGrayas y CabezaMedusa (ParteAnatomica). Las reglas usan la categoria cuando la condicion es
 * generica ("tener un arma" para matar a Medusa, "tener una proteccion" para fabricar el EscudoEspejo)
 * y la capacidad proporcionada en el resto de casos.</p>
 *
 * <p>Decision de diseno: inmutable, porque es conocimiento del dominio y se comparte entre escenarios.
 * Quien lo posee lo dice el hecho {@code Posesion}; que capacidad da lo dice el hecho
 * {@code ProporcionaCapacidad}. Asi los hechos quedan "desempaquetados" y las reglas pueden hacer
 * {@code Posesion(objeto == $o)} directamente, sin recorrer listas dentro del objeto.</p>
 *
 * <p>Alternativa descartada: un campo {@code poseedor} mutable dentro de {@code Objeto}. Obligaria a
 * hacer {@code modify} sobre el objeto en cada robo o intercambio (OjoGrayas pasa de las Grayas a
 * Perseo y despues a las Ninfas) y duplicaria la informacion de {@code tiene_objeto}.</p>
 */
public final class Objeto implements EntidadMitologica {

    /** Nombre local del IRI del individuo en mitologia.owl (p. ej. "HozAcero"). Clave de igualdad. */
    private final String id;
    /** Nombre legible para la historia (p. ej. "Hoz de Acero"); por defecto igual al id. */
    private final String nombre;
    /**
     * Subclases OWL de {@code Objeto} a las que pertenece el individuo. Conjunto inmodificable porque
     * las subclases no son disjuntas en la ontologia y un objeto podria pertenecer a varias.
     */
    private final Set<CategoriaObjeto> categorias;

    /**
     * Crea un objeto del dominio.
     *
     * @param id         nombre local del IRI en el OWL; obligatorio
     * @param nombre     nombre para mostrar; si es {@code null} se usa el id
     * @param categorias subclases OWL del objeto; {@code null} o vacio equivale a un {@code Objeto}
     *                   generico sin subclase
     * @throws IllegalArgumentException si el id es nulo o vacio
     */
    public Objeto(String id, String nombre, Set<CategoriaObjeto> categorias) {
        // El id es la clave de igualdad y el enlace con la ontologia: no puede faltar.
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("El objeto necesita id");
        this.id = id;
        this.nombre = nombre == null ? id : nombre;
        // Copia defensiva en un EnumSet (compacto y ordenado) envuelto como inmodificable: quien llama
        // no puede alterar las categorias despues, y asi Drools nunca ve cambiar un dato sin modify.
        // EnumSet.copyOf falla con colecciones vacias, por eso el caso vacio usa emptySet().
        this.categorias = categorias == null || categorias.isEmpty()
                ? Collections.<CategoriaObjeto>emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(categorias));
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

    /** Subclases OWL del objeto (vista inmodificable). */
    public Set<CategoriaObjeto> getCategorias() {
        return categorias;
    }

    /**
     * Indica si el objeto pertenece a la subclase OWL dada. Version generica de los getters booleanos,
     * util en reglas que reciben la categoria como dato.
     */
    public boolean esDeCategoria(CategoriaObjeto categoria) {
        return categorias.contains(categoria);
    }

    // Getters booleanos para patrones Drools legibles: Objeto(arma), Objeto(proteccion)...
    // Las categorias son inmutables, asi que no hay problemas de reactividad.

    /** Clase OWL {@code Arma} (p. ej. HozAcero). Necesaria para la regla de matar a Medusa. */
    public boolean isArma() {
        return esDeCategoria(CategoriaObjeto.ARMA);
    }

    /** Clase OWL {@code Proteccion} (p. ej. EscudoBronce, CascoHades, EscudoEspejo). */
    public boolean isProteccion() {
        return esDeCategoria(CategoriaObjeto.PROTECCION);
    }

    /** Clase OWL {@code ArtefactoMagico} (p. ej. SandaliasAladas, ZurronMagico). */
    public boolean isArtefactoMagico() {
        return esDeCategoria(CategoriaObjeto.ARTEFACTO_MAGICO);
    }

    /** Clase OWL {@code ParteAnatomica} (p. ej. OjoGrayas, CabezaMedusa). */
    public boolean isParteAnatomica() {
        return esDeCategoria(CategoriaObjeto.PARTE_ANATOMICA);
    }

    /** Igualdad por id: un individuo OWL es un IRI, independientemente de cuantas instancias Java haya. */
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Objeto && id.equals(((Objeto) o).id));
    }

    /** Coherente con {@link #equals(Object)}: depende solo del id, que es inmutable. */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /** Nombre seguido de sus categorias, p. ej. "HozAcero[ARMA]"; pensado para depuracion. */
    @Override
    public String toString() {
        return nombre + categorias;
    }
}
