package ch.zhaw.splab.podilizerproc.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public class AsyncLambda {

    private AsyncLambda() {}

    public static <T> CompletableFuture<T> dispatch(Callable<T> func) {
        CompletableFuture<T> result = new CompletableFuture<>();
        new Thread(() -> {
            try {
                result.complete(func.call());
            } catch (Exception e) {
                result.completeExceptionally(e);
            }
        }).start();
        return result;
    }

    public static CompletableFuture<Void> dispatch(Runnable func) {
        return AsyncLambda.dispatch(() -> {
            func.run();
            return null;
        });
    }
}
