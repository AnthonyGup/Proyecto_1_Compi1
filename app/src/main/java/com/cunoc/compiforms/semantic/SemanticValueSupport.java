package com.cunoc.compiforms.semantic;

import com.cunoc.compiforms.form.model.ResultadoValor;
import com.cunoc.compiforms.form.model.elements.FormElement;
import com.cunoc.compiforms.form.model.elements.Orientacion;
import com.cunoc.compiforms.form.model.elements.TableRow;
import com.cunoc.compiforms.form.model.styles.BorderStyle;
import com.cunoc.compiforms.form.model.styles.BorderType;
import com.cunoc.compiforms.form.model.styles.ColorValue;
import com.cunoc.compiforms.form.model.styles.FontFamily;
import com.cunoc.compiforms.form.model.styles.NamedColor;
import com.cunoc.compiforms.form.model.styles.StyleSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ayuda a convertir y validar los valores que vienen del lenguaje.
 * Por ejemplo: convertir un texto como "10.5" en un número real.
 */
public class SemanticValueSupport {
    private final ParserSemanticSupport soportePrincipal;

    public SemanticValueSupport(ParserSemanticSupport soportePrincipal) {
        this.soportePrincipal = soportePrincipal;
    }

    /**
     * Intenta convertir cualquier cosa a un número Decimal (Double).
     */
    public Double convertirADouble(Object valorGenerico) {
        if (valorGenerico == null) return null;
        
        // Si el valor viene empaquetado en un "ResultadoValor", sacamos el contenido
        if (valorGenerico instanceof ResultadoValor) {
            valorGenerico = ((ResultadoValor) valorGenerico).valor;
        }
        
        // Si ya es un número, solo lo devolvemos como Double
        if (valorGenerico instanceof Number) {
            return ((Number) valorGenerico).doubleValue();
        }
        
        // Si es texto, intentamos convertirlo
        try {
            return Double.parseDouble(valorGenerico.toString());
        } catch (Exception e) {
            return null; 
        }
    }

    public Integer convertirAEntero(Object valorGenerico) {
        Double numeroDecimal = convertirADouble(valorGenerico);
        if (numeroDecimal == null) return null;
        return numeroDecimal.intValue();
    }

    public String convertirATexto(Object valorGenerico) {
        if (valorGenerico == null) return "";
        if (valorGenerico instanceof ResultadoValor) {
            valorGenerico = ((ResultadoValor) valorGenerico).valor;
        }
        return valorGenerico.toString();
    }

    /**
     * Quita las comillas de los textos. Ejemplo: "\"Hola\"" -> "Hola"
     */
    public String normalizarLiteralCadena(Object valorCrudo) {
        if (valorCrudo == null) return "";
        String texto = valorCrudo.toString();
        
        boolean tieneComillas = texto.startsWith("\"") && texto.endsWith("\"");
        if (tieneComillas && texto.length() >= 2) {
            return texto.substring(1, texto.length() - 1);
        }
        return texto;
    }

    // --- MÉTODOS PARA EXTRAER LISTAS ---

    public ArrayList<Object> extraerListaObjetos(Object valor) {
        if (valor instanceof List) {
            return new ArrayList<>((List<?>) valor);
        }
        ArrayList<Object> listaNueva = new ArrayList<>();
        if (valor != null) listaNueva.add(valor);
        return listaNueva;
    }

    public ArrayList<FormElement> extraerElementosFormulario(Object valor) {
        ArrayList<FormElement> elementosEncontrados = new ArrayList<>();
        for (Object item : extraerListaObjetos(valor)) {
            if (item instanceof FormElement) {
                elementosEncontrados.add((FormElement) item);
            }
        }
        return elementosEncontrados;
    }

    /**
     * Extrae específicamente las filas de una tabla.
     */
    public ArrayList<TableRow> extraerFilasTabla(Object valor) {
        ArrayList<TableRow> filasEncontradas = new ArrayList<>();
        for (Object item : extraerListaObjetos(valor)) {
            if (item instanceof TableRow) {
                filasEncontradas.add((TableRow) item);
            }
        }
        return filasEncontradas;
    }

    public ArrayList<String> extraerListaTextos(Object valor) {
        ArrayList<String> textos = new ArrayList<>();
        for (Object item : extraerListaObjetos(valor)) {
            textos.add(convertirATexto(item));
        }
        return textos;
    }

    public ArrayList<Integer> extraerListaEnteros(Object valor) {
        ArrayList<Integer> enteros = new ArrayList<>();
        for (Object item : extraerListaObjetos(valor)) {
            Integer numero = convertirAEntero(item);
            if (numero != null) enteros.add(numero);
        }
        return enteros;
    }

    // --- CONVERSORES DE ESTILOS Y DISEÑO ---

    public Orientacion convertirAOrientacion(Object valor) {
        String texto = convertirATexto(valor).toUpperCase();
        if (texto.contains("HORIZONTAL")) return Orientacion.HORIZONTAL;
        return Orientacion.VERTICAL;
    }

    public FontFamily convertirAFuente(Object valor) {
        String texto = convertirATexto(valor).toUpperCase();
        if (texto.contains("MONO")) return FontFamily.MONO;
        if (texto.contains("SANS")) return FontFamily.SANS_SERIF;
        if (texto.contains("CURSIVE")) return FontFamily.CURSIVE;
        return FontFamily.SANS_SERIF;
    }

    public BorderType convertirATipoBorde(Object valor) {
        String texto = convertirATexto(valor).toUpperCase();
        if (texto.contains("DOTTED")) return BorderType.DOTTED;
        if (texto.contains("DOUBLE")) return BorderType.DOUBLE;
        return BorderType.LINE;
    }

    public ColorValue crearColorNombrado(Object nombre) {
        String texto = convertirATexto(nombre).toUpperCase();
        try {
            return new ColorValue.Named(NamedColor.valueOf(texto));
        } catch (IllegalArgumentException e) {
            return new ColorValue.Named(NamedColor.BLACK);
        }
    }

    public ColorValue crearColorDesdeTexto(Object texto) {
        return new ColorValue.Hex(convertirATexto(texto));
    }

    public BorderStyle crearEstiloBorde(Object tamano, Object tipo, Object color) {
        Double grosor = convertirADouble(tamano);
        BorderType tipoBorde = convertirATipoBorde(tipo);
        ColorValue colorBorde = (color instanceof ColorValue) ? (ColorValue) color : crearColorNombrado("BLACK");
        
        return new BorderStyle(grosor, tipoBorde, colorBorde);
    }

    public StyleSet construirEstilos(Object valor) {
        if (valor instanceof Map) {
            Map<?, ?> mapa = (Map<?, ?>) valor;
            
            ColorValue colorT = (mapa.get("textColor") instanceof ColorValue) ? (ColorValue) mapa.get("textColor") : null;
            ColorValue colorF = (mapa.get("backgroundColor") instanceof ColorValue) ? (ColorValue) mapa.get("backgroundColor") : null;
            FontFamily fuente = (mapa.get("fontFamily") instanceof FontFamily) ? (FontFamily) mapa.get("fontFamily") : null;
            Double tamano = convertirADouble(mapa.get("textSize"));
            BorderStyle borde = (mapa.get("border") instanceof BorderStyle) ? (BorderStyle) mapa.get("border") : null;
            
            return new StyleSet(colorT, colorF, fuente, tamano, borde);
        }
        return new StyleSet(null, null, null, null, null);
    }

    // --- MÉTODOS DE ATRIBUTOS ---

    public HashMap<String, Object> crearAtributo(String nombre, Object valor) {
        HashMap<String, Object> mapa = new HashMap<>();
        mapa.put(nombre, valor);
        return mapa;
    }

    public HashMap<String, Object> combinarAtributos(Object grupo1, Object grupo2) {
        HashMap<String, Object> resultado = new HashMap<>();
        
        if (grupo1 instanceof Map) {
            resultado.putAll((Map<? extends String, ?>) grupo1);
        }
        
        if (grupo2 instanceof Map) {
            Map<?, ?> mapa2 = (Map<?, ?>) grupo2;
            for (Object clave : mapa2.keySet()) {
                String nombreClave = clave.toString();
                if (resultado.containsKey(nombreClave)) {
                    soportePrincipal.addSemanticError("¡Cuidado! El atributo '" + nombreClave + "' está repetido.");
                }
                resultado.put(nombreClave, mapa2.get(clave));
            }
        }
        return resultado;
    }

    public void validarAtributosObligatorios(Map<String, Object> mapaAtributos, String nombreElemento, String[] obligatorios) {
        for (String atributo : obligatorios) {
            if (!mapaAtributos.containsKey(atributo)) {
                soportePrincipal.addSemanticError("El elemento " + nombreElemento + " requiere el atributo: " + atributo);
            }
        }
    }
}
