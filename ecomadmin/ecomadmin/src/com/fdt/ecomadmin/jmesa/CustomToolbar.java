package com.fdt.ecomadmin.jmesa;

import org.jmesa.view.ViewUtils;
import org.jmesa.view.html.toolbar.AbstractToolbar;
import org.jmesa.view.html.toolbar.MaxRowsItem;
import org.jmesa.view.html.toolbar.ToolbarItemType;

public class CustomToolbar extends AbstractToolbar {

    @Override
    public String render() {
        addToolbarItem(ToolbarItemType.FIRST_PAGE_ITEM);
        addToolbarItem(ToolbarItemType.PREV_PAGE_ITEM);
        addToolbarItem(ToolbarItemType.NEXT_PAGE_ITEM);
        addToolbarItem(ToolbarItemType.LAST_PAGE_ITEM);
        addToolbarItem(ToolbarItemType.SEPARATOR);
        MaxRowsItem maxRowsItem = (MaxRowsItem) addToolbarItem(ToolbarItemType.MAX_ROWS_ITEM);
        if (getMaxRowsIncrements() != null) {
            maxRowsItem.setIncrements(getMaxRowsIncrements());
        }
        boolean editable = ViewUtils.isEditable(getCoreContext().getWorksheet());
        if (editable) {
            addToolbarItem(ToolbarItemType.SEPARATOR);
            addToolbarItem(ToolbarItemType.SAVE_WORKSHEET_ITEM);
            addToolbarItem(ToolbarItemType.CLEAR_WORKSHEET_ITEM);
        }
        return super.render();
    }
}
