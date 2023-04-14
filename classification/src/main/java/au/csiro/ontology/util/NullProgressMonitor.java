/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com).
 * All rights reserved. Use is subject to license terms and conditions.
 */
package au.csiro.ontology.util;


/**
 * Implementation of {@link IProgressMonitor} that does nothing.
 * 
 * @author Alejandro Metke
 *
 */
public class NullProgressMonitor implements IProgressMonitor {

    public void taskStarted(String taskName) {
        
    }

    public void taskEnded() {
        
    }

    public void step(int value, int max) {
        
    }

    public void taskBusy() {
        
    }

}
