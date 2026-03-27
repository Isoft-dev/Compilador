package com.mycompany.arbolsintactico;

import java.util.List;

/**
 * Regla: invocación → METODO_PRINTLN "(" literal_cadena ")"
 */
public final class NodoInvocacionPrintln extends NodoParseo {

    private final NodoTerminal println;
    private final NodoTerminal parentesisIzq;
    private final NodoTerminal literalCadena;
    private final NodoTerminal parentesisDer;

    public NodoInvocacionPrintln(
            NodoTerminal println,
            NodoTerminal parentesisIzq,
            NodoTerminal literalCadena,
            NodoTerminal parentesisDer) {
        this.println = println;
        this.parentesisIzq = parentesisIzq;
        this.literalCadena = literalCadena;
        this.parentesisDer = parentesisDer;
    }

    @Override
    public String etiqueta() {
        return "invocacion_println";
    }

    @Override
    public List<NodoParseo> hijos() {
        return List.of(println, parentesisIzq, literalCadena, parentesisDer);
    }
}
