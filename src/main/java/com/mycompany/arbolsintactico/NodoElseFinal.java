package com.mycompany.arbolsintactico;

import java.util.List;

/**
 * Regla: resto_condicional → "else" bloque
 */
public final class NodoElseFinal extends NodoRestoCondicional {

    private final NodoTerminal kwElse;
    private final NodoBloque cuerpo;

    public NodoElseFinal(NodoTerminal kwElse, NodoBloque cuerpo) {
        this.kwElse = kwElse;
        this.cuerpo = cuerpo;
    }

    public NodoBloque getCuerpo() {
        return cuerpo;
    }

    @Override
    public String etiqueta() {
        return "else";
    }

    @Override
    public List<NodoParseo> hijos() {
        return List.of(kwElse, cuerpo);
    }
}
