/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.arbolsintactico;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author vpmen
 */
public class ArbolSintactico {

    public static void main(String[] args) {
        configurarConsolaUtf8();
        Scanner s = new Scanner(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        Instant timestamp = Instant.now();

        System.out.println("VIVIANA ISAPAMELA MENDEZ CHÉ");
        System.out.println("5190 - 23 - 3597");
        System.out.println(timestamp);
/*
        NodoParseo raizSimple = FabricaArbolIfEjemplo.crearArbolIfMayorEdad();
        System.out.println("--- Parse tree (fábrica): if (edad >= 18) { ... } ---");
        System.out.println(raizSimple.imprimirArbol());

        NodoParseo raizCompleto = FabricaArbolIfEjemplo.crearArbolIfElseIfElseEdad();
        System.out.println("--- Parse tree (fábrica): if / else if / else (>, ==) ---");
        System.out.println(raizCompleto.imprimirArbol());
*/
        System.out.println();
        String rutaArchivo = resolverRutaFuente(args, s);
        System.out.println("Archivo fuente: " + Path.of(rutaArchivo).toAbsolutePath());
        System.out.println("=== Análisis léxico + sintáctico (modo pánico) + semántico ===");
        String fuentePrueba = cargarFuenteDesdeArchivo(rutaArchivo);
        AnalizadorLexico lex = new AnalizadorLexico(fuentePrueba);
        List<Token> tokens = lex.tokenizar();
        for (String adv : lex.getAdvertenciasLexico()) {
            System.out.println("Advertencia léxica: " + adv);
        }

        AnalizadorSintactico parser = new AnalizadorSintactico(tokens);
        NodoPrograma programa = parser.parsePrograma();

        System.out.println("--- Log de errores sintácticos ---");
        List<String> errores = parser.getLogErrores();
        if (errores.isEmpty()) {
            System.out.println("(sin errores)");
        } else {
            for (String e : errores) {
                System.out.println(e);
            }
        }

        System.out.println("--- Árbol del fragmento recuperado ---");
        System.out.println(programa.imprimirArbol());

        System.out.println();
        System.out.println("=== Análisis semántico (Mini-Lang) ===");
        AnalizadorSemantico semantico = new AnalizadorSemantico();
        semantico.analizar(programa);

        System.out.println("--- Tabla de símbolos (declaraciones) ---");
        if (semantico.getDeclaraciones().isEmpty()) {
            System.out.println("(sin declaraciones de variables)");
        } else {
            for (Symbol sym : semantico.getDeclaraciones()) {
                System.out.println(sym);
            }
        }

        System.out.println("--- Errores semánticos ---");
        List<String> errSem = semantico.getErrores();
        if (errSem.isEmpty()) {
            System.out.println("Compilación Semántica Exitosa");
        } else {
            for (String e : errSem) {
                System.out.println(e);
            }
        }

        s.close();
    }

    /**
     * Resuelve qué archivo analizar: argumento de línea de comandos, o texto escrito en consola.
     * Si no hay argumentos y el usuario deja la línea vacía, usa {@code casos_prueba_errores.txt}
     * relativo al directorio de trabajo.
     */
    private static String resolverRutaFuente(String[] args, Scanner consola) {
        if (args.length > 0 && !args[0].isBlank()) {
            return args[0].trim();
        }
        System.out.print("Ruta del archivo fuente a analizar (Enter = casos_prueba_errores.txt): ");
        System.out.flush();
        String linea = consola.nextLine().trim();
        if (linea.isEmpty()) {
            return "casos_prueba_errores.txt";
        }
        return linea;
    }

    /**
     * Lee el contenido del archivo UTF-8. Si falla, usa una fuente embebida (solo respaldo).
     */
    private static String cargarFuenteDesdeArchivo(String rutaStr) {
        Path ruta = Path.of(rutaStr);
        try {
            return Files.readString(ruta);
        } catch (IOException e) {
            System.err.println("No se pudo leer " + ruta.toAbsolutePath() + ": " + e.getMessage());
            System.err.println("Usando fuente embebida con los mismos cuatro casos de error sintáctico.");
            return """
                    if (edad >= 18 {
                      System.out.println("error falta parentesis");
                    }
                    if (x > 0) {
                      System.out.println("error falta punto y coma")
                    }
                    if (y) {
                      System.out.println("error expresion incompleta");
                    }
                    if (z == 0) {
                      System.out.println("fragmento correcto");
                    }
                    """;
        }
    }

    /**
     * En Windows la consola suele usar otra página de códigos que UTF-8; al enviar ya en UTF-8
     * y usar codificación de archivo/consola coherente se muestran bien tildes y la ñ.
     */
    private static void configurarConsolaUtf8() {
        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
            // mantener System.out/err por defecto
        }
    }
}
