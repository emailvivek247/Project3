
/*
    Phonetix for Lucene: phonetic algorithms for the Lucene search-engine.
    Copyright (C) 2001-2003  Claus Engel

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package com.fdt.sdl.core.analyzer.phonetix.lucene;

import java.io.Reader;
import java.util.Set;

import com.fdt.sdl.core.analyzer.phonetix.PhoneticEncoder;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * Filters {@link StandardTokenizer} with {@link StandardFilter},
 * {@link LowerCaseFilter}, {@link StopFilter}, and {@link PhoneticFilter}.
 * <br><br>
 * To use a <tt>PhoneticAnalyzer</tt>, simply build it with a
 * {@link PhoneticEncoder} and use it to create and search indices
 * of Lucene.
 *
 * @see com.tangentum.phonetix.Soundex
 * @see com.tangentum.phonetix.Metaphone
 * @see com.tangentum.phonetix.DoubleMetaphone
 * @author Claus Engel
 */
public class PhoneticAnalyzer extends Analyzer
{
    protected final PhoneticEncoder encoder;
    protected final Set stopTable;

    /**
     * An array containing some common words that are not usually useful for searching.
     */
    private static final String[] STOP_WORDS =
    {
        "a", "and", "are", "as", "at", "be", "but", "by",
        "for", "if", "in", "into", "is", "it",
        "no", "not", "of", "on", "or", "s", "such",
        "t", "that", "the", "their", "then", "there", "these",
        "they", "this", "to", "was", "will", "with"
    };

    /**
     * Builds an analyzer with the given phonetic encoder.
     */
    public PhoneticAnalyzer (final PhoneticEncoder phoneticEncoder)
    {
        encoder = phoneticEncoder;
        stopTable = StopFilter.makeStopSet(STOP_WORDS);
    }

    /**
     * Builds an analyzer with the given phonetic encoder
     * and the given stop words.
     */
    public PhoneticAnalyzer (final PhoneticEncoder phoneticEncoder, final String[] stopWords)
    {
        encoder = phoneticEncoder;
        stopTable = StopFilter.makeStopSet(stopWords);
    }

    /**
     * Returns the phonetic encoder.
     */
    public final PhoneticEncoder encoder()
    {
        return encoder;
    }

    /**
     * Constructs a {@link StandardTokenizer} filtered by a {@link
     * StandardFilter}, a {@link LowerCaseFilter}, a {@link StopFilter},
     * and a {@link PhoneticFilter}.
     */
    public TokenStream tokenStream (String fieldname, final Reader reader)
    {
        TokenStream result = new StandardTokenizer(reader);
        result = new StandardFilter(result);
        result = new LowerCaseFilter(result);
        result = new StopFilter(result, stopTable);
        result = new PhoneticFilter(result,encoder);
        return result;
    }

}

