/*
 */
package net.javacoding.xsearch.utility;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used in configAnalyzer.vm to render analyzed tokens
 * 
 */
public class AnalyzerUtil {
    private static Logger logger = LoggerFactory.getLogger(AnalyzerUtil.class.getName());

    public static Token[] tokensFromAnalysis(Analyzer analyzer, String text) throws IOException {
        TokenStream stream = analyzer.tokenStream("contents", new StringReader(text));
        ArrayList<Token> tokenList = new ArrayList<Token>();
        while (true) {
            Token token = stream.next();
            if (token == null) break;

            tokenList.add(token);
        }
        stream.close();

        return (Token[]) tokenList.toArray(new Token[0]);
    }
    public static Token[] tokensFromAnalysis(String analyzerClassName, String text) {
        if(analyzerClassName==null || text == null) return null;
        try {
            return tokensFromAnalysis((Analyzer) (Class.forName(analyzerClassName).newInstance()) , text);
        } catch (Throwable e){
            logger.warn("Error from "+analyzerClassName+":",e);
            e.printStackTrace();
        }
        return null;
    }
    
    public static String convertReaderToString(Reader reader) {
    	String string = null;
    	StringBuffer inputData = new StringBuffer(1000);
		char[] buffer = new char[1024];
		int numRead = 0;
		try {
			while ((numRead = reader.read(buffer)) != -1) {
				String readData = String.valueOf(buffer, 0, numRead);
				inputData.append(readData);
				buffer = new char[1024];
			}
			reader.close();
		} catch (IOException ioe) {
			return null;
		}
		string = inputData.toString();   	
    	return string;
    }
    
    public static Reader convertStringToReader(String string) {
    	Reader reader = null;
    	InputStream inputStream = new ByteArrayInputStream(string.getBytes());
		reader = new BufferedReader(new InputStreamReader(inputStream));
		return reader;
    }
    
    public static Reader trimReader(Reader reader) {
    	Reader outputReader = null;
    	String trimmedString = null;
    	String string = convertReaderToString(reader);
		if(string== null) {
			return null;
		}
		trimmedString = string.trim();
		outputReader = convertStringToReader(trimmedString);
		return outputReader;    	
    }
    
    public static Reader removeSpecialCharactersReader(Reader reader) {
    	Reader outputReader = null;
    	String outputString = null;
    	String string = convertReaderToString(reader);
		if(string== null) {
			return null;
		}
		outputString = string.replaceAll("[^A-Za-z0-9]", "");
		outputReader = convertStringToReader(outputString);
		return outputReader;
    }
    

}
