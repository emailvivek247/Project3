package com.fdt.sdl.core.analyzer;

import org.apache.lucene.analysis.*;
import java.io.*;

import org.apache.lucene.analysis.snowball.*;


public final class SpanishSnowballAnalyzer extends Analyzer{
    Analyzer a = new SnowballAnalyzer("Spanish");
    public SpanishSnowballAnalyzer(){}

    public TokenStream tokenStream(String fieldName, Reader aReader){
        return a.tokenStream(fieldName, aReader);
    }
}
