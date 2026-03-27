package com.mycompany.arbolsintactico;

/**
 * Clasificación léxica de los símbolos terminales que aparecen en el parse tree
 * del fragmento analizado.
 */
public enum TipoToken {
    /** Fin de entrada (sincronización y fin de análisis). */
    EOF,
    PALABRA_CLAVE_IF,
    PALABRA_CLAVE_ELSE,
    /** Anclas de sincronización (inicio de estructuras). */
    PALABRA_CLAVE_WHILE,
    PALABRA_CLAVE_FOR,
    PALABRA_CLAVE_DEF,
    PALABRA_CLAVE_CLASS,
    /** Delimitador de bloque alternativo (ancla). */
    PALABRA_CLAVE_END,
    PARENTESIS_IZQ,
    PARENTESIS_DER,
    LLAVE_IZQ,
    LLAVE_DER,
    OPERADOR_IGUAL_IGUAL,
    OPERADOR_DISTINTO,
    OPERADOR_MENOR,
    OPERADOR_MAYOR,
    OPERADOR_MENOR_IGUAL,
    OPERADOR_MAYOR_IGUAL,
    IDENTIFICADOR,
    LITERAL_ENTERO,
    LITERAL_CADENA,
    PUNTO_Y_COMA,
    /** Representa la secuencia reconocida como llamada a impresión en consola. */
    METODO_PRINTLN
}
