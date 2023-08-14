/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
