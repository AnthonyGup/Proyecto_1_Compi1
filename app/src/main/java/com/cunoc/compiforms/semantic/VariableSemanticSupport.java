package com.cunoc.compiforms.semantic;

import com.cunoc.compiforms.data.Variable;
import com.cunoc.compiforms.data.VariableType;
import com.cunoc.compiforms.form.model.ResultadoValor;
import java.util.HashMap;

/**
 * Esta clase se encarga de todo lo relacionado con la memoria del script:
 * guardar variables, cambiar sus valores y buscarlas cuando se necesitan.
 */
public class VariableSemanticSupport {
    // La "memoria" donde guardamos las variables por su nombre
    private final HashMap<String, Variable<?>> tablaDeVariables;
    private final SemanticValueSupport ayudaValores;
    private final ParserSemanticSupport soportePrincipal;

    public VariableSemanticSupport(
        HashMap<String, Variable<?>> variables,
        SemanticValueSupport ayudaValores,
        ParserSemanticSupport soportePrincipal
    ) {
        this.tablaDeVariables = variables;
        this.ayudaValores = ayudaValores;
        this.soportePrincipal = soportePrincipal;
    }

    /**
     * Crea una nueva variable en la memoria.
     * Si el nombre ya existe, genera un error.
     */
    public void declararVariable(String nombre, VariableType tipo, Object valorInicial) {
        // Primero revisamos si el nombre ya está ocupado
        if (tablaDeVariables.containsKey(nombre)) {
            soportePrincipal.addSemanticError("Error: La variable '" + nombre + "' ya fue declarada anteriormente.");
            return;
        }

        // Preparamos el valor para que coincida con el tipo (limpiar textos, convertir números)
        Object valorLimpio = prepararValorSegunTipo(tipo, valorInicial);
        
        // Creamos la "cajita" (Variable) para guardar el valor
        Variable<Object> nuevaVariable = new Variable<>(valorLimpio, tipo, nombre);
        
        // La guardamos en nuestra tabla de memoria
        tablaDeVariables.put(nombre, nuevaVariable);
    }

    /**
     * Cambia el valor de una variable que ya existe.
     */
    public void asignarVariable(String nombre, VariableType tipoEsperado, Object valorNuevo) {
        // Buscamos la variable en nuestra tabla
        Variable<Object> variableEncontrada = (Variable<Object>) tablaDeVariables.get(nombre);
        
        // Si no existe, no podemos asignarle nada
        if (variableEncontrada == null) {
            soportePrincipal.addSemanticError("Error: No puedes darle un valor a '" + nombre + "' porque no existe.");
            return;
        }
        
        // Preparamos el nuevo valor y lo guardamos en la cajita
        Object valorLimpio = prepararValorSegunTipo(tipoEsperado, valorNuevo);
        variableEncontrada.setValue(valorLimpio);
    }

    /**
     * Busca una variable y devuelve su valor empaquetado.
     */
    public Object resolverValorVariable(String nombre) {
        Variable variable = tablaDeVariables.get(nombre);
        
        if (variable == null) {
            soportePrincipal.addSemanticError("Error: No se encontró la variable '" + nombre + "'.");
            // Devolvemos un valor por defecto (0.0) para que el programa no se detenga
            return new ResultadoValor(0.0, 0, null);
        }
        
        // Devolvemos el valor que tenía guardado la variable
        return new ResultadoValor(variable.getValue(), 0, null);
    }

    public void prepararVariableFor(String n) {
        // Por ahora no necesitamos hacer nada especial aquí
    }

    /**
     * Se encarga de que los datos estén en el formato correcto.
     * Si es un número, se asegura de que sea un Double.
     * Si es un texto, se asegura de que sea un String.
     */
    private Object prepararValorSegunTipo(VariableType tipo, Object valorCrudo) {
        if (tipo == VariableType.NUMBER) {
            Double numero = ayudaValores.convertirADouble(valorCrudo);
            // Si no era un número válido, le ponemos 0.0 por defecto
            return (numero == null) ? 0.0 : numero;
        }
        
        if (tipo == VariableType.STRING) {
            return ayudaValores.convertirATexto(valorCrudo);
        }
        
        // Para otros tipos (como SPECIAL), devolvemos el valor tal cual
        return valorCrudo;
    }
}
