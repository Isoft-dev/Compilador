package com.mycompany.arbolsintactico;

import java.util.List;

/**
 * Regla: bloque → "{" stmt_expr "}"
 */
public final class NodoBloque extends NodoParseo {

    private final NodoTerminal llaveIzq;
    private final NodoSentenciaExpresion sentencia;
    private final NodoTerminal llaveDer;

    public NodoBloque(NodoTerminal llaveIzq, NodoSentenciaExpresion sentencia, NodoTerminal llaveDer) {
        this.llaveIzq = llaveIzq;
        this.sentencia = sentencia;
        this.llaveDer = llaveDer;
    }

    @Override
    public String etiqueta() {
        return "bloque";
    }

    @Override
    public List<NodoParseo> hijos() {
        return List.of(llaveIzq, sentencia, llaveDer);
    }
}
