/*
 * Copyright 2004 original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fdt.common.ui.jmesa;

import java.text.NumberFormat;
import java.util.Locale;

import org.jmesa.util.ItemUtils;
import org.jmesa.view.editor.AbstractPatternCellEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An editor to work with numbers. Just send in a valid number pattern and the number will be
 * formated.
 * 
 * @since 2.2
 * @author Venkata Pratti
 */
public class CurrencyCellEditor extends AbstractPatternCellEditor {
	
    private Logger logger = LoggerFactory.getLogger(CurrencyCellEditor.class);

    public CurrencyCellEditor() {
    }

    /**
     * @param pattern The pattern to use.
     */
    public CurrencyCellEditor(String pattern) {
        setPattern(pattern);
    }

    /**
     * Get the formatted number value based on the pattern set.
     */
    public Object getValue(Object item, String property, int rowcount) {
        Object itemValue = null;

        try {
            itemValue = ItemUtils.getItemValue(item, property);
            if (itemValue == null) {
                return null;
            }

            Locale locale = new Locale("en", "US");
            NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
            itemValue = nf.format(itemValue);
        } catch (Exception e) {
            logger.warn("Could not process number editor with property " + property);
        }

        return itemValue;
    }
}
