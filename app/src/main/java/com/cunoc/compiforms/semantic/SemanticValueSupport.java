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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ayuda a convertir y validar los valores que vienen del lenguaje.
 * Por ejemplo: convertir un texto como "10.5" en un número real.
 */
public class SemanticValueSupport {
    private final ParserSemanticSupport mainSupport;
    private static final Pattern STAR_COUNT_PATTERN = Pattern.compile("@\\[:star(?:-|:)(\\d+|number):\\]");

    public SemanticValueSupport(ParserSemanticSupport mainSupport) {
        this.mainSupport = mainSupport;
    }

    /**
     * Intenta convertir cualquier cosa a un número Decimal (Double).
     */
    public Double convertirADouble(Object valorGenerico) {
        if (valorGenerico == null) {
            return null;
        }

        if (valorGenerico instanceof ResultadoValor) {
            valorGenerico = ((ResultadoValor) valorGenerico).valor;
        }

        if (valorGenerico instanceof Number) {
            return ((Number) valorGenerico).doubleValue();
        }

        try {
            return Double.parseDouble(valorGenerico.toString());
        } catch (Exception e) {
            return null;
        }
    }

    public Integer convertirAEntero(Object valorGenerico) {
        Double numeroDecimal = convertirADouble(valorGenerico);
        if (numeroDecimal == null) {
            return null;
        }
        return numeroDecimal.intValue();
    }

    public String convertirATexto(Object valorGenerico) {
        if (valorGenerico == null) {
            return "";
        }

        if (valorGenerico instanceof ResultadoValor) {
            valorGenerico = ((ResultadoValor) valorGenerico).valor;
        }

        return aplicarCodigosEmoji(valorGenerico.toString());
    }

    /**
     * Quita las comillas de los textos. Ejemplo: "\"Hola\"" a "Hola"
     */
    public String normalizarLiteralCadena(Object valorCrudo) {
        if (valorCrudo == null) {
            return "";
        }

        String texto = valorCrudo.toString();

        boolean tieneComillas = texto.startsWith("\"") && texto.endsWith("\"");
        if (tieneComillas && texto.length() >= 2) {
            return aplicarCodigosEmoji(texto.substring(1, texto.length() - 1));
        }
        return aplicarCodigosEmoji(texto);
    }

    private String aplicarCodigosEmoji(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }

        String resultado = texto
            .replace("@[:smile:]", "\uD83D\uDE04")
            .replace("@[:)]", "\uD83D\uDE04")
            .replace("@[:sad:]", "\uD83D\uDE1E")
            .replace("@[:(]", "\uD83D\uDE1E")
            .replace("@[:serious:]", "\uD83D\uDE10")
            .replace("@[:|]", "\uD83D\uDE10")
            .replace("@[:heart:]", "\u2764\uFE0F")
            .replace("@[<3]", "\u2764\uFE0F")
            .replace("@[:cat:]", "\uD83D\uDE3A")
            .replace("@[:^:]", "\uD83D\uDE3A")
            .replace("@[:star:]", "\u2B50");

        Matcher matcher = STAR_COUNT_PATTERN.matcher(resultado);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String countToken = matcher.group(1);
            int count;
            if ("number".equalsIgnoreCase(countToken)) {
                count = 1;
            } else {
                count = Integer.parseInt(countToken);
            }

            if (count < 0) {
                count = 0;
            }
            if (count > 30) {
                count = 30;
            }

            String estrellas = "\u2B50".repeat(count);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(estrellas));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    // --- MÉTODOS PARA EXTRAER LISTAS ---

    public ArrayList<Object> extraerListaObjetos(Object valor) {
        if (valor instanceof List) {
            return new ArrayList<>((List<?>) valor);
        }

        ArrayList<Object> listaNueva = new ArrayList<>();
        if (valor != null) {
            listaNueva.add(valor);
        }
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
            if (numero != null) {
                enteros.add(numero);
            }
        }
        return enteros;
    }

    // --- CONVERSORES DE ESTILOS Y DISEÑO ---

    public Orientacion convertirAOrientacion(Object valor) {
        String texto = convertirATexto(valor).toUpperCase();
        if (texto.contains("HORIZONTAL")) {
            return Orientacion.HORIZONTAL;
        }
        return Orientacion.VERTICAL;
    }

    public FontFamily convertirAFuente(Object valor) {
        String texto = convertirATexto(valor).toUpperCase();
        if (texto.contains("MONO")) {
            return FontFamily.MONO;
        }
        if (texto.contains("SANS")) {
            return FontFamily.SANS_SERIF;
        }
        if (texto.contains("CURSIVE")) {
            return FontFamily.CURSIVE;
        }
        return FontFamily.SANS_SERIF;
    }

    public BorderType convertirATipoBorde(Object valor) {
        String texto = convertirATexto(valor).toUpperCase();
        if (texto.contains("DOTTED")) {
            return BorderType.DOTTED;
        }
        if (texto.contains("DOUBLE")) {
            return BorderType.DOUBLE;
        }
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
        ColorValue colorBorde;
        if (color instanceof ColorValue) {
            colorBorde = (ColorValue) color;
        } else {
            colorBorde = crearColorNombrado("BLACK");
        }

        return new BorderStyle(grosor, tipoBorde, colorBorde);
    }

    public StyleSet construirEstilos(Object valor) {
        if (valor instanceof Map) {
            Map<?, ?> mapa = (Map<?, ?>) valor;

            ColorValue colorTexto = null;
            if (mapa.get("textColor") instanceof ColorValue) {
                colorTexto = (ColorValue) mapa.get("textColor");
            }

            ColorValue colorFondo = null;
            if (mapa.get("backgroundColor") instanceof ColorValue) {
                colorFondo = (ColorValue) mapa.get("backgroundColor");
            }

            FontFamily familiaFuente = null;
            if (mapa.get("fontFamily") instanceof FontFamily) {
                familiaFuente = (FontFamily) mapa.get("fontFamily");
            }

            Double tamano = convertirADouble(mapa.get("textSize"));

            BorderStyle borde = null;
            if (mapa.get("border") instanceof BorderStyle) {
                borde = (BorderStyle) mapa.get("border");
            }

            return new StyleSet(colorTexto, colorFondo, familiaFuente, tamano, borde);
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
                    mainSupport.addSemanticError("¡Cuidado! El atributo '" + nombreClave + "' está repetido.");
                }
                resultado.put(nombreClave, mapa2.get(clave));
            }
        }
        return resultado;
    }

    public void validarAtributosObligatorios(Map<String, Object> mapaAtributos, String nombreElemento, String[] obligatorios) {
        for (String atributo : obligatorios) {
            if (!mapaAtributos.containsKey(atributo)) {
                mainSupport.addSemanticError("El elemento " + nombreElemento + " requiere el atributo: " + atributo);
            }
        }
    }
}
