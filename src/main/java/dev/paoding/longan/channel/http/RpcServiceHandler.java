package dev.paoding.longan.channel.http;

import com.google.gson.JsonElement;
import dev.paoding.longan.core.Internationalization;
import dev.paoding.longan.service.ServiceException;
import dev.paoding.longan.util.GsonUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LANGUAGE;

@Component
public class RpcServiceHandler extends AbstractServiceHandler {
    private final Logger logger = LoggerFactory.getLogger(RpcServiceHandler.class);
    @Resource
    private HandlerInterceptor handlerInterceptor;
    @Resource
    private RpcServiceInvoker rpcServiceInvoker;

    public FullHttpResponse channelRead(ChannelHandlerContext ctx, FullHttpRequest request) {
        HttpVersion httpVersion = request.protocolVersion();

        if (request.method() != HttpMethod.POST) {
            String message = "Request method " + request.method() + " not supported";
            return writeText(httpVersion, HttpResponseStatus.METHOD_NOT_ALLOWED, message);
        }

        HttpRequest httpRequest = new HttpRequestImpl(request);
        String uri = request.uri();
        uri = uri.substring(4);
        try {
            Internationalization.setLanguage(httpRequest.getHeader(CONTENT_LANGUAGE));
            TimeZoneThreadLocal.put(request.headers().get("Time-Zone"));
            if (handlerInterceptor.preHandle(httpRequest)) {
                if (HttpPostRequestDecoder.isMultipart(request)) {
                    HttpDataFactory factory = new DefaultHttpDataFactory(true);
                    HttpPostMultipartRequestDecoder decoder = new HttpPostMultipartRequestDecoder(factory, request);
                    try {
                        List<InterfaceHttpData> httpDataList = decoder.getBodyHttpDatas();
                        for (InterfaceHttpData httpData : httpDataList) {
                            if (httpData.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                                FileUpload fileUpload = (FileUpload) httpData;
                                if (fileUpload.isCompleted()) {
                                    Object result = rpcServiceInvoker.invoke(uri, new MultipartFile(fileUpload));
                                    return writeJson(httpVersion, HttpResponseStatus.OK, GsonUtils.toJson(result));
                                }
                            }
                        }
                    } finally {
                        decoder.destroy();
                    }
                } else {
                    String text = request.content().toString(CharsetUtil.UTF_8);
                    Map<String, JsonElement> map = GsonUtils.toMap(text);
                    Object result = rpcServiceInvoker.invokeRpcService(uri, map);
                    if (result == null) {
                        return writeNoContent(httpVersion);
                    } else {
                        if (ByteFile.class.isAssignableFrom(result.getClass())) {
                            return write(httpVersion, (ByteFile) result);
                        } else {
                            return writeJson(httpVersion, HttpResponseStatus.OK, GsonUtils.toJson(result));
                        }
                    }
                }
            } else {
                return writeText(httpVersion, HttpResponseStatus.FORBIDDEN, "Forbidden " + request.uri() + " is denied");
            }
        } catch (ServiceException e) {
            return writeJson(httpVersion, e.getHttpResponseStatus(), ExceptionResult.of(e));
        } catch (Exception e) {
            logger.info("An error occurred in the request " + uri, e);
            ExceptionResult exceptionResult = ExceptionResult.of("internal.server.error", "An error occurred in the request " + uri);
            return writeJson(httpVersion, HttpResponseStatus.INTERNAL_SERVER_ERROR, exceptionResult);
        } finally {
            handlerInterceptor.afterCompletion();
            Internationalization.remove();
            TimeZoneThreadLocal.remove();
        }
        return writeNoContent(httpVersion);
    }

    @Override
    public void postHandle(HttpResponse httpResponse) {
        handlerInterceptor.postHandle(httpResponse);
    }
}
