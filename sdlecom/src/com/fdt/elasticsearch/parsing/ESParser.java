package com.fdt.elasticsearch.parsing;


public class ESParser {

    public ESQuery parse(String queryStr) {
        ESQuery query = new ESQuery(queryStr);
        Context context = new Context(queryStr);
        ParserState state = ParserState.DEFAULT;
        while (context != null) {
            state = state.nextState(context, query);
            context = context.slideForward();
        }
        if (state != ParserState.DEFAULT) {
            throw new RuntimeException("Not a valid query string");
        }
        return query;
    }

    public enum ParserState {
        IN_PHRASE {
            @Override
            public ParserState nextState(Context context, ESQuery query) {
                Character character = context.character;
                if (character.charValue() == '"' && !context.isPreviousCharAnEscape()) {
                    query.addPhrase(context);
                    context.resetTrackers();
                    return DEFAULT;
                } else {
                    return IN_PHRASE;
                }
            }
        },
        IN_FIELD {
            @Override
            public ParserState nextState(Context context, ESQuery query) {
                Character character = context.character;
                if (Character.isWhitespace(character)) {
                    throw new RuntimeException("Whitespace in middle of field");
                } else if (character.charValue() == ':' && !context.isPreviousCharAnEscape()) {
                    context.setCurrentField();
                    return DEFAULT;
                } else if (character.charValue() == '"' && !context.isPreviousCharAnEscape()) {
                    throw new RuntimeException("Found double-quote in middle of field");
                } else {
                    return IN_FIELD;
                }
            }
        },
        IN_TERM {
            @Override
            public ParserState nextState(Context context, ESQuery query) {
                Character character = context.character;
                if (Character.isWhitespace(character)) {
                    query.addTerm(context);
                    context.resetTrackers();
                    return DEFAULT;
                } else if (character.charValue() == '"' && !context.isPreviousCharAnEscape()) {
                    throw new RuntimeException("Found double-quote in middle of term");
                } else {
                    return IN_TERM;
                }
            }
        },
        DEFAULT {
            @Override
            public ParserState nextState(Context context, ESQuery query) {
                Character character = context.character;
                if (Character.isWhitespace(character)) {
                    return DEFAULT;
                } else if (character.charValue() == '"') {
                    context.phraseStart = context.index;
                    return IN_PHRASE;
                } else if (character.charValue() == '+') {
                    context.setRequired();
                    return DEFAULT;
                } else if (character.charValue() == '-') {
                    context.setProhibited();
                    return DEFAULT;
                } else if (context.hasColonBeforeWhitespace()) {
                    context.fieldStart = context.index;
                    return IN_FIELD;
                } else {
                    context.termStart = context.index;
                    return IN_TERM;
                }
            }
        };

        public abstract ParserState nextState(Context context, ESQuery query);
    }

    public class Context {

        public final String queryStr;
        public Character character;
        public Integer index;

        public int phraseStart;
        public int fieldStart;
        public int termStart;

        public String currentField;
        public boolean required;
        public boolean prohibited;

        public Context(String queryStr) {
            this.queryStr = queryStr + " ";
            this.character = queryStr.charAt(0);
            this.index = 0;
        }

        public void setCurrentField() {
            currentField = queryStr.substring(fieldStart, index);
        }

        public void setRequired() {
            required = true;
        }

        public void setProhibited() {
            prohibited = true;
        }

        public void resetTrackers() {
            phraseStart = -1;
            fieldStart = -1;
            termStart = -1;
            currentField = null;
            required = false;
            prohibited = false;
        }

        public boolean isPreviousCharAnEscape() {
            if (index == 0) {
                return false;
            } else if (queryStr.charAt(index - 1) == '\\') {
                return true;
            } else {
                return false;
            }
        }

        public boolean hasColonBeforeWhitespace() {
            String remaining = queryStr.substring(index);
            for (int i = 0; i < remaining.length(); i++) {
                char aChar = remaining.charAt(i);
                if (Character.isWhitespace(aChar)) {
                    return false;
                } else if (aChar == ':') {
                    if (remaining.charAt(i - 1) != '\\') {
                        return true;
                    }
                }
            }
            return false;
        }

        public Context slideForward() {
            if (queryStr.length() == index + 1) {
                return null;
            } else {
                this.character = queryStr.charAt(index + 1);
                this.index = index + 1;
                return this;
            }
        }
    }
 
    public static void main(String[] args) {
        ESParser parser = new ESParser();
        ESQuery query = parser.parse("otherField:\"Assumed Names,Brand\"");
        System.out.println(query.parts.get(0).getAsQueryStr());
    }
}
