package com.fdt.sdl.core.analyzer.pt;

import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;


/**
 * <p>
 * Lucene Analyzer for brazilian portuguese language. This does not do stemmer
 * or others advanceds processing, only remove portuguese {@link #STOP_WORDS}
 * and avoid especial characters, like, but not only, "á", "ç", "õ", etc. Indeed,
 * any accentuated characters is parse to a similar non accentuated characterer.
 * For instance, "á" is parsed to "a". Further more, this analyzer does all
 * works made by a StandardAnalyzer.
 * <p>
 * Stop words can be assigned using:
 * <ul>
 * <li>{@link #PortugueseAnalyzer() Empty constructor}, which will use
 * {@link #STOP_WORDS default stop words} set;</li>
 * <li>{@link #PortugueseAnalyzer(String[]) String-array-based-constructor},
 * to determine your own stop words set;</li>
 * <li>{@link #PortugueseAnalyzer(String) String-comma-delimeted-constructor},
 * which will {@link java.lang.String#split(java.lang.String) split} a String
 * using comma like delimeter. In other words, if you provide "stop, words" the
 * stop words set will be {stop, word}</li>
 * </ul>
 *
 * @see org.apache.lucene.analysis.standard.StandardAnalyzer
 */
public class PortugueseAnalyzer extends Analyzer {

	/**
	 * <code>STOP_WORDS</code> contains a default set of stop words.
	 */
	public static final String[] STOP_WORDS = new String[] { "0", "1", "2",
			"3", "4", "5", "6", "7", "8", "9", "a", "ainda", "alem", "algum",
			"alguma", "alguns", "ali", "além", "ambas", "ambos", "ano", "anos",
			"antes", "ao", "aonde", "aos", "apenas", "apos", "aquela",
			"aquele", "aqueles", "as", "assim", "ato", "até", "b", "bem",
			"boa", "bom", "c", "cada", "cargo", "carta", "casa", "com", "como",
			"consta", "contra", "contudo", "cuja", "cujas", "cujo", "cujos",
			"d", "da", "daquele", "dar", "das", "data", "de", "dela", "dele",
			"deles", "demais", "depois", "desde", "desta", "deste", "deu",
			"dia", "dias", "dispoe", "dispoem", "dito", "diversa", "diversas",
			"diversos", "diz", "do", "dois", "dos", "dr", "duas", "durante",
			"e", "ela", "elas", "ele", "eles", "em", "enfim", "entao", "entre",
			"então", "era", "eram", "essa", "essas", "esse", "esses", "esta",
			"estas", "estava", "este", "estes", "f", "fazer", "fez", "ficou",
			"fim", "foi", "foram", "fr", "g", "gente", "geral", "h", "ha",
			"havia", "hoje", "há", "i", "isso", "isto", "j", "já", "k", "l",
			"lhe", "lhes", "logo", "lugar", "m", "maior", "mais", "mas", "me",
			"mediante", "menos", "mesma", "mesmas", "mesmo", "mesmos", "muito",
			"muitos", "n", "na", "nao", "nas", "nem", "nesse", "nesta",
			"neste", "no", "nome", "nos", "nossa", "nosso", "nossos", "nova",
			"novo", "não", "nós", "o", "onde", "ordem", "os", "ou", "outra",
			"outras", "outro", "outros", "p", "para", "parte", "pela", "pelas",
			"pelo", "pelos", "perante", "pois", "por", "porque", "portanto",
			"porém", "pouco", "propios", "proprio", "q", "quais", "qual",
			"qualquer", "quando", "quanto", "que", "quem", "quer", "r", "rua",
			"s", "se", "segundo", "seja", "sem", "sempre", "sendo", "ser",
			"seu", "seus", "sob", "sobre", "sua", "suas", "são", "só", "sôbre",
			"t", "tal", "tambem", "também", "tanto", "tem", "tendo", "ter",
			"teu", "teus", "teve", "tinha", "tinham", "toda", "todas", "todo",
			"todos", "três", "tua", "tuas", "tudo", "tão", "u", "um", "uma",
			"umas", "uns", "v", "veio", "vem", "vez", "vê", "w", "x", "y", "z",
			"à", "às", "é", "êle" };

	private Set stopWords = new HashSet();

	/**
	 * Construct a analyzer with {@link #STOP_WORDS default stop words} set
	 */
	public PortugueseAnalyzer () {

		this(STOP_WORDS);

	}

	/**
	 * @param stopWords
	 */
	public PortugueseAnalyzer ( String[] stopWords ) {

		this.stopWords = StopFilter.makeStopSet(stopWords);

	}

	/**
	 * @param words
	 */
	public PortugueseAnalyzer ( String words ) {

		this(makeArray(words));

	}

	/**
	 * @param fieldName
	 * @param reader
	 *
	 * @return
	 *
	 * @see StandardEnglishAnalyzer#tokenStream(java.lang.String, java.io.Reader)
	 */
	public TokenStream tokenStream( String fieldName, Reader reader ) {

		TokenStream result = new StandardTokenizer(reader);

		result = new StandardFilter(result);
		result = new LowerCaseFilter(result);
		result = new StopFilter(result, stopWords);
		result = new SpecialCharFilter(result);

		return result;

	}

	/**
	 * Construct a array from a String with comma separated words.
	 *
	 * @param words a String with comma separated words
	 *
	 * @return a array with all words from <tt>words</tt>
	 */
	private static String[] makeArray( String words ) {

		String[] split = words.split(", ");
		String[] result = new String[split.length];

		for (int i = 0; i < split.length; i++) {

			result[i] = split[i].trim().toLowerCase();

		}

		return result;

	}

}
