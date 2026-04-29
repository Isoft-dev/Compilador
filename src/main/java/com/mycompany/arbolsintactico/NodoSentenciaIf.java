package com.mycompany.arbolsintactico;

import java.util.List;

/**
 * Regla: stmt_if → "if" "(" expr ")" bloque resto_condicional
 */
public final class NodoSentenciaIf extends NodoParseo {

    private final NodoTerminal kwIf;
    private final NodoTerminal parentesisIzq;
    private final NodoExpresion condicion;
    private final NodoTerminal parentesisDer;
    private final NodoBloque cuerpo;
    private final NodoRestoCondicional resto;

    public NodoSentenciaIf(
            NodoTerminal kwIf,
            NodoTerminal parentesisIzq,
            NodoExpresion condicion,
            NodoTerminal parentesisDer,
            NodoBloque cuerpo,
            NodoRestoCondicional resto) {
        this.kwIf = kwIf;
        this.parentesisIzq = parentesisIzq;
        this.condicion = condicion;
        this.parentesisDer = parentesisDer;
        this.cuerpo = cuerpo;
        this.resto = resto;
    }

    public NodoExpresion getCondicion() {
        return condicion;
    }

    public NodoBloque getCuerpo() {
        return cuerpo;
    }

    public NodoRestoCondicional getResto() {
        return resto;
    }

    @Override
    public String etiqueta() {
        return "stmt_if";
    }

    @Override
    public List<NodoParseo> hijos() {
        return List.of(kwIf, parentesisIzq, condicion, parentesisDer, cuerpo, resto);
    }
}
