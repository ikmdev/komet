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
