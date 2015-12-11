package com.fdt.elasticsearch.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.javacoding.xsearch.config.Column;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fdt.sdl.core.analyzer.synonym.SynonymText;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;

public abstract class AbstractAnalyzer {

    protected final ObjectMapper mapper = new ObjectMapper();
 
    protected final List<AbstractTokenizer> tokenizers = new ArrayList<>();
    protected final List<AbstractTokenFilter> tokenFilters = new ArrayList<>();
    protected final List<AbstractCharFilter> charFilters = new ArrayList<>();
    protected final String name;

    public AbstractAnalyzer(String name) {
        this.name = name;
    }

    public abstract ObjectNode getAsJsonObject();

    public String getName() {
        return name;
    }

    public void addTokenizer(AbstractTokenizer tokenizer) {
        tokenizers.add(tokenizer);
    }

    public List<AbstractTokenizer> getTokenizers() {
        return tokenizers;
    }

    public void addTokenFilter(AbstractTokenFilter charFilter) {
        tokenFilters.add(charFilter);
    }

    public List<AbstractTokenFilter> getTokenFilters() {
        return tokenFilters;
    }

    public void addCharFilter(AbstractCharFilter charFilter) {
        charFilters.add(charFilter);
    }

    public List<AbstractCharFilter> getCharFilters() {
        return charFilters;
    }

    public String getAsString() {
        return writeToString(getAsJsonObject());
    }

    public String getAsStringPrettyPrint() {
        return writeToString(getAsJsonObject(), true);
    }

    private String writeToString(ObjectNode objectNode) {
        try {
            return mapper.writeValueAsString(objectNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String writeToString(ObjectNode objectNode, boolean pretty) {
        if (pretty) {
            try {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            return writeToString(objectNode);
        }
    }

    public static Optional<AbstractAnalyzer> fromColumn(Column column) {
        Optional<AbstractAnalyzer> result = Optional.empty();
        if (column != null) {
            String analyzerClassName = column.getAnalyzerName();
            if (analyzerClassName != null && !analyzerClassName.isEmpty()) {
                result = AbstractAnalyzer.fromAnalyzerClassName(analyzerClassName);
            }
        }
        return result;
    }

    public static Optional<AbstractAnalyzer> fromAnalyzerClassName(String analyzerClassName) {
        AbstractAnalyzer result = null;
        switch (analyzerClassName) {
        case "com.fdt.sdl.core.analyzer.NumberLowerCaseAnalyzer":
            result = new PatternAnalyzer
                    .Builder(getESAnalyzerName(analyzerClassName))
                    .withPattern("[\\W_]+")
                    .withLowercase(true)
                    .build();
            break;
        case "com.fdt.sdl.core.analyzer.AlphaNumericAnalyzer":
            result = new CustomAnalyzer
                    .Builder(getESAnalyzerName(analyzerClassName))
                    .withTokenizerName("standard")
                    .addTokenFilterName("standard")
                    .addTokenFilterName("lowercase")
                    .addTokenFilterName("empty_stopwords_filter")
                    .addCharFilterName("alpha_numeric_char_filter")
                    .build();
            result.addCharFilter(new PatternReplaceCharFilter
                    .Builder("alpha_numeric_char_filter")
                    .withPattern("[^A-Za-z0-9]")
                    .withReplacement("")
                    .build());
            result.addTokenFilter(new StopTokenFilter
                    .Builder("empty_stopwords_filter")
                    .withStopwordLanguageSet("_none_")
                    .build());
            break;
        case "com.fdt.sdl.core.analyzer.DoubleMetaphoneAnalyzer":
            result = new CustomAnalyzer
                    .Builder(getESAnalyzerName(analyzerClassName))
                    .withTokenizerName("standard")
                    .addTokenFilterName("standard")
                    .addTokenFilterName("lowercase")
                    .addTokenFilterName("stop")
                    .addTokenFilterName("double_metaphone_filter")
                    .build();
            result.addTokenFilter(new PhoneticTokenFilter
                    .Builder("double_metaphone_filter")
                    .withEncoder("double_metaphone")
                    .build());
            break;
        case "com.fdt.sdl.core.analyzer.DateAnalyzer":
            // This is, I promise, what the current DateAnalyzer class does
            result = new StandardAnalyzer
                    .Builder(getESAnalyzerName(analyzerClassName))
                    .withStopwordLanguageSet("_english_")
                    .build();
            break;
        case "com.fdt.sdl.core.analyzer.KeywordCaseInsensitiveAlphNumericAnalyzer":
            result = new CustomAnalyzer
                    .Builder(getESAnalyzerName(analyzerClassName))
                    .withTokenizerName("keyword_1024_tokenizer")
                    .addTokenFilterName("lowercase")
                    .addCharFilterName("alpha_numeric_space_char_filter")
                    .build();
            result.addTokenizer(new KeywordTokenizer
                    .Builder("keyword_1024_tokenizer")
                    .withBufferSize(1024)
                    .build());
            result.addCharFilter(new PatternReplaceCharFilter
                    .Builder("alpha_numeric_space_char_filter")
                    .withPattern("[^ A-Za-z0-9]")
                    .withReplacement("")
                    .build());
            break;
        case "com.fdt.sdl.core.analyzer.OneWordNumberLowerCaseAnalyzer":
            result = new CustomAnalyzer
                    .Builder(getESAnalyzerName(analyzerClassName))
                    .withTokenizerName("whitespace")
                    .addTokenFilterName("lowercase")
                    .addCharFilterName("alpha_numeric_space_char_filter")
                    .build();
            result.addCharFilter(new PatternReplaceCharFilter
                    .Builder("alpha_numeric_space_char_filter")
                    .withPattern("[^ A-Za-z0-9]")
                    .withReplacement("")
                    .build());
            break;
        case "com.fdt.sdl.core.analyzer.synonym.SynonymAlgorithm":
            result = new CustomAnalyzer
                    .Builder(getESAnalyzerName(analyzerClassName))
                    .withTokenizerName("standard")
                    .addTokenFilterName("standard")
                    .addTokenFilterName("lowercase")
                    .addTokenFilterName("synonym_token_filter")
                    .build();
            result.addTokenFilter(new SynonymTokenFilter
                    .Builder("synonym_token_filter")
                    .addSynonym(SynonymText.getSynonyms())
                    .build());
            break;
        case "org.apache.lucene.analysis.WhitespaceAnalyzer":
            result = new WhitespaceAnalyzer
                    .Builder(getESAnalyzerName(analyzerClassName))
                    .build();
            break;
        case "org.apache.lucene.analysis.standard.StandardAnalyzer":
            // The default for the ES standard analyzer is an empty stopword set but
            // for the Lucene StandardAnalyzer class, the default is English stopwords
            // so we will preserve that default here
            result = new StandardAnalyzer
                    .Builder(getESAnalyzerName(analyzerClassName))
                    .withStopwordLanguageSet("_english_")
                    .build();
            break;
        }
        return Optional.ofNullable(result);
    }

    public static String getESAnalyzerName(String analyzerClassName) {
        String justClassName = analyzerClassName.substring(analyzerClassName.lastIndexOf(".") + 1);
        Converter<String, String> converter = CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);
        return converter.convert(justClassName);
    }
}
