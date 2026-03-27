package com.mycompany.arbolsintactico;

import java.util.List;

/**
 * Nodo de un parse tree: no terminal (regla de gramática) o terminal (token).
 */
public abstract class NodoParseo {

    /**
     * Etiqueta mostrada al volcar el árbol (nombre de la regla o token con lexema).
     */
    public abstract String etiqueta();

    public abstract List<NodoParseo> hijos();

    public final String imprimirArbol() {
        StringBuilder sb = new StringBuilder();
        imprimir(sb, 0);
        return sb.toString();
    }

    private void imprimir(StringBuilder sb, int nivel) {
        sb.append("  ".repeat(nivel));
        sb.append(etiqueta()).append(System.lineSeparator());
        for (NodoParseo h : hijos()) {
            h.imprimir(sb, nivel + 1);
        }
    }
}
