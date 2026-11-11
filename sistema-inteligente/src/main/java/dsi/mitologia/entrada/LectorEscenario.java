package dsi.mitologia.entrada;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dsi.mitologia.escenario.DominioMitologico;
import dsi.mitologia.escenario.EscenarioMitologico;
import dsi.mitologia.escenario.Objetivo;
import dsi.mitologia.escenario.PreparadorEscenario;
import dsi.mitologia.escenario.TipoObjetivo;

public class LectorEscenario {

	private static final Pattern PREGUNTA = Pattern.compile(
	        "^Puede (.+?) (liberar|matar) a (.+?)\\?$");

    private static final Pattern FAVOR = Pattern.compile(
            "^(.+?) tiene el favor de (.+)$");

    private static final Pattern ENOJO = Pattern.compile(
            "^(.+?) tiene el enojo de (.+)$");

    private static final Pattern NO_TIENE = Pattern.compile(
            "^(.+?) no tiene (.+)$");

    private static final Pattern TIENE = Pattern.compile(
            "^(.+?) tiene (.+)$");

    public EscenarioMitologico leer(
            File archivo, DominioMitologico dominio) throws IOException {

        List<String> lineas = Files.readAllLines(
                archivo.toPath(), StandardCharsets.UTF_8);

        PreparadorEscenario preparador = null;
        boolean cabeceraEncontrada = false;

        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i).trim();

            // Algunos editores añaden esta marca al principio del archivo.
            if (i == 0 && linea.startsWith("\uFEFF")) {
                linea = linea.substring(1).trim();
            }

            if (linea.isEmpty()) {
                continue;
            }

            try {
                if (preparador == null) {
                    Matcher pregunta = PREGUNTA.matcher(linea);

                    if (!pregunta.matches()) {
                        throw new IllegalArgumentException(
                        		"La pregunta debe tener la forma: "
                        		+ "Puede <personaje> liberar a <personaje>? "
                        		+ "o Puede <personaje> matar a <personaje>?");
                    }

                    String protagonista = pregunta.group(1).trim();
                    String accion = pregunta.group(2).trim();
                    String destino = pregunta.group(3).trim();

                    dominio.getPersonaje(protagonista);
                    dominio.getPersonaje(destino);

                    TipoObjetivo tipo;

                    if (accion.equals("matar")) {
                        tipo = TipoObjetivo.MATAR;
                    } else {
                        tipo = TipoObjetivo.LIBERAR;
                    }

                    Objetivo objetivo = new Objetivo(
                            tipo, protagonista, destino);


                    String id = archivo.getName()
                            .replaceFirst("^Escenario", "")
                            .replaceFirst("\\.txt$", "");

                    preparador = new PreparadorEscenario(
                            id, linea, objetivo, dominio);

                } else if (!cabeceraEncontrada) {
                    if (!linea.equals("Condiciones:")) {
                        throw new IllegalArgumentException(
                                "Se esperaba la cabecera Condiciones:");
                    }

                    cabeceraEncontrada = true;

                } else {
                    aplicarCondicion(linea, preparador);
                }

            } catch (IllegalArgumentException | IllegalStateException e) {
                throw new IllegalArgumentException(
                        archivo.getName() + ", linea " + (i + 1)
                        + ": " + e.getMessage(), e);
            }
        }

        if (preparador == null) {
            throw new IllegalArgumentException(
                    archivo.getName() + ": falta la pregunta");
        }

        if (!cabeceraEncontrada) {
            throw new IllegalArgumentException(
                    archivo.getName() + ": falta Condiciones:");
        }

        return preparador.construir();
    }

    private void aplicarCondicion(
            String linea, PreparadorEscenario preparador) {

        Matcher matcher = FAVOR.matcher(linea);

        if (matcher.matches()) {
            preparador.favor(
                    matcher.group(1).trim(), matcher.group(2).trim());
            return;
        }

        matcher = ENOJO.matcher(linea);

        if (matcher.matches()) {
            preparador.enojo(
                    matcher.group(1).trim(), matcher.group(2).trim());
            return;
        }

        matcher = NO_TIENE.matcher(linea);

        if (matcher.matches()) {
            preparador.noTiene(
                    matcher.group(1).trim(), matcher.group(2).trim());
            return;
        }

        matcher = TIENE.matcher(linea);

        if (matcher.matches()) {
            preparador.tiene(
                    matcher.group(1).trim(), matcher.group(2).trim());
            return;
        }

        throw new IllegalArgumentException(
                "Condicion no reconocida: " + linea);
    }
}