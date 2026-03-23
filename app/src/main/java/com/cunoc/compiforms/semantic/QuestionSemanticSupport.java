package com.cunoc.compiforms.semantic;

import com.cunoc.compiforms.data.Variable;
import com.cunoc.compiforms.form.model.PreguntaConComodines;
import com.cunoc.compiforms.form.model.ResultadoValor;
import com.cunoc.compiforms.form.model.questions.QuestionElement;

import java.util.List;
import java.util.Map;

public class QuestionSemanticSupport {
    private final Map<String, Variable<?>> variables;
    private final SemanticValueSupport valueSupport;
    private final ParserSemanticSupport mainSupport;

    public QuestionSemanticSupport(
        Map<String, Variable<?>> variables,
        SemanticValueSupport valueSupport,
        ParserSemanticSupport mainSupport
    ) {
        this.variables = variables;
        this.valueSupport = valueSupport;
        this.mainSupport = mainSupport;
    }

    public Object crearPreguntaConComodines(QuestionElement pregunta, Object mapaAtributos) {
        int wildcardCount = contarComodines(mapaAtributos);
        return new PreguntaConComodines(pregunta, wildcardCount);
    }

    public void validarDraw(String nombreVariable, Object argumentos) {
        Variable<?> variable = variables.get(nombreVariable);
        if (variable == null) {
            mainSupport.addSemanticError("Variable '" + nombreVariable + "' no declarada");
            return;
        }

        List<Object> argumentList = valueSupport.extraerListaObjetos(argumentos);
        int receivedCount = argumentList.size();

        int expectedCount = 0;
        if (variable.getValue() instanceof PreguntaConComodines) {
            expectedCount = ((PreguntaConComodines) variable.getValue()).cantidadComodines;
        }

        if (receivedCount != expectedCount) {
            mainSupport.addSemanticError("draw() recibio " + receivedCount + " valores, pero esperaba " + expectedCount);
        }
    }

    public void validarIndiceCorrecto(String nombrePregunta, Integer indice, int totalOpciones) {
        // Implement if needed
    }

    public void validarIndicesCorrectos(String nombrePregunta, List<Integer> indices, int totalOpciones) {
        // Implement if needed
    }

    public void validarCantidadOpcionesSelect(List<String> opciones) {
        // Implement if needed
    }

    private int contarComodines(Object valor) {
        if (valor == null) {
            return 0;
        }

        if (valor instanceof ResultadoValor resultadoValor) {
            return resultadoValor.cantidadComodines;
        }

        if (valor instanceof Map map) {
            int totalWildcards = 0;
            for (Object value : map.values()) {
                totalWildcards += contarComodines(value);
            }
            return totalWildcards;
        }

        if (valor instanceof List list) {
            int totalWildcards = 0;
            for (Object value : list) {
                totalWildcards += contarComodines(value);
            }
            return totalWildcards;
        }

        return 0;
    }
}
