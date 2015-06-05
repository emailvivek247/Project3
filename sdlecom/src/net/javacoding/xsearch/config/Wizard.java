package net.javacoding.xsearch.config;

import java.util.ArrayList;

/**
 * 
 *
 * Wizard containing steps to complete one task
 */
public class Wizard implements ConfigConstants {

    private String name = null;
    private Step[] steps = null;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Step[] getSteps() {
        checkInit();
        return steps;
    }
    public void setSteps(Step[] steps) {
        this.steps = steps;
    }

    private void checkInit() {
        if(steps==null && stepArray !=null){
            steps = stepArray.toArray(new Step[0]);
        }
    }
    private transient ArrayList<Step> stepArray = null;
    public void addStep(Step step) {
        if(stepArray==null){
            stepArray = new ArrayList<Step>();
        }
        stepArray.add(step);
    }
    public int getCurrent(String actionName) {
        if(actionName==null) return -1;
        checkInit();
        for(int i=0;i<steps.length;i++){
            if(steps[i].getAction().indexOf(actionName)>=0){
                return i;
            }
        }
        return -1;
    }
    public Step getCurrentStep(int current) {
        checkInit();
        if(current>=0 && current<=steps.length-1){
            return steps[current];
        }
        return null;
    }
    public Step getPreviousStep(int current) {
        checkInit();
        if(current-1>=0 && current-1<=steps.length-1){
            return steps[current-1];
        }
        return null;
    }
    public Step getNextStep(int current) {
        checkInit();
        if(current+1<=steps.length-1 && current+1>=0){
            return steps[current+1];
        }
        return null;
    }
}
