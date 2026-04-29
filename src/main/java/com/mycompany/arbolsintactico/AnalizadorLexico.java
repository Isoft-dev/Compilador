package com.mycompany.arbolsintactico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tokeniza el fuente según los símbolos usados por el {@link AnalizadorSintactico}.
 */
public final class AnalizadorLexico {

    private static final String SUFIJO_PRINTLN = ".out.println";
    private static final Map<String, TipoToken> PALABRAS_CLAVE = new HashMap<>();

    static {
        PALABRAS_CLAVE.put("if", TipoToken.PALABRA_CLAVE_IF);
        PALABRAS_CLAVE.put("else", TipoToken.PALABRA_CLAVE_ELSE);
        PALABRAS_CLAVE.put("int", TipoToken.PALABRA_CLAVE_INT);
        PALABRAS_CLAVE.put("float", TipoToken.PALABRA_CLAVE_FLOAT);
        PALABRAS_CLAVE.put("while", TipoToken.PALABRA_CLAVE_WHILE);
        PALABRAS_CLAVE.put("for", TipoToken.PALABRA_CLAVE_FOR);
        PALABRAS_CLAVE.put("def", TipoToken.PALABRA_CLAVE_DEF);
        PALABRAS_CLAVE.put("class", TipoToken.PALABRA_CLAVE_CLASS);
        PALABRAS_CLAVE.put("end", TipoToken.PALABRA_CLAVE_END);
    }

    private final String fuente;
    private int pos;
    private int linea;
    private int columna;
    private final List<String> advertenciasLexico = new ArrayList<>();

    public AnalizadorLexico(String fuente) {
        this.fuente = fuente != null ? fuente : "";
        this.pos = 0;
        this.linea = 1;
        this.columna = 1;
    }

    public List<String> getAdvertenciasLexico() {
        return List.copyOf(advertenciasLexico);
    }

    private void avanzarCaracter() {
        if (pos < fuente.length() && fuente.charAt(pos) == '\n') {
            linea++;
            columna = 1;
        } else {
            columna++;
        }
        if (pos < fuente.length()) {
            pos++;
        }
    }

    private void saltarEspaciosYComentarios() {
        while (pos < fuente.length()) {
            char c = fuente.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\r') {
                avanzarCaracter();
                continue;
            }
            if (c == '\n') {
                avanzarCaracter();
                continue;
            }
            if (c == '/' && pos + 1 < fuente.length() && fuente.charAt(pos + 1) == '/') {
                while (pos < fuente.length() && fuente.charAt(pos) != '\n') {
                    avanzarCaracter();
                }
                continue;
            }
            break;
        }
    }

    private Token tokenDesdeLinea(int lineaInicio, int colInicio, TipoToken tipo, String lexema) {
        return new Token(tipo, lexema, lineaInicio, colInicio);
    }

    private Token leerCadenaLiteral() {
        int lineaInicio = linea;
        int colInicio = columna;
        avanzarCaracter(); // "
        StringBuilder sb = new StringBuilder();
        while (pos < fuente.length()) {
            char c = fuente.charAt(pos);
            if (c == '\\' && pos + 1 < fuente.length()) {
                avanzarCaracter();
                sb.append(fuente.charAt(pos));
                avanzarCaracter();
                continue;
            }
            if (c == '"') {
                avanzarCaracter();
                return tokenDesdeLinea(lineaInicio, colInicio, TipoToken.LITERAL_CADENA, sb.toString());
            }
            sb.append(c);
            avanzarCaracter();
        }
        advertenciasLexico.add("Cadena sin cerrar en línea " + lineaInicio);
        return tokenDesdeLinea(lineaInicio, colInicio, TipoToken.LITERAL_CADENA, sb.toString());
    }

    private Token leerNumero() {
        int lineaInicio = linea;
        int colInicio = columna;
        int inicio = pos;
        while (pos < fuente.length() && Character.isDigit(fuente.charAt(pos))) {
            avanzarCaracter();
        }
        if (pos < fuente.length() && fuente.charAt(pos) == '.') {
            avanzarCaracter();
            while (pos < fuente.length() && Character.isDigit(fuente.charAt(pos))) {
                avanzarCaracter();
            }
            String lex = fuente.substring(inicio, pos);
            return tokenDesdeLinea(lineaInicio, colInicio, TipoToken.LITERAL_FLOAT, lex);
        }
        String lex = fuente.substring(inicio, pos);
        return tokenDesdeLinea(lineaInicio, colInicio, TipoToken.LITERAL_ENTERO, lex);
    }

    private Token leerIdentificadorOClave() {
        int lineaInicio = linea;
        int colInicio = columna;
        int inicio = pos;
        while (pos < fuente.length()) {
            char c = fuente.charAt(pos);
            if (Character.isLetterOrDigit(c) || c == '_') {
                avanzarCaracter();
            } else {
                break;
            }
        }
        String lex = fuente.substring(inicio, pos);
        if ("System".equals(lex) && pos < fuente.length() && fuente.startsWith(SUFIJO_PRINTLN, pos)) {
            pos += SUFIJO_PRINTLN.length();
            columna += SUFIJO_PRINTLN.length();
            return tokenDesdeLinea(lineaInicio, colInicio, TipoToken.METODO_PRINTLN, "System.out.println");
        }
        TipoToken tipo = PALABRAS_CLAVE.getOrDefault(lex, TipoToken.IDENTIFICADOR);
        return tokenDesdeLinea(lineaInicio, colInicio, tipo, lex);
    }

    private Token leerOperadorOUnico() {
        int lineaInicio = linea;
        int colInicio = columna;
        char c = fuente.charAt(pos);
        char sig = pos + 1 < fuente.length() ? fuente.charAt(pos + 1) : '\0';

        return switch (c) {
            case '(' -> {
                avanzarCaracter();
                yield tokenDesdeLinea(lineaInicio, colInicio, TipoToken.PARENTESIS_IZQ, "(");
            }
            case ')' -> {
                avanzarCaracter();
                yield tokenDesdeLinea(lineaInicio, colInicio, TipoToken.PARENTESIS_DER, ")");
            }
            case '{' -> {
                avanzarCaracter();
                yield tokenDesdeLinea(lineaInicio, colInicio, TipoToken.LLAVE_IZQ, "{");
            }
            case '}' -> {
                avanzarCaracter();
                yield tokenDesdeLinea(lineaInicio, colInicio, TipoToken.LLAVE_DER, "}");
            }
            case ';' -> {
                avanzarCaracter();
                yield tokenDesdeLinea(lineaInicio, colInicio, TipoToken.PUNTO_Y_COMA, ";");
            }
            case '=' -> {
                if (sig == '=') {
                    avanzarCaracter();
                    avanzarCaracter();
                    yield tokenDesdeLinea(lineaInicio, colInicio, TipoToken.OPERADOR_IGUAL_IGUAL, "==");
                }
                avanzarCaracter();
                yield tokenDesdeLinea(lineaInicio, colInicio, TipoToken.OPERADOR_ASIGNACION, "=");
            }
            case '+' -> {
                avanzarCaracter();
                yield tokenDesdeLinea(lineaInicio, colInicio, TipoToken.OPERADOR_MAS, "+");
            }
            case '-' -> {
                avanzarCaracter();
                yield tokenDesdeLinea(lineaInicio, colInicio, TipoToken.OPERADOR_MENOS, "-");
            }
            case '*' -> {
                avanzarCaracter();
                yield tokenDesdeLinea(lineaInicio, colInicio, TipoToken.OPERADOR_MULTIPLICACION, "*");
            }
            case '/' -> {
                avanzarCaracter();
                yield tokenDesdeLinea(lineaInicio, colInicio, TipoToken.OPERADOR_DIVISION, "/");
            }
            case '&' -> {
                if (sig == '&') {
                    avanzarCaracter();
                    avanzarCaracter();
                    yield tokenDesdeLinea(lineaInicio, colInicio, TipoToken.OPERADOR_Y_LOGICO, "&&");
                }
                advertenciasLexico.add("Caracter '&' incompleto (¿&&?) en línea " + lineaInicio);
                avanzarCaracter();
                yield tokenDesdeLinea(lineaInicio, colInicio, TipoToken.IDENTIFICADOR, "&");
            }
            case '|' -> {
                if (sig == '|') {
                    avanzarCaracter();
                    avanzarCaracter();
                    yield tokenDesdeLinea(lineaInicio, colInicio, TipoToken.OPERADOR_O_LOGICO, "||");
                }
                advertenciasLexico.add("Caracter '|' incompleto (¿||?) en línea " + lineaInicio);
                avanzarCaracter();
                yield tokenDesdeLinea(lineaInicio, colInicio, TipoToken.IDENTIFICADOR, "|");
            }
            case '!' -> {
                if (sig == '=') {
                    avanzarCaracter();
                    avanzarCaracter();
                    yield tokenDesdeLinea(lineaInicio, colInicio, TipoToken.OPERADOR_DISTINTO, "!=");
                }
                advertenciasLexico.add("Caracter '!' inesperado en línea " + lineaInicio);
                avanzarCaracter();
                yield tokenDesdeLinea(lineaInicio, colInicio, TipoToken.IDENTIFICADOR, "!");
            }
            case '<' -> {
                if (sig == '=') {
                    avanzarCaracter();
                    avanzarCaracter();
                    yield tokenDesdeLinea(lineaInicio, colInicio, TipoToken.OPERADOR_MENOR_IGUAL, "<=");
                }
                avanzarCaracter();
                yield tokenDesdeLinea(lineaInicio, colInicio, TipoToken.OPERADOR_MENOR, "<");
            }
            case '>' -> {
                if (sig == '=') {
                    avanzarCaracter();
                    avanzarCaracter();
                    yield tokenDesdeLinea(lineaInicio, colInicio, TipoToken.OPERADOR_MAYOR_IGUAL, ">=");
                }
                avanzarCaracter();
                yield tokenDesdeLinea(lineaInicio, colInicio, TipoToken.OPERADOR_MAYOR, ">");
            }
            default -> {
                advertenciasLexico.add("Caracter no reconocido '" + c + "' en línea " + lineaInicio);
                avanzarCaracter();
                yield tokenDesdeLinea(lineaInicio, colInicio, TipoToken.IDENTIFICADOR, String.valueOf(c));
            }
        };
    }

    public List<Token> tokenizar() {
        List<Token> tokens = new ArrayList<>();
        while (true) {
            saltarEspaciosYComentarios();
            if (pos >= fuente.length()) {
                tokens.add(new Token(TipoToken.EOF, "", linea, columna));
                break;
            }
            char c = fuente.charAt(pos);
            if (c == '"') {
                tokens.add(leerCadenaLiteral());
                continue;
            }
            if (Character.isDigit(c)) {
                tokens.add(leerNumero());
                continue;
            }
            if (Character.isLetter(c) || c == '_') {
                tokens.add(leerIdentificadorOClave());
                continue;
            }
            tokens.add(leerOperadorOUnico());
        }
        return tokens;
    }
}
