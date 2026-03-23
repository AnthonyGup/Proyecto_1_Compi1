package com.cunoc.compiforms;

import java_cup.runtime.Symbol;
import java.util.ArrayList;
import java.util.List;
import com.cunoc.compiforms.tokens.*;

%%

%public
%unicode
%class Lexer
%cup
%line
%column
%char

%{

  private ArrayList<TokenInfo> tokens = new ArrayList<>();
  private ArrayList<ErrorInfo> errors = new ArrayList<>();

  public List<TokenInfo> getTokens() {
      return tokens;
  }

  public List<ErrorInfo> getErrors() {
      return errors;
  }

  private Symbol symbol(int type, TokenType visualType) {
    int start = (int) yychar;
    int end = (int) (yychar + yylength());

    tokens.add(new TokenInfo(visualType, yytext(), start, end, yyline + 1, yycolumn + 1));

    return new Symbol(type, yyline, yycolumn, yytext());
  }

  private void addLexicalError(String lexeme, int line, int column) {
      errors.add(new ErrorInfo(ErrorType.LEXICAL, lexeme, line + 1, column + 1, "Símbolo no reconocido en el lenguaje"));
  }

%}

/*
============================================================
|                          MACROS                          |
============================================================
*/

DIGITO          = [0-9]
LETRA           = [a-zA-Z_]
ID              = {LETRA}({LETRA}|{DIGITO})*
ENTERO          = {DIGITO}+
DECIMAL         = {DIGITO}+"."{DIGITO}+
ESPACIO         = [ \t\r\n\f]+
CADENA          = \"([^\\\"\n]|\\.)*\"
COMMENT_LINEA   = \$[^\n]*
COMMENT_BLOQUE  = "/*"([^*]|\*+[^*/])*\*+"/"

HEX_COLOR       = \#[0-9a-fA-F]{6}
RGB_COLOR       = (rgb)?\([ \t]*{DIGITO}+[ \t]*,[ \t]*{DIGITO}+[ \t]*,[ \t]*{DIGITO}+[ \t]*\)
HSL_COLOR       = (<[ \t]*{DIGITO}+[ \t]*,[ \t]*{DIGITO}+[ \t]*,[ \t]*{DIGITO}+[ \t]*>)|(hsl\([ \t]*{DIGITO}+%?[ \t]*,[ \t]*{DIGITO}+%?[ \t]*,[ \t]*{DIGITO}+%?[ \t]*\))

/* Emojis del enunciado */
EMOJI_SMILE_FACE        = @\[:\)+\]
EMOJI_SMILE_WORD        = @\[:smile:\]
EMOJI_SAD_FACE          = @\[:\(+\]
EMOJI_SAD_WORD          = @\[:sad:\]
EMOJI_SERIOUS_FACE      = @\[:\|+\]
EMOJI_SERIOUS_WORD      = @\[:serious:\]
EMOJI_HEART_FACE        = @\[<+3+\]
EMOJI_HEART_WORD        = @\[:heart:\]
EMOJI_CAT_FACE          = @\[:\^+:\]
EMOJI_CAT_WORD          = @\[:cat:\]
EMOJI_STAR              = @\[:star:\]
EMOJI_STAR_COUNT_COLON  = @\[:star:({DIGITO}+|number):\]
EMOJI_STAR_COUNT_DASH   = @\[:star-({DIGITO}+|number):\]
EMOJI = ({EMOJI_SMILE_FACE}|{EMOJI_SMILE_WORD}|{EMOJI_SAD_FACE}|{EMOJI_SAD_WORD}|{EMOJI_SERIOUS_FACE}|{EMOJI_SERIOUS_WORD}|{EMOJI_HEART_FACE}|{EMOJI_HEART_WORD}|{EMOJI_CAT_FACE}|{EMOJI_CAT_WORD}|{EMOJI_STAR}|{EMOJI_STAR_COUNT_COLON}|{EMOJI_STAR_COUNT_DASH})

%%

/*
=================================================================
|                        PALABRAS RESERVADAS                     |
=================================================================
*/

      // Estructuras
      "SECTION"               { return symbol(sym.SECTION, TokenType.KEYWORD); }
      "TABLE"                 { return symbol(sym.TABLE, TokenType.KEYWORD); }
      "TEXT"                  { return symbol(sym.TEXT, TokenType.KEYWORD); }

      // Preguntas
      "OPEN_QUESTION"         { return symbol(sym.OPEN_QUESTION, TokenType.KEYWORD); }
      "DROP_QUESTION"         { return symbol(sym.DROP_QUESTION, TokenType.KEYWORD); }
      "SELECT_QUESTION"       { return symbol(sym.SELECT_QUESTION, TokenType.KEYWORD); }
      "MULTIPLE_QUESTION"     { return symbol(sym.MULTIPLE_QUESTION, TokenType.KEYWORD); }

      // Control
      "IF"                    { return symbol(sym.IF, TokenType.KEYWORD); }
      "ELSE"                  { return symbol(sym.ELSE, TokenType.KEYWORD); }
      "WHILE"                 { return symbol(sym.WHILE, TokenType.KEYWORD); }
      "DO"                    { return symbol(sym.DO, TokenType.KEYWORD); }
      "FOR"                   { return symbol(sym.FOR, TokenType.KEYWORD); }
      "IN"                    { return symbol(sym.IN, TokenType.KEYWORD); }
      "in"                    { return symbol(sym.IN, TokenType.KEYWORD); }

      // Definición de variables
      "number"                { return symbol(sym.NUMBER, TokenType.KEYWORD); }
      "string"                { return symbol(sym.STRING, TokenType.KEYWORD); }
      "special"               { return symbol(sym.SPECIAL, TokenType.KEYWORD); }

      // Orientación
      "VERTICAL"              { return symbol(sym.VERTICAL, TokenType.KEYWORD); }
      "HORIZONTAL"            { return symbol(sym.HORIZONTAL, TokenType.KEYWORD); }

      // Fuentes
      "MONO"                  { return symbol(sym.MONO, TokenType.KEYWORD); }
      "SANS_SERIF"            { return symbol(sym.SANS_SERIF, TokenType.KEYWORD); }
      "CURSIVE"               { return symbol(sym.CURSIVE, TokenType.KEYWORD); }

      // Bordes
      "LINE"                  { return symbol(sym.LINE, TokenType.KEYWORD); }
      "DOTTED"                { return symbol(sym.DOTTED, TokenType.KEYWORD); }
      "DOUBLE"                { return symbol(sym.DOUBLE, TokenType.KEYWORD); }

      // Colores predefinidos
      "RED"                   { return symbol(sym.RED, TokenType.KEYWORD); }
      "BLUE"                  { return symbol(sym.BLUE, TokenType.KEYWORD); }
      "GREEN"                 { return symbol(sym.GREEN, TokenType.KEYWORD); }
      "PURPLE"                { return symbol(sym.PURPLE, TokenType.KEYWORD); }
      "SKY"                   { return symbol(sym.SKY, TokenType.KEYWORD); }
      "YELLOW"                { return symbol(sym.YELLOW, TokenType.KEYWORD); }
      "BLACK"                 { return symbol(sym.BLACK, TokenType.KEYWORD); }
      "WHITE"                 { return symbol(sym.WHITE, TokenType.KEYWORD); }

      // Funciones especiales
      "who_is_that_pokemon"   { return symbol(sym.WHO_IS_THAT_POKEMON, TokenType.KEYWORD); }
      "draw"                  { return symbol(sym.DRAW, TokenType.KEYWORD); }

      // Atributos
      "width"                 { return symbol(sym.WIDTH, TokenType.KEYWORD); }
      "height"                { return symbol(sym.HEIGHT, TokenType.KEYWORD); }
      "label"                 { return symbol(sym.LABEL, TokenType.KEYWORD); }
      "orientation"           { return symbol(sym.ORIENTATION, TokenType.KEYWORD); }
      "elements"              { return symbol(sym.ELEMENTS, TokenType.KEYWORD); }
      "pointX"                { return symbol(sym.POINTX, TokenType.KEYWORD); }
      "pointY"                { return symbol(sym.POINTY, TokenType.KEYWORD); }
      "content"               { return symbol(sym.CONTENT, TokenType.KEYWORD); }
      "options"               { return symbol(sym.OPTIONS, TokenType.KEYWORD); }
      "correct"               { return symbol(sym.CORRECT, TokenType.KEYWORD); }

      // Estilos con espacio obligatorio entre comillas
      "\"color\""             { return symbol(sym.COLOR, TokenType.KEYWORD); }
      "\"background color\""  { return symbol(sym.BACKGROUND_COLOR, TokenType.KEYWORD); }
      "\"font family\""       { return symbol(sym.FONT_FAMILY, TokenType.KEYWORD); }
      "\"text size\""         { return symbol(sym.TEXT_SIZE, TokenType.KEYWORD); }
      "\"border\""            { return symbol(sym.BORDER, TokenType.KEYWORD); }
      "styles"                { return symbol(sym.STYLES, TokenType.KEYWORD); }

/*
============================================================
|                   OPERADORES ARITMETICOS                 |
============================================================
*/

"+"                     { return symbol(sym.MAS, TokenType.OPERATOR); }
"-"                     { return symbol(sym.MENOS, TokenType.OPERATOR); }
"*"                     { return symbol(sym.POR, TokenType.OPERATOR); }
"/"                     { return symbol(sym.DIV, TokenType.OPERATOR); }
"^"                     { return symbol(sym.POTENCIA, TokenType.OPERATOR); }
"%"                     { return symbol(sym.MODULO, TokenType.OPERATOR); }

"("                     { return symbol(sym.PAREN_IZQ, TokenType.SYMBOL); }
")"                     { return symbol(sym.PAREN_DER, TokenType.SYMBOL); }

/*
============================================================
|                   OPERADORES DE COMPARACION              |
============================================================
*/

"=="                    { return symbol(sym.IGUAL, TokenType.OTHER); }
"!="                    { return symbol(sym.DIFERENTE, TokenType.OTHER); }
"!!"                    { return symbol(sym.DIFERENTE, TokenType.OTHER); }
">="                    { return symbol(sym.MAYOR_IGUAL, TokenType.OTHER); }
"<="                    { return symbol(sym.MENOR_IGUAL, TokenType.OTHER); }
">"                     { return symbol(sym.MAYOR, TokenType.OTHER); }
"<"                     { return symbol(sym.MENOR, TokenType.OTHER); }

/*
============================================================
|                     OPERADORES LOGICOS                   |
============================================================
*/

"&&"                    { return symbol(sym.AND, TokenType.OTHER); }
"||"                    { return symbol(sym.OR, TokenType.OTHER); }
"~"                     { return symbol(sym.NOT, TokenType.OTHER); }

"="                     { return symbol(sym.ASIGNACION, TokenType.OPERATOR); }

/*
============================================================
|                        SIMBOLOS                          |
============================================================
*/

"{"                     { return symbol(sym.LLAVE_IZQ, TokenType.SYMBOL); }
"}"                     { return symbol(sym.LLAVE_DER, TokenType.SYMBOL); }
"["                     { return symbol(sym.COR_IZQ, TokenType.SYMBOL); }
"]"                     { return symbol(sym.COR_DER, TokenType.SYMBOL); }

":"                     { return symbol(sym.DOS_PUNTOS, TokenType.SYMBOL); }
","                     { return symbol(sym.COMA, TokenType.SYMBOL); }
";"                     { return symbol(sym.PUNTOYCOMA, TokenType.SYMBOL); }
".."                    { return symbol(sym.RANGO, TokenType.SYMBOL); }
"."                     { return symbol(sym.PUNTO, TokenType.SYMBOL); }

"?"                     { return symbol(sym.WILDCARD, TokenType.OTHER); }

/*
============================================================
|                       LITERALES                          |
============================================================
*/

{DECIMAL}               { return symbol(sym.DECIMAL, TokenType.NUMBER); }
{ENTERO}                { return symbol(sym.ENTERO, TokenType.NUMBER); }
{CADENA}                { return symbol(sym.CADENA, TokenType.STRING); }
{ID}                    { return symbol(sym.ID, TokenType.VARIABLE); }

{HEX_COLOR}             { return symbol(sym.HEX_COLOR, TokenType.KEYWORD); }
{RGB_COLOR}             { return symbol(sym.RGB_COLOR, TokenType.KEYWORD); }
{HSL_COLOR}             { return symbol(sym.HSL_COLOR, TokenType.KEYWORD); }

{EMOJI}                 { return symbol(sym.EMOJI, TokenType.EMOJI); }

/*
============================================================
|                       COMENTARIOS                        |
============================================================
*/

{COMMENT_LINEA}         { /* ignora comentarios */ }
{COMMENT_BLOQUE}        { /* ignora comentarios */ }

{ESPACIO}               { /* ignora espacios */ }

/* ----------- ERROR LEXICO ----------- */

. {
    addLexicalError(yytext(), yyline, yycolumn);
    return symbol(sym.ERROR, TokenType.ERROR);
}
