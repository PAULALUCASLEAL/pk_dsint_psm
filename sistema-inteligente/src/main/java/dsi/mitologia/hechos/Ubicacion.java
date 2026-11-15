package dsi.mitologia.hechos;

import dsi.mitologia.modelo.Lugar;
import dsi.mitologia.modelo.Personaje;

/**
 * Hecho dinámico que representa la ubicación actual de un personaje en la memoria de trabajo.
 * Es mutable para permitir transiciones de estado (moverse) mediante modify() en Drools.
 */
public class Ubicacion {
    
    // El personaje es final porque este hecho le pertenece estructuralmente. 
    // Si Teseo cambia de lugar, actualizamos la ubicación, no creamos un hecho para otra persona.
    private final Personaje personaje;
    
    // Mutable para que las reglas de Drools puedan actualizar el estado del laberinto al exterior.
    private Lugar lugar;

    public Ubicacion(Personaje personaje, Lugar lugar) {
        if (personaje == null || lugar == null) {
            throw new IllegalArgumentException("La ubicación requiere un personaje y un lugar válidos.");
        }
        this.personaje = personaje;
        this.lugar = lugar;
    }

    public Personaje getPersonaje() {
        return personaje;
    }

    public Lugar getLugar() {
        return lugar;
    }

    public void setLugar(Lugar lugar) {
        if (lugar == null) {
            throw new IllegalArgumentException("El nuevo lugar no puede ser nulo.");
        }
        this.lugar = lugar;
    }

    @Override
    public String toString() {
        return personaje.getNombre() + " está en " + lugar.getNombre();
    }
}