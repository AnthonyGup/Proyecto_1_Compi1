package com.cunoc.compiforms.pkm;

import java_cup.runtime.Symbol;

%%

%public
%unicode
%class PKMLexer
%cup
%cupsym pkm_sym
%line
%column
%char
%ignorecase
%eofval{
  return new java_cup.runtime.Symbol(pkm_sym.EOF);
%eofval}

%{
  private Symbol symbol(int type) {
    return new Symbol(type, yyline, yycolumn);
  }
  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
  }
%}

/* Expresiones Regulares */
WhiteSpace = [ \t\r\n\f]+
Digit      = [0-9]
Number     = -?{Digit}+(\.{Digit}+)?
String     = \"([^\"\\]|\\.)*\"
HexColor   = #[0-9a-fA-F]{6}
Identifier = [A-Za-z_][A-Za-z0-9_]*

%%

<YYINITIAL> {
  {WhiteSpace}   { /* Ignorar espacios */ }

  /* Palabras Reservadas de Metadatos (Normalizadas a minÃºsculas en el valor) */
  "Author"       { return symbol(pkm_sym.AUTHOR, yytext().toLowerCase()); }
  "Fecha"        { return symbol(pkm_sym.FECHA, yytext().toLowerCase()); }
  "Hora"         { return symbol(pkm_sym.HORA, yytext().toLowerCase()); }
  "Description"  { return symbol(pkm_sym.DESCRIPTION, yytext().toLowerCase()); }
  "Total"        { return symbol(pkm_sym.TOTAL, yytext().toLowerCase()); }
  "de"           { return symbol(pkm_sym.DE, yytext().toLowerCase()); }
  "Secciones"    { return symbol(pkm_sym.SECCIONES, yytext().toLowerCase()); }
  "Preguntas"    { return symbol(pkm_sym.PREGUNTAS, yytext().toLowerCase()); }
  "Abiertas"     { return symbol(pkm_sym.ABIERTAS, yytext().toLowerCase()); }
  "Desplegables" { return symbol(pkm_sym.DESPLEGABLES, yytext().toLowerCase()); }
  "Selección"    { return symbol(pkm_sym.SELECCION, yytext().toLowerCase()); }
  "Múltiples"    { return symbol(pkm_sym.MULTIPLES, yytext().toLowerCase()); }

  /* Funciones reservadas */
  "WHO_IS_THAT_POKEMON" { return symbol(pkm_sym.WHO_IS_THAT_POKEMON, yytext().toUpperCase()); }

  /* Palabras Reservadas de Estructura */
  "Section"      { return symbol(pkm_sym.SECTION, yytext().toLowerCase()); }
  "Table"        { return symbol(pkm_sym.TABLE, yytext().toLowerCase()); }
  "Open"         { return symbol(pkm_sym.OPEN, yytext().toLowerCase()); }
  "Drop"         { return symbol(pkm_sym.DROP, yytext().toLowerCase()); }
  "Select"       { return symbol(pkm_sym.SELECT, yytext().toLowerCase()); }
  "Multiple"     { return symbol(pkm_sym.MULTIPLE, yytext().toLowerCase()); }
  "Style"        { return symbol(pkm_sym.STYLE, yytext().toLowerCase()); }
  "Content"      { return symbol(pkm_sym.CONTENT, yytext().toLowerCase()); }
  "Line"         { return symbol(pkm_sym.LINE, yytext().toLowerCase()); }
  "Element"      { return symbol(pkm_sym.ELEMENT, yytext().toLowerCase()); }

  /* OrientaciÃ³n y Constantes */
  "VERTICAL"     { return symbol(pkm_sym.ORIENTATION_VAL, "VERTICAL"); }
  "HORIZONTAL"   { return symbol(pkm_sym.ORIENTATION_VAL, "HORIZONTAL"); }

  /* Atributos de Estilo */
  "color"        { return symbol(pkm_sym.COLOR_ATTR, yytext().toLowerCase()); }
  "background"   { return symbol(pkm_sym.BACKGROUND_ATTR, yytext().toLowerCase()); }
  "font"         { return symbol(pkm_sym.FONT_ATTR, yytext().toLowerCase()); }
  "family"       { return symbol(pkm_sym.FAMILY_ATTR, yytext().toLowerCase()); }
  "text"         { return symbol(pkm_sym.TEXT_ATTR, yytext().toLowerCase()); }
  "size"         { return symbol(pkm_sym.SIZE_ATTR, yytext().toLowerCase()); }
  "border"       { return symbol(pkm_sym.BORDER_ATTR, yytext().toLowerCase()); }

  /* Colores Predefinidos */
  "RED"          { return symbol(pkm_sym.COLOR_NAME, "RED"); }
  "BLUE"         { return symbol(pkm_sym.COLOR_NAME, "BLUE"); }
  "GREEN"        { return symbol(pkm_sym.COLOR_NAME, "GREEN"); }
  "PURPLE"       { return symbol(pkm_sym.COLOR_NAME, "PURPLE"); }
  "SKY"          { return symbol(pkm_sym.COLOR_NAME, "SKY"); }
  "YELLOW"       { return symbol(pkm_sym.COLOR_NAME, "YELLOW"); }
  "BLACK"        { return symbol(pkm_sym.COLOR_NAME, "BLACK"); }
  "WHITE"        { return symbol(pkm_sym.COLOR_NAME, "WHITE"); }

  /* Tipos de Fuente */
  "MONO"         { return symbol(pkm_sym.FONT_VAL, "MONO"); }
  "SANS_SERIF"   { return symbol(pkm_sym.FONT_VAL, "SANS_SERIF"); }
  "CURSIVE"      { return symbol(pkm_sym.FONT_VAL, "CURSIVE"); }

  /* Tipos de Borde (DOTTED y DOUBLE) - LINE ya estÃ¡ arriba */
  "DOTTED"       { return symbol(pkm_sym.DOTTED, "DOTTED"); }
  "DOUBLE"       { return symbol(pkm_sym.DOUBLE, "DOUBLE"); }

  /* SÃ­mbolos */
  "###"          { return symbol(pkm_sym.TRIPLE_HASH); }
  "#"            { return symbol(pkm_sym.HASH); }
  "</"           { return symbol(pkm_sym.OPEN_END_TAG); }
  "/>"           { return symbol(pkm_sym.CLOSE_SELF_TAG); }
  "<"            { return symbol(pkm_sym.LT); }
  ">"            { return symbol(pkm_sym.GT); }
  "{"            { return symbol(pkm_sym.LBRACE); }
  "}"            { return symbol(pkm_sym.RBRACE); }
  "["            { return symbol(pkm_sym.LBRACK); }
  "]"            { return symbol(pkm_sym.RBRACK); }
  "("            { return symbol(pkm_sym.LPAREN); }
  ")"            { return symbol(pkm_sym.RPAREN); }
  "="            { return symbol(pkm_sym.EQUALS); }
  ":"            { return symbol(pkm_sym.COLON); }
  ","            { return symbol(pkm_sym.COMMA); }

  /* Valores */
  {Number}       { return symbol(pkm_sym.NUMBER_VAL, yytext()); }
  {String}       { return symbol(pkm_sym.STRING_VAL, yytext().substring(1, yytext().length()-1)); }
  {HexColor}     { return symbol(pkm_sym.HEX_COLOR, yytext()); }
  {Identifier}   { return symbol(pkm_sym.IDENTIFIER, yytext()); }

  /* Error LÃ©xico */
  . { return symbol(pkm_sym.ERROR, yytext()); }
}

