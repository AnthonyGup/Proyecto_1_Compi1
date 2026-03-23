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
        ResultadoValor valorIzquierdo = obtenerResultado(izquierda);
        ResultadoValor valorDerecho = obtenerResultado(derecha);
        int cantidadComodines = valorIzquierdo.cantidadComodines + valorDerecho.cantidadComodines;

        boolean esSuma = "+".equals(operador);
        boolean izquierdaEsTexto = valorIzquierdo.valor instanceof String;
        boolean derechaEsTexto = valorDerecho.valor instanceof String;

        if (esSuma && (izquierdaEsTexto || derechaEsTexto)) {
            String textoIzquierdo = valueSupport.convertirATexto(valorIzquierdo.valor);
            String textoDerecho = valueSupport.convertirATexto(valorDerecho.valor);
            String textoFinal = textoIzquierdo + textoDerecho;
            return new ResultadoValor(textoFinal, cantidadComodines, null);
        }

        double numeroIzquierdo = resolverNumero(valorIzquierdo);
        double numeroDerecho = resolverNumero(valorDerecho);
        double resultado = 0.0;

        switch (operador) {
            case "+":
                resultado = numeroIzquierdo + numeroDerecho;
                break;
            case "-":
                resultado = numeroIzquierdo - numeroDerecho;
                break;
            case "*":
                resultado = numeroIzquierdo * numeroDerecho;
                break;
            case "/":
                if (numeroDerecho != 0) {
                    resultado = numeroIzquierdo / numeroDerecho;
                } else {
                    mainSupport.addSemanticError("Division por cero");
                }
                break;
            case "^":
                resultado = Math.pow(numeroIzquierdo, numeroDerecho);
                break;
            case "%":
                resultado = numeroIzquierdo % numeroDerecho;
                break;
            default:
                mainSupport.addSemanticError("Operador aritmetico no soportado: " + operador);
                break;
            }

        return new ResultadoValor(resultado, cantidadComodines, null);
    }

    public Object evaluarComparacion(Object izquierda, Object derecha, String operador) {
        ResultadoValor valorIzquierdo = obtenerResultado(izquierda);
        ResultadoValor valorDerecho = obtenerResultado(derecha);
        int cantidadComodines = valorIzquierdo.cantidadComodines + valorDerecho.cantidadComodines;

        double numeroIzquierdo = resolverNumero(valorIzquierdo);
        double numeroDerecho = resolverNumero(valorDerecho);
        boolean esVerdadero;

        switch (operador) {
            case ">":
                esVerdadero = numeroIzquierdo > numeroDerecho;
                break;
            case "<":
                esVerdadero = numeroIzquierdo < numeroDerecho;
                break;
            case ">=":
                esVerdadero = numeroIzquierdo >= numeroDerecho;
                break;
            case "<=":
                esVerdadero = numeroIzquierdo <= numeroDerecho;
                break;
            case "==":
                esVerdadero = numeroIzquierdo == numeroDerecho;
                break;
            case "!=":
                esVerdadero = numeroIzquierdo != numeroDerecho;
                break;
            default:
                esVerdadero = false;
                break;
        }

        double valorBooleano = 0.0;
        if (esVerdadero) {
            valorBooleano = 1.0;
        }

        return new ResultadoValor(valorBooleano, cantidadComodines, "COMPARACION");
    }

    public Object evaluarOperacionLogica(Object izquierda, Object derecha, String operador) {
        ResultadoValor valorIzquierdo = obtenerResultado(izquierda);
        ResultadoValor valorDerecho = obtenerResultado(derecha);
        int cantidadComodines = valorIzquierdo.cantidadComodines + valorDerecho.cantidadComodines;

        mainSupport.getValidadorOperadoresLogicos().validarOperador(operador, mainSupport);

        double numeroIzquierdo = resolverNumero(valorIzquierdo);
        double numeroDerecho = resolverNumero(valorDerecho);

        boolean izquierdaCumple = numeroIzquierdo >= 1.0;
        boolean derechaCumple = numeroDerecho >= 1.0;

        boolean resultadoLogico;
        if ("OR".equals(operador)) {
            resultadoLogico = izquierdaCumple || derechaCumple;
        } else if ("AND".equals(operador)) {
            resultadoLogico = izquierdaCumple && derechaCumple;
        } else {
            resultadoLogico = false;
        }

        double valorBooleano = 0.0;
        if (resultadoLogico) {
            valorBooleano = 1.0;
        }

        return new ResultadoValor(valorBooleano, cantidadComodines, "LOGICA");
    }

    public Object negarOperacion(Object valorOriginal) {
        ResultadoValor resultado = obtenerResultado(valorOriginal);

        if ("COMPARACION".equals(resultado.operadorLogico)) {
            return negarOperacionComparacion(valorOriginal);
        }

        if ("LOGICA".equals(resultado.operadorLogico)) {
            return negarOperacionLogica(valorOriginal);
        }

        mainSupport.addSemanticError("La negacion solo se aplica a comparaciones o expresiones logicas.");
        return new ResultadoValor(0.0, resultado.cantidadComodines, "LOGICA");
    }

    public Object negarOperacionLogica(Object valorOriginal) {
        ResultadoValor valorResultado = obtenerResultado(valorOriginal);
        if (!"LOGICA".equals(valorResultado.operadorLogico)) {
            mainSupport.addSemanticError("La negacion logica solo se aplica a expresiones logicas (AND/OR).");
            return new ResultadoValor(0.0, valorResultado.cantidadComodines, "LOGICA");
        }

        double valorNumerico = resolverNumero(valorResultado);
        boolean esVerdadero = valorNumerico >= 1.0;
        if (esVerdadero) {
            return new ResultadoValor(0.0, valorResultado.cantidadComodines, "LOGICA");
        }
        return new ResultadoValor(1.0, valorResultado.cantidadComodines, "LOGICA");
    }

    public Object negarOperacionComparacion(Object valorOriginal) {
        ResultadoValor valorResultado = obtenerResultado(valorOriginal);
        if (!"COMPARACION".equals(valorResultado.operadorLogico)) {
            mainSupport.addSemanticError("La negacion de comparacion solo se aplica a resultados de comparaciones (> < >= <= == !=).");
            return new ResultadoValor(0.0, valorResultado.cantidadComodines, "COMPARACION");
        }

        double valorNumerico = resolverNumero(valorResultado);
        boolean esVerdadero = valorNumerico >= 1.0;
        if (esVerdadero) {
            return new ResultadoValor(0.0, valorResultado.cantidadComodines, "COMPARACION");
        }
        return new ResultadoValor(1.0, valorResultado.cantidadComodines, "COMPARACION");
    }

    private ResultadoValor obtenerResultado(Object valor) {
        if (valor instanceof ResultadoValor) {
            return (ResultadoValor) valor;
        }
        return new ResultadoValor(valor, 0, null);
    }

    private double resolverNumero(ResultadoValor resultadoValor) {
        Double numero = valueSupport.convertirADouble(resultadoValor.valor);
        if (numero == null) {
            mainSupport.addSemanticError("Se esperaba un numero");
            return 0.0;
        }
        return numero;
    }
}
