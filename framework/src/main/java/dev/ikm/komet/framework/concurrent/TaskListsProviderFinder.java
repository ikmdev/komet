package dev.ikm.komet.framework.concurrent;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ServiceLoader;

public enum TaskListsProviderFinder {
    INSTANCE;

    TaskListsService service;

    TaskListsProviderFinder() {
        Class serviceClass = TaskListsService.class;
        ServiceLoader<TaskListsService> serviceLoader = ServiceLoader.load(serviceClass);
        Optional<TaskListsService> optionalService = serviceLoader.findFirst();
        if (optionalService.isPresent()) {
            this.service = optionalService.get();
        } else {
            throw new NoSuchElementException("No " + serviceClass.getName() +
                    " found by ServiceLoader...");
        }
    }

    public TaskListsService get() {
        return service;
    }

}
