package dev.paoding.longan.channel.http;


import dev.paoding.longan.service.InternalServerException;
import dev.paoding.longan.service.ServiceException;

public class ExceptionResult {
    private String code;
    private String message;

    public static ExceptionResult of(InternalServerException internalServerException) {
        return of(internalServerException.getCode());
    }

    public static ExceptionResult of(ServiceException serviceException) {
        return of(serviceException.getCode(), serviceException.getMessage());
    }

    public static ExceptionResult of(HttpRequestException httpRequestException) {
        return of(httpRequestException.getCode(), httpRequestException.getMessage());
    }

    public static ExceptionResult of(String code) {
        ExceptionResult exceptionResult = new ExceptionResult();
        exceptionResult.setCode(code);
        return exceptionResult;
    }

    public static ExceptionResult of(String code, String message) {
        ExceptionResult exceptionResult = new ExceptionResult();
        exceptionResult.code = code;
        exceptionResult.message = message;
        return exceptionResult;
    }

    public ExceptionResult message(String message) {
        this.message = message;
        return this;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
