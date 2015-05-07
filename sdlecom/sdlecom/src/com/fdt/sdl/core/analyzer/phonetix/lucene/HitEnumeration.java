
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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.Hits;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;


/**
 * An <tt>HitEnumeration</tt> adapts the results of a Lucene search
 * to the standard Java collections.
 *
 * @author Claus Engel
 */
public final class HitEnumeration implements Enumeration
{
    private final Hashtable visited;
    private final Hits hits;
    private int index;

    /**
     * Constructs an enumeration over the specified hits. If unique is
     * <code>true</code>, each document is only returned once.
     */
    public HitEnumeration (final Hits hits, boolean unique)
    {
        this.hits    = hits;
        this.index   = 0;
        this.visited = unique ? new Hashtable(31) : null;
    }

    /**
     * Constructs an enumeration over the specified hits, which returns
     * each document only once.
     */
    public HitEnumeration (final Hits hits)
    {
        this(hits, true);
    }

    public synchronized boolean hasMoreElements()
    {
        return index < hits.length();
    }

    public synchronized Object nextElement()
    {
        if (index < hits.length())
        {
            try
            {
                final Document result = hits.doc(index++);
                if (visited != null)
                {
                    visited.put(new RankedDocument(result),null);

                    try
                    {
                        // advance to next valid (i.e. unvisited) index
                        for (; index < hits.length(); index++)
                        {
                            final Document doc = hits.doc(index);
                            if (!visited.contains(new RankedDocument(doc)))
                                break; // index is on next valid document-index
                        }
                    }
                    catch (java.io.IOException e)
                    {
                        index = hits.length();
                    }
                }

                return result;
            }
            catch (java.io.IOException e)
            {
                index = hits.length();
            }
        }
        throw new java.util.NoSuchElementException();
    }

    // the Documents doesn't have a valid "equals"-operator, so we have
    // to wrap it cleanly here, to make Documents comparable
    private static class RankedDocument
    {
        private final Document document;
        private int hashCode;

        private RankedDocument (final Document doc)
        {
            document = doc;

            hashCode = 0;
            List fields = doc.getFields();
            for (int k=0; k<fields.size() ;k++) {
                Field f = (Field)fields.get(k);
                hashCode ^= hashCode(f);
            }
        }

        public int hashCode()
        {
            return hashCode;
        }

        public boolean equals (final Object o)
        {
            if (this == o)
                return true;
            else if (o instanceof RankedDocument)
            {
                RankedDocument doc = (RankedDocument) o;
                if (hashCode != doc.hashCode)
                    return false;

                final List fields1 = document.getFields(), fields2 = doc.document.getFields();
                if(fields1.size()!=fields2.size()) {
                    return false;
                }
                for(int i=0;i<fields1.size();i++) {
                    if(!equals((Field)fields1.get(i),(Field)fields2.get(i))) {
                        return false;
                    }
                }
                return true;
            }
            else
                return false;
        }

        static int hashCode (final Field f)
        {
            return f.toString().hashCode();
        }

        static boolean equals (final Field f1, final Field f2)
        {
            if (f1.name() != f2.name())
                return false;
            else if (f1.isStored() != f2.isStored())
                return false;
            else if (f1.isIndexed() != f2.isIndexed())
                return false;
            else if (f1.isTokenized() != f2.isTokenized())
                return false;
            else
                return f1.toString().equals(f2.toString());
        }
    }
}

