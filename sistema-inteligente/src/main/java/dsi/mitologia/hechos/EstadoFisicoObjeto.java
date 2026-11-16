package dsi.mitologia.hechos;

import dsi.mitologia.modelo.Objeto;

/**
 * Hecho dinámico que representa la condición física actual de un objeto.
 */
public class EstadoFisicoObjeto {
    private final Objeto objeto;
    private String condicion;

    public EstadoFisicoObjeto(Objeto objeto, String condicion) {
        if (objeto == null || condicion == null || condicion.trim().isEmpty()) {
            throw new IllegalArgumentException("El estado físico requiere un objeto y una condición válida.");
        }
        this.objeto = objeto;
        this.condicion = condicion;
    }

    public Objeto getObjeto() {
        return objeto;
    }

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        if (condicion == null || condicion.trim().isEmpty()) {
            throw new IllegalArgumentException("La condición no puede estar vacía.");
        }
        this.condicion = condicion;
    }

    @Override
    public String toString() {
        return objeto.getNombre() + " está " + condicion;
    }
}