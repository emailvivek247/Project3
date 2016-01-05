package com.fdt.elasticsearch.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fdt.elasticsearch.parsing.ESParser.Context;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;

public class ESQuery {

    public final String queryStr;
    public final List<QueryPart> parts;

    public ESQuery(String queryStr) {
        this.queryStr = queryStr;
        this.parts = new ArrayList<>();
    }

    public void addPhrase(Context context) {
        String part = queryStr.substring(context.phraseStart, context.index + 1);
        PartType partType = PartType.fromContext(context);
        String field = context.currentField;
        parts.add(new QueryPart(part, true, partType, field));
    }

    public void addTerm(Context context) {
        String part = queryStr.substring(context.termStart, context.index);
        PartType partType = PartType.fromContext(context);
        String field = context.currentField;
        parts.add(new QueryPart(part, false, partType, field));
    }

    public String getNonFieldPartsStr() {
        return parts.stream()
                .filter(p -> p.field == null)
                .map(p -> p.part)
                .collect(Collectors.joining(" "));
    }

    public List<QueryPart> getFieldParts() {
        return parts.stream().filter(p -> p.field != null).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append("{queryStr=").append(queryStr).append(", ");
        builder.append("parts={");
        builder.append(Joiner.on(", ").join(parts));
        builder.append("}}");
        return builder.toString();
    }

    public static class QueryPart {

        public String part;
        public boolean isPhrase;
        public PartType partType;
        public String field;

        public QueryPart(String part, boolean isPhrase, PartType partType, String field) {
            this.part = part;
            this.isPhrase = isPhrase;
            this.partType = partType;
            this.field = field;
        }

        public String getAsQueryStr() {
            StringBuilder builder = new StringBuilder();
            if (partType == PartType.REQUIRED) {
                builder.append("+");
            } else if (partType == PartType.PROHIBITED) {
                builder.append("-");
            }
            if (field != null) {
                builder.append(field).append(":");
            }
            builder.append(part);
            return builder.toString();
        }

        public String getAsDateRangeQueryStr() {
            StringBuilder builder = new StringBuilder();
            if (partType == PartType.REQUIRED) {
                builder.append("+");
            } else if (partType == PartType.PROHIBITED) {
                builder.append("-");
            }
            if (field != null) {
                builder.append(field).append(":");
            }
            builder.append(getDateRangeValue(part));
            return builder.toString();
        }

        private static String getDateRangeValue(String value) {
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            if (value.matches("\\d\\d\\d\\d")) {
                int intValue = Integer.parseInt(value);
                value = "{" + intValue + " TO " + (intValue + 1) + "}";
            }
            Pattern yearMonthPattern = Pattern.compile("(\\d\\d\\d\\d)/(\\d\\d)");
            Matcher yearMonthMatcher = yearMonthPattern.matcher(value);
            if (yearMonthMatcher.matches()) {
                int year = Integer.parseInt(yearMonthMatcher.group(1));
                int month = Integer.parseInt(yearMonthMatcher.group(2));
                int newYear = year;
                int newMonth = month + 1;
                if (month == 12) {
                    newYear = year + 1;
                    newMonth = 1;
                }
                String monthStr = String.format("%02d", month);
                String newMonthStr = String.format("%02d", newMonth);
                value = "{" + year + "-" + monthStr + " TO " + 
                        newYear + "-" + newMonthStr + "}";
            }
            return value;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("part", part)
                .add("isPhrase", isPhrase)
                .add("partType", partType)
                .add("field", field)
                .toString();
        }
    }

    public enum PartType {

        NORMAL, REQUIRED, PROHIBITED;

        public static PartType fromContext(Context context) {
            if (context.required) {
                return REQUIRED;
            } else if (context.prohibited) {
                return PROHIBITED;
            } else {
                return NORMAL;
            }
        }
    }

}
