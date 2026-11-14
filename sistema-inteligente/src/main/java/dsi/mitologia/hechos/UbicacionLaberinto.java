package dsi.mitologia.hechos;

import dsi.mitologia.modelo.Personaje;

//Clase que representa si un personaje está dentro o fuera del laberinto

public final class UbicacionLaberinto {

    private final Personaje personaje;
    private boolean dentro;

    public UbicacionLaberinto(Personaje personaje, boolean dentro) {
        if (personaje == null) {
            throw new IllegalArgumentException(
                    "La ubicacion necesita un personaje");
        }

        this.personaje = personaje;
        this.dentro = dentro;
    }

    public Personaje getPersonaje() {
        return personaje;
    }

    public boolean isDentro() {
        return dentro;
    }

    public void setDentro(boolean dentro) {
        this.dentro = dentro;
    }

    @Override
    public String toString() {
        return "UbicacionLaberinto("
                + personaje.getId()
                + ", dentro=" + dentro + ")";
    }
}