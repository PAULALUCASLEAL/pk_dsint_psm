package dsi.mitologia.hechos;

import java.util.Objects;

import dsi.mitologia.modelo.Personaje;

//Un pesonaje ayuda a otro personaje

public final class AyudaHumana {

    private final Personaje ayudante;
    private final Personaje beneficiario;

    public AyudaHumana(Personaje ayudante, Personaje beneficiario) {
        if (ayudante == null || beneficiario == null) {
            throw new IllegalArgumentException(
                    "La ayuda necesita ayudante y beneficiario");
        }

        this.ayudante = ayudante;
        this.beneficiario = beneficiario;
    }

    public Personaje getAyudante() {
        return ayudante;
    }

    public Personaje getBeneficiario() {
        return beneficiario;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof AyudaHumana)) {
            return false;
        }

        AyudaHumana otra = (AyudaHumana) objeto;

        return ayudante.equals(otra.ayudante)
                && beneficiario.equals(otra.beneficiario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ayudante, beneficiario);
    }

    @Override
    public String toString() {
        return "AyudaHumana("
                + ayudante.getId()
                + " -> " + beneficiario.getId() + ")";
    }
}