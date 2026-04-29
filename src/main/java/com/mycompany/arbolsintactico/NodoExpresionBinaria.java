package com.mycompany.arbolsintactico;

import java.util.List;

/**
 * Expresión binaria: izquierda operador derecha.
 */
public final class NodoExpresionBinaria extends NodoExpresion {

    private final NodoExpresion izquierda;
    private final NodoTerminal operador;
    private final NodoExpresion derecha;

    public NodoExpresionBinaria(NodoExpresion izquierda, NodoTerminal operador, NodoExpresion derecha) {
        this.izquierda = izquierda;
        this.operador = operador;
        this.derecha = derecha;
    }

    public NodoExpresion getIzquierda() {
        return izquierda;
    }

    public NodoTerminal getOperador() {
        return operador;
    }

    public NodoExpresion getDerecha() {
        return derecha;
    }

    @Override
    public String etiqueta() {
        return "expr_bin";
    }

    @Override
    public List<NodoParseo> hijos() {
        return List.of(izquierda, operador, derecha);
    }
}
