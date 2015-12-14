package com.fdt.elasticsearch.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.javacoding.xsearch.config.Column;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fdt.elasticsearch.util.StopwordLoader;
import com.fdt.elasticsearch.util.SynonymLoader;
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
            boolean synAndStop = column.getNeedSynonymsAndStopwords();
            if (analyzerClassName != null && !analyzerClassName.isEmpty()) {
                result = AbstractAnalyzer.fromAnalyzerClassName(analyzerClassName, synAndStop);
            }
        }
        return result;
    }

    public static Optional<AbstractAnalyzer> fromAnalyzerClassName(String analyzerClassName) {
        return fromAnalyzerClassName(analyzerClassName, false);
    }

    public static Optional<AbstractAnalyzer> fromAnalyzerClassName(String analyzerClassName, boolean synAndStop) {
        AbstractAnalyzer result = null;
        CustomAnalyzer.Builder builder = null;
        switch (analyzerClassName) {
        case "com.fdt.sdl.core.analyzer.NumberLowerCaseAnalyzer":
            if (!synAndStop) {
                result = new PatternAnalyzer
                        .Builder(getESAnalyzerName(analyzerClassName))
                        .withPattern("[\\W_]+")
                        .withLowercase(true)
                        .build();
            } else {
                result = new CustomAnalyzer
                        .Builder(getESAnalyzerName(analyzerClassName))
                        .withTokenizerName("non_word_tokenizer")
                        .addTokenFilterName("lowercase")
                        .addTokenFilterName("custom_dictionary_stopwords")
                        .addTokenFilterName("custom_dictionary_synonyms")
                        .build();
                result.addTokenizer(new PatternTokenizer
                        .Builder("non_word_tokenizer")
                        .withPattern("[\\W_]+")
                        .build());
                result.addTokenFilter(new StopTokenFilter
                        .Builder("custom_dictionary_stopwords")
                        .addStopword(StopwordLoader.getStopwords())
                        .build());
                result.addTokenFilter(new SynonymTokenFilter
                        .Builder("custom_dictionary_synonyms")
                        .addSynonym(SynonymLoader.getSynonymsFromDictionaryFile())
                        .build());
            }
            break;
        case "com.fdt.sdl.core.analyzer.AlphaNumericAnalyzer":
            builder = new CustomAnalyzer
                    .Builder(getESAnalyzerName(analyzerClassName))
                    .withTokenizerName("standard")
                    .addTokenFilterName("standard")
                    .addTokenFilterName("lowercase")
                    .addCharFilterName("alpha_numeric_char_filter");
            if (!synAndStop) {
                result = builder
                        .addTokenFilterName("empty_stopwords_filter")
                        .build();
                result.addTokenFilter(new StopTokenFilter
                        .Builder("empty_stopwords_filter")
                        .withStopwordLanguageSet("_none_")
                        .build());
            } else {
                result = builder
                        .addTokenFilterName("custom_dictionary_stopwords")
                        .addTokenFilterName("custom_dictionary_synonyms")
                        .build();
                result.addTokenFilter(new StopTokenFilter
                        .Builder("custom_dictionary_stopwords")
                        .addStopword(StopwordLoader.getStopwords())
                        .build());
                result.addTokenFilter(new SynonymTokenFilter
                        .Builder("custom_dictionary_synonyms")
                        .addSynonym(SynonymLoader.getSynonymsFromDictionaryFile())
                        .build());
            }
            result.addCharFilter(new PatternReplaceCharFilter
                    .Builder("alpha_numeric_char_filter")
                    .withPattern("[^A-Za-z0-9]")
                    .withReplacement("")
                    .build());
            break;
        case "com.fdt.sdl.core.analyzer.DoubleMetaphoneAnalyzer":
            builder = new CustomAnalyzer
                    .Builder(getESAnalyzerName(analyzerClassName))
                    .withTokenizerName("standard")
                    .addTokenFilterName("standard")
                    .addTokenFilterName("lowercase")
                    .addTokenFilterName("stop")
                    .addTokenFilterName("double_metaphone_filter");
            if (!synAndStop) {
                result = builder.build();
            } else {
                result = builder
                        .addTokenFilterName("custom_dictionary_stopwords")
                        .addTokenFilterName("custom_dictionary_synonyms")
                        .build();
                result.addTokenFilter(new StopTokenFilter
                        .Builder("custom_dictionary_stopwords")
                        .addStopword(StopwordLoader.getStopwords())
                        .build());
                result.addTokenFilter(new SynonymTokenFilter
                        .Builder("custom_dictionary_synonyms")
                        .addSynonym(SynonymLoader.getSynonymsFromDictionaryFile())
                        .build());
            }
            result.addTokenFilter(new PhoneticTokenFilter
                    .Builder("double_metaphone_filter")
                    .withEncoder("double_metaphone")
                    .build());
            break;
        case "com.fdt.sdl.core.analyzer.DateAnalyzer":
            // This is, I promise, what the current DateAnalyzer class does
            if (!synAndStop) {
                result = new StandardAnalyzer
                        .Builder(getESAnalyzerName(analyzerClassName))
                        .withStopwordLanguageSet("_english_")
                        .build();
            } else {
                result = new CustomAnalyzer
                        .Builder(getESAnalyzerName(analyzerClassName))
                        .withTokenizerName("standard")
                        .addTokenFilterName("standard")
                        .addTokenFilterName("lowercase")
                        .addTokenFilterName("stop")
                        .addTokenFilterName("custom_dictionary_stopwords")
                        .addTokenFilterName("custom_dictionary_synonyms")
                        .build();
                result.addTokenFilter(new StopTokenFilter
                        .Builder("custom_dictionary_stopwords")
                        .addStopword(StopwordLoader.getStopwords())
                        .build());
                result.addTokenFilter(new SynonymTokenFilter
                        .Builder("custom_dictionary_synonyms")
                        .addSynonym(SynonymLoader.getSynonymsFromDictionaryFile())
                        .build());
            }
            break;
        case "com.fdt.sdl.core.analyzer.KeywordCaseInsensitiveAlphNumericAnalyzer":
            builder = new CustomAnalyzer
                    .Builder(getESAnalyzerName(analyzerClassName))
                    .withTokenizerName("keyword_1024_tokenizer")
                    .addTokenFilterName("lowercase")
                    .addCharFilterName("alpha_numeric_space_char_filter");
            if (!synAndStop) {
                result = builder.build();
            } else {
                result = builder
                        .addTokenFilterName("custom_dictionary_stopwords")
                        .addTokenFilterName("custom_dictionary_synonyms")
                        .build();
                result.addTokenFilter(new StopTokenFilter
                        .Builder("custom_dictionary_stopwords")
                        .addStopword(StopwordLoader.getStopwords())
                        .build());
                result.addTokenFilter(new SynonymTokenFilter
                        .Builder("custom_dictionary_synonyms")
                        .addSynonym(SynonymLoader.getSynonymsFromDictionaryFile())
                        .build());
            }
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
            builder = new CustomAnalyzer
                    .Builder(getESAnalyzerName(analyzerClassName))
                    .withTokenizerName("whitespace")
                    .addTokenFilterName("lowercase")
                    .addCharFilterName("alpha_numeric_space_char_filter");
            if (!synAndStop) {
                result = builder.build();
            } else {
                result = builder
                        .addTokenFilterName("custom_dictionary_stopwords")
                        .addTokenFilterName("custom_dictionary_synonyms")
                        .build();
                result.addTokenFilter(new StopTokenFilter
                        .Builder("custom_dictionary_stopwords")
                        .addStopword(StopwordLoader.getStopwords())
                        .build());
                result.addTokenFilter(new SynonymTokenFilter
                        .Builder("custom_dictionary_synonyms")
                        .addSynonym(SynonymLoader.getSynonymsFromDictionaryFile())
                        .build());
            }
            result.addCharFilter(new PatternReplaceCharFilter
                    .Builder("alpha_numeric_space_char_filter")
                    .withPattern("[^ A-Za-z0-9]")
                    .withReplacement("")
                    .build());
            break;
        case "com.fdt.sdl.core.analyzer.synonym.SynonymAlgorithm":
            builder = new CustomAnalyzer
                    .Builder(getESAnalyzerName(analyzerClassName))
                    .withTokenizerName("standard")
                    .addTokenFilterName("standard")
                    .addTokenFilterName("lowercase")
                    .addTokenFilterName("custom_sys_prop_synonyms");
            if (!synAndStop) {
                result = builder.build();
            } else {
                result = builder
                        .addTokenFilterName("custom_dictionary_stopwords")
                        .addTokenFilterName("custom_dictionary_synonyms")
                        .build();
                result.addTokenFilter(new StopTokenFilter
                        .Builder("custom_dictionary_stopwords")
                        .addStopword(StopwordLoader.getStopwords())
                        .build());
                result.addTokenFilter(new SynonymTokenFilter
                        .Builder("custom_dictionary_synonyms")
                        .addSynonym(SynonymLoader.getSynonymsFromDictionaryFile())
                        .build());
            }
            result.addTokenFilter(new SynonymTokenFilter
                    .Builder("custom_sys_prop_synonyms")
                    .addSynonym(SynonymLoader.getSynonymsFromSystemProperties())
                    .build());
            break;
        case "org.apache.lucene.analysis.WhitespaceAnalyzer":
            if (!synAndStop) {
                result = new WhitespaceAnalyzer
                        .Builder(getESAnalyzerName(analyzerClassName))
                        .build();
            } else {
                result = new CustomAnalyzer
                        .Builder(getESAnalyzerName(analyzerClassName))
                        .withTokenizerName("whitespace")
                        .addTokenFilterName("custom_dictionary_stopwords")
                        .addTokenFilterName("custom_dictionary_synonyms")
                        .build();
                result.addTokenFilter(new StopTokenFilter
                        .Builder("custom_dictionary_stopwords")
                        .addStopword(StopwordLoader.getStopwords())
                        .build());
                result.addTokenFilter(new SynonymTokenFilter
                        .Builder("custom_dictionary_synonyms")
                        .addSynonym(SynonymLoader.getSynonymsFromDictionaryFile())
                        .build());
            }
            break;
        case "org.apache.lucene.analysis.standard.StandardAnalyzer":
            // The default for the ES standard analyzer is an empty stopword set but
            // for the Lucene StandardAnalyzer class, the default is English stopwords
            // so we will preserve that default here
            if (!synAndStop) {
                result = new StandardAnalyzer
                        .Builder(getESAnalyzerName(analyzerClassName))
                        .withStopwordLanguageSet("_english_")
                        .build();
            } else {
                result = new CustomAnalyzer
                        .Builder(getESAnalyzerName(analyzerClassName))
                        .withTokenizerName("standard")
                        .addTokenFilterName("standard")
                        .addTokenFilterName("lowercase")
                        .addTokenFilterName("stop")
                        .addTokenFilterName("custom_dictionary_stopwords")
                        .addTokenFilterName("custom_dictionary_synonyms")
                        .build();
                result.addTokenFilter(new StopTokenFilter
                        .Builder("custom_dictionary_stopwords")
                        .addStopword(StopwordLoader.getStopwords())
                        .build());
                result.addTokenFilter(new SynonymTokenFilter
                        .Builder("custom_dictionary_synonyms")
                        .addSynonym(SynonymLoader.getSynonymsFromDictionaryFile())
                        .build());
            }
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
