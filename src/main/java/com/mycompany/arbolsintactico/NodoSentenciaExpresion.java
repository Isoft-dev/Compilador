package com.mycompany.arbolsintactico;

import java.util.List;

/**
 * Regla: stmt_expr → invocacion ";"
 */
public final class NodoSentenciaExpresion extends NodoParseo {

    private final NodoInvocacionPrintln invocacion;
    private final NodoTerminal puntoYComa;

    public NodoSentenciaExpresion(NodoInvocacionPrintln invocacion, NodoTerminal puntoYComa) {
        this.invocacion = invocacion;
        this.puntoYComa = puntoYComa;
    }

    @Override
    public String etiqueta() {
        return "stmt_expr";
    }

    @Override
    public List<NodoParseo> hijos() {
        return List.of(invocacion, puntoYComa);
    }
}
