package dsi.mitologia.motor;

import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Global;
import org.kie.api.runtime.KieSession;

import dsi.mitologia.escenario.EscenarioMitologico;

/**
 * Puente entre el modelo Java y el motor de reglas: es la UNICA clase del modelo que conoce la API de
 * Drools (KieSession, globals, paquetes). Vuelca un escenario en una KieSession, dispara las reglas y
 * recoge el estado final.
 *
 * <p>Decision de diseno: se separa modelo y motor siguiendo el patron MVC que pide el profesor. El
 * modelo ({@code escenario}, {@code hechos}, {@code modelo}, {@code eventos}) son POJOs que no importan
 * nada de {@code org.kie}; la vista (salida por consola o fichero) solo lee el registro de eventos; y
 * este controlador es el unico que sabe como se habla con Drools. Asi el modelo se puede probar sin
 * motor (ver {@code ModeloMitologicoTest}) y un cambio de version de Drools solo toca esta clase.</p>
 *
 * <p>Relacion con la nota O del enunciado: aqui no se infiere nada. Java solo prepara los datos de
 * entrada (hechos desempaquetados del escenario) y recoge los de salida; toda la cadena causal del mito
 * (Ceto libre por el enojo de Poseidon, Andromeda presa, favores de Atenea/Hermes/Hefesto/Hades,
 * objetos, capacidades, muerte de Medusa, petrificacion de Ceto, liberacion de Andromeda) la deducen
 * las reglas por encadenamiento hacia delante.</p>
 *
 * <p>Relacion con los agenda-group: las reglas del mito se agruparan en agendas (estado inicial e
 * historia). Esta clase no fija el foco: la primera regla de la agenda MAIN (o un {@code auto-focus})
 * es la que debe pasar el foco a cada grupo, de modo que el orden de fases quede declarado en el DRL y
 * no programado en Java. Si en el futuro hiciera falta, el sitio para un
 * {@code getAgenda().getAgendaGroup(..).setFocus()} seria justo antes de {@code fireAllRules()}.</p>
 *
 * <p>Ciclo de vida: una sesion por escenario, para que dos escenarios no compartan memoria de trabajo.
 * El llamador crea la sesion y la cierra ({@code dispose()}); esta clase no la cierra porque los tests
 * necesitan seguir consultandola despues de ejecutar (p. ej. para retirar un hecho a mano).</p>
 */
public final class MotorReglas {

    /**
     * Nombre del global DRL donde las reglas registran los eventos ({@code global RegistroEventos registro}).
     * Se usa un global y no un hecho porque el registro es salida para la vista, no conocimiento sobre el
     * que se razone: no debe casar con patrones ni provocar activaciones.
     */
    public static final String GLOBAL_REGISTRO = "registro";

    /** Clase de utilidad sin estado: no se instancia. */
    private MotorReglas() {
    }

    /**
     * Ejecuta un escenario completo sobre una sesion ya creada.
     *
     * <ol>
     *   <li>Enlaza el global {@value #GLOBAL_REGISTRO} con el registro del escenario, para que los
     *       consecuentes anoten cada paso del mito (actor, accion, justificacion, regla, causas).</li>
     *   <li>Inserta los hechos desempaquetados del escenario: personajes, objetos, capacidades,
     *       {@code ProporcionaCapacidad}, filiaciones, posesiones, favores, enojos, cautiverios... y el
     *       propio {@code Objetivo} (la pregunta), para que las reglas puedan razonar sobre el.</li>
     *   <li>Dispara las reglas ({@code fireAllRules}) hasta que la agenda queda vacia.</li>
     *   <li>Sincroniza el {@code EstadoAventura} con la memoria de trabajo final (solo copia, no
     *       deduce) y evalua si el objetivo se cumple.</li>
     * </ol>
     *
     * @return numero de reglas disparadas. Se devuelve porque es la forma mas barata de detectar
     *         problemas del motor: 0 indica que no ha casado nada (hechos mal insertados o DRL no
     *         cargado) y un numero desproporcionado delata un bucle de reglas que habria que cortar con
     *         {@code no-loop}, {@code lock-on-active} o {@code insertLogical}.
     */
    public static int ejecutar(KieSession sesion, EscenarioMitologico escenario) {
        return ejecutar(sesion, escenario, -1);
    }

    /**
     * Igual que {@link #ejecutar(KieSession, EscenarioMitologico)} pero con un limite de disparos.
     *
     * <p>Lo usan las pruebas para demostrar que las reglas terminan: si se alcanza el limite es que hay
     * un bucle (una regla que se reactiva a si misma) y la prueba falla en lugar de colgarse.</p>
     *
     * @param maxDisparos numero maximo de reglas a disparar; negativo = sin limite
     */
    public static int ejecutar(KieSession sesion, EscenarioMitologico escenario, int maxDisparos) {
        // El global solo se fija si el DRL lo declara: setGlobal sobre un global inexistente lanza
        // excepcion, y algunas KieBase de prueba no lo necesitan.
        if (declaraGlobal(sesion, GLOBAL_REGISTRO)) {
            sesion.setGlobal(GLOBAL_REGISTRO, escenario.getRegistro());
        }
        // Hechos desempaquetados: cada relacion es un objeto propio (Posesion, Favor...) y no una lista
        // dentro de Personaje, para que las reglas puedan casar sobre ella y modificarla una a una.
        for (Object hecho : escenario.getHechosParaDrools()) {
            sesion.insert(hecho);
        }
        int disparadas = maxDisparos < 0 ? sesion.fireAllRules() : sesion.fireAllRules(maxDisparos);
        // Lectura de salida: se copia lo que las reglas dejaron en memoria de trabajo.
        escenario.getEstado().sincronizarDesde(sesion.getObjects());
        escenario.evaluarResultado();
        return disparadas;
    }

    /**
     * Indica si alguno de los paquetes de la KieBase de la sesion declara un global con ese nombre.
     * Permite reutilizar {@link #ejecutar} con bases que no usan el registro sin que falle.
     */
    private static boolean declaraGlobal(KieSession sesion, String nombre) {
        for (KiePackage p : sesion.getKieBase().getKiePackages()) {
            for (Global g : p.getGlobalVariables()) {
                if (nombre.equals(g.getName())) return true;
            }
        }
        return false;
    }
}
