package com.cunoc.compiforms.semantic;

import com.cunoc.compiforms.Lexer;
import com.cunoc.compiforms.Parser;
import com.cunoc.compiforms.data.Variable;
import com.cunoc.compiforms.form.model.FormDocument;
import com.cunoc.compiforms.form.model.ParseResult;
import com.cunoc.compiforms.form.model.PreguntaConComodines;
import com.cunoc.compiforms.form.model.ResultadoValor;
import com.cunoc.compiforms.form.model.questions.QuestionElement;
import com.cunoc.compiforms.tokens.ErrorInfo;
import com.cunoc.compiforms.tokens.TokenInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Esta clase actúa como el coordinador central de todo el análisis semántico.
 * Su objetivo es organizar las validaciones y transformaciones que ocurren mientras
 * el Parser lee el código del usuario.
 */
public class ParserSemanticSupport {
    private final Parser elParser;
    
    // Aquí guardamos las variables que el usuario declara en su script
    private final HashMap<String, Variable<?>> tablaDeVariables = new HashMap<>();
    
    // Listas para recolectar problemas encontrados durante el análisis
    private final ArrayList<String> erroresSemanticos = new ArrayList<>();
    private final ArrayList<String> advertenciasSemanticas = new ArrayList<>();
    
    // Estos son nuestros "expertos" en diferentes áreas
    private final SemanticValueSupport expertoEnValores;
    private final VariableSemanticSupport expertoEnVariables;
    private final ExpressionSemanticSupport expertoEnExpresiones;
    private final QuestionSemanticSupport expertoEnPreguntas;
    
    // Aquí se va armando la estructura final del formulario
    private FormDocument documentoFormulario = new FormDocument(new ArrayList<>());

    public ParserSemanticSupport(Parser parser) {
        this.elParser = parser;
        
        // Inicializamos a nuestros expertos
        this.expertoEnValores = new SemanticValueSupport(this);
        this.expertoEnVariables = new VariableSemanticSupport(tablaDeVariables, expertoEnValores, this);
        this.expertoEnExpresiones = new ExpressionSemanticSupport(expertoEnValores, this);
        this.expertoEnPreguntas = new QuestionSemanticSupport(tablaDeVariables, expertoEnValores, this);
    }

    // Métodos para que el Parser pueda acceder a cada experto de forma clara
    public SemanticValueSupport getValores() { return expertoEnValores; }
    public VariableSemanticSupport getVariablesSoporte() { return expertoEnVariables; }
    public ExpressionSemanticSupport getExpresiones() { return expertoEnExpresiones; }
    public QuestionSemanticSupport getPreguntas() { return expertoEnPreguntas; }

    /**
     * Registra un error semántico para mostrarlo al final.
     */
    public void addSemanticError(String mensaje) {
        this.erroresSemanticos.add(mensaje);
    }

    /**
     * Registra una advertencia (algo que no detiene el programa pero es sospechoso).
     */
    public void addWarning(String mensaje) {
        this.advertenciasSemanticas.add(mensaje);
    }

    public List<String> getWarnings() { return advertenciasSemanticas; }
    public List<String> getSemanticErrors() { return erroresSemanticos; }
    public Map<String, Variable<?>> getVariables() { return tablaDeVariables; }

    /**
     * Construye el resultado final combinando todo lo que recolectamos.
     */
    public ParseResult getParseResult() {
        Lexer lexerDelParser = elParser.getLexer();
        
        List<TokenInfo> listaDeTokens = new ArrayList<>();
        List<ErrorInfo> listaDeErroresLexicos = new ArrayList<>();
        
        if (lexerDelParser != null) {
            listaDeTokens = lexerDelParser.getTokens();
            listaDeErroresLexicos = lexerDelParser.getErrors();
        }
        
        return new ParseResult(
            documentoFormulario,
            tablaDeVariables,
            listaDeTokens,
            listaDeErroresLexicos,
            elParser.getSyntaxErrors(),
            erroresSemanticos,
            advertenciasSemanticas
        );
    }

    /**
     * Crea un objeto que representa un valor simple (sin comodines '?').
     */
    public Object crearResultadoValor(Object valorReal) { 
        return new ResultadoValor(valorReal, 0, null); 
    }

    /**
     * Crea un objeto que representa un comodín '?' usado en una expresión.
     */
    public Object crearResultadoConComodin() { 
        return new ResultadoValor(null, 1, null); 
    }

    /**
     * Extrae el objeto de pregunta real, sin importar si viene envuelta
     * por el sistema de comodines.
     */
    public QuestionElement extraerPreguntaElemento(Object valorRecibido) {
        // Si el valor es una pregunta que contiene comodines (?)
        if (valorRecibido instanceof PreguntaConComodines) {
            PreguntaConComodines envoltorio = (PreguntaConComodines) valorRecibido;
            return envoltorio.pregunta;
        }
        
        // Si el valor ya es directamente el objeto de la pregunta
        if (valorRecibido instanceof QuestionElement) {
            QuestionElement preguntaDirecta = (QuestionElement) valorRecibido;
            return preguntaDirecta;
        }
        
        // Si no es ninguna de las anteriores, devolvemos nada
        return null;
    }
    
    public FormDocument getParsedDocument() { return documentoFormulario; }
    public void setParsedDocument(FormDocument doc) { this.documentoFormulario = doc; }
}
