package com.cunoc.compiforms.form.model;

public class ResultadoValor {
    public Object valor;
    public int cantidadComodines;
    public String operadorLogico;

    public ResultadoValor(Object valor, int cantidadComodines, String operadorLogico) {
        this.valor = valor;
        this.cantidadComodines = cantidadComodines;
        this.operadorLogico = operadorLogico;
    }
}
