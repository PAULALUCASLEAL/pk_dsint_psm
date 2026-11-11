package dsi.mitologia.modelo;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import dsi.mitologia.modelo.enums.CategoriaObjeto;

/**
 * Clase OWL {@code Objeto} y sus subclases (Arma, Proteccion, ArtefactoMagico, ParteAnatomica).
 */
public final class Objeto implements EntidadMitologica {

    /** Nombre local del IRI del objeto en mitologia.owl*/
    private final String id;
    /** Nombre legible para la historia; por defecto igual al id. */
    private final String nombre;
    /**
     * Subclases OWL de {@code Objeto} a las que pertenece el objeto.
     */
    private final Set<CategoriaObjeto> categorias; 

    /**
     * Crea un objeto del dominio.
     *
     * @param id         nombre local del IRI en el OWL
     * @param nombre     nombre para mostrar
     * @param categorias subclases OWL del objeto
     * @throws IllegalArgumentException si el id es nulo o vacio
     */
    public Objeto(String id, String nombre, Set<CategoriaObjeto> categorias) {
        // El id es obligatorio
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

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    /** Subclases OWL del objeto (inmodificable). */
    public Set<CategoriaObjeto> getCategorias() {
        return categorias;
    }

    /**
     * Indica si el objeto pertenece a la subclase OWL dada.
     */
    public boolean esDeCategoria(CategoriaObjeto categoria) {
        return categorias.contains(categoria);
    }

    /** Clase OWL {@code Arma} */
    public boolean isArma() {
        return esDeCategoria(CategoriaObjeto.ARMA);
    }

    /** Clase OWL {@code Proteccion}*/
    public boolean isProteccion() {
        return esDeCategoria(CategoriaObjeto.PROTECCION);
    }

    /** Clase OWL {@code ArtefactoMagico}*/
    public boolean isArtefactoMagico() {
        return esDeCategoria(CategoriaObjeto.ARTEFACTO_MAGICO);
    }

    /** Clase OWL {@code ParteAnatomica}*/
    public boolean isParteAnatomica() {
        return esDeCategoria(CategoriaObjeto.PARTE_ANATOMICA);
    }

    /** Igualdad por id*/
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Objeto && id.equals(((Objeto) o).id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /** Nombre seguido de sus categorias */
    @Override
    public String toString() {
        return nombre + categorias;
    }
}
