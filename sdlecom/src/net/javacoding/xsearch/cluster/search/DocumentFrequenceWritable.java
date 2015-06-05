/**
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacoding.xsearch.cluster.search;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DocumentFrequenceWritable implements Writable {
  private ReadWriteLock _frequenciesLock = new ReentrantReadWriteLock(true);
  private Map<TermWritable, Integer> _frequencies = new HashMap<TermWritable, Integer>();

  private AtomicInteger _numDocs = new AtomicInteger();

  public void put(final String field, final String term, final int frequency) {
    _frequenciesLock.writeLock().lock();
    try {
      add(new TermWritable(field, term), frequency);
    } finally {
      _frequenciesLock.writeLock().unlock();
    }
  }

  private void add(final TermWritable key, final int frequency) {
    int result = frequency;
    final Integer frequencyObject = _frequencies.get(key);
    if (frequencyObject != null) {
      result += frequencyObject.intValue();
    }
    _frequencies.put(key, result);
  }

  public void putAll(final Map<TermWritable, Integer> frequencyMap) {
    _frequenciesLock.writeLock().lock();
    try {
      final Set<TermWritable> keySet = frequencyMap.keySet();
      for (final TermWritable key : keySet) {
        add(key, frequencyMap.get(key).intValue());
      }
    } finally {
      _frequenciesLock.writeLock().unlock();
    }
  }

  public Integer get(final String field, final String term) {
    return get(new TermWritable(field, term));
  }

  public void addNumDocs(final int numDocs) {
    _numDocs.addAndGet(numDocs);
  }

  public Integer get(final TermWritable key) {
    _frequenciesLock.readLock().lock();
    try {
      return _frequencies.get(key);
    } finally {
      _frequenciesLock.readLock().unlock();
    }
  }

  public Map<TermWritable, Integer> getAll() {
    return Collections.unmodifiableMap(_frequencies);
  }

  public void readFields(final DataInput in) throws IOException {
    _frequenciesLock.writeLock().lock();
    try {
      final int size = in.readInt();
      for (int i = 0; i < size; i++) {
        final TermWritable term = new TermWritable();
        term.readFields(in);
        final int frequency = in.readInt();
        _frequencies.put(term, frequency);
      }
      _numDocs.set(in.readInt());
    } finally {
      _frequenciesLock.writeLock().unlock();
    }
  }

  public void write(final DataOutput out) throws IOException {
    _frequenciesLock.readLock().lock();
    try {
      out.writeInt(_frequencies.size());
      for (final TermWritable key : _frequencies.keySet()) {
        key.write(out);
        final Integer frequency = _frequencies.get(key);
        out.writeInt(frequency);
      }
      out.writeInt(_numDocs.get());
    } finally {
      _frequenciesLock.readLock().unlock();
    }
  }

  public int getNumDocs() {
    return _numDocs.get();
  }

  public void setNumDocs(final int numDocs) {
    _numDocs.set(numDocs);
  }

  @Override
  public String toString() {
    return "numDocs: " + getNumDocs() + getAll();
  }
}
