package com.cunoc.compiforms.semantic;

public class LogicalOperatorValidator {
    private String currentOperator;
    private boolean isInsideLogicalExpression;

    public LogicalOperatorValidator() {
        this.currentOperator = null;
        this.isInsideLogicalExpression = false;
    }

    public void iniciarExpresionLogica() {
        this.currentOperator = null;
        this.isInsideLogicalExpression = true;
    }

    public void terminarExpresionLogica() {
        this.currentOperator = null;
        this.isInsideLogicalExpression = false;
    }

    public boolean validarOperador(String operador, ParserSemanticSupport support) {
        if (!isInsideLogicalExpression) {
            iniciarExpresionLogica();
            this.currentOperator = operador;
            return true;
        }

        if (currentOperator == null) {
            this.currentOperator = operador;
            return true;
        }

        if (!currentOperator.equals(operador)) {
            support.addSemanticError(
                String.format(
                    "No se puede mezclar operadores logicos: '%s' y '%s' en la misma expresion",
                    currentOperator, operador
                )
            );
            return false;
        }

        return true;
    }
}
