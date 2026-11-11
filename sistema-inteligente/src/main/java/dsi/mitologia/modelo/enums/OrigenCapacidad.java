package dsi.mitologia.modelo.enums;

/**
 * De donde procede una capacidad disponible. El origen es una decision del
 * modelo Java para poder retirar la capacidad cuando desaparece su fuente. Es decir,
 * si un personaje deja de tener un objeto, debe dejar de tener la capacidad que le daba dicho objeto
 */

public enum OrigenCapacidad {
    /** Capacidad propia del personaje */
    PROPIA,
    /** La da un objeto que el personaje posee*/
    OBJETO,
    /** La da un conjuro que el personaje posee*/
    CONJURO,
    /** La da una criatura que el personaje tiene capturada y monta */
    MONTURA
}
