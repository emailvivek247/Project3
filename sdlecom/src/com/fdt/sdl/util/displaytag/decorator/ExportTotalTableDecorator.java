package com.fdt.sdl.util.displaytag.decorator;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import net.javacoding.xsearch.search.HitDocument;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.displaytag.decorator.DisplaytagColumnDecorator;
import org.displaytag.decorator.TableDecorator;
import org.displaytag.exception.DecoratorException;
import org.displaytag.model.HeaderCell;
import org.displaytag.model.TableModel;


/**
 * A table decorator which adds rows with totals (for column with the "total" attribute set) and subtotals (grouping by
 * the column with a group="1" attribute).
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class ExportTotalTableDecorator extends TableDecorator
{

    /**
     * Logger.
     */
    private static Log log = LogFactory.getLog(ExportTotalTableDecorator.class);

    /**
     * total amount.
     */
    private Map<String, Double> grandTotals = new HashMap<String, Double>();

    /**
     * total amount for current group.
     */
    private Map<String, Double> subTotals = new HashMap<String, Double>();

    /**
     * Previous values needed for grouping.
     */
    private Map<String, Object> previousValues = new HashMap<String, Object>();

    /**
     * Name of the property used for grouping.
     */
    private String groupPropertyName;

    /**
     * Label used for subtotals. Default: "{0} total".
     */
    private String subtotalLabel = "{0} subtotal";

    /**
     * Label used for totals. Default: "Total".
     */
    private String totalLabel = "Total";

    /**
     * Setter for <code>subtotalLabel</code>.
     * @param subtotalLabel The subtotalLabel to set.
     */
    public void setSubotalLabel(String subtotalLabel)
    {
        this.subtotalLabel = subtotalLabel;
    }

    /**
     * Setter for <code>totalLabel</code>.
     * @param totalLabel The totalLabel to set.
     */
    public void setTotalLabel(String totalLabel)
    {
        this.totalLabel = totalLabel;
    }

    /**
     * @see org.displaytag.decorator.Decorator#init(PageContext, Object, TableModel)
     */
    public void init(PageContext context, Object decorated, TableModel tableModel)
    {
        super.init(context, decorated, tableModel);

        // reset
        groupPropertyName = null;
        grandTotals.clear();
        subTotals.clear();
        previousValues.clear();
        if (tableModel.getSubTotalLabel() != null && !tableModel.getSubTotalLabel().isEmpty()) {
        	this.subtotalLabel = tableModel.getSubTotalLabel();	
        } else {
            if (tableModel.getSubTotalLabel() != null && tableModel.getSubTotalLabel().isEmpty()) {
            	this.subtotalLabel = "{0}";	
            }
        }
        
        if (tableModel.getTotalLabel() != null) {
        	this.totalLabel = tableModel.getTotalLabel();	
        }

        for (Iterator<HeaderCell> it = tableModel.getHeaderCellList().iterator(); it.hasNext();)
        {
            HeaderCell cell = it.next();
            if (cell.getGroup() == 1)
            {
                groupPropertyName = cell.getIndexColumnName();
            }
        }
    }

    public String startRow()
    {
        String subtotalRow = null;

        if (groupPropertyName != null)
        {
            Object groupedPropertyValue = getIndexColumnValue(groupPropertyName);
            Object previousGroupedPropertyValue = previousValues.get(groupPropertyName);
            // subtotals
            if (previousGroupedPropertyValue != null
                && !ObjectUtils.equals(previousGroupedPropertyValue, groupedPropertyValue))
            {
                subtotalRow = createTotalRow(false);
            }
            previousValues.put(groupPropertyName, groupedPropertyValue);
        }

        for (Iterator<HeaderCell> it = tableModel.getHeaderCellList().iterator(); it.hasNext();)
        {
            HeaderCell cell = it.next();
            if (cell.isTotaled())
            {
            	String totalPropertyName = cell.getIndexColumnName();
                Number amount = (Number) getIndexColumnValueInNumber(totalPropertyName);
            	
                Number previousSubTotal = subTotals.get(totalPropertyName);
                Number previousGrandTotals = grandTotals.get(totalPropertyName);

                subTotals.put(totalPropertyName, new Double((previousSubTotal != null
                    ? previousSubTotal.doubleValue()
                    : 0)
                    + (amount != null ? amount.doubleValue() : 0)));

                grandTotals.put(totalPropertyName, new Double((previousGrandTotals != null ? previousGrandTotals
                    .doubleValue() : 0)
                    + (amount != null ? amount.doubleValue() : 0)));
            }
        }

        return subtotalRow;
    }

    /**
     * After every row completes we evaluate to see if we should be drawing a new total line and summing the results
     * from the previous group.
     * @return String
     */
    public final String finishRow()
    {
        StringBuffer buffer = new StringBuffer(1000);

        // Grand totals...
        if (getViewIndex() == ((List) getDecoratedObject()).size() - 1)
        {
            if (groupPropertyName != null)
            {
                buffer.append(createTotalRow(false));
            }
            buffer.append(createTotalRow(true));
        }
        return buffer.toString();

    }

    protected String createTotalRow(boolean grandTotal)
    {
        StringBuffer buffer = new StringBuffer(1000);
        buffer.append("\n<tr class=\"total\">"); //$NON-NLS-1$

        List<HeaderCell> headerCells = tableModel.getHeaderCellList();

        for (Iterator<HeaderCell> it = headerCells.iterator(); it.hasNext();)
        {
            HeaderCell cell = it.next();
            String cssClass = ObjectUtils.toString(cell.getHtmlAttributes().get("class"));

            buffer.append("<td"); //$NON-NLS-1$
            if (StringUtils.isNotEmpty(cssClass))
            {
                buffer.append(" class=\""); //$NON-NLS-1$
                buffer.append(cssClass);
                buffer.append("\""); //$NON-NLS-1$
            }
            buffer.append(">"); //$NON-NLS-1$

            if (cell.isTotaled())
            {
                String totalPropertyName = cell.getIndexColumnName();
                Object total = grandTotal ? grandTotals.get(totalPropertyName) : subTotals.get(totalPropertyName);

                DisplaytagColumnDecorator[] decorators = cell.getColumnDecorators();
                for (int j = 0; j < decorators.length; j++)
                {
                    try
                    {
                        total = decorators[j].decorate(total, this.getPageContext(), tableModel.getMedia());
                    }
                    catch (DecoratorException e)
                    {
                        log.warn(e.getMessage(), e);
                        // ignore, use undecorated value for totals
                    }
                }
                buffer.append(total);
            }
            else if (groupPropertyName != null && groupPropertyName.equals(cell.getIndexColumnName()))
            {
                buffer.append(grandTotal ? totalLabel : MessageFormat.format(subtotalLabel, new Object[]{previousValues
                    .get(groupPropertyName)}));
            }

            buffer.append("</td>"); //$NON-NLS-1$

        }

        buffer.append("</tr>"); //$NON-NLS-1$

        // reset subtotal
        this.subTotals.clear();

        return buffer.toString();
    }

    /**
     * Shortcut for evaluating Index Column Value in the current row object. Can be useful for implementing anonymous decorators
     * in jsp pages without having to know/import the decorated object Class.
     * @param propertyName property to lookup in current row object. Can also be a nested or indexed property.
     * @since 1.1
     */
    protected String getIndexColumnValue(String indexColumnName)
    {
        	return ((HitDocument)getCurrentRowObject()).get(indexColumnName);
    }
    
    /**
     * Shortcut for evaluating Index Column Value in the current row object. Can be useful for implementing anonymous decorators
     * in jsp pages without having to know/import the decorated object Class.
     * @param propertyName property to lookup in current row object. Can also be a nested or indexed property.
     * @since 1.1
     */
    protected Number getIndexColumnValueInNumber(String indexColumnName)
    {
    	Number number = null;
    	try {
    		number = new Double(((HitDocument)getCurrentRowObject()).get(indexColumnName));
    	} catch (NumberFormatException numberFormatException) {
    		number = new Double("0");
    	}
    	return number;
    }
}