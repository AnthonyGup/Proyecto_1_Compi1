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
    private final Parser parser;
    
    // Aquí guardamos las variables que el usuario declara en su script
    private final HashMap<String, Variable<?>> variableTable = new HashMap<>();
    
    // Listas para recolectar problemas encontrados durante el análisis
    private final ArrayList<String> semanticErrors = new ArrayList<>();
    private final ArrayList<String> semanticWarnings = new ArrayList<>();
    
    private final LogicalOperatorValidator validadorOperadoresLogicos;
    
    // Aquí se va armando la estructura final del formulario
    private FormDocument parsedDocument = new FormDocument(new ArrayList<>());

    public ParserSemanticSupport(Parser parser) {
        this.parser = parser;
        this.validadorOperadoresLogicos = new LogicalOperatorValidator();
    }

    public LogicalOperatorValidator getValidadorOperadoresLogicos() {
        return validadorOperadoresLogicos;
    }

    /**
     * Registra un error semántico para mostrarlo al final.
     */
    public void addSemanticError(String mensaje) {
        this.semanticErrors.add(mensaje);
    }

    /**
     * Registra una advertencia
     */
    public void addWarning(String mensaje) {
        this.semanticWarnings.add(mensaje);
    }

    public List<String> getWarnings() {
        return semanticWarnings;
    }

    public List<String> getSemanticErrors() {
        return semanticErrors;
    }

    public Map<String, Variable<?>> getVariables() {
        return variableTable;
    }

    /**
     * Construye el resultado final combinando todito
     */
    public ParseResult getParseResult() {
        Lexer parserLexer = parser.getLexer();
        
        List<TokenInfo> tokenList = new ArrayList<>();
        List<ErrorInfo> lexicalErrorList = new ArrayList<>();
        
        if (parserLexer != null) {
            tokenList = parserLexer.getTokens();
            lexicalErrorList = parserLexer.getErrors();
        }
        
        return new ParseResult(
            parsedDocument,
            variableTable,
            tokenList,
            lexicalErrorList,
            parser.getSyntaxErrors(),
            semanticErrors,
            semanticWarnings
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
     * Extrae el objeto de pregunta, sin importar si viene envuelta
     * por el sistema de comodines.
     */
    public QuestionElement extraerPreguntaElemento(Object valorRecibido) {
        if (valorRecibido instanceof PreguntaConComodines) {
            PreguntaConComodines wrappedQuestion = (PreguntaConComodines) valorRecibido;
            return wrappedQuestion.pregunta;
        }

        if (valorRecibido instanceof QuestionElement) {
            QuestionElement directQuestion = (QuestionElement) valorRecibido;
            return directQuestion;
        }

        return null;
    }

    public FormDocument getParsedDocument() {
        return parsedDocument;
    }

    public void setParsedDocument(FormDocument doc) {
        this.parsedDocument = doc;
    }

}
