package dsi.mitologia.modelo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;

import dsi.mitologia.escenario.DominioMitologico;
import dsi.mitologia.escenario.EscenarioMitologico;
import dsi.mitologia.escenario.EstadoAventura;
import dsi.mitologia.escenario.Objetivo;
import dsi.mitologia.escenario.ResultadoMision;
import dsi.mitologia.escenario.TipoObjetivo;
import dsi.mitologia.eventos.EventoMitologico;
import dsi.mitologia.eventos.RegistroEventos;
import dsi.mitologia.eventos.TipoAccion;
import dsi.mitologia.hechos.CapacidadDisponible;
import dsi.mitologia.hechos.Cautiverio;
import dsi.mitologia.hechos.Posesion;
import dsi.mitologia.modelo.enums.EstadoLibertad;
import dsi.mitologia.modelo.enums.EstadoVital;
import dsi.mitologia.modelo.enums.TipoPersonaje;

/**
 * Pruebas 1-11 del modelo Java (sin Drools), mas comprobaciones de invariantes del estado.
 *
 * <p>Por que importan: las reglas del mito solo podran hacer insert/modify/delete sobre estos hechos,
 * asi que el modelo debe garantizar por si mismo que el estado es coherente (un objeto tiene un solo
 * dueno, un muerto no actua, el favor solo lo da un Dios, no se libera a quien esta libre). Si una
 * regla futura intenta un paso imposible, el modelo lo rechaza con excepcion en vez de dejar un estado
 * absurdo. Ninguna de estas operaciones infiere: Java solo aplica cambios que se le piden.</p>
 *
 * <p>Se usa {@link DominioDePrueba} y no el OWL para que estas pruebas no dependan de la ontologia.</p>
 */
public class ModeloMitologicoTest {

    /** Dominio de prueba nuevo en cada test. */
    private DominioMitologico dominio;
    /** Estado inicial de un escenario sobre ese dominio (Hades con el casco, Atenea con el escudo). */
    private EstadoAventura estado;
    /** Copias del estado (no los prototipos del dominio) de los individuos mas usados. */
    private Personaje perseo;
    private Personaje hades;
    private Objeto casco;
    private Capacidad invisibilidad;

    /** Arrange comun: estado inicial y referencias a Perseo, Hades, CascoHades e Invisibilidad. */
    @Before
    public void preparar() {
        dominio = DominioDePrueba.crear();
        estado = EstadoAventura.inicial(dominio);
        perseo = estado.getPersonaje("Perseo");
        hades = estado.getPersonaje("Hades");
        casco = estado.getObjeto("CascoHades");
        invisibilidad = estado.getCapacidad("Invisibilidad");
    }

    /**
     * Prueba 1: un personaje puede pertenecer a varias clases OWL a la vez (Perseo es Heroe y Semidios)
     * y sin estado explicito arranca vivo y libre. Detectaria un modelo de herencia simple que obligue
     * a elegir un solo tipo, o valores por defecto incorrectos para estado_vital / estado_libertad.
     */
    // Prueba 1
    @Test
    public void perseoEsHeroeYSemidios() {
        Personaje p = new Personaje("Perseo", "Perseo", EnumSet.of(TipoPersonaje.HEROE, TipoPersonaje.SEMIDIOS));
        assertTrue(p.isHeroe());
        assertTrue(p.isSemidios());
        assertFalse(p.isDios());
        assertEquals(EstadoVital.VIVO, p.getEstadoVital());
        assertEquals(EstadoLibertad.LIBRE, p.getEstadoLibertad());
    }

    /**
     * Prueba 2: matar cambia estado_vital de Medusa a muerto y deja constancia de quien la mato
     * (mata_a). Matar dos veces es un error. Papel en el mito: la muerte de Medusa es el paso que da
     * acceso a la CabezaMedusa; detectaria que una regla pudiera repetir el suceso en bucle.
     */
    // Prueba 2
    @Test
    public void medusaPasaDeVivaAMuerta() {
        Personaje medusa = estado.getPersonaje("Medusa");
        assertEquals(EstadoVital.VIVO, medusa.getEstadoVital());
        // Act
        estado.matar(perseo, medusa);
        // Assert: estado vital y hecho Muerte
        assertEquals(EstadoVital.MUERTO, medusa.getEstadoVital());
        assertTrue(estado.haMatado("Perseo", "Medusa"));
        try {
            estado.matar(perseo, medusa);
            fail("No se puede matar dos veces");
        } catch (IllegalStateException esperado) {
            // correcto
        }
    }

    /** estado_vital tiene cardinalidad 1 en la ontologia: nunca puede quedar sin valor. */
    @Test(expected = IllegalArgumentException.class)
    public void elEstadoVitalNoPuedeQuedarVacio() {
        perseo.setEstadoVital(null);
    }

    /**
     * Prueba 3: transferir el CascoHades de Hades a Perseo deja un unico poseedor y MODIFICA el mismo
     * hecho Posesion en lugar de crear otro. Por que importa: en Drools la regla de entrega hara un
     * {@code modify} de la Posesion; si se crease una nueva, convivirian dos duenos del mismo objeto.
     */
    // Prueba 3
    @Test
    public void transferirElCascoDejaUnSoloPropietario() {
        // Arrange: estado por defecto del OWL, Hades tiene_objeto CascoHades.
        assertSame(hades, estado.poseedorDe(casco));
        Posesion antes = estado.getPosesiones().get(0);
        // Act
        estado.transferirObjeto(hades, perseo, casco);
        // Assert: nuevo dueno, el antiguo ya no lo tiene y solo hay una Posesion del casco.
        assertSame(perseo, estado.poseedorDe(casco));
        assertTrue(estado.posee(perseo, casco));
        assertFalse(estado.posee(hades, casco));
        int posesionesDelCasco = 0;
        for (Posesion p : estado.getPosesiones()) {
            if (p.getObjeto().equals(casco)) posesionesDelCasco++;
        }
        assertEquals(1, posesionesDelCasco);
        assertSame("Se modifica el mismo hecho", antes, estado.getPosesiones().get(0));
    }

    /** Nadie puede entregar un objeto que no tiene (Perseo no puede dar el casco a Hades al principio). */
    @Test(expected = IllegalStateException.class)
    public void noSeTransfiereLoQueNoSePosee() {
        estado.transferirObjeto(perseo, hades, casco);
    }

    /** Un objeto con dueno no puede otorgarse a otro: evita dos Posesion del mismo objeto. */
    @Test(expected = IllegalStateException.class)
    public void noSeDuplicaUnaPosesion() {
        estado.otorgarObjeto(perseo, casco);
    }

    /**
     * Prueba 4: Perseo solo puede tener la Invisibilidad del casco si posee el casco (proporciona_capacidad
     * CascoHades -&gt; Invisibilidad), y aun teniendolo Java NO la deduce sola: la concede una regla. Una
     * segunda concesion igual no duplica. Detectaria inferencia escondida en Java (prohibida por la nota O)
     * o capacidades duplicadas que dispararian reglas de mas.
     */
    // Prueba 4
    @Test
    public void perseoObtieneInvisibilidadCuandoTieneElCasco() {
        assertTrue(estado.proporciona(casco, invisibilidad));
        // Sin la fuente, la concesion se rechaza.
        try {
            estado.concederCapacidad(CapacidadDisponible.deObjeto(perseo, invisibilidad, casco));
            fail("Sin el casco no puede obtener la capacidad del casco");
        } catch (IllegalStateException esperado) {
            // correcto
        }
        estado.transferirObjeto(hades, perseo, casco);
        assertFalse("Java no infiere la capacidad: la concede una regla", estado.tieneCapacidad(perseo, invisibilidad));
        assertTrue(estado.concederCapacidad(CapacidadDisponible.deObjeto(perseo, invisibilidad, casco)));
        assertTrue(estado.tieneCapacidad(perseo, invisibilidad));
        assertFalse("No se duplica", estado.concederCapacidad(CapacidadDisponible.deObjeto(perseo, invisibilidad, casco)));
    }

    /**
     * Prueba 5: al devolver el casco, Perseo pierde la Invisibilidad que dependia de el. Es el equivalente
     * en Java del mantenimiento de la verdad que en Drools dara {@code insertLogical}; detectaria
     * capacidades "huerfanas" que sobrevivan a su fuente.
     */
    // Prueba 5
    @Test
    public void alPerderElCascoPierdeLaInvisibilidad() {
        estado.transferirObjeto(hades, perseo, casco);
        estado.concederCapacidad(CapacidadDisponible.deObjeto(perseo, invisibilidad, casco));
        // Act: el casco vuelve a Hades
        estado.transferirObjeto(perseo, hades, casco);
        assertFalse(estado.tieneCapacidad(perseo, invisibilidad));
    }

    /**
     * Prueba 6: con dos fuentes de Invisibilidad (casco y anillo de prueba), perder una no quita la
     * capacidad. Detectaria que se retiren TODAS las capacidades de un tipo al perder cualquier fuente.
     */
    // Prueba 6
    @Test
    public void conCascoYAnilloSigueInvisibleAlPerderElCasco() {
        Objeto anillo = estado.getObjeto("AnilloInvisibilidad");
        estado.otorgarObjeto(perseo, anillo);
        estado.transferirObjeto(hades, perseo, casco);
        estado.concederCapacidad(CapacidadDisponible.deObjeto(perseo, invisibilidad, casco));
        estado.concederCapacidad(CapacidadDisponible.deObjeto(perseo, invisibilidad, anillo));
        assertEquals(2, estado.fuentesDe(perseo, invisibilidad).size());

        // Act: se retira solo el casco; queda el anillo como unica fuente.
        estado.retirarObjeto(perseo, casco);
        assertTrue(estado.tieneCapacidad(perseo, invisibilidad));
        assertEquals(anillo, estado.fuentesDe(perseo, invisibilidad).get(0).getFuente());
    }

    /**
     * El Vuelo puede venir de origenes distintos (conjuro o montura capturada, p. ej. Pegaso). Liberar
     * la montura retira solo la fuente montura. Detectaria que el origen de la capacidad no forme parte
     * de su identidad (equals), lo que mezclaria fuentes distintas.
     */
    @Test
    public void elVueloPuedeVenirDeUnConjuroODeUnaMontura() {
        Capacidad vuelo = estado.getCapacidad("Vuelo");
        Objeto conjuro = estado.getObjeto("ConjuroVuelo");
        Personaje pegaso = estado.getPersonaje("Pegaso");
        // Fuente 1: conjuro poseido
        estado.otorgarObjeto(perseo, conjuro);
        estado.concederCapacidad(CapacidadDisponible.deConjuro(perseo, vuelo, conjuro));
        // Fuente 2: montura capturada (apresa_a)
        estado.apresar(perseo, pegaso);
        estado.concederCapacidad(CapacidadDisponible.deMontura(perseo, vuelo, pegaso));
        // Act: se libera la montura
        estado.liberar(perseo, pegaso);
        assertEquals("Liberar la montura retira solo esa fuente", 1, estado.fuentesDe(perseo, vuelo).size());
        assertTrue(estado.tieneCapacidad(perseo, vuelo));
    }

    /**
     * Prueba 7: Ceto apresa a Andromeda (apresa_a / esta_preso_por). Cambia estado_libertad y crea el
     * hecho Cautiverio. Papel en el mito: es la situacion que Perseo debe resolver.
     */
    // Prueba 7
    @Test
    public void cetoApresaAAndromeda() {
        Personaje ceto = estado.getPersonaje("Ceto");
        Personaje andromeda = estado.getPersonaje("Andromeda");
        estado.apresar(ceto, andromeda);
        assertEquals(EstadoLibertad.PRESO, andromeda.getEstadoLibertad());
        assertSame(ceto, estado.captorDe(andromeda));
        // Cautiverio con equals por valor: las reglas podran buscarlo sin conservar la referencia.
        assertTrue(estado.getCautiverios().contains(new Cautiverio(ceto, andromeda)));
    }

    /**
     * Prueba 8: tras eliminar a Ceto, Perseo libera a Andromeda (libera_a): queda libre, sin captor, sin
     * cautiverios y con el hecho Liberacion que responde a la pregunta del escenario. Liberar dos veces es
     * un error. Detectaria cautiverios que sobrevivan a la liberacion o liberaciones repetidas en bucle.
     */
    // Prueba 8
    @Test
    public void liberarAAndromedaQuitaElCautiverio() {
        Personaje ceto = estado.getPersonaje("Ceto");
        Personaje andromeda = estado.getPersonaje("Andromeda");
        estado.apresar(ceto, andromeda);
        estado.matar(perseo, ceto);
        // Act
        estado.liberar(perseo, andromeda);
        // Assert
        assertEquals(EstadoLibertad.LIBRE, andromeda.getEstadoLibertad());
        assertNull(estado.captorDe(andromeda));
        assertTrue(estado.getCautiverios().isEmpty());
        assertTrue(estado.haLiberado("Perseo", "Andromeda"));
        try {
            estado.liberar(perseo, andromeda);
            fail("No se libera a quien ya esta libre");
        } catch (IllegalStateException esperado) {
            // correcto
        }
    }

    /**
     * Prueba 9: un evento guarda actor, accion, destinatario, objeto, justificacion, regla y las causas
     * (orden de eventos previos). Es lo que permite contar la historia como secuencia causal (el favor de
     * Hades justifica la entrega del casco). Detectaria numeracion incorrecta o causas perdidas.
     */
    // Prueba 9
    @Test
    public void elEventoGuardaActorAccionYJustificacion() {
        RegistroEventos registro = new RegistroEventos();
        // Arrange/act: evento 1 (favor) y evento 2 (entrega) que cita al 1 como causa.
        EventoMitologico favor = registro.registrar(EventoMitologico.nuevo(TipoAccion.OTORGAR_FAVOR, hades)
                .destinatario(perseo).justificacion("condicion inicial"));
        EventoMitologico entrega = registro.registrar(EventoMitologico.nuevo(TipoAccion.OBTENER_OBJETO, perseo)
                .destinatario(hades).objeto(casco)
                .justificacion("tener el favor de Hades").regla("R-prueba").causas(favor));
        assertEquals(2, entrega.getOrden());
        assertSame(perseo, entrega.getActor());
        assertEquals(TipoAccion.OBTENER_OBJETO, entrega.getAccion());
        assertEquals("tener el favor de Hades", entrega.getJustificacion());
        assertEquals("R-prueba", entrega.getRegla());
        assertEquals(casco, entrega.getObjeto());
        assertEquals(1, (int) entrega.getCausas().get(0));
        assertEquals(2, registro.size());
    }

    /**
     * Prueba 10: dos escenarios sobre el mismo dominio no se contaminan: lo que pasa en uno (Perseo con
     * el casco, Medusa muerta, eventos) no aparece en el otro ni altera el dominio; y la copia profunda
     * de un estado es independiente. Detectaria personajes compartidos por referencia, lo que haria que
     * ejecutar un fichero de entrada cambiase el resultado del siguiente.
     */
    // Prueba 10
    @Test
    public void dosEscenariosNoSeContaminan() {
        Objetivo liberar = new Objetivo(TipoObjetivo.LIBERAR, "Perseo", "Andromeda");
        EscenarioMitologico uno = new EscenarioMitologico("F2-1", "?", liberar, dominio);
        EscenarioMitologico dos = new EscenarioMitologico("F2-2", "?", liberar, dominio);
        // Act sobre el escenario uno
        EstadoAventura e1 = uno.getEstado();
        e1.transferirObjeto(e1.getPersonaje("Hades"), e1.getPersonaje("Perseo"), e1.getObjeto("CascoHades"));
        e1.matar(e1.getPersonaje("Perseo"), e1.getPersonaje("Medusa"));
        uno.getRegistro().registrar(EventoMitologico.nuevo(TipoAccion.MATAR, e1.getPersonaje("Perseo")));

        // Assert sobre el escenario dos y el dominio
        EstadoAventura e2 = dos.getEstado();
        assertTrue(e2.posee("Hades", "CascoHades"));
        assertEquals(EstadoVital.VIVO, e2.getPersonaje("Medusa").getEstadoVital());
        assertEquals(0, dos.getRegistro().size());
        assertEquals("El dominio tampoco cambia", EstadoVital.VIVO, dominio.getPersonaje("Medusa").getEstadoVital());

        // La copia profunda tampoco comparte personajes con el original.
        EstadoAventura copia = e1.copia();
        copia.morir(copia.getPersonaje("Perseo"));
        assertEquals(EstadoVital.VIVO, e1.getPersonaje("Perseo").getEstadoVital());
    }

    /**
     * Prueba 11: los tipos se guardan como conjunto (Perseo: HEROE y SEMIDIOS) y no se contagian entre
     * personajes (Hades no es Heroe). Detectaria un conjunto de tipos compartido entre instancias.
     */
    // Prueba 11
    @Test
    public void unPersonajePuedeTenerVariasCategorias() {
        assertTrue(perseo.isHeroe() && perseo.isSemidios());
        assertEquals(EnumSet.of(TipoPersonaje.HEROE, TipoPersonaje.SEMIDIOS), perseo.getTipos());
        assertFalse(estado.getPersonaje("Hades").isHeroe());
    }

    /**
     * El objetivo ("Puede Perseo liberar a Andromeda?") se evalua mirando solo los hechos: PENDIENTE
     * antes de evaluar, NO_CONSEGUIDO sin hecho Liberacion y CONSEGUIDO cuando existe. Detectaria que la
     * respuesta dependa de algo distinto del estado final que dejan las reglas.
     */
    @Test
    public void elObjetivoSeEvaluaSobreLosHechos() {
        EscenarioMitologico e = new EscenarioMitologico("F1-1", "Puede Perseo liberar a Andromeda",
                new Objetivo(TipoObjetivo.LIBERAR, "Perseo", "Andromeda"), dominio);
        assertEquals(ResultadoMision.PENDIENTE, e.getResultado());
        assertEquals(ResultadoMision.NO_CONSEGUIDO, e.evaluarResultado());
        // Arrange: se fuerza la secuencia minima apresar -> liberar
        EstadoAventura s = e.getEstado();
        s.apresar(s.getPersonaje("Ceto"), s.getPersonaje("Andromeda"));
        s.liberar(s.getPersonaje("Perseo"), s.getPersonaje("Andromeda"));
        assertEquals(ResultadoMision.CONSEGUIDO, e.evaluarResultado());
    }

    /**
     * Relacion con la ontologia: el rango de tiene_favor es Dios. Medusa (Criatura) no puede dar favor;
     * detectaria que una condicion inicial mal escrita introdujese un favor imposible.
     */
    @Test(expected = IllegalStateException.class)
    public void elFavorSoloPuedeVenirDeUnDios() {
        estado.agregarFavor(perseo, estado.getPersonaje("Medusa"));
    }

    /** Un personaje muerto no puede ser actor de ningun suceso (aqui, apresar a Andromeda). */
    @Test(expected = IllegalStateException.class)
    public void unMuertoNoPuedeActuar() {
        estado.morir(perseo);
        estado.apresar(perseo, estado.getPersonaje("Andromeda"));
    }

    /**
     * Papel en el mito: Atenea entrega a Perseo el EscudoBronce y este se convierte en EscudoEspejo (que
     * proporciona Reflejo, necesario para mirar a Medusa sin petrificarse). Tras transformarlo, Perseo
     * posee el espejo y el bronce deja de tener dueno. Detectaria que coexistan ambos escudos.
     */
    @Test
    public void transformarElEscudoDeBronceEnEscudoEspejo() {
        Personaje atenea = estado.getPersonaje("Atenea");
        Objeto bronce = estado.getObjeto("EscudoBronce");
        Objeto espejo = estado.getObjeto("EscudoEspejo");
        estado.transferirObjeto(atenea, perseo, bronce);
        // Act
        estado.transformarObjeto(perseo, bronce, espejo);
        assertTrue(estado.posee(perseo, espejo));
        assertNull(estado.poseedorDe(bronce));
    }
}
