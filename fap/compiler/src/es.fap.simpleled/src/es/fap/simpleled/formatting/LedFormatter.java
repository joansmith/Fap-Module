/*
 * generated by Xtext
 */
package es.fap.simpleled.formatting;

import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.formatting.impl.AbstractDeclarativeFormatter;
import org.eclipse.xtext.formatting.impl.FormattingConfig;
import org.eclipse.xtext.util.Pair;
import es.fap.simpleled.services.LedGrammarAccess;

/**
 * This class contains custom formatting description.
 * 
 * see : http://www.eclipse.org/Xtext/documentation/latest/xtext.html#formatting
 * on how and when to use it 
 * 
 * Also see {@link org.eclipse.xtext.xtext.XtextFormattingTokenSerializer} as an example
 */
public class LedFormatter extends AbstractDeclarativeFormatter {
	
	@Override
	protected void configureFormatting(FormattingConfig c) {
		
		LedGrammarAccess f = (LedGrammarAccess)getGrammarAccess();
		
		String[] elementosSaltoLineaFormularios = {"Grupo", "AgruparCampos", "Texto", "Fecha", "Combo", "Tabla", "SubirArchivo", "Boton", "Check", 
										"AgrupaBotones", "Nip", "Solicitante", "PersonaFisica", "PersonaJuridica", "Persona", "Direccion", "DireccionMapa",
										"Wiki", "AreaTexto", "Enlace", "Form", "SubirArchivoAed", "EditarArchivoAed", "FirmaSimple",
										"EntidadAutomatica", "Accion", "Columna"};
		
		String[] elementosSaltoLineaPermisos = {"vars", "when", "return", "else", "mensaje"};
		
		// Para que no corte las lineas, como fueron escritas en origen, nunca (aunque sean muy largas)
		c.setAutoLinewrap(10000);
	    
		// GLOBALMENTE  a cualquier fichero .FAP
		//		Para los comentarios
		// 			Comentarios en 1 sola linea // ...
		c.setLinewrap(1).before(f.getSL_COMMENTRule());
		//			Comentarios en multiples líneas /* ... */
	    c.setLinewrap(1).before(f.getML_COMMENTRule());
	    c.setLinewrap(1).after(f.getML_COMMENTRule());
	    //		Para que no separe identificadores con "." o con "?", en su nombre
	    for (Keyword signos : f.findKeywords(".", "?")) {
	    	c.setNoSpace().before(signos);
	    	c.setNoSpace().after(signos);
	    }
		// 		Para los bloques con '{' '}'
	    for (Pair<Keyword, Keyword> pair : f.findKeywordPairs("{", "}")) {
	    	// Despues del '{' colocamos un salto de línea
	    	c.setLinewrap(1).after(pair.getFirst());
	    	// Antes del '}' colocamos un salto de línea
	    	c.setLinewrap(1).before(pair.getSecond());
	    	// Despues del '}' colocamos un salto de línea
	    	c.setLinewrap(1).after(pair.getSecond());
	    	// Identamos todo lo que esté entre '{' '}'
	    	c.setIndentation(pair.getFirst(), pair.getSecond());
	    }

	    // Para los FORMULARIOS
	    // 		Para los elementos que se definen dentro de los bloques '{' '}'
	    for (Keyword elemento : f.findKeywords(elementosSaltoLineaFormularios)) {
	    	c.setLinewrap(1).before(elemento);
	    }
	    
	    // Para las LISTAS
	    // Para las listas hay 3 opciones:
	    // 		Para cuando sólo haya VALOR
	    c.setLinewrap(1).before(f.getElementoListaAccess().getValueSTRINGTerminalRuleCall_0_0());
	    //		Para cuando haya CLAVE-VALOR o sólo CLAVE
	    c.setLinewrap(1).before(f.getElementoListaAccess().getKeyAssignment_1_0());
	    //		Para cuando es una Lista DEPENDIENTE
	    c.setLinewrap(1).before(f.getElementoListaDependienteAccess().getRule());
	    
	    // Para los PERMISOS
	    int contador = f.findKeywords(elementosSaltoLineaPermisos).size();
	    int total = contador;
	    //		Para el contenido de los permisos
	    for (Keyword elemento : f.findKeywords(elementosSaltoLineaPermisos)) {
	    	c.setLinewrap(1).before(elemento);
	    	c.setLinewrap(1).after(elemento);
	    	if (contador !=  total){
	    		c.setIndentationDecrement().before(elemento);
	    		c.setIndentationIncrement().after(elemento);
	    	}
	    	contador--;
	    }
	    // 		Saltos de línea para separar los Permisos
	    c.setIndentationIncrement().after(f.getPermisoAccess().getLeftCurlyBracketKeyword_2());
	    c.setIndentationDecrement().before(f.getPermisoAccess().getRightCurlyBracketKeyword_6());
	    c.setLinewrap(2).after(f.getPermisoAccess().getRightCurlyBracketKeyword_6());	    
	}
}
