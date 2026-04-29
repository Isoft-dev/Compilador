package com.mycompany.arbolsintactico;

import java.util.List;

/**
 * Bloque: {@code "{" lista_sentencias "}"}
 */
public final class NodoBloque extends NodoParseo {

    private final NodoTerminal llaveIzq;
    private final List<NodoParseo> sentencias;
    private final NodoTerminal llaveDer;

    public NodoBloque(NodoTerminal llaveIzq, List<NodoParseo> sentencias, NodoTerminal llaveDer) {
        this.llaveIzq = llaveIzq;
        this.sentencias = List.copyOf(sentencias);
        this.llaveDer = llaveDer;
    }

    public List<NodoParseo> getSentencias() {
        return sentencias;
    }

    @Override
    public String etiqueta() {
        return "bloque";
    }

    @Override
    public List<NodoParseo> hijos() {
        List<NodoParseo> h = new java.util.ArrayList<>();
        h.add(llaveIzq);
        h.addAll(sentencias);
        h.add(llaveDer);
        return h;
    }
}
