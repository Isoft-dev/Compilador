package com.mycompany.arbolsintactico;

import java.util.List;

/**
 * Construye parse trees para fragmentos con {@code if}, {@code else if} y {@code else}.
 */
public final class FabricaArbolIfEjemplo {

    private FabricaArbolIfEjemplo() {
    }

    private static NodoTerminal t(TipoToken tipo, String lexema) {
        return new NodoTerminal(tipo, lexema, 1);
    }

    private static NodoExpresion exprRel(TipoToken opTipo, String opLex, String id, String entero) {
        return new NodoExpresionBinaria(
                new NodoExpresionAtomo(t(TipoToken.IDENTIFICADOR, id)),
                t(opTipo, opLex),
                new NodoExpresionAtomo(t(TipoToken.LITERAL_ENTERO, entero)));
    }

    private static NodoBloque bloquePrintln(String mensaje) {
        NodoInvocacionPrintln inv = new NodoInvocacionPrintln(
                t(TipoToken.METODO_PRINTLN, "System.out.println"),
                t(TipoToken.PARENTESIS_IZQ, "("),
                t(TipoToken.LITERAL_CADENA, mensaje),
                t(TipoToken.PARENTESIS_DER, ")"));
        NodoSentenciaExpresion stmt = new NodoSentenciaExpresion(inv, t(TipoToken.PUNTO_Y_COMA, ";"));
        return new NodoBloque(
                t(TipoToken.LLAVE_IZQ, "{"),
                List.of(stmt),
                t(TipoToken.LLAVE_DER, "}"));
    }

    /**
     * Solo {@code if (edad >= 18) { ... }} sin ramas {@code else}.
     */
    public static NodoSentenciaIf crearArbolIfMayorEdad() {
        NodoExpresion cond = exprRel(TipoToken.OPERADOR_MAYOR_IGUAL, ">=", "edad", "18");
        NodoBloque bloque = bloquePrintln("Eres mayor de edad.");
        return new NodoSentenciaIf(
                t(TipoToken.PALABRA_CLAVE_IF, "if"),
                t(TipoToken.PARENTESIS_IZQ, "("),
                cond,
                t(TipoToken.PARENTESIS_DER, ")"),
                bloque,
                new NodoSinResto());
    }

    /**
     * Cadena completa equivalente a if / else if / else sobre edad.
     */
    public static NodoSentenciaIf crearArbolIfElseIfElseEdad() {
        NodoTerminal kwIf = t(TipoToken.PALABRA_CLAVE_IF, "if");
        NodoTerminal parIzq1 = t(TipoToken.PARENTESIS_IZQ, "(");
        NodoExpresion cond1 = exprRel(TipoToken.OPERADOR_MAYOR, ">", "edad", "18");
        NodoTerminal parDer1 = t(TipoToken.PARENTESIS_DER, ")");
        NodoBloque bloque1 = bloquePrintln("Eres mayor de edad.");

        NodoTerminal kwElse2 = t(TipoToken.PALABRA_CLAVE_ELSE, "else");
        NodoTerminal kwIf2 = t(TipoToken.PALABRA_CLAVE_IF, "if");
        NodoTerminal parIzq2 = t(TipoToken.PARENTESIS_IZQ, "(");
        NodoExpresion cond2 = exprRel(TipoToken.OPERADOR_IGUAL_IGUAL, "==", "edad", "18");
        NodoTerminal parDer2 = t(TipoToken.PARENTESIS_DER, ")");
        NodoBloque bloque2 = bloquePrintln("Tienes 18 años.");

        NodoTerminal kwElse3 = t(TipoToken.PALABRA_CLAVE_ELSE, "else");
        NodoBloque bloque3 = bloquePrintln("Eres menor de edad.");

        NodoRestoCondicional restoFinal = new NodoElseFinal(kwElse3, bloque3);
        NodoRestoCondicional restoMedio = new NodoElseIf(
                kwElse2, kwIf2, parIzq2, cond2, parDer2, bloque2, restoFinal);

        return new NodoSentenciaIf(
                kwIf, parIzq1, cond1, parDer1, bloque1, restoMedio);
    }
}
