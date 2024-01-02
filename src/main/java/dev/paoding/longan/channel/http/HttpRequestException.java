package dev.paoding.longan.channel.http;

import io.netty.handler.codec.http.HttpResponseStatus;

public abstract class HttpRequestException extends RuntimeException {
    protected String code;
    protected String responseType;
//    protected MethodInvocation methodInvocation;

    public HttpRequestException(String message) {
        super(message);
    }

    //    public HttpRequestException(MethodInvocation methodInvocation, String message) {
//        this(methodInvocation.getMethod(), methodInvocation.getLineNumber(), message);
//        this.methodInvocation = methodInvocation;
//    }
//
//    public HttpRequestException(Method method, int lineNumber, String message) {
//        super(message);
//        Class<?> serviceClass = method.getDeclaringClass();
//        StackTraceElement[] stackTrace = new StackTraceElement[1];
//        stackTrace[0] = new StackTraceElement(serviceClass.getName(), method.getName(), serviceClass.getSimpleName() + ".java", lineNumber);
//        this.setStackTrace(stackTrace);
//    }
//
//    public HttpRequestException(Method method, int lineNumber, String message, Throwable throwable) {
//        super(message);
//        Class<?> serviceClass = method.getDeclaringClass();
//        StackTraceElement[] superStackTrace = throwable.getStackTrace();
//        StackTraceElement[] stackTrace = new StackTraceElement[1 + superStackTrace.length];
//        stackTrace[0] = new StackTraceElement(serviceClass.getName(), method.getName(), serviceClass.getSimpleName() + ".java", lineNumber);
//        System.arraycopy(superStackTrace, 0, stackTrace, 1, superStackTrace.length);
//        this.setStackTrace(stackTrace);
//    }

    public abstract HttpResponseStatus getHttpResponseStatus();

//    public MethodInvocation getMethodInvocation() {
//        return methodInvocation;
//    }

    public String getCode() {
        return code;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getResponseType() {
        return responseType;
    }
}
