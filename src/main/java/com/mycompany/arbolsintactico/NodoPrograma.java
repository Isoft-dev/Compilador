package com.mycompany.arbolsintactico;

import java.util.List;

/**
 * Raíz de un programa: secuencia de sentencias {@code if} parseadas.
 */
public final class NodoPrograma extends NodoParseo {

    private final List<NodoParseo> sentencias;

    public NodoPrograma(List<NodoParseo> sentencias) {
        this.sentencias = List.copyOf(sentencias);
    }

    @Override
    public String etiqueta() {
        return "programa";
    }

    @Override
    public List<NodoParseo> hijos() {
        return sentencias;
    }
}
