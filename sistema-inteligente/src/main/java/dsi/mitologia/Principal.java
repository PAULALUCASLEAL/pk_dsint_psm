package dsi.mitologia;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import dsi.mitologia.entrada.LectorEscenario;
import dsi.mitologia.escenario.DominioMitologico;
import dsi.mitologia.escenario.EscenarioMitologico;
import dsi.mitologia.escenario.Objetivo;
import dsi.mitologia.escenario.PreparadorEscenario;
import dsi.mitologia.escenario.ResultadoMision;
import dsi.mitologia.eventos.EventoMitologico;
import dsi.mitologia.eventos.TipoAccion;
import dsi.mitologia.motor.MotorReglas;

/**
 * Punto de entrada del sistema inteligente DSI_Mitologia.
 *
 * ESTADO: las reglas reales de la fase 1.b estan en src/main/resources/reglas. Mientras no exista el
 * lector de EscenarioFx-y.txt, {@link #main} ejecuta los dos escenarios del enunciado (pag. 11-12)
 * construidos con {@link PreparadorEscenario} y muestra la respuesta por consola.
 *
 * <p>Funcion prevista (fase 1.b): por cada fichero de escenario del directorio {@code escenarios}
 * (EscenarioFx-y.txt, donde x es la fase y y el numero de escenario) se leen la pregunta ("Puede
 * Perseo liberar a Andromeda?") y las condiciones ("Perseo tiene el favor de Atenea", "Atenea no tiene
 * Escudo de Bronce"...), se insertan como hechos en una sesion Drools nueva y se ejecutan las reglas.
 * El resultado se escribe en {@code soluciones/RespuestaFx_y.txt} (mismo sufijo que la entrada): primero
 * la respuesta ("Si, ... puede ..." o "No, ...") y despues la secuencia causal que la justifica (Ceto
 * queda libre, Andromeda presa, Perseo obtiene los objetos, mata a Medusa, petrifica a Ceto...).</p>
 *
 * <p>Decision de diseno: Java solo procesa los datos de entrada y escribe la salida; no infiere nada.
 * Toda la inferencia se hace con reglas Drools en encadenamiento hacia delante (Drools resuelve mal el
 * encadenamiento hacia atras), usando agenda-groups para mostrar primero el estado inicial y despues la
 * historia. Cada escenario usa su propia {@link KieSession} para que los escenarios sean independientes.</p>
 *
 * <p>Pendiente: lector de EscenarioFx-y.txt, insercion del conocimiento por defecto y de las
 * condiciones y escritor de RespuestaFx_y.txt. La regla temporal ping -> pong se ha trasladado a los
 * recursos de test (PruebaIntegracionDroolsTest).</p>
 */
public class Principal {

    /** Ruta del OWL relativa a sistema-inteligente (desde donde se lanza mvn exec:java). */
    static final String OWL = "../ontologia/mitologia.owl";

    /**
     * Arranca el sistema.
     *
     * @param args opcionalmente, {@code args[0]} = directorio de escenarios y {@code args[1]} =
     *             directorio de soluciones; si faltan se usan los nombres por defecto del enunciado
     */
    public static void main(String[] args) throws IOException {

        String escenarios = "escenarios";
        String soluciones = "soluciones";

        if (args.length >= 1) {
            escenarios = args[0];
        }

        if (args.length >= 2) {
            soluciones = args[1];
        }

        File directorioEntrada = new File(escenarios);
        File directorioSalida = new File(soluciones);

        if (!directorioEntrada.isDirectory()) {
            throw new IOException(
                    "No existe el directorio de escenarios: "
                    + directorioEntrada.getAbsolutePath());
        }

        File[] archivos = directorioEntrada.listFiles(
                archivo -> archivo.isFile()
                        && archivo.getName().matches(
                        		"EscenarioF[12]-[1-8]\\.txt"));

        if (archivos == null) {
            throw new IOException(
                    "No se puede leer el directorio de escenarios");
        }

        if (archivos.length == 0) {
            System.out.println(
                    "No hay archivos EscenarioF2-1.txt a EscenarioF2-8.txt en "
                    + directorioEntrada.getAbsolutePath());
            return;
        }

        Arrays.sort(archivos, Comparator.comparing(File::getName));

        Files.createDirectories(directorioSalida.toPath());

        DominioMitologico dominio =
                PreparadorEscenario.cargarDominio(new File(OWL));

        KieContainer contenedor =
                KieServices.Factory.get().getKieClasspathContainer();

        LectorEscenario lector = new LectorEscenario();

        for (File archivo : archivos) {

            System.out.println("\n*** " + archivo.getName() + " ***");

            String entrada = new String(
                    Files.readAllBytes(archivo.toPath()),
                    StandardCharsets.UTF_8);

            System.out.println(entrada);
            System.out.println("*** Fin Descripcion Escenario ***\n");

            EscenarioMitologico escenario = lector.leer(archivo, dominio);

            ejecutar(contenedor, escenario);

            String textoRespuesta = respuesta(escenario);

            String nombreRespuesta = archivo.getName()
                    .replaceFirst("EscenarioF([12])-", "RespuestaF$1_");

            File archivoRespuesta =
                    new File(directorioSalida, nombreRespuesta);

            Files.write(
                    archivoRespuesta.toPath(),
                    textoRespuesta.getBytes(StandardCharsets.UTF_8));

            System.out.println(textoRespuesta);
            System.out.println(
                    "Respuesta guardada en: "
                    + archivoRespuesta.getAbsolutePath());

            System.out.println("*** Fin Escenario ***\n");
        }
    }

    /** Ejecuta un escenario en una sesion nueva, que se libera siempre (escenarios independientes). */
    static void ejecutar(KieContainer contenedor, EscenarioMitologico esc) {
        KieSession sesion = contenedor.newKieSession("mitologiaSesion");
        try {
            MotorReglas.ejecutar(sesion, esc);
        } finally {
            sesion.dispose();
        }
    }

    /**
     * Texto de respuesta: pregunta, condiciones, "Si/No" y la secuencia de acontecimientos en el orden
     * en que las reglas los registraron (respeta el orden parcial causal). Solo da formato a los eventos;
     * no deduce nada.
     */
    static String respuesta(EscenarioMitologico esc) {
        StringBuilder sb = new StringBuilder();
        sb.append("Escenario ").append(esc.getId()).append(": ").append(esc.getPregunta()).append('\n');
        sb.append("Condiciones:\n");
        for (String c : esc.getCondiciones()) sb.append("  ").append(c).append('\n');
        Objetivo o = esc.getObjetivo();
        if (esc.getResultado() == ResultadoMision.CONSEGUIDO) {
            sb.append("Si, ").append(o.getProtagonistaId()).append(" puede liberar a ").append(o.getDestinoId())
              .append(" debido a que:\n");
        } else {
            sb.append("No, no se puede demostrar que ").append(o.getProtagonistaId()).append(" pueda liberar a ")
              .append(o.getDestinoId()).append(". Lo que ocurre es:\n");
        }
        for (EventoMitologico e : esc.getRegistro().getEventos()) {
            boolean explicativo = e.getAccion() == TipoAccion.INFORMACION && e.getRegla() != null
                    && (e.getRegla().startsWith("R2") || e.getRegla().startsWith("R31 Obstaculo"));
            if ((e.getAccion() != TipoAccion.INFORMACION && e.getAccion() != TipoAccion.CUMPLIR_OBJETIVO) || explicativo) {
                sb.append("  ").append(e.getDescripcion()).append('.').append('\n');
            }
        }
        return sb.toString();
    }
}
