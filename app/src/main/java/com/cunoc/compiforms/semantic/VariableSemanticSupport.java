package com.cunoc.compiforms.semantic;

import com.cunoc.compiforms.data.Variable;
import com.cunoc.compiforms.data.VariableType;
import com.cunoc.compiforms.form.model.ResultadoValor;
import java.util.Map;

/**
 * Esta clase se encarga de todo lo relacionado con la memoria del script:
 * guardar variables, cambiar sus valores y buscarlas cuando se necesitan.
 */
public class VariableSemanticSupport {
    private final Map<String, Variable<?>> variableTable;
    private final SemanticValueSupport valueSupport;
    private final ParserSemanticSupport mainSupport;

    public VariableSemanticSupport(
        Map<String, Variable<?>> variables,
        SemanticValueSupport valueSupport,
        ParserSemanticSupport mainSupport
    ) {
        this.variableTable = variables;
        this.valueSupport = valueSupport;
        this.mainSupport = mainSupport;
    }

    /**
     * Crea una nueva variable en la memoria.
     * Si el nombre ya existe, genera un error.
     */
    public void declararVariable(String nombre, VariableType tipo, Object valorInicial) {
        if (variableTable.containsKey(nombre)) {
            mainSupport.addSemanticError("Error: La variable '" + nombre + "' ya fue declarada anteriormente.");
            return;
        }

        Object cleanValue = prepararValorSegunTipo(tipo, valorInicial);
        Variable<Object> createdVariable = new Variable<>(cleanValue, tipo, nombre);
        variableTable.put(nombre, createdVariable);
    }

    /**
     * Cambia el valor de una variable que ya existe.
     */
    public void asignarVariable(String nombre, VariableType tipoEsperado, Object valorNuevo) {
        Variable<Object> foundVariable = (Variable<Object>) variableTable.get(nombre);

        if (foundVariable == null) {
            mainSupport.addSemanticError("Error: No puedes darle un valor a '" + nombre + "' porque no existe.");
            return;
        }

        Object cleanValue = prepararValorSegunTipo(tipoEsperado, valorNuevo);
        foundVariable.setValue(cleanValue);
    }

    /**
     * Cambia el valor de una variable usando su tipo declarado real.
     * Esto permite expresiones como: string a = b; o a = "x" + b;
     */
    public void asignarVariableSegunTipoDeclarado(String nombre, Object valorNuevo) {
        Variable<Object> foundVariable = (Variable<Object>) variableTable.get(nombre);

        if (foundVariable == null) {
            mainSupport.addSemanticError("Error: No puedes darle un valor a '" + nombre + "' porque no existe.");
            return;
        }

        VariableType declaredType = foundVariable.getType();
        Object cleanValue = prepararValorSegunTipo(declaredType, valorNuevo);
        foundVariable.setValue(cleanValue);
    }

    /**
     * Busca una variable y devuelve su valor empaquetado.
     */
    public Object resolverValorVariable(String nombre) {
        Variable variable = variableTable.get(nombre);

        if (variable == null) {
            mainSupport.addSemanticError("Error: No se encontró la variable '" + nombre + "'.");
            return new ResultadoValor(0.0, 0, null);
        }

        return new ResultadoValor(variable.getValue(), 0, null);
    }

    public void prepararVariableFor(String nombreVariable) {
        Variable<?> variable = variableTable.get(nombreVariable);

        if (variable == null) {
            variableTable.put(nombreVariable, new Variable<>(0.0, VariableType.NUMBER, nombreVariable));
            return;
        }

        if (variable.getType() != VariableType.NUMBER) {
            mainSupport.addSemanticError(
                "Error: La variable '" + nombreVariable + "' usada en FOR debe ser de tipo number."
            );
        }
    }

    /**
     * Se encarga de que los datos estén en el formato correcto.
     * Si es un número, se asegura de que sea un Double.
     * Si es un texto, se asegura de que sea un String.
     */
    private Object prepararValorSegunTipo(VariableType tipo, Object valorCrudo) {
        if (tipo == VariableType.NUMBER) {
            Double numberValue = valueSupport.convertirADouble(valorCrudo);
            if (numberValue == null) {
                return 0.0;
            }
            return numberValue;
        }

        if (tipo == VariableType.STRING) {
            return valueSupport.convertirATexto(valorCrudo);
        }

        return valorCrudo;
    }
}
