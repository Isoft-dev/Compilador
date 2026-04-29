package com.mycompany.arbolsintactico;

import java.util.ArrayList;
import java.util.List;

/**
 * Declaración: {@code tipo identificador ("=" expr)? ";"}
 */
public final class NodoDeclaracion extends NodoParseo {

    private final NodoTerminal tipo;
    private final NodoTerminal identificador;
    /** Puede ser {@code null} si no hay inicializador. */
    private final NodoExpresion inicializador;
    private final NodoTerminal puntoYComa;

    public NodoDeclaracion(
            NodoTerminal tipo,
            NodoTerminal identificador,
            NodoExpresion inicializador,
            NodoTerminal puntoYComa) {
        this.tipo = tipo;
        this.identificador = identificador;
        this.inicializador = inicializador;
        this.puntoYComa = puntoYComa;
    }

    public NodoTerminal getTipo() {
        return tipo;
    }

    public NodoTerminal getIdentificador() {
        return identificador;
    }

    public NodoExpresion getInicializador() {
        return inicializador;
    }

    public NodoTerminal getPuntoYComa() {
        return puntoYComa;
    }

    @Override
    public String etiqueta() {
        return "decl";
    }

    @Override
    public List<NodoParseo> hijos() {
        List<NodoParseo> h = new ArrayList<>();
        h.add(tipo);
        h.add(identificador);
        if (inicializador != null) {
            h.add(inicializador);
        }
        h.add(puntoYComa);
        return h;
    }
}
