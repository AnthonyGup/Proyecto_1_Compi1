package com.cunoc.compiforms.semantic;

import com.cunoc.compiforms.data.Variable;
import com.cunoc.compiforms.form.model.PreguntaConComodines;
import com.cunoc.compiforms.form.model.ResultadoValor;
import com.cunoc.compiforms.form.model.questions.QuestionElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionSemanticSupport {
    private final HashMap<String, Variable<?>> variables;
    private final SemanticValueSupport valueSupport;
    private final ParserSemanticSupport mainSupport;

    public QuestionSemanticSupport(
        HashMap<String, Variable<?>> variables,
        SemanticValueSupport valueSupport,
        ParserSemanticSupport mainSupport
    ) {
        this.variables = variables;
        this.valueSupport = valueSupport;
        this.mainSupport = mainSupport;
    }

    public Object crearPreguntaConComodines(QuestionElement pregunta, Object mapaAtributos) {
        int comodines = contarComodines(mapaAtributos);
        return new PreguntaConComodines(pregunta, comodines);
    }

    public void validarDraw(String nombreVariable, Object argumentos) {
        Variable<?> variable = variables.get(nombreVariable);
        if (variable == null) {
            mainSupport.addSemanticError("Variable '" + nombreVariable + "' no declarada");
            return;
        }

        List<Object> listaArgumentos = valueSupport.extraerListaObjetos(argumentos);
        int cantidadRecibida = listaArgumentos.size();
        
        int cantidadEsperada = 0;
        if (variable.getValue() instanceof PreguntaConComodines) {
            cantidadEsperada = ((PreguntaConComodines) variable.getValue()).cantidadComodines;
        }

        if (cantidadRecibida != cantidadEsperada) {
            mainSupport.addSemanticError("draw() recibio " + cantidadRecibida + " valores, pero esperaba " + cantidadEsperada);
        }
    }

    public void validarIndiceCorrecto(String n, Integer i, int t) { /* Implement if needed */ }
    public void validarIndicesCorrectos(String n, List<Integer> i, int t) { /* Implement if needed */ }
    public void validarCantidadOpcionesSelect(List<String> o) { /* Implement if needed */ }

    private int contarComodines(Object valor) {
        if (valor == null) return 0;
        if (valor instanceof ResultadoValor resultadoValor) {
            return resultadoValor.cantidadComodines;
        }
        if (valor instanceof Map map) {
            int suma = 0;
            for (Object v : map.values()) {
                suma += contarComodines(v);
            }
            return suma;
        }
        if (valor instanceof List list) {
            int suma = 0;
            for (Object v : list) {
                suma += contarComodines(v);
            }
            return suma;
        }

        return 0;
    }
}
