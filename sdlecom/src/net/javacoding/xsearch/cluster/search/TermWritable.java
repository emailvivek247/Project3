package net.javacoding.xsearch.cluster.search;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TermWritable implements Writable {

  private String _text;

  private String _field;

  public TermWritable() {
    // needed for serialization
  }

  public TermWritable(final String field, final String text) {
    _field = field;
    _text = text;
  }

  public void readFields(final DataInput in) throws IOException {
    _field = in.readUTF();
    _text = in.readUTF();
  }

  public void write(final DataOutput out) throws IOException {
    out.writeUTF(_field);
    out.writeUTF(_text);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_field == null) ? 0 : _field.hashCode());
    result = prime * result + ((_text == null) ? 0 : _text.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final TermWritable other = (TermWritable) obj;

    if (_text == null) {
      if (other._text != null)
        return false;
    } else if (!_text.equals(other._text))
      return false;

    if (_field == null) {
      if (other._field != null)
        return false;
    } else if (!_field.equals(other._field))
      return false;

    return true;
  }

  @Override
  public String toString() {
    return _field + ":" + _text;
  }

}
