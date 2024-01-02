package dev.paoding.longan.data;

import dev.paoding.longan.service.ServiceException;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * 如果查询的数据不存在会抛出此异常。
 */
public class DataNotFoundException extends ServiceException {

    public DataNotFoundException(String message) {
        super(message);
        this.code = "data.not.found";
    }

    @Override
    public HttpResponseStatus getHttpResponseStatus() {
        return HttpResponseStatus.SERVICE_UNAVAILABLE;
    }
}
