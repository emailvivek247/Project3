package net.javacoding.xsearch.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import net.javacoding.xsearch.api.protocol.SearchProtocol;
import net.javacoding.xsearch.search.result.SearchResult;
import net.javacoding.xsearch.utility.U;
import net.javacoding.xsearch.utility.VMTool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Document implements SDLIndexDocument {

    private SearchProtocol.Document document;
    private List<Field> fields;

    private JsonObject source;
    private JsonObject highlight;
    private float score;

    private final static String[] NO_STRINGS = new String[0];

    public Document(SearchProtocol.Document document) {
        this.document = document;
    }

    public Document(JsonObject hit) {
        source = hit.get("_source").getAsJsonObject();
        score = hit.get("_score").getAsFloat();
        if (hit.get("highlight") != null) {
            highlight = hit.get("highlight").getAsJsonObject();
        }
    }

    public float getScore() {
        return document != null ? document.getScore() : score;
    }

    public float getBoost() {
        return document != null ? document.getBoost() : 1F;
    }

    public String get(String field) {
        return getString(field);
    }

    public String getString(String field) {
        return getString(field, false);
    }

    public String getString(String field, boolean useHighlight) {
        String value = null;
        if (document != null) {
            for (SearchProtocol.Field f : document.getFieldList()) {
                if (f.getName().equals(field)) {
                    value = f.getValue();
                }
            }
        } else if (source != null) {
            if (useHighlight && highlight != null) {
                JsonElement element = highlight.get(field);
                if (element != null) {
                    value = element.getAsString();
                }
            }
            if (value == null) {
                JsonElement element = source.get(field);
                if (element != null) {
                    value = element.getAsString();
                }
            }
        }
        return value == null ? "" : value;
    }

    /**
     * One document can have several fields of the same name. For example, one
     * article can have several comments, if you select comments in the
     * subsequent query.
     * 
     * @param field name
     * @return a list of field values
     */
    @Override
    public List<String> getValuesList(String field) {
        List<String> ret = new ArrayList<>();
        if (document != null) {
            for (SearchProtocol.Field f : document.getFieldList()) {
                if (f.getName().equals(field)) {
                    ret.add(f.getValue());
                }
            }
        } else if (source != null) {
            JsonElement element = source.get(field);
            if (element != null) {
                if (element.isJsonArray()) {
                    element.getAsJsonArray().forEach(e -> ret.add(e.getAsString()));
                } else if (element.isJsonPrimitive()) {
                    ret.add(element.getAsString());
                }
            }
        }
        return ret;
    }

    @Override
    public String[] getValues(String field) {
        List<String> ret = new ArrayList<>();
        if (document != null) {
            for (SearchProtocol.Field f : document.getFieldList()) {
                if (f.getName().equals(field)) {
                    ret.add(f.getValue());
                }
            }
            if (ret.size() == 0) {
                return NO_STRINGS;
            }
        } else if (source != null) {
            JsonElement element = source.get(field);
            if (element != null) {
                if (element.isJsonArray()) {
                    element.getAsJsonArray().forEach(e -> ret.add(e.getAsString()));
                } else if (element.isJsonPrimitive()) {
                    ret.add(element.getAsString());
                }
            }
        }
        return (String[]) ret.toArray(new String[ret.size()]);
    }

    public String[] getValues(String field, SearchResult searchResult) {
        List<String> ret = new ArrayList<>();
        if (document != null) {
            for (SearchProtocol.Field f : document.getFieldList()) {
                if (f.getName().equals(field)) {
                    ret.add(f.getValue());
                }
            }
            if (ret.size() == 0) {
                return NO_STRINGS;
            }
        } else if (source != null) {
            List<String> strippedHighlights = new ArrayList<>();
            List<String> finalHighlights = new ArrayList<>();
            if (highlight != null) {
                List<String> highlights = new ArrayList<>();
                JsonElement element = highlight.get(field);
                if (element != null) {
                    if (element.isJsonArray()) {
                        element.getAsJsonArray().forEach(e -> highlights.add(e.getAsString()));
                    } else if (element.isJsonPrimitive()) {
                        highlights.add(element.getAsString());
                    }
                    strippedHighlights.addAll(highlights.stream().map((h) ->{
                        if (h.contains("hl-tag-replace")) {
                            h = h.replaceAll("<hl-tag-replace>", "");
                            h = h.replaceAll("</hl-tag-replace>", "");
                        }
                        return h;
                    }).collect(Collectors.toList()));
                    finalHighlights.addAll(highlights.stream().map((h) ->{
                        if (h.contains("hl-tag-replace")) {
                            h = h.replaceAll("<hl-tag-replace>", searchResult.getBeginTag());
                            h = h.replaceAll("</hl-tag-replace>", searchResult.getEndTag());
                        }
                        return h;
                    }).collect(Collectors.toList()));
                }
            }
            JsonElement element = source.get(field);
            if (element != null) {
                if (element.isJsonArray()) {
                    for (JsonElement value : element.getAsJsonArray()) {
                        ret.add(value.getAsString());
                    }
                } else if (element.isJsonPrimitive()) {
                    ret.add(element.getAsString());
                }
                ret = ret.stream().sorted((one, two) -> {
                    if (strippedHighlights.contains(one) && !strippedHighlights.contains(two)) {
                        return -1;
                    } else if (!strippedHighlights.contains(one) && strippedHighlights.contains(two)) {
                        return 1;
                    } else {
                        return 0;
                    }
                 }).map((v) -> {
                    if (strippedHighlights.contains(v)) {
                        return finalHighlights.get(strippedHighlights.indexOf(v));
                    } else {
                        return v;
                    }
                }).collect(Collectors.toList());
            }
        }
        return (String[]) ret.toArray(new String[ret.size()]);
    }

    @Override
    public Date getDate(String field) {
        if (document != null) {
            for (SearchProtocol.Field f : document.getFieldList()) {
                if (f.getName().equals(field) && f.getType().equals(SearchProtocol.Field.Type.DATETIME)) {
                    return VMTool.storedStringToDate(f.getValue());
                }
            }
        } else if (source != null) {
            JsonElement element = source.get(field);
            if (element != null) {
                return VMTool.storedStringToDate(source.get(field).getAsString());
            }
        }
        return new Date();
    }

    public Integer getInteger(String field) {
        for (SearchProtocol.Field f : document.getFieldList()) {
            if (f.getName().equals(field) && f.getType().equals(SearchProtocol.Field.Type.NUMBER)) {
                return U.getInteger(f.getValue());
            }
        }
        return null;
    }

    @Override
    public Long getLong(String field) {
        for (SearchProtocol.Field f : document.getFieldList()) {
            if (f.getName().equals(field) && f.getType().equals(SearchProtocol.Field.Type.NUMBER)) {
                return VMTool.storedStringToLong(f.getValue());
            }
        }
        return null;
    }

    @Override
    public Double getDouble(String field) {
        for (SearchProtocol.Field f : document.getFieldList()) {
            if (f.getName().equals(field) && f.getType().equals(SearchProtocol.Field.Type.NUMBER)) {
                return U.getDouble(f.getValue(), 0);
            }
        }
        return null;
    }

    public Float getFloat(String field) {
        for (SearchProtocol.Field f : this.document.getFieldList()) {
            if (f.getName().equals(field) && f.getType().equals(SearchProtocol.Field.Type.NUMBER)) {
                return U.getFloat(f.getValue(), 0);
            }
        }
        return null;
    }

    public List<Field> getFieldList() {
        if (fields == null) {
            fields = new ArrayList<>();
            for (SearchProtocol.Field f : document.getFieldList()) {
                fields.add(new Field(f));
            }
        }
        return fields;
    }

    public final Object getObject(String field) {
        if (document != null) {
            for (SearchProtocol.Field f : this.document.getFieldList()) {
                if (f.getName().equals(field) && f.getType().equals(SearchProtocol.Field.Type.DATETIME)) {
                    return getDate(field);
                } else if (f.getName().equals(field) && f.getType().equals(SearchProtocol.Field.Type.STRING)) {
                    return getString(field);
                } else if (f.getName().equals(field) && f.getType().equals(SearchProtocol.Field.Type.NUMBER)) {
                    return getString(field);
                } else if (f.getName().equals(field)) {
                    return f.getValue();
                } else {
                    return null;
                }
            }
        } else if (source != null) {
            return getString(field);
        }
        return null;

    }

    @Override
    public List<org.apache.lucene.document.Field> fields() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> fieldNames() {
        List<String> result = null;
        if (document != null) {
            result = document.getFieldList().stream().map(f -> f.getName()).collect(Collectors.toList());
        } else if (source != null) {
            result = source.entrySet().stream().map(e -> e.getKey()).collect(Collectors.toList());
        }
        return result;
    }
}
