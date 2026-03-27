package com.mycompany.arbolsintactico;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser recursivo con recuperación en <em>modo pánico</em> mediante
 * {@link #sincronizar()}.
 *
 * <p>Gramática (informal):
 * <pre>
 * programa           → lista_sentencias
 * lista_sentencias   → stmt_if lista_sentencias | ε
 * stmt_if            → "if" "(" expr_rel ")" bloque resto_condicional
 * resto_condicional  → ε | "else" "if" "(" expr_rel ")" bloque resto_condicional | "else" bloque
 * expr_rel           → IDENTIFICADOR OP_COMPARACION LITERAL_ENTERO
 * bloque             → "{" stmt_expr "}"
 * stmt_expr          → invocacion_println ";"
 * invocacion_println → METODO_PRINTLN "(" LITERAL_CADENA ")"
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
                || t == TipoToken.PALABRA_CLAVE_CLASS;
    }

    private boolean esDelimitadorBloque(TipoToken t) {
        return t == TipoToken.LLAVE_DER || t == TipoToken.PALABRA_CLAVE_END;
    }

    /**
     * B. El algoritmo de sincronización: consumir hasta ancla segura.
     * <ul>
     *   <li>{@code ;} — se consume y se retorna.</li>
     *   <li>{@code if}, {@code while}, {@code for}, {@code def}, {@code class} — no se consume; retorno.</li>
     *   <li>{@code }} o {@code end} — no se consume; retorno.</li>
     * </ul>
     */
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

    private static boolean esOperadorComparacion(TipoToken t) {
        return t == TipoToken.OPERADOR_IGUAL_IGUAL
                || t == TipoToken.OPERADOR_DISTINTO
                || t == TipoToken.OPERADOR_MENOR
                || t == TipoToken.OPERADOR_MAYOR
                || t == TipoToken.OPERADOR_MENOR_IGUAL
                || t == TipoToken.OPERADOR_MAYOR_IGUAL;
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
            case IDENTIFICADOR -> "identificador";
            case PALABRA_CLAVE_IF -> "if";
            case PALABRA_CLAVE_ELSE -> "else";
            default -> t.name();
        };
    }

    private NodoTerminal obligar(TipoToken esperado) {
        Token t = tokenActual();
        if (t.getTipo() == esperado) {
            avanzarToken();
            return new NodoTerminal(t.getTipo(), t.getLexema());
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

    public NodoPrograma parsePrograma() {
        List<NodoParseo> sentencias = new ArrayList<>();
        while (tokenActual().getTipo() != TipoToken.EOF) {
            // Tras un error, puede quedar '}' de cierre sin pareja; se descarta para seguir.
            while (tokenActual().getTipo() == TipoToken.LLAVE_DER) {
                avanzarToken();
            }
            if (tokenActual().getTipo() == TipoToken.EOF) {
                break;
            }
            if (tokenActual().getTipo() == TipoToken.PALABRA_CLAVE_IF) {
                int antes = indice;
                NodoSentenciaIf s = parseStmtIf();
                if (s != null) {
                    sentencias.add(s);
                } else if (antes == indice) {
                    avanzarToken();
                }
            } else {
                Token t = tokenActual();
                String msg = String.format(
                        "Error [Línea %d, Columna %d]: Se esperaba sentencia 'if'; se encontró \"%s\".",
                        t.getLinea(),
                        t.getColumna(),
                        t.getLexema());
                marcarError(msg);
                int antes = indice;
                sincronizar();
                if (antes == indice && tokenActual().getTipo() != TipoToken.EOF) {
                    avanzarToken();
                }
            }
        }
        return new NodoPrograma(sentencias);
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
        NodoExpresionRelacional expr = parseExprRel();
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

    private NodoExpresionRelacional parseExprRel() {
        Token id = tokenActual();
        if (id.getTipo() != TipoToken.IDENTIFICADOR) {
            String msg = String.format(
                    "Error [Línea %d, Columna %d]: Se esperaba identificador en expresión relacional; se encontró \"%s\".",
                    id.getLinea(),
                    id.getColumna(),
                    id.getLexema());
            registrarErrorYSincronizar(msg);
            return null;
        }
        avanzarToken();
        NodoTerminal ident = new NodoTerminal(TipoToken.IDENTIFICADOR, id.getLexema());

        Token op = tokenActual();
        if (!esOperadorComparacion(op.getTipo())) {
            String msg = String.format(
                    "Error [Línea %d, Columna %d]: Se esperaba operador de comparación; se encontró \"%s\".",
                    op.getLinea(),
                    op.getColumna(),
                    op.getLexema());
            registrarErrorYSincronizar(msg);
            return null;
        }
        avanzarToken();
        NodoTerminal operador = new NodoTerminal(op.getTipo(), op.getLexema());

        Token num = tokenActual();
        if (num.getTipo() != TipoToken.LITERAL_ENTERO) {
            String msg = String.format(
                    "Error [Línea %d, Columna %d]: Se esperaba literal entero; se encontró \"%s\".",
                    num.getLinea(),
                    num.getColumna(),
                    num.getLexema());
            registrarErrorYSincronizar(msg);
            return null;
        }
        avanzarToken();
        NodoTerminal literal = new NodoTerminal(TipoToken.LITERAL_ENTERO, num.getLexema());
        return new NodoExpresionRelacional(ident, operador, literal);
    }

    private NodoBloque parseBloque() {
        NodoTerminal lz = obligar(TipoToken.LLAVE_IZQ);
        if (lz == null) {
            return null;
        }
        NodoSentenciaExpresion stmt = parseStmtExpr();
        if (stmt == null) {
            return null;
        }
        NodoTerminal ld = obligar(TipoToken.LLAVE_DER);
        if (ld == null) {
            return null;
        }
        return new NodoBloque(lz, stmt, ld);
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
            String msg = String.format(
                    "Error [Línea %d, Columna %d]: Se esperaba cadena literal; se encontró \"%s\".",
                    cad.getLinea(),
                    cad.getColumna(),
                    cad.getLexema());
            registrarErrorYSincronizar(msg);
            return null;
        }
        avanzarToken();
        NodoTerminal str = new NodoTerminal(TipoToken.LITERAL_CADENA, cad.getLexema());
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
        NodoTerminal kwElse = new NodoTerminal(TipoToken.PALABRA_CLAVE_ELSE, tElse.getLexema());

        if (tokenActual().getTipo() == TipoToken.PALABRA_CLAVE_IF) {
            Token tIf = tokenActual();
            avanzarToken();
            NodoTerminal kwIf = new NodoTerminal(TipoToken.PALABRA_CLAVE_IF, tIf.getLexema());
            NodoTerminal pIzq = obligar(TipoToken.PARENTESIS_IZQ);
            if (pIzq == null) {
                return new NodoSinResto();
            }
            NodoExpresionRelacional e = parseExprRel();
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
}
