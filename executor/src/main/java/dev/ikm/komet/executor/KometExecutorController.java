package dev.ikm.komet.executor;

import com.google.auto.service.AutoService;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.ExecutorController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

@AutoService({ExecutorController.class})
public class KometExecutorController implements ExecutorController {
    private static final Logger LOG = LoggerFactory.getLogger(KometExecutorController.class);
    private static AlertDialogSubscriber alertDialogSubscriber;
    private static AtomicReference<KometExecutorProvider> providerReference = new AtomicReference<>();

    @Override
    public KometExecutorProvider create() {
        if (providerReference.get() == null) {
            providerReference.updateAndGet(executorProvider -> {
                if (executorProvider != null) {
                    return executorProvider;
                }
                KometExecutorController.alertDialogSubscriber = new AlertDialogSubscriber();
                return new KometExecutorProvider();
            });
            providerReference.get().start();
        }
        return providerReference.get();
    }

    @Override
    public void stop() {
        providerReference.updateAndGet(executorProvider -> {
            if (executorProvider != null) {
                executorProvider.stop();
            }
            return null;
        });
    }


    @AutoService(CachingService.class)
    public static class CacheProvider implements CachingService {
        @Override
        public void reset() {
            providerReference.updateAndGet(executorProvider -> {
                if (executorProvider != null) {
                    executorProvider.stop();
                }
                return null;
            });
        }
    }

}
