package br.com.smn.tools.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class SMNUtilities {
    static NumberFormat nf = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(new Locale("pt", "BR")));

    public static String getMonetaryStringValue(double value){
        return nf.format(value);
    }
}
