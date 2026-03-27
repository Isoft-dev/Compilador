package com.mycompany.arbolsintactico;

import java.util.Collections;
import java.util.List;

/**
 * Hoja del parse tree: token con tipo léxico y lexema exacto del fuente.
 */
public final class NodoTerminal extends NodoParseo {

    private final TipoToken tipo;
    private final String lexema;

    public NodoTerminal(TipoToken tipo, String lexema) {
        this.tipo = tipo;
        this.lexema = lexema;
    }

    public TipoToken getTipo() {
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }

    @Override
    public String etiqueta() {
        return tipo.name() + " \"" + lexema + "\"";
    }

    @Override
    public List<NodoParseo> hijos() {
        return Collections.emptyList();
    }
}
