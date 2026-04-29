package com.mycompany.arbolsintactico;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Pila de mapas nombre → {@link Symbol} para ámbitos anidados.
 */
public final class SymbolTable {

    private final Deque<Map<String, Symbol>> ambitos = new ArrayDeque<>();
    private int nivelActual = 0;

    public SymbolTable() {
        entrarAmbito();
    }

    /** Nivel de ámbito actual (0 = global tras el primer {@link #entrarAmbito}). */
    public int getNivelActual() {
        return nivelActual;
    }

    public void entrarAmbito() {
        ambitos.push(new HashMap<>());
        nivelActual = ambitos.size() - 1;
    }

    /**
     * Cierra el ámbito superior: elimina variables de ese nivel.
     */
    public void salirAmbito() {
        if (ambitos.size() <= 1) {
            return;
        }
        ambitos.pop();
        nivelActual = ambitos.size() - 1;
    }

    /**
     * Declara en el ámbito actual. Retorna vacío si el nombre ya existe en este nivel.
     */
    public Optional<Symbol> declararActual(String nombre, TipoSemantico tipo, int linea) {
        Map<String, Symbol> actual = ambitos.peek();
        if (actual.containsKey(nombre)) {
            return Optional.empty();
        }
        Symbol s = new Symbol(nombre, tipo, nivelActual, linea);
        actual.put(nombre, s);
        return Optional.of(s);
    }

    /**
     * Búsqueda desde el nivel actual hacia el exterior (shadowing: el más interno gana).
     */
    public Optional<Symbol> buscar(String nombre) {
        for (Map<String, Symbol> map : ambitos) {
            Symbol s = map.get(nombre);
            if (s != null) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    /**
     * Snapshot ordenado para impresión (todas las entradas conocidas por nivel externo→interno).
     */
    public List<Symbol> todasLasEntradasInsercion() {
        List<Symbol> out = new ArrayList<>();
        Deque<Map<String, Symbol>> copia = new ArrayDeque<>(ambitos);
        while (!copia.isEmpty()) {
            Map<String, Symbol> m = copia.removeLast();
            out.addAll(m.values());
        }
        return out;
    }

    /** Mapa estable nombre → última definición visible (para volcado final). */
    public Map<String, Symbol> mapaLegibleGlobal() {
        Map<String, Symbol> acum = new LinkedHashMap<>();
        Deque<Map<String, Symbol>> copia = new ArrayDeque<>(ambitos);
        while (!copia.isEmpty()) {
            Map<String, Symbol> m = copia.removeLast();
            for (Map.Entry<String, Symbol> e : m.entrySet()) {
                acum.putIfAbsent(e.getKey(), e.getValue());
            }
        }
        return acum;
    }
}
