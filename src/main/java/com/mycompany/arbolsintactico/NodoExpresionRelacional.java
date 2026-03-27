package com.mycompany.arbolsintactico;

import java.util.List;

/**
 * Regla: expr_rel → IDENTIFICADOR OP_COMPARACION LITERAL_ENTERO
 * <p>
 * {@code OP_COMPARACION} es uno de: {@code ==}, {@code !=}, {@code <}, {@code >},
 * {@code <=}, {@code >=} (lexema y tipo en {@link NodoTerminal}).
 */
public final class NodoExpresionRelacional extends NodoParseo {

    private final NodoTerminal identificador;
    private final NodoTerminal operador;
    private final NodoTerminal literalEntero;

    public NodoExpresionRelacional(
            NodoTerminal identificador,
            NodoTerminal operador,
            NodoTerminal literalEntero) {
        this.identificador = identificador;
        this.operador = operador;
        this.literalEntero = literalEntero;
    }

    @Override
    public String etiqueta() {
        return "expr_rel";
    }

    @Override
    public List<NodoParseo> hijos() {
        return List.of(identificador, operador, literalEntero);
    }
}
