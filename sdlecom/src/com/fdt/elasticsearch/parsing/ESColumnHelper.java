package com.fdt.elasticsearch.parsing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.utility.U;

public class ESColumnHelper {

    private List<Column> columnList;
    private Set<String> dynamicSearchableColumns;

    public ESColumnHelper(List<Column> columnList, String columnsString) {
        this.columnList = columnList;
        if(!U.isEmpty(columnsString)) {
            dynamicSearchableColumns = new HashSet<String>();
            String[] list = columnsString.toLowerCase().split(",");
            for(String c : list) {
                dynamicSearchableColumns.add(c);
            }
        }
    }

    public List<Column> getSearchCols() {
        return columnList.stream()
            .filter(c -> isSearchable(c))
            .filter(c -> !c.getIsDate())
            .collect(Collectors.toList());
    }

    private boolean isSearchable(Column column) {
        return dynamicSearchableColumns == null && column.getIsSearchable() || dynamicSearchableColumns != null
                && dynamicSearchableColumns.contains(column.getColumnName().toLowerCase());
    }
}
