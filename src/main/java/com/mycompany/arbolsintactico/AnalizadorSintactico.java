package com.mycompany.arbolsintactico;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser recursivo Mini-Lang con recuperación en modo pánico.
 *
 * <pre>
 * programa           → lista_sentencias
 * lista_sentencias   → sentencia lista_sentencias | ε
 * sentencia          → decl | asignacion | stmt_if | stmt_while | stmt_expr | bloque
 * decl               → tipo IDENT ("=" expr)? ";"
 * tipo               → "int" | "float"
 * asignacion         → IDENT "=" expr ";"
 * stmt_if            → "if" "(" expr ")" bloque resto_condicional
 * stmt_while         → "while" "(" expr ")" bloque
 * stmt_expr          → invocacion_println ";"
 * bloque             → "{" lista_sentencias "}"
 * expr               → or_expr
 * or_expr            → and_expr ( "||" and_expr )*
 * and_expr           → eq_expr ( "&&" eq_expr )*
 * eq_expr            → rel_expr ( ("=="|"!=") rel_expr )*
 * rel_expr           → add_expr ( ("<"|">"|"<="|">=") add_expr )*
 * add_expr           → mul_expr ( ("+"|"-") mul_expr )*
 * mul_expr           → primary ( ("*"|"/") primary )*
 * primary            → LITERAL_ENTERO | LITERAL_FLOAT | IDENTIFICADOR | "(" expr ")"
 * </pre>
 */
public final class AnalizadorSintactico {

    private final List<Token> tokens;
    private int indice;
    private final List<String> logErrores = new ArrayList<>();

    public AnalizadorSintactico(List<Token> tokens) {
        this.tokens = tokens;
        this.indice = 0;
    }

    public List<String> getLogErrores() {
        return List.copyOf(logErrores);
    }

    private Token tokenActual() {
        return tokens.get(indice);
    }

    private Token tokenMirar(int delta) {
        int i = indice + delta;
        if (i < 0 || i >= tokens.size()) {
            return tokens.get(tokens.size() - 1);
        }
        return tokens.get(i);
    }

    private void avanzarToken() {
        if (tokenActual().getTipo() != TipoToken.EOF) {
            indice++;
        }
    }

    private boolean esPalabraClaveDeInicio(TipoToken t) {
        return t == TipoToken.PALABRA_CLAVE_IF
                || t == TipoToken.PALABRA_CLAVE_WHILE
                || t == TipoToken.PALABRA_CLAVE_FOR
                || t == TipoToken.PALABRA_CLAVE_DEF
                || t == TipoToken.PALABRA_CLAVE_CLASS
                || t == TipoToken.PALABRA_CLAVE_INT
                || t == TipoToken.PALABRA_CLAVE_FLOAT;
    }

    private boolean esDelimitadorBloque(TipoToken t) {
        return t == TipoToken.LLAVE_DER || t == TipoToken.PALABRA_CLAVE_END;
    }

    public void sincronizar() {
        while (tokenActual().getTipo() != TipoToken.EOF) {
            if (tokenActual().getTipo() == TipoToken.PUNTO_Y_COMA) {
                avanzarToken();
                return;
            }
            if (esPalabraClaveDeInicio(tokenActual().getTipo())) {
                return;
            }
            if (esDelimitadorBloque(tokenActual().getTipo())) {
                return;
            }
            avanzarToken();
        }
    }

    private void marcarError(String mensaje) {
        logErrores.add(mensaje);
    }

    private void registrarErrorYSincronizar(String mensaje) {
        marcarError(mensaje);
        sincronizar();
    }

    private NodoTerminal obligar(TipoToken esperado) {
        Token t = tokenActual();
        if (t.getTipo() == esperado) {
            avanzarToken();
            return terminalDesdeToken(t);
        }
        String msg = String.format(
                "Error [Línea %d, Columna %d]: Se esperaba '%s' antes de '%s'.",
                t.getLinea(),
                t.getColumna(),
                nombreTokenEsperado(esperado),
                t.getLexema());
        registrarErrorYSincronizar(msg);
        return null;
    }

    private static NodoTerminal terminalDesdeToken(Token t) {
        return new NodoTerminal(t.getTipo(), t.getLexema(), t.getLinea());
    }

    private static String nombreTokenEsperado(TipoToken t) {
        return switch (t) {
            case PARENTESIS_IZQ -> "(";
            case PARENTESIS_DER -> ")";
            case LLAVE_IZQ -> "{";
            case LLAVE_DER -> "}";
            case PUNTO_Y_COMA -> ";";
            case METODO_PRINTLN -> "System.out.println";
            case LITERAL_CADENA -> "literal de cadena";
            case LITERAL_ENTERO -> "literal entero";
            case LITERAL_FLOAT -> "literal float";
            case IDENTIFICADOR -> "identificador";
            case PALABRA_CLAVE_IF -> "if";
            case PALABRA_CLAVE_ELSE -> "else";
            case PALABRA_CLAVE_INT -> "int";
            case PALABRA_CLAVE_FLOAT -> "float";
            case PALABRA_CLAVE_WHILE -> "while";
            case OPERADOR_ASIGNACION -> "=";
            default -> t.name();
        };
    }

    public NodoPrograma parsePrograma() {
        List<NodoParseo> sentencias = new ArrayList<>();
        while (tokenActual().getTipo() != TipoToken.EOF) {
            while (tokenActual().getTipo() == TipoToken.LLAVE_DER) {
                avanzarToken();
            }
            if (tokenActual().getTipo() == TipoToken.EOF) {
                break;
            }
            int antes = indice;
            NodoParseo s = parseSentencia();
            if (s != null) {
                sentencias.add(s);
            } else if (antes == indice) {
                Token t = tokenActual();
                marcarError(String.format(
                        "Error [Línea %d, Columna %d]: No se pudo analizar sentencia; se encontró \"%s\".",
                        t.getLinea(), t.getColumna(), t.getLexema()));
                sincronizar();
                if (indice == antes && tokenActual().getTipo() != TipoToken.EOF) {
                    avanzarToken();
                }
            }
        }
        return new NodoPrograma(sentencias);
    }

    private NodoParseo parseSentencia() {
        TipoToken tt = tokenActual().getTipo();
        if (tt == TipoToken.PALABRA_CLAVE_INT || tt == TipoToken.PALABRA_CLAVE_FLOAT) {
            return parseDeclaracion();
        }
        if (tt == TipoToken.IDENTIFICADOR && tokenMirar(1).getTipo() == TipoToken.OPERADOR_ASIGNACION) {
            return parseAsignacion();
        }
        if (tt == TipoToken.PALABRA_CLAVE_IF) {
            return parseStmtIf();
        }
        if (tt == TipoToken.PALABRA_CLAVE_WHILE) {
            return parseStmtWhile();
        }
        if (tt == TipoToken.METODO_PRINTLN) {
            return parseStmtExpr();
        }
        if (tt == TipoToken.LLAVE_IZQ) {
            return parseBloque();
        }
        return null;
    }

    private NodoDeclaracion parseDeclaracion() {
        NodoTerminal tipo = obligarTipo();
        if (tipo == null) {
            return null;
        }
        NodoTerminal id = obligar(TipoToken.IDENTIFICADOR);
        if (id == null) {
            return null;
        }
        NodoExpresion ini = null;
        if (tokenActual().getTipo() == TipoToken.OPERADOR_ASIGNACION) {
            avanzarToken();
            ini = parseExpr();
            if (ini == null) {
                return null;
            }
        }
        NodoTerminal pyc = obligar(TipoToken.PUNTO_Y_COMA);
        if (pyc == null) {
            return null;
        }
        return new NodoDeclaracion(tipo, id, ini, pyc);
    }

    private NodoTerminal obligarTipo() {
        Token t = tokenActual();
        if (t.getTipo() == TipoToken.PALABRA_CLAVE_INT || t.getTipo() == TipoToken.PALABRA_CLAVE_FLOAT) {
            avanzarToken();
            return terminalDesdeToken(t);
        }
        String msg = String.format(
                "Error [Línea %d, Columna %d]: Se esperaba 'int' o 'float'; se encontró \"%s\".",
                t.getLinea(), t.getColumna(), t.getLexema());
        registrarErrorYSincronizar(msg);
        return null;
    }

    private NodoAsignacion parseAsignacion() {
        Token idTok = tokenActual();
        avanzarToken();
        NodoTerminal id = terminalDesdeToken(idTok);
        NodoTerminal eq = obligar(TipoToken.OPERADOR_ASIGNACION);
        if (eq == null) {
            return null;
        }
        NodoExpresion ex = parseExpr();
        if (ex == null) {
            return null;
        }
        NodoTerminal pyc = obligar(TipoToken.PUNTO_Y_COMA);
        if (pyc == null) {
            return null;
        }
        return new NodoAsignacion(id, eq, ex, pyc);
    }

    private NodoSentenciaIf parseStmtIf() {
        NodoTerminal kwIf = obligar(TipoToken.PALABRA_CLAVE_IF);
        if (kwIf == null) {
            return null;
        }
        NodoTerminal pIzq = obligar(TipoToken.PARENTESIS_IZQ);
        if (pIzq == null) {
            return null;
        }
        NodoExpresion expr = parseExpr();
        if (expr == null) {
            return null;
        }
        NodoTerminal pDer = obligar(TipoToken.PARENTESIS_DER);
        if (pDer == null) {
            return null;
        }
        NodoBloque bloque = parseBloque();
        if (bloque == null) {
            return null;
        }
        NodoRestoCondicional resto = parseRestoCondicional();
        return new NodoSentenciaIf(kwIf, pIzq, expr, pDer, bloque, resto);
    }

    private NodoSentenciaWhile parseStmtWhile() {
        NodoTerminal kw = obligar(TipoToken.PALABRA_CLAVE_WHILE);
        if (kw == null) {
            return null;
        }
        NodoTerminal pIzq = obligar(TipoToken.PARENTESIS_IZQ);
        if (pIzq == null) {
            return null;
        }
        NodoExpresion expr = parseExpr();
        if (expr == null) {
            return null;
        }
        NodoTerminal pDer = obligar(TipoToken.PARENTESIS_DER);
        if (pDer == null) {
            return null;
        }
        NodoBloque bloque = parseBloque();
        if (bloque == null) {
            return null;
        }
        return new NodoSentenciaWhile(kw, pIzq, expr, pDer, bloque);
    }

    private NodoSentenciaExpresion parseStmtExpr() {
        NodoInvocacionPrintln inv = parseInvocacionPrintln();
        if (inv == null) {
            return null;
        }
        NodoTerminal pyc = obligar(TipoToken.PUNTO_Y_COMA);
        if (pyc == null) {
            return null;
        }
        return new NodoSentenciaExpresion(inv, pyc);
    }

    private NodoBloque parseBloque() {
        NodoTerminal lz = obligar(TipoToken.LLAVE_IZQ);
        if (lz == null) {
            return null;
        }
        List<NodoParseo> lista = parseListaSentencias();
        NodoTerminal ld = obligar(TipoToken.LLAVE_DER);
        if (ld == null) {
            return null;
        }
        return new NodoBloque(lz, lista, ld);
    }

    private List<NodoParseo> parseListaSentencias() {
        List<NodoParseo> lista = new ArrayList<>();
        while (tokenActual().getTipo() != TipoToken.LLAVE_DER && tokenActual().getTipo() != TipoToken.EOF) {
            int antes = indice;
            NodoParseo s = parseSentencia();
            if (s != null) {
                lista.add(s);
            } else if (antes == indice) {
                break;
            }
        }
        return lista;
    }

    private NodoInvocacionPrintln parseInvocacionPrintln() {
        NodoTerminal pr = obligar(TipoToken.METODO_PRINTLN);
        if (pr == null) {
            return null;
        }
        NodoTerminal pIzq = obligar(TipoToken.PARENTESIS_IZQ);
        if (pIzq == null) {
            return null;
        }
        Token cad = tokenActual();
        if (cad.getTipo() != TipoToken.LITERAL_CADENA) {
            marcarError(String.format(
                    "Error [Línea %d, Columna %d]: Se esperaba cadena literal; se encontró \"%s\".",
                    cad.getLinea(), cad.getColumna(), cad.getLexema()));
            registrarErrorYSincronizar("");
            return null;
        }
        avanzarToken();
        NodoTerminal str = terminalDesdeToken(cad);
        NodoTerminal pDer = obligar(TipoToken.PARENTESIS_DER);
        if (pDer == null) {
            return null;
        }
        return new NodoInvocacionPrintln(pr, pIzq, str, pDer);
    }

    private NodoRestoCondicional parseRestoCondicional() {
        if (tokenActual().getTipo() != TipoToken.PALABRA_CLAVE_ELSE) {
            return new NodoSinResto();
        }
        Token tElse = tokenActual();
        avanzarToken();
        NodoTerminal kwElse = terminalDesdeToken(tElse);

        if (tokenActual().getTipo() == TipoToken.PALABRA_CLAVE_IF) {
            Token tIf = tokenActual();
            avanzarToken();
            NodoTerminal kwIf = terminalDesdeToken(tIf);
            NodoTerminal pIzq = obligar(TipoToken.PARENTESIS_IZQ);
            if (pIzq == null) {
                return new NodoSinResto();
            }
            NodoExpresion e = parseExpr();
            if (e == null) {
                return new NodoSinResto();
            }
            NodoTerminal pDer = obligar(TipoToken.PARENTESIS_DER);
            if (pDer == null) {
                return new NodoSinResto();
            }
            NodoBloque b = parseBloque();
            if (b == null) {
                return new NodoSinResto();
            }
            NodoRestoCondicional resto = parseRestoCondicional();
            return new NodoElseIf(kwElse, kwIf, pIzq, e, pDer, b, resto);
        }

        NodoBloque bloque = parseBloque();
        if (bloque == null) {
            return new NodoSinResto();
        }
        return new NodoElseFinal(kwElse, bloque);
    }

    // ——— Expresiones (precedencia ascendente: or < and < eq < rel < add < mul < primary) ———

    private NodoExpresion parseExpr() {
        return parseOr();
    }

    private NodoExpresion parseOr() {
        NodoExpresion left = parseAnd();
        if (left == null) {
            return null;
        }
        while (tokenActual().getTipo() == TipoToken.OPERADOR_O_LOGICO) {
            Token op = tokenActual();
            avanzarToken();
            NodoExpresion right = parseAnd();
            if (right == null) {
                return left;
            }
            left = new NodoExpresionBinaria(left, terminalDesdeToken(op), right);
        }
        return left;
    }

    private NodoExpresion parseAnd() {
        NodoExpresion left = parseEq();
        if (left == null) {
            return null;
        }
        while (tokenActual().getTipo() == TipoToken.OPERADOR_Y_LOGICO) {
            Token op = tokenActual();
            avanzarToken();
            NodoExpresion right = parseEq();
            if (right == null) {
                return left;
            }
            left = new NodoExpresionBinaria(left, terminalDesdeToken(op), right);
        }
        return left;
    }

    private NodoExpresion parseEq() {
        NodoExpresion left = parseRel();
        if (left == null) {
            return null;
        }
        while (tokenActual().getTipo() == TipoToken.OPERADOR_IGUAL_IGUAL
                || tokenActual().getTipo() == TipoToken.OPERADOR_DISTINTO) {
            Token op = tokenActual();
            avanzarToken();
            NodoExpresion right = parseRel();
            if (right == null) {
                return left;
            }
            left = new NodoExpresionBinaria(left, terminalDesdeToken(op), right);
        }
        return left;
    }

    private NodoExpresion parseRel() {
        NodoExpresion left = parseAdd();
        if (left == null) {
            return null;
        }
        while (esOperadorRelacional(tokenActual().getTipo())) {
            Token op = tokenActual();
            avanzarToken();
            NodoExpresion right = parseAdd();
            if (right == null) {
                return left;
            }
            left = new NodoExpresionBinaria(left, terminalDesdeToken(op), right);
        }
        return left;
    }

    private static boolean esOperadorRelacional(TipoToken t) {
        return t == TipoToken.OPERADOR_MENOR
                || t == TipoToken.OPERADOR_MAYOR
                || t == TipoToken.OPERADOR_MENOR_IGUAL
                || t == TipoToken.OPERADOR_MAYOR_IGUAL;
    }

    private NodoExpresion parseAdd() {
        NodoExpresion left = parseMul();
        if (left == null) {
            return null;
        }
        while (tokenActual().getTipo() == TipoToken.OPERADOR_MAS
                || tokenActual().getTipo() == TipoToken.OPERADOR_MENOS) {
            Token op = tokenActual();
            avanzarToken();
            NodoExpresion right = parseMul();
            if (right == null) {
                return left;
            }
            left = new NodoExpresionBinaria(left, terminalDesdeToken(op), right);
        }
        return left;
    }

    private NodoExpresion parseMul() {
        NodoExpresion left = parsePrimary();
        if (left == null) {
            return null;
        }
        while (tokenActual().getTipo() == TipoToken.OPERADOR_MULTIPLICACION
                || tokenActual().getTipo() == TipoToken.OPERADOR_DIVISION) {
            Token op = tokenActual();
            avanzarToken();
            NodoExpresion right = parsePrimary();
            if (right == null) {
                return left;
            }
            left = new NodoExpresionBinaria(left, terminalDesdeToken(op), right);
        }
        return left;
    }

    private NodoExpresion parsePrimary() {
        Token t = tokenActual();
        if (t.getTipo() == TipoToken.LITERAL_ENTERO
                || t.getTipo() == TipoToken.LITERAL_FLOAT
                || t.getTipo() == TipoToken.IDENTIFICADOR) {
            avanzarToken();
            return new NodoExpresionAtomo(terminalDesdeToken(t));
        }
        if (t.getTipo() == TipoToken.PARENTESIS_IZQ) {
            avanzarToken();
            NodoExpresion inner = parseExpr();
            obligar(TipoToken.PARENTESIS_DER);
            return inner;
        }
        String msg = String.format(
                "Error [Línea %d, Columna %d]: Se esperaba expresión primaria; se encontró \"%s\".",
                t.getLinea(), t.getColumna(), t.getLexema());
        registrarErrorYSincronizar(msg);
        return null;
    }
}
