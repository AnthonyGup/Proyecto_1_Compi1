package com.cunoc.compiforms.semantic;

import com.cunoc.compiforms.form.model.ResultadoValor;

public class ExpressionSemanticSupport {
    private final SemanticValueSupport valueSupport;
    private final ParserSemanticSupport mainSupport;

    public ExpressionSemanticSupport(SemanticValueSupport valueSupport, ParserSemanticSupport mainSupport) {
        this.valueSupport = valueSupport;
        this.mainSupport = mainSupport;
    }

    public Object evaluarOperacionAritmetica(Object izquierda, Object derecha, String operador) {
        ResultadoValor valIzquierda = obtenerResultado(izquierda);
        ResultadoValor valDerecha = obtenerResultado(derecha);
        int comodines = valIzquierda.cantidadComodines + valDerecha.cantidadComodines;

        // Concatenacion de texto si es suma y hay strings
        if ("+".equals(operador) && (valIzquierda.valor instanceof String || valDerecha.valor instanceof String)) {
            String texto = valueSupport.convertirATexto(valIzquierda.valor) + valueSupport.convertirATexto(valDerecha.valor);
            return new ResultadoValor(texto, comodines, null);
        }

        double numIzquierda = resolverNumero(valIzquierda);
        double numDerecha = resolverNumero(valDerecha);
        double resultado = 0.0;

        switch (operador) {
            case "+" -> resultado = numIzquierda + numDerecha;
            case "-" -> resultado = numIzquierda - numDerecha;
            case "*" -> resultado = numIzquierda * numDerecha;
            case "/" -> {
                if (numDerecha != 0) resultado = numIzquierda / numDerecha;
                else mainSupport.addSemanticError("Division por cero");
            }
            case "^" -> resultado = Math.pow(numIzquierda, numDerecha);
            case "%" -> resultado = numIzquierda % numDerecha;
        }

        return new ResultadoValor(resultado, comodines, null);
    }

    public Object evaluarComparacion(Object izquierda, Object derecha, String operador) {
        ResultadoValor valIzquierda = obtenerResultado(izquierda);
        ResultadoValor valDerecha = obtenerResultado(derecha);
        int comodines = valIzquierda.cantidadComodines + valDerecha.cantidadComodines;
        
        double n1 = resolverNumero(valIzquierda);
        double n2 = resolverNumero(valDerecha);
        boolean res = switch (operador) {
            case ">" -> n1 > n2;
            case "<" -> n1 < n2;
            case ">=" -> n1 >= n2;
            case "<=" -> n1 <= n2;
            case "==" -> n1 == n2;
            case "!=" -> n1 != n2;
            default -> false;
        };

        return new ResultadoValor(res ? 1.0 : 0.0, comodines, null);
    }

    public Object evaluarOperacionLogica(Object i, Object d, String o) {
        return new ResultadoValor(0.0, 0, null); // Placeholder
    }

    public Object negarOperacionLogica(Object v) {
        return new ResultadoValor(0.0, 0, null); // Placeholder
    }

    private ResultadoValor obtenerResultado(Object o) {
        if (o instanceof ResultadoValor) return (ResultadoValor) o;
        return new ResultadoValor(o, 0, null);
    }

    private double resolverNumero(ResultadoValor rv) {
        Double d = valueSupport.convertirADouble(rv.valor);
        if (d == null) {
            mainSupport.addSemanticError("Se esperaba un numero");
            return 0.0;
        }
        return d;
    }
}
