package com.mycompany.arbolsintactico;

import java.util.List;

/**
 * Asignación: {@code identificador "=" expr ";"}
 */
public final class NodoAsignacion extends NodoParseo {

    private final NodoTerminal identificador;
    private final NodoTerminal operadorAsignacion;
    private final NodoExpresion expresion;
    private final NodoTerminal puntoYComa;

    public NodoAsignacion(
            NodoTerminal identificador,
            NodoTerminal operadorAsignacion,
            NodoExpresion expresion,
            NodoTerminal puntoYComa) {
        this.identificador = identificador;
        this.operadorAsignacion = operadorAsignacion;
        this.expresion = expresion;
        this.puntoYComa = puntoYComa;
    }

    public NodoTerminal getIdentificador() {
        return identificador;
    }

    public NodoExpresion getExpresion() {
        return expresion;
    }

    public NodoTerminal getPuntoYComa() {
        return puntoYComa;
    }

    @Override
    public String etiqueta() {
        return "asignacion";
    }

    @Override
    public List<NodoParseo> hijos() {
        return List.of(identificador, operadorAsignacion, expresion, puntoYComa);
    }
}
