package dev.paoding.longan.channel.http;

public interface HandlerInterceptor {

    default boolean preHandle(HttpRequest request) {
        return true;
    }

    default void postHandle(HttpResponse response) {

    }

    default void afterCompletion() {

    }
}
