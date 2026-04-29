package com.mycompany.arbolsintactico;

/**
 * Entrada de la tabla de símbolos (nombre, tipo declarado, nivel de ámbito, línea).
 */
public final class Symbol {

    private final String nombre;
    private final TipoSemantico tipoDato;
    private final int nivelAmbito;
    private final int lineaDeclaracion;

    public Symbol(String nombre, TipoSemantico tipoDato, int nivelAmbito, int lineaDeclaracion) {
        this.nombre = nombre;
        this.tipoDato = tipoDato;
        this.nivelAmbito = nivelAmbito;
        this.lineaDeclaracion = lineaDeclaracion;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoSemantico getTipoDato() {
        return tipoDato;
    }

    public int getNivelAmbito() {
        return nivelAmbito;
    }

    public int getLineaDeclaracion() {
        return lineaDeclaracion;
    }

    @Override
    public String toString() {
        return nombre + " : " + tipoDato.name().toLowerCase()
                + " (ámbito " + nivelAmbito + ", línea " + lineaDeclaracion + ")";
    }
}
