package dev.paoding.longan.channel.http;

import com.google.gson.JsonElement;
import dev.paoding.longan.annotation.RequestBody;
import dev.paoding.longan.core.MethodInvocation;
import dev.paoding.longan.core.Result;
import dev.paoding.longan.core.ServiceInvoker;
import dev.paoding.longan.data.Between;
import dev.paoding.longan.service.DuplicateParameterException;
import dev.paoding.longan.service.MethodNotAllowedException;
import dev.paoding.longan.service.SystemException;
import dev.paoding.longan.service.UnsupportedMediaTypeException;
import dev.paoding.longan.util.GsonUtils;
import dev.paoding.longan.validation.BeanCleaner;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Slf4j
@Component
public class HttpServiceInvoker extends ServiceInvoker {
    private final AntPathMatcher matcher = new AntPathMatcher();

    public Result invokeService(MethodInvocation methodInvocation, String path, String query, FullHttpRequest httpRequest) throws SystemException {
        HttpMethod httpMethod = httpRequest.method();
        String contentType = httpRequest.headers().get(HttpHeaderNames.CONTENT_TYPE);
        HttpDataEntity httpDataEntity = parseQueryParameter(methodInvocation.getPath(), path, query);

        Object[] arguments;
        if (httpMethod == HttpMethod.GET) {
            arguments = parseArguments(methodInvocation, httpDataEntity);
            return invoke(methodInvocation, arguments);
        } else if (httpMethod == HttpMethod.POST) {
            if (contentType == null) {
                throw new UnsupportedMediaTypeException(methodInvocation.getResponseType());
            }
            boolean isApplicationJson = isApplicationJson(contentType);
            if (isApplicationJson) {
                if (methodInvocation.hasRequestBody()) {
                    arguments = parseArguments(methodInvocation, httpDataEntity, true, httpRequest.content());
                } else {
                    String body = httpRequest.content().toString(CharsetUtil.UTF_8);
                    Map<String, JsonElement> jsonElementMap = GsonUtils.toMap(body);
                    arguments = parseArguments(methodInvocation, httpDataEntity, jsonElementMap);
                }
                return invoke(methodInvocation, arguments);
            } else if (HttpPostRequestDecoder.isMultipart(httpRequest)) {
                HttpDataFactory factory = new DefaultHttpDataFactory(true);
                HttpPostMultipartRequestDecoder decoder = new HttpPostMultipartRequestDecoder(factory, httpRequest);
                try {
                    parseMultipartFormData(decoder, httpDataEntity);
                    arguments = parseArguments(methodInvocation, httpDataEntity);
                    return invoke(methodInvocation, arguments);
                } finally {
                    decoder.destroy();
                }
            } else if (contentType.equals(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())) {
                parseFormUrlEncoded(httpRequest, httpDataEntity);
                arguments = parseArguments(methodInvocation, httpDataEntity);
                return invoke(methodInvocation, arguments);
            } else {
                arguments = parseArguments(methodInvocation, httpDataEntity, false, httpRequest.content());
                return invoke(methodInvocation, arguments);
            }
        } else {
            throw new MethodNotAllowedException(methodInvocation.getResponseType());
        }

    }

    private boolean isApplicationJson(String contentType) {
        return contentType != null && contentType.startsWith(HttpHeaderValues.APPLICATION_JSON.toString());
    }

    private Object[] parseArguments(MethodInvocation methodInvocation, HttpDataEntity httpDataEntity, Map<String, JsonElement> jsonElementMap) {
        Set<String> textParameterNames = httpDataEntity.getTextParameterNames();
        for (String textParameterName : textParameterNames) {
            if (jsonElementMap.containsKey(textParameterName)) {
                throw new DuplicateParameterException(methodInvocation.getResponseType(), textParameterName);
            }
        }

        Parameter[] parameters = methodInvocation.getParameters();
        Object[] arguments = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (httpDataEntity.containsParameterName(parameter.getName())) {
                arguments[i] = methodInvocation.getParameter(httpDataEntity, parameter);
            } else {
                JsonElement jsonElement = jsonElementMap.get(parameter.getName());
                Object argument = null;
                if (jsonElement != null) {
                    argument = GsonUtils.fromJson(jsonElement, parameter.getParameterizedType());
                    if (Between.class.isAssignableFrom(parameter.getType())) {
                        Between<?> between = (Between<?>) argument;
                        between.setField(parameter.getName());
                    }
                }
                arguments[i] = argument;
            }
            methodInvocation.validateParameter(i, arguments[i]);
        }
        return arguments;
    }

    private Object[] parseArguments(MethodInvocation methodInvocation, HttpDataEntity httpDataEntity, boolean isApplicationJson, ByteBuf body) {
        Parameter[] parameters = methodInvocation.getParameters();
        Object[] arguments = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
            if (requestBody != null) {
                arguments[i] = methodInvocation.getParameter(isApplicationJson, body, parameter);
            } else {
                arguments[i] = methodInvocation.getParameter(httpDataEntity, parameter);
            }
            methodInvocation.validateParameter(i, arguments[i]);
        }
        return arguments;
    }

    private Object[] parseArguments(MethodInvocation methodInvocation, HttpDataEntity httpDataEntity) {
        Parameter[] parameters = methodInvocation.getParameters();
        Object[] arguments = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            arguments[i] = methodInvocation.getParameter(httpDataEntity, parameter);
            methodInvocation.validateParameter(i, arguments[i]);
        }
        return arguments;
    }

    private HttpDataEntity parseQueryParameter(String mapping, String path, String query) {
        HttpDataEntity httpDataEntity = new HttpDataEntity(query);
        if (matcher.isPattern(mapping)) {
            httpDataEntity.putAll(matcher.extractUriTemplateVariables(mapping, path));
        }
        return httpDataEntity;
    }

    private void parseMultipartFormData(HttpPostMultipartRequestDecoder requestDecoder, HttpDataEntity httpDataEntity) {
        try {
            List<InterfaceHttpData> httpDataList = requestDecoder.getBodyHttpDatas();
            for (InterfaceHttpData data : httpDataList) {
                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                    FileUpload fileUpload = (FileUpload) data;
                    if (fileUpload.isCompleted()) {
                        httpDataEntity.put(fileUpload.getName(), new MultipartFile(fileUpload));
                    }
                } else if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                    Attribute attribute = (Attribute) data;
                    httpDataEntity.put(attribute.getName(), attribute.getValue());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseFormUrlEncoded(FullHttpRequest httpRequest, HttpDataEntity httpDataEntity) {
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(httpRequest);
        try {
            List<InterfaceHttpData> httpDataList = decoder.getBodyHttpDatas();
            for (InterfaceHttpData data : httpDataList) {
                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                    Attribute attribute = (Attribute) data;
                    httpDataEntity.put(attribute.getName(), attribute.getValue());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            decoder.destroy();
        }
    }

}
