package com.cunoc.compiforms.form.model;

import com.cunoc.compiforms.form.model.questions.QuestionElement;

public class PreguntaConComodines {
    public QuestionElement pregunta;
    public int cantidadComodines;

    public PreguntaConComodines(QuestionElement pregunta, int cantidadComodines) {
        this.pregunta = pregunta;
        this.cantidadComodines = cantidadComodines;
    }
}
