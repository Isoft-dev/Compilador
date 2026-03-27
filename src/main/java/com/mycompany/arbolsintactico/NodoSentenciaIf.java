package com.mycompany.arbolsintactico;

import java.util.List;

/**
 * Regla: stmt_if → "if" "(" expr_rel ")" bloque resto_condicional
 */
public final class NodoSentenciaIf extends NodoParseo {

    private final NodoTerminal kwIf;
    private final NodoTerminal parentesisIzq;
    private final NodoExpresionRelacional condicion;
    private final NodoTerminal parentesisDer;
    private final NodoBloque cuerpo;
    private final NodoRestoCondicional resto;

    public NodoSentenciaIf(
            NodoTerminal kwIf,
            NodoTerminal parentesisIzq,
            NodoExpresionRelacional condicion,
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

    @Override
    public String etiqueta() {
        return "stmt_if";
    }

    @Override
    public List<NodoParseo> hijos() {
        return List.of(kwIf, parentesisIzq, condicion, parentesisDer, cuerpo, resto);
    }
}
