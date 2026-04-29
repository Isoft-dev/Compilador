package com.mycompany.arbolsintactico;

import java.util.Collections;
import java.util.List;

/**
 * Hoja del parse tree: token con tipo léxico y lexema exacto del fuente.
 */
public final class NodoTerminal extends NodoParseo {

    private final TipoToken tipo;
    private final String lexema;
    /** Línea en el fuente (-1 si no se registró). */
    private final int linea;

    public NodoTerminal(TipoToken tipo, String lexema) {
        this(tipo, lexema, -1);
    }

    public NodoTerminal(TipoToken tipo, String lexema, int linea) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linea = linea;
    }

    public TipoToken getTipo() {
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }

    /**
     * Línea del token en el fuente, o -1 si no está disponible.
     */
    public int getLinea() {
        return linea;
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
