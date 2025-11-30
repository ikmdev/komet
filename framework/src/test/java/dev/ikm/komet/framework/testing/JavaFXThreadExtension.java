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
package dev.ikm.komet.framework.testing;

import javafx.application.Platform;
import org.junit.jupiter.api.extension.*;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * JUnit 5 extension that automatically executes test methods on the JavaFX Application Thread.
 * <p>
 * Usage:
 * <pre>{@code
 * @ExtendWith(JavaFXThreadExtension.class)
 * class MyTest {
 *     @Test
 *     @RunOnJavaFXThread
 *     void testSomething() {
 *         // This runs on JavaFX thread automatically
 *     }
 * }
 * }</pre>
 */
public class JavaFXThreadExtension implements InvocationInterceptor, BeforeAllCallback {

    private static final long DEFAULT_TIMEOUT_SECONDS = 30;
    private static final Object LOCK = new Object();
    private static boolean initialized = false;

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RunOnJavaFXThread {
        long timeout() default DEFAULT_TIMEOUT_SECONDS;
        TimeUnit unit() default TimeUnit.SECONDS;
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        synchronized (LOCK) {
            if (!initialized) {
                CountDownLatch latch = new CountDownLatch(1);
                Platform.startup(latch::countDown);
                if (!latch.await(5, TimeUnit.SECONDS)) {
                    throw new RuntimeException("JavaFX platform failed to start within 5 seconds");
                }
                initialized = true;
            }
        }
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {

        Method method = invocationContext.getExecutable();
        RunOnJavaFXThread annotation = method.getAnnotation(RunOnJavaFXThread.class);

        if (annotation == null) {
            annotation = method.getDeclaringClass().getAnnotation(RunOnJavaFXThread.class);
        }

        if (annotation != null) {
            runOnJavaFXThread(invocation, annotation.timeout(), annotation.unit());
        } else {
            invocation.proceed();
        }
    }

    private void runOnJavaFXThread(Invocation<Void> invocation, long timeout, TimeUnit unit)
            throws Throwable {

        if (Platform.isFxApplicationThread()) {
            invocation.proceed();
            return;
        }


        // Use longer timeout when debugging
        if (isDebuggerAttached()) {
            timeout = Math.max(timeout, 3600); // At least 1 hour when debugging
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> exceptionRef = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                invocation.proceed();
            } catch (Throwable t) {
                exceptionRef.set(t);
            } finally {
                latch.countDown();
            }
        });

        boolean completed = latch.await(timeout, unit);
        if (!completed) {
            throw new RuntimeException(
                String.format("Test timed out after %,d %s waiting for JavaFX thread",
                    timeout, unit.toString().toLowerCase()));
        }

        Throwable exception = exceptionRef.get();
        if (exception != null) {
            throw exception;
        }
    }

    private boolean isDebuggerAttached() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean()
                .getInputArguments().toString().contains("-agentlib:jdwp");
    }
}
