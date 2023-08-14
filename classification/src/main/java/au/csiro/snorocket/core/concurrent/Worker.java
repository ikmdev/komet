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
package au.csiro.snorocket.core.concurrent;

import java.util.Queue;

/**
 * Represents a worker in charge of deriving axioms in a {@link Context}.
 * 
 * @author Alejandro Metke
 * 
 */
public class Worker implements Runnable {

    private final Queue<Context> todo;

    /**
     * 
     * @param todo
     */
    public Worker(Queue<Context> todo) {
        this.todo = todo;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        // Process contexts until there are no more left in the queue
        while (true) {
            Context ctx = todo.poll();
            if (ctx == null)
                break;
            ctx.processOntology();
        }
    }

}
