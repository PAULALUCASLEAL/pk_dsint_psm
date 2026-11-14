package dsi.mitologia.hechos;

//Determina si un objeto está roto o no lo está

//Si es false es que no está roto, si es true es que está roto.

public final class EstadoRoturaObjeto {

    private final dsi.mitologia.modelo.Objeto objeto;
    private boolean roto;

    public EstadoRoturaObjeto(
            dsi.mitologia.modelo.Objeto objeto, boolean roto) {

        if (objeto == null) {
            throw new IllegalArgumentException(
                    "El estado necesita un objeto");
        }

        this.objeto = objeto;
        this.roto = roto;
    }

    public dsi.mitologia.modelo.Objeto getObjeto() {
        return objeto;
    }

    public boolean isRoto() {
        return roto;
    }

    public void setRoto(boolean roto) {
        this.roto = roto;
    }

    @Override
    public String toString() {
        return "EstadoRoturaObjeto("
                + objeto.getId()
                + ", roto=" + roto + ")";
    }
}