package com.fdt.sdl.styledesigner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.XMLSerializable;

import com.fdt.sdl.styledesigner.htmlelement.HtmlElement;
import com.fdt.sdl.styledesigner.operation.ScaffoldOperation;
import com.fdt.sdl.styledesigner.value.ColumnValue;
import com.fdt.sdl.styledesigner.value.ColumnsValue;
import com.fdt.sdl.styledesigner.value.ScaffoldValue;
import com.fdt.sdl.styledesigner.variable.ColumnVariable;
import com.fdt.sdl.styledesigner.variable.ColumnsVariable;
import com.fdt.sdl.styledesigner.variable.MultiSelectColumnVariable;
import com.fdt.sdl.styledesigner.variable.ScaffoldVariable;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("scaffold")
public class Scaffold extends XMLSerializable{
	
    @XStreamAlias("order")
    @XStreamAsAttribute
    public int order;
   
    @XStreamAlias("isPartial")
    @XStreamAsAttribute
    public boolean isPartial;
   
    @XStreamAlias("isChart")
    @XStreamAsAttribute
    public boolean isChart;
   
    @XStreamAsAttribute
    public boolean isMap;
    
    @XStreamAlias("name")
	public String name;
    
    @XStreamAlias("description")
	public String description;
   
    @XStreamAlias("notes")
	public String notes;
    
    @XStreamAlias("variables")
	public List<ScaffoldVariable> variables;
    
    @XStreamAlias("operations")
    public List<ScaffoldOperation> operations;
    
    public transient String longName;
	
	public transient List<ScaffoldValue> values;

	public Scaffold() {
		super();
	}
    
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
    public String getLongName() {
        return longName;
    }
	public List<ScaffoldVariable> getVariables() {
		return variables==null? new ArrayList<ScaffoldVariable>() : variables;
	}
    public List<ScaffoldVariable> getAllVariables() {
        if(variables==null) {
            return new ArrayList<ScaffoldVariable>();
        }else {
            List<ScaffoldVariable> allVariables = new ArrayList<ScaffoldVariable>();
            for(ScaffoldVariable v : variables) {
                allVariables.add(v);
                if(v.children!=null&&v.children.size()>0) {
                    allVariables.addAll(v.children);
                }
            }
            return allVariables;
        }
    }
	public List<ScaffoldValue> getValues(){
	    return values == null ? new ArrayList<ScaffoldValue>() : values;
	}
    public List<ScaffoldOperation> getOperations() {
        return operations==null? new ArrayList<ScaffoldOperation>() : operations;
    }
	public boolean getIsPartial() {
	    return isPartial;
	}
	public boolean getIsChart() {
	    return isChart;
	}	
	public boolean getIsMap() {
	    return isMap;
	}	
	
	public void loadColumnsFromDataset(DatasetConfiguration dc) {
        if(variables!=null) {
        	for(ScaffoldVariable v : variables) {
        		if (!v.getType().equalsIgnoreCase("htmlelement")) {
        			this.loadScaffoldVariables(dc, v);
        		} else {
        			this.loadHtmlVariables(dc, v);
        		}
        	}
        }
    }
	
	private void loadHtmlVariables(DatasetConfiguration dc, ScaffoldVariable scaffoldVariable) {
		if (((HtmlElement) scaffoldVariable).getHtmlElementType().equalsIgnoreCase("table")) {
			for (HtmlElement tr : ((HtmlElement) scaffoldVariable).getChildElements()) {
				for (HtmlElement td : tr.getChildElements()) {
					for (ScaffoldVariable sVariable : td.getChildElements()) {
						loadScaffoldVariables(dc, sVariable);
					}
				}
			}
		}		
	}
	
	private void loadScaffoldVariables(DatasetConfiguration dc, ScaffoldVariable v) {
		if(v instanceof ColumnsVariable) {
	        ColumnsVariable columns = (ColumnsVariable)v;
	        if(columns.columnSelector!=null) {
	            List<Column> selectedColumns = new ArrayList<Column>(dc.getColumns(false));
	            filterColumns(selectedColumns, columns.columnSelector);
	            columns.defaultValue = new ColumnsValue();
	            columns.defaultValue.columns = new ArrayList<ColumnValue>();
	            for(Column c:selectedColumns) {
	                ColumnValue cv = new ColumnValue();
	                cv.columnName = c.getColumnName();
	                columns.defaultValue.columns.add(cv);
	            }
	        }
	    }else if(v instanceof ColumnVariable) {
	        ColumnVariable column = (ColumnVariable)v;
	        if(column.columnSelector!=null) {
	            List<Column> selectedColumns = new ArrayList<Column>(dc.getColumns(false));
	            filterColumns(selectedColumns, column.columnSelector);
	            column.defaultValue = new ColumnValue();
	            for(Column c:selectedColumns) {
	                column.defaultValue.columnName = c.getColumnName();
	                break;
	            }
	        }
	    }else if(v instanceof MultiSelectColumnVariable) {
	        MultiSelectColumnVariable column = (MultiSelectColumnVariable)v;
	        if(column.columnSelector!=null) {
	            List<Column> selectedColumns = new ArrayList<Column>(dc.getColumns(false));
	            filterColumns(selectedColumns, column.columnSelector);
	            column.defaultValue = new ColumnsValue();
	            column.defaultValue.columns = new ArrayList<ColumnValue>();
	            for(Column c:selectedColumns) {
	                ColumnValue cv = new ColumnValue();
	                cv.columnName = c.getColumnName();
	                column.defaultValue.columns.add(cv);
	            }
	        }
	    }
	}
	
    private void filterColumns(List<Column> selectedColumns, String columnSelector){
        if(columnSelector.indexOf(ColumnsVariable.COLUMN_DISPLAYABLE)>=0) {
            for(int i=selectedColumns.size()-1;i>=0;i--) {
                if(!selectedColumns.get(i).canBeDisplayable()) {
                    selectedColumns.remove(i);
                }
            }
        }
        if(columnSelector.indexOf(ColumnsVariable.COLUMN_FILTERABLE)>=0) {
            for(int i=selectedColumns.size()-1;i>=0;i--) {
                if(!selectedColumns.get(i).getIsFilterable()) {
                    selectedColumns.remove(i);
                }
            }
        }
        if(columnSelector.indexOf(ColumnsVariable.COLUMN_SORTABLE)>=0) {
            for(int i=selectedColumns.size()-1;i>=0;i--) {
                if(!selectedColumns.get(i).canBeSortable()) {
                    selectedColumns.remove(i);
                }
            }
        }
        if(columnSelector.indexOf(ColumnsVariable.COLUMN_IS_NUMBER)>=0) {
            for(int i=selectedColumns.size()-1;i>=0;i--) {
                if(!selectedColumns.get(i).getIsNumber()) {
                    selectedColumns.remove(i);
                }
            }
        }
        if(columnSelector.indexOf(ColumnsVariable.COLUMN_IS_DATE)>=0) {
            for(int i=selectedColumns.size()-1;i>=0;i--) {
                if(!selectedColumns.get(i).getIsDate()) {
                    selectedColumns.remove(i);
                }
            }
        }
        if(columnSelector.indexOf(ColumnsVariable.COLUMN_IS_STRING)>=0) {
            for(int i=selectedColumns.size()-1;i>=0;i--) {
                if(!selectedColumns.get(i).getIsText()) {
                    selectedColumns.remove(i);
                }
            }
        }
        if(columnSelector.indexOf(ColumnVariable.COLUMN_PRIMARY_KEY)>=0) {
            for(int i=selectedColumns.size()-1;i>=0;i--) {
                if(!selectedColumns.get(i).getIsPrimaryKey()) {
                    selectedColumns.remove(i);
                }
            }
        }
        if(columnSelector.indexOf(ColumnVariable.COLUMN_MODIFIED_DATE)>=0) {
            for(int i=selectedColumns.size()-1;i>=0;i--) {
                if(!selectedColumns.get(i).getIsModifiedDate()) {
                    selectedColumns.remove(i);
                }
            }
        }
    }
    public List<Template> filterAcceptableTemplates(Template[] templates) throws IOException {
        List<Template> ret = new ArrayList<Template>();
        for(Template t : templates) {
            boolean allAccept = true;
            for(ScaffoldOperation o : this.getOperations()) {
                if(!o.accept(t)) {
                    allAccept = false;
                }
            }
            if(allAccept) {
                ret.add(t);
            }
        }
        return ret;
    }
	
}
