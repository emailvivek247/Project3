/*
 * Copyright 2004.
 */

package net.javacoding.xsearch.config;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

/**
 * The set of Digester rules required to parse a dataset configuration file.
 *
 */
public class WebConfigurationRuleSet extends RuleSetBase {

    // --------------------------------------------------------- Public Methods

    /**
     * Add the set of Rule instances defined in this RuleSet to the specified
     * <code>Digester</code> instance, associating them with our namespace URI
     * (if any). This method should only be called by a Digester instance.
     * These rules assume that an instance of
     * <code>net.javacoding.xsearch.config.WebConfiguration</code> is
     * pushed onto the evaluation stack before parsing begins.
     *
     * @param digester Digester instance to which the new Rule instances should
     *                 be added.
     */
    public void addRuleInstances(Digester digester) {
        digester.addObjectCreate("xsearch-web/analyzers/analyzer", "net.javacoding.xsearch.config.AnalyzerChoice");
        digester.addSetProperties("xsearch-web/analyzers/analyzer", "name", "name");
        digester.addBeanPropertySetter("xsearch-web/analyzers/analyzer/class", "className");
        digester.addBeanPropertySetter("xsearch-web/analyzers/analyzer/description", "description");
        digester.addBeanPropertySetter("xsearch-web/analyzers/analyzer/language", "language");
        digester.addSetNext("xsearch-web/analyzers/analyzer", "addAnalyzerChoice");

        digester.addObjectCreate("xsearch-web/similarities/similarity", "net.javacoding.xsearch.config.SimilarityChoice");
        digester.addSetProperties("xsearch-web/similarities/similarity", "name", "name");
        digester.addBeanPropertySetter("xsearch-web/similarities/similarity/class", "className");
        digester.addBeanPropertySetter("xsearch-web/similarities/similarity/description", "description");
        digester.addSetNext("xsearch-web/similarities/similarity", "addSimilarityChoice");
        
        digester.addCallMethod("xsearch-web/column-classes/column-class", "addColumnClassName", 2);
        digester.addCallParam("xsearch-web/column-classes/column-class", 0, "name");
        digester.addCallParam("xsearch-web/column-classes/column-class/value", 1);

        digester.addCallMethod("xsearch-web/index-field-types/index-field-type", "addIndexFieldType", 2);
        digester.addCallParam("xsearch-web/index-field-types/index-field-type", 0, "name");
        digester.addCallParam("xsearch-web/index-field-types/index-field-type/value", 1);

        digester.addObjectCreate("xsearch-web/wizard", "net.javacoding.xsearch.config.Wizard");
        digester.addSetProperties("xsearch-web/wizard", "name", "name");
        digester.addObjectCreate("xsearch-web/wizard/step", "net.javacoding.xsearch.config.Step");
        digester.addSetProperties("xsearch-web/wizard/step", "name", "name");
        digester.addBeanPropertySetter("xsearch-web/wizard/step/action", "action");
        digester.addBeanPropertySetter("xsearch-web/wizard/step/repeatable", "repeatable");
        digester.addBeanPropertySetter("xsearch-web/wizard/step/description", "description");
        digester.addSetNext("xsearch-web/wizard/step", "addStep");
        digester.addSetNext("xsearch-web/wizard", "setWizard");

        digester.addCallMethod("xsearch-web/date-formats/date-format", "addDateFormat", 1);
        digester.addCallParam("xsearch-web/date-formats/date-format", 0);

        digester.addCallMethod("xsearch-web/number-formats/number-format", "addNumberFormat", 1);
        digester.addCallParam("xsearch-web/number-formats/number-format", 0);

        digester.addCallMethod("xsearch-web/commands/command", "addCommand", 2);
        digester.addCallParam("xsearch-web/commands/command", 0, "name");
        digester.addCallParam("xsearch-web/commands/command", 1);

    }

}
