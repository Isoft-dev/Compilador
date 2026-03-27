/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.arbolsintactico;

import java.io.IOException;
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
        Scanner s = new Scanner(System.in);
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
        System.out.println("=== Análisis con modo pánico (lexer + parser) ===");
        String fuentePrueba = cargarFuentePruebaErrores(args);
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

        s.close();
    }

    /**
     * Carga {@code casos_prueba_errores.txt} desde el directorio de trabajo o desde la ruta en {@code args[0]}.
     */
    private static String cargarFuentePruebaErrores(String[] args) {
        Path ruta = Path.of(args.length > 0 ? args[0] : "casos_prueba_errores.txt");
        try {
            return Files.readString(ruta);
        } catch (IOException e) {
            System.err.println("No se pudo leer " + ruta.toAbsolutePath() + ": " + e.getMessage());
            System.err.println("Usando fuente embebida con los mismos tres errores.");
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
}
