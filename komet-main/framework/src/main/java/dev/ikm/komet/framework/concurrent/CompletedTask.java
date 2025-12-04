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
package dev.ikm.komet.framework.concurrent;

import javafx.concurrent.Task;

public class CompletedTask extends Task {
    final String title;
    final String message;
    final String completionTime;

    public CompletedTask(String title, String message, String completionTime) {
        this.title = title;
        this.message = message;
        this.completionTime = completionTime;
        this.updateTitle(title);
        this.updateMessage(message);
        this.run();
    }

    public String completionTime() {
        return completionTime;
    }

    public String title() {
        return title;
    }

    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return "CompletedTask{" +
                "title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", completionTime='" + completionTime + '\'' +
                '}';
    }

    @Override
    protected final Object call() throws Exception {
        return null;
    }
}
