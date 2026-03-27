package com.mycompany.arbolsintactico;

import java.util.List;

/**
 * Regla: resto_condicional → "else" "if" "(" expr_rel ")" bloque resto_condicional
 */
public final class NodoElseIf extends NodoRestoCondicional {

    private final NodoTerminal kwElse;
    private final NodoTerminal kwIf;
    private final NodoTerminal parentesisIzq;
    private final NodoExpresionRelacional condicion;
    private final NodoTerminal parentesisDer;
    private final NodoBloque cuerpo;
    private final NodoRestoCondicional resto;

    public NodoElseIf(
            NodoTerminal kwElse,
            NodoTerminal kwIf,
            NodoTerminal parentesisIzq,
            NodoExpresionRelacional condicion,
            NodoTerminal parentesisDer,
            NodoBloque cuerpo,
            NodoRestoCondicional resto) {
        this.kwElse = kwElse;
        this.kwIf = kwIf;
        this.parentesisIzq = parentesisIzq;
        this.condicion = condicion;
        this.parentesisDer = parentesisDer;
        this.cuerpo = cuerpo;
        this.resto = resto;
    }

    @Override
    public String etiqueta() {
        return "else_if";
    }

    @Override
    public List<NodoParseo> hijos() {
        return List.of(kwElse, kwIf, parentesisIzq, condicion, parentesisDer, cuerpo, resto);
    }
}
