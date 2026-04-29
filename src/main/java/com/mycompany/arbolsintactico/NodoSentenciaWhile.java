package com.mycompany.arbolsintactico;

import java.util.List;

/**
 * {@code while "(" expr ")" bloque}
 */
public final class NodoSentenciaWhile extends NodoParseo {

    private final NodoTerminal kwWhile;
    private final NodoTerminal parentesisIzq;
    private final NodoExpresion condicion;
    private final NodoTerminal parentesisDer;
    private final NodoBloque cuerpo;

    public NodoSentenciaWhile(
            NodoTerminal kwWhile,
            NodoTerminal parentesisIzq,
            NodoExpresion condicion,
            NodoTerminal parentesisDer,
            NodoBloque cuerpo) {
        this.kwWhile = kwWhile;
        this.parentesisIzq = parentesisIzq;
        this.condicion = condicion;
        this.parentesisDer = parentesisDer;
        this.cuerpo = cuerpo;
    }

    public NodoExpresion getCondicion() {
        return condicion;
    }

    public NodoBloque getCuerpo() {
        return cuerpo;
    }

    @Override
    public String etiqueta() {
        return "stmt_while";
    }

    @Override
    public List<NodoParseo> hijos() {
        return List.of(kwWhile, parentesisIzq, condicion, parentesisDer, cuerpo);
    }
}
