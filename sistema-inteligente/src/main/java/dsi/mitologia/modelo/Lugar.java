package dsi.mitologia.modelo;

import java.util.Objects;

/**
 * Representa un lugar físico o conceptual en la ontología (ej. LaberintoCreta, MundoExterior).
 * Es una entidad del dominio estática e inmutable.
 */
public final class Lugar implements EntidadMitologica {
    
    private final String id;
    private final String nombre;

    public Lugar(String id, String nombre) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El lugar necesita un ID válido.");
        }
        this.id = id;
        // Si el OWL no define un nombre legible, usamos el ID.
        this.nombre = (nombre != null && !nombre.trim().isEmpty()) ? nombre : id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lugar lugar = (Lugar) o;
        // La identidad de la entidad mitológica la dicta estrictamente su ID del OWL.
        return id.equals(lugar.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nombre;
    }
}