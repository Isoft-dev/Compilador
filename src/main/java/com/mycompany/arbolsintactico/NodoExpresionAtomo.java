package com.mycompany.arbolsintactico;

import java.util.Collections;
import java.util.List;

/**
 * Hoja de expresión: identificador o literal numérico.
 */
public final class NodoExpresionAtomo extends NodoExpresion {

    private final NodoTerminal valor;

    public NodoExpresionAtomo(NodoTerminal valor) {
        this.valor = valor;
    }

    public NodoTerminal getValor() {
        return valor;
    }

    @Override
    public String etiqueta() {
        return "expr_atom";
    }

    @Override
    public List<NodoParseo> hijos() {
        return Collections.singletonList(valor);
    }
}
