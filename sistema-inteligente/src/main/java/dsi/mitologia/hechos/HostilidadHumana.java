package dsi.mitologia.hechos;

import java.util.Objects;

import dsi.mitologia.modelo.Personaje;

//Un pesonaje mantiene hostilidad contra otro

public final class HostilidadHumana {

    private final Personaje hostil;
    private final Personaje destinatario;

    public HostilidadHumana(
            Personaje hostil, Personaje destinatario) {

        if (hostil == null || destinatario == null) {
            throw new IllegalArgumentException(
                    "La hostilidad necesita ambos personajes");
        }

        this.hostil = hostil;
        this.destinatario = destinatario;
    }

    public Personaje getHostil() {
        return hostil;
    }

    public Personaje getDestinatario() {
        return destinatario;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof HostilidadHumana)) {
            return false;
        }

        HostilidadHumana otra = (HostilidadHumana) objeto;

        return hostil.equals(otra.hostil)
                && destinatario.equals(otra.destinatario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostil, destinatario);
    }

    @Override
    public String toString() {
        return "HostilidadHumana("
                + hostil.getId()
                + " -> " + destinatario.getId() + ")";
    }
}