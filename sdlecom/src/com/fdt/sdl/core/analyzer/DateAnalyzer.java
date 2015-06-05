package com.fdt.sdl.core.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class DateAnalyzer extends Analyzer {
	
	 private static final Analyzer STANDARD = new StandardAnalyzer();

	 public final TokenStream tokenStream(String paramString, Reader paramReader) {
         return STANDARD.tokenStream(paramString, paramReader);
	 }
}













/*

package com.fdt.falcondatalayer.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.TokenStream;

public class DateAnalyzer extends Analyzer {
	
	/** This code has been kept like this so that this can be re visited later **/

	// public final TokenStream tokenStream(String paramString, Reader
	// paramReader) {
	// System.out.println(paramReader.getClass().getName());
	// String convertedDate =
	// DateUtil.convertDateToFormatyyyymmdd(this.convertString(paramReader));
	// System.out.println("======>>>" + convertedDate);
	// Reader convertedParamReader = new StringReader(convertedDate);
	// TokenStream result = new StandardTokenizer(convertedParamReader);
	// return result;
	// }
	//
	// private String convertString(Reader reader) {
	// String convertedString = null;
	// try {
	// BufferedReader in = new BufferedReader(reader);
	// StringBuffer stringBuffer = new StringBuffer();
	// String line = in.readLine();
	// while (line != null) {
	// stringBuffer.append(line);
	// line = in.readLine();
	// }
	// convertedString = stringBuffer.toString();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return convertedString;
	// }
//}