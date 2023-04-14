/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com).
 * All rights reserved. Use is subject to license terms and conditions.
 */
package au.csiro.ontology.util;

/**
 * This interface should be implemented by objects that wish to monitor the 
 * progress of a task.
 * 
 * @author Alejandro Metke
 *
 */
public interface IProgressMonitor {
    
    /**
     * Indicates that some task has started.
     */
    void taskStarted(String taskName);

    /**
     * Indicates that the previously started task has now ended.
     */
    void taskEnded();

    /**
     * Indicates that there has been progress in a task.
     *  
     * @param value
     * @param max
     */
    void step(int value, int max);

    /**
     * Indicates that there is a task in progress whose length cannot be
     * determined.
     */
    void taskBusy();
}
