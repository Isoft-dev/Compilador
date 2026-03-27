package com.mycompany.arbolsintactico;

import java.util.Collections;
import java.util.List;

/**
 * Regla: resto_condicional → ε (no hay {@code else} ni {@code else if}).
 */
public final class NodoSinResto extends NodoRestoCondicional {

    @Override
    public String etiqueta() {
        return "resto_condicional (vacío)";
    }

    @Override
    public List<NodoParseo> hijos() {
        return Collections.emptyList();
    }
}
