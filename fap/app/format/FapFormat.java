package format;

import java.math.BigDecimal;

public class FapFormat {

	public static String format(Object value) {
		if (value == null)
			return "";
		if (value instanceof BigDecimal) {
				Integer decimales = properties.FapProperties.getInt("fap.format.moneda");
				if (decimales == null) {
					return null;
				}
	      		java.text.DecimalFormat decim = (java.text.DecimalFormat) java.text.NumberFormat.getInstance(java.util.Locale.US);
				java.text.DecimalFormatSymbols dfs = new java.text.DecimalFormatSymbols();
				char ch = ',';
				dfs.setDecimalSeparator(ch);
				ch = '.';
				dfs.setGroupingSeparator(ch);
				decim.setMaximumIntegerDigits(308);
				decim.setDecimalFormatSymbols(dfs);
				decim.setMaximumFractionDigits(decimales);
				return decim.format(value);
		}

		return value.toString();
	}
	
	public static String format(BigDecimal value, String tipo) {
		if (value == null)
			return "";
		
		Integer decimales = properties.FapProperties.getInt("fap.format."+tipo);
		if (decimales == null) {
			return "";
		}
		
  		java.text.DecimalFormat decim = (java.text.DecimalFormat) java.text.NumberFormat.getInstance(java.util.Locale.US);
		java.text.DecimalFormatSymbols dfs = new java.text.DecimalFormatSymbols();
		char ch = ',';
		dfs.setDecimalSeparator(ch);
		ch = '.';
		dfs.setGroupingSeparator(ch);
		decim.setMaximumIntegerDigits(308);
		decim.setDecimalFormatSymbols(dfs);
		decim.setMaximumFractionDigits(decimales);
		return decim.format(value);
	}
}