package com.mycompany.arbolsintactico;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Recorre el AST y aplica reglas Mini-Lang: ámbitos, tabla de símbolos e inferencia de tipos.
 */
public final class AnalizadorSemantico {

    private final SymbolTable tabla = new SymbolTable();
    private final List<String> errores = new ArrayList<>();
    /** Todas las declaraciones exitosas (para volcado ordenado). */
    private final List<Symbol> declaraciones = new ArrayList<>();

    public List<String> getErrores() {
        return List.copyOf(errores);
    }

    public List<Symbol> getDeclaraciones() {
        return List.copyOf(declaraciones);
    }

    public Map<String, Symbol> getTablaLegible() {
        Map<String, Symbol> m = new LinkedHashMap<>();
        for (Symbol s : declaraciones) {
            m.putIfAbsent(s.getNombre(), s);
        }
        return m;
    }

    public void analizar(NodoPrograma programa) {
        for (NodoParseo s : programa.hijos()) {
            procesarSentencia(s);
        }
    }

    private void procesarSentencia(NodoParseo n) {
        switch (n) {
            case NodoDeclaracion d -> procesarDeclaracion(d);
            case NodoAsignacion a -> procesarAsignacion(a);
            case NodoSentenciaIf si -> procesarIf(si);
            case NodoSentenciaWhile w -> procesarWhile(w);
            case NodoSentenciaExpresion se -> procesarStmtExpr(se);
            case NodoBloque b -> procesarBloque(b);
            default -> errores.add("Error semántico: sentencia no soportada: " + n.etiqueta());
        }
    }

    private void procesarBloque(NodoBloque b) {
        tabla.entrarAmbito();
        for (NodoParseo s : b.getSentencias()) {
            procesarSentencia(s);
        }
        tabla.salirAmbito();
    }

    private void procesarDeclaracion(NodoDeclaracion d) {
        String nombre = d.getIdentificador().getLexema();
        int linea = lineaNodo(d.getIdentificador());
        TipoSemantico tipoVar = tipoDesdeKeyword(d.getTipo().getTipo());

        Optional<Symbol> ok = tabla.declararActual(nombre, tipoVar, linea);
        if (ok.isEmpty()) {
            errores.add("Error [Línea " + linea + "]: Variable [" + nombre + "] ya declarada en este ámbito.");
            return;
        }
        declaraciones.add(ok.get());

        if (d.getInicializador() != null) {
            TipoSemantico tIni = inferirExpresion(d.getInicializador());
            if (!asignacionPermitida(tipoVar, tIni)) {
                errores.add("Error [Línea " + linea + "]: Tipos incompatibles en inicialización de '" + nombre + "'.");
            }
        }
    }

    private void procesarAsignacion(NodoAsignacion a) {
        String nombre = a.getIdentificador().getLexema();
        int linea = lineaNodo(a.getIdentificador());
        Optional<Symbol> sym = tabla.buscar(nombre);
        if (sym.isEmpty()) {
            errores.add("Error [Línea " + linea + "]: Variable [" + nombre + "] no definida.");
            inferirExpresion(a.getExpresion());
            return;
        }
        TipoSemantico tVar = sym.get().getTipoDato();
        TipoSemantico tExpr = inferirExpresion(a.getExpresion());
        if (!asignacionPermitida(tVar, tExpr)) {
            errores.add("Error [Línea " + linea + "]: Asignación incompatible para '" + nombre + "' (se esperaba compatibilidad con "
                    + tVar.name().toLowerCase() + ").");
        }
    }

    private void procesarIf(NodoSentenciaIf s) {
        TipoSemantico tc = inferirExpresion(s.getCondicion());
        int lineaCond = lineaExpresion(s.getCondicion());
        if (tc != TipoSemantico.BOOLEAN && tc != TipoSemantico.ERROR) {
            errores.add("Error [Línea " + lineaCond + "]: Tipo no booleano en estructura de control.");
        }
        procesarBloque(s.getCuerpo());
        procesarRestoCondicional(s.getResto());
    }

    private void procesarWhile(NodoSentenciaWhile w) {
        TipoSemantico tc = inferirExpresion(w.getCondicion());
        int lineaCond = lineaExpresion(w.getCondicion());
        if (tc != TipoSemantico.BOOLEAN && tc != TipoSemantico.ERROR) {
            errores.add("Error [Línea " + lineaCond + "]: Tipo no booleano en estructura de control.");
        }
        procesarBloque(w.getCuerpo());
    }

    private void procesarStmtExpr(NodoSentenciaExpresion se) {
        // println: sin chequeo de tipos en Mini-Lang para cadenas
    }

    private void procesarRestoCondicional(NodoRestoCondicional r) {
        if (r instanceof NodoSinResto) {
            return;
        }
        if (r instanceof NodoElseIf ei) {
            TipoSemantico tc = inferirExpresion(ei.getCondicion());
            int lineaCond = lineaExpresion(ei.getCondicion());
            if (tc != TipoSemantico.BOOLEAN && tc != TipoSemantico.ERROR) {
                errores.add("Error [Línea " + lineaCond + "]: Tipo no booleano en estructura de control.");
            }
            procesarBloque(ei.getCuerpo());
            procesarRestoCondicional(ei.getResto());
            return;
        }
        if (r instanceof NodoElseFinal ef) {
            procesarBloque(ef.getCuerpo());
        }
    }

    private TipoSemantico inferirExpresion(NodoExpresion e) {
        if (e instanceof NodoExpresionAtomo at) {
            return atomoTipo(at);
        }
        if (e instanceof NodoExpresionBinaria bin) {
            TipoSemantico izq = inferirExpresion(bin.getIzquierda());
            TipoSemantico der = inferirExpresion(bin.getDerecha());
            return combinar(bin.getOperador().getTipo(), izq, der, lineaNodo(bin.getOperador()));
        }
        return TipoSemantico.ERROR;
    }

    private TipoSemantico atomoTipo(NodoExpresionAtomo at) {
        NodoTerminal v = at.getValor();
        return switch (v.getTipo()) {
            case LITERAL_ENTERO -> TipoSemantico.INT;
            case LITERAL_FLOAT -> TipoSemantico.FLOAT;
            case IDENTIFICADOR -> {
                Optional<Symbol> s = tabla.buscar(v.getLexema());
                if (s.isEmpty()) {
                    int ln = lineaNodo(v);
                    errores.add("Error [Línea " + ln + "]: Variable [" + v.getLexema() + "] no definida.");
                    yield TipoSemantico.ERROR;
                }
                yield s.get().getTipoDato();
            }
            default -> TipoSemantico.ERROR;
        };
    }

    private TipoSemantico combinar(TipoToken op, TipoSemantico a, TipoSemantico b, int lineaOp) {
        if (a == TipoSemantico.ERROR || b == TipoSemantico.ERROR) {
            return TipoSemantico.ERROR;
        }
        if (esAritmetico(op)) {
            return combinarAritmetico(op, a, b, lineaOp);
        }
        if (esComparacion(op)) {
            return combinarComparacion(a, b, lineaOp);
        }
        if (esLogico(op)) {
            if (a == TipoSemantico.BOOLEAN && b == TipoSemantico.BOOLEAN) {
                return TipoSemantico.BOOLEAN;
            }
            errores.add("Error [Línea " + lineaOp + "]: Tipos incompatibles (se esperaban booleanos para " + op + ").");
            return TipoSemantico.ERROR;
        }
        errores.add("Error [Línea " + lineaOp + "]: Operador no aplicable a los tipos dados.");
        return TipoSemantico.ERROR;
    }

    private static boolean esAritmetico(TipoToken op) {
        return op == TipoToken.OPERADOR_MAS
                || op == TipoToken.OPERADOR_MENOS
                || op == TipoToken.OPERADOR_MULTIPLICACION
                || op == TipoToken.OPERADOR_DIVISION;
    }

    private static boolean esComparacion(TipoToken op) {
        return op == TipoToken.OPERADOR_IGUAL_IGUAL
                || op == TipoToken.OPERADOR_DISTINTO
                || op == TipoToken.OPERADOR_MENOR
                || op == TipoToken.OPERADOR_MAYOR
                || op == TipoToken.OPERADOR_MENOR_IGUAL
                || op == TipoToken.OPERADOR_MAYOR_IGUAL;
    }

    private static boolean esLogico(TipoToken op) {
        return op == TipoToken.OPERADOR_Y_LOGICO || op == TipoToken.OPERADOR_O_LOGICO;
    }

    private TipoSemantico combinarAritmetico(TipoToken op, TipoSemantico a, TipoSemantico b, int lineaOp) {
        if ((a == TipoSemantico.INT || a == TipoSemantico.FLOAT) && (b == TipoSemantico.INT || b == TipoSemantico.FLOAT)) {
            if (a == TipoSemantico.INT && b == TipoSemantico.INT) {
                return TipoSemantico.INT;
            }
            return TipoSemantico.FLOAT;
        }
        errores.add("Error [Línea " + lineaOp + "]: Tipos incompatibles en expresión aritmética.");
        return TipoSemantico.ERROR;
    }

    /**
     * Comparaciones: operandos numéricos (int/float con promoción implícita) → boolean.
     */
    private TipoSemantico combinarComparacion(TipoSemantico a, TipoSemantico b, int lineaOp) {
        if ((a == TipoSemantico.INT || a == TipoSemantico.FLOAT) && (b == TipoSemantico.INT || b == TipoSemantico.FLOAT)) {
            return TipoSemantico.BOOLEAN;
        }
        errores.add("Error [Línea " + lineaOp + "]: Tipos incompatibles en comparación.");
        return TipoSemantico.ERROR;
    }

    private static boolean asignacionPermitida(TipoSemantico variable, TipoSemantico expresion) {
        if (expresion == TipoSemantico.ERROR) {
            return true;
        }
        if (variable == expresion) {
            return true;
        }
        if (variable == TipoSemantico.FLOAT && expresion == TipoSemantico.INT) {
            return true;
        }
        return false;
    }

    private static TipoSemantico tipoDesdeKeyword(TipoToken t) {
        return t == TipoToken.PALABRA_CLAVE_FLOAT ? TipoSemantico.FLOAT : TipoSemantico.INT;
    }

    private static int lineaNodo(NodoTerminal t) {
        int ln = t.getLinea();
        return ln > 0 ? ln : 1;
    }

    private static int lineaExpresion(NodoExpresion e) {
        if (e instanceof NodoExpresionAtomo at) {
            return lineaNodo(at.getValor());
        }
        if (e instanceof NodoExpresionBinaria bin) {
            return lineaNodo(bin.getOperador());
        }
        return 1;
    }
}
