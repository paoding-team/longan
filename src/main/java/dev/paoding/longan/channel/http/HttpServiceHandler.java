package dev.paoding.longan.channel.http;

import dev.paoding.longan.core.MethodInvocation;
import dev.paoding.longan.core.Result;
import dev.paoding.longan.service.InternalServerException;
import dev.paoding.longan.service.MethodNotAllowedException;
import dev.paoding.longan.service.MethodNotFoundException;
import dev.paoding.longan.service.ServiceException;
import dev.paoding.longan.util.GsonUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AsciiString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_OCTET_STREAM;

@Component
public class HttpServiceHandler extends AbstractServiceHandler {
    private final Logger logger = LoggerFactory.getLogger(HttpServiceHandler.class);
    @Resource
    private HandlerInterceptor handlerInterceptor;
    @Resource
    private HttpServiceInvoker httpServiceInvoker;

    public FullHttpResponse channelRead(ChannelHandlerContext ctx, FullHttpRequest request) {
        HttpVersion httpVersion = request.protocolVersion();
        HttpRequest httpRequest = new HttpRequestImpl(request);
        try {
            if (handlerInterceptor.preHandle(httpRequest)) {
                Result result = httpServiceInvoker.invokeService(request);
                AsciiString contentType = result.getType();
                Object content = result.getValue();
                if (content == null) {
                    return writeNoContent(httpVersion);
                }
                if (contentType.equals(APPLICATION_JSON)) {
                    if (content instanceof String) {
                        return writeJson(httpVersion, HttpResponseStatus.OK, content.toString());
                    } else {
                        return writeJson(httpVersion, HttpResponseStatus.OK, GsonUtils.toJson(content));
                    }
                } else if (contentType.startsWith("image")) {
                    return write(httpVersion, HttpResponseStatus.OK, (byte[]) content, contentType);
                } else if (contentType.equals(APPLICATION_OCTET_STREAM)) {
                    return write(httpVersion, HttpResponseStatus.OK, (byte[]) content, APPLICATION_OCTET_STREAM);
                } else {
                    return write(httpVersion, HttpResponseStatus.OK, content.toString(), contentType);
                }
            } else {
                return writeText(httpVersion, HttpResponseStatus.FORBIDDEN, "Forbidden " + request.uri() + " is denied");
            }
        } catch (HttpRequestException e) {
            if (APPLICATION_JSON.toString().equals(e.getResponseType())) {
                return writeJson(httpVersion, e.getHttpResponseStatus(), ExceptionResult.of(e));
            } else {
                return writeText(httpVersion, e.getHttpResponseStatus(), e.getMessage());
            }
        } catch (ServiceException e) {
            MethodInvocation methodInvocation = e.getMethodInvocation();
            if (methodInvocation.getResponseType().equals(APPLICATION_JSON.toString())) {
                return writeJson(httpVersion, e.getHttpResponseStatus(), ExceptionResult.of(e));
            } else {
                return writeText(httpVersion, e.getHttpResponseStatus(), e.getMessage());
            }
        } catch (InternalServerException e) {
            logger.info("An error occurred in the request", e);
            MethodInvocation methodInvocation = e.getMethodInvocation();
            if (methodInvocation.getResponseType().equals(APPLICATION_JSON.toString())) {
                return writeJson(httpVersion, e.getHttpResponseStatus(), ExceptionResult.of(e));
            } else {
                return writeText(httpVersion, e.getHttpResponseStatus(), e.getMessage());
            }
        } catch (MethodNotFoundException e) {
            logger.warn(e.getMessage());
            return writeText(httpVersion, e.getHttpResponseStatus(), e.getMessage());
        } catch (MethodNotAllowedException e) {
            return writeText(httpVersion, e.getHttpResponseStatus(), e.getMessage());
        } catch (Exception e) {
            logger.warn("An error occurred in the request", e);
            return writeText(httpVersion, HttpResponseStatus.INTERNAL_SERVER_ERROR, HttpResponseStatus.INTERNAL_SERVER_ERROR.codeAsText().toString());
        } finally {
            handlerInterceptor.afterCompletion();
        }
    }

    @Override
    protected void postHandle(HttpResponse httpResponse) {
        handlerInterceptor.postHandle(httpResponse);
    }


}
