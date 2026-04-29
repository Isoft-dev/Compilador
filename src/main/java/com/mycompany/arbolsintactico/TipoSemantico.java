package com.mycompany.arbolsintactico;

/**
 * Tipos del lenguaje Mini-Lang para inferencia y comprobación semántica.
 */
public enum TipoSemantico {
    INT,
    FLOAT,
    BOOLEAN,
    /** Expresión inválida o variable no encontrada (propaga error). */
    ERROR
}
