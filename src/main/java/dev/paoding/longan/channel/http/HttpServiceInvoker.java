package dev.paoding.longan.channel.http;

import com.google.gson.JsonElement;
import dev.paoding.longan.annotation.RequestBody;
import dev.paoding.longan.core.MethodDescriptor;
import dev.paoding.longan.core.MethodInvocation;
import dev.paoding.longan.core.Result;
import dev.paoding.longan.core.ServiceInvoker;
import dev.paoding.longan.data.Between;
import dev.paoding.longan.service.*;
import dev.paoding.longan.util.GsonUtils;
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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
public class HttpServiceInvoker extends ServiceInvoker {
    private final Map<HttpMethod, List<MethodInvocation>> dynamicMethodMap = new ConcurrentHashMap<>();
    private final Map<HttpMethod, Map<String, MethodInvocation>> staticMethodMap = new ConcurrentHashMap<>();
    private final AntPathMatcher matcher = new AntPathMatcher();

    {
        for (RequestMethod requestMethod : RequestMethod.values()) {
            dynamicMethodMap.put(HttpMethod.valueOf(requestMethod.name()), new ArrayList<>());
            staticMethodMap.put(HttpMethod.valueOf(requestMethod.name()), new ConcurrentHashMap<>());
        }
    }

    public void addStaticMethod(Object service, MethodDescriptor methodDescriptor, RequestMethod requestMethod, String mapping) {
        MethodInvocation methodInvocation = createMethodWrap(service, methodDescriptor, mapping);
        staticMethodMap.get(HttpMethod.valueOf(requestMethod.name())).put(mapping, methodInvocation);
    }

    public void addDynamicMethod(Object service, MethodDescriptor methodDescriptor, RequestMethod requestMethod, String mapping) {
        MethodInvocation methodInvocation = createMethodWrap(service, methodDescriptor, mapping);
        dynamicMethodMap.get(HttpMethod.valueOf(requestMethod.name())).add(methodInvocation);
    }

    private MethodInvocation createMethodWrap(Object service, MethodDescriptor methodDescriptor, String mapping) {
        MethodInvocation methodInvocation = new MethodInvocation();
        methodInvocation.setService(service);
        methodInvocation.setMapping(mapping);
        methodInvocation.setMethod(methodDescriptor.getMethod());
        methodInvocation.setLineNumber(methodDescriptor.getLineNumber());
        return methodInvocation;
    }

    private MethodInvocation findMethodInvocation(HttpMethod httpMethod, String path) {
        if (staticMethodMap.get(httpMethod).containsKey(path)) {
            return staticMethodMap.get(httpMethod).get(path);
        }
        for (MethodInvocation methodInvocation : dynamicMethodMap.get(httpMethod)) {
            String mapping = methodInvocation.getMapping();
            if (matcher.match(mapping, path)) {
                return methodInvocation;
            }
        }
        return null;
    }

    private String[] parseURI(String uri) {
        uri = URLDecoder.decode(uri, StandardCharsets.UTF_8);
        int i = uri.indexOf("?");
        if (i > 0) {
            return new String[]{uri.substring(0, i), uri.substring(i + 1)};
        } else {
            return new String[]{uri};
        }
    }

    public Result invokeService(FullHttpRequest httpRequest) throws SystemException {
        String uri = httpRequest.uri().substring(4);
        String[] array = parseURI(uri);
        String path = array[0];
        String query = null;
        if (array.length > 1) {
            query = array[1];
        }

        MethodInvocation methodInvocation = findMethodInvocation(httpRequest.method(), path);
        if (methodInvocation == null) {
            throw new MethodNotFoundException(path + " not found");
        }

        HttpMethod httpMethod = httpRequest.method();
        String contentType = httpRequest.headers().get(HttpHeaderNames.CONTENT_TYPE);
        HttpDataEntity httpDataEntity = parseQueryParameter(methodInvocation.getMapping(), path, query);

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
                    return invoke(methodInvocation, arguments);
                } else {
                    String body = httpRequest.content().toString(CharsetUtil.UTF_8);
                    Map<String, JsonElement> jsonElementMap = GsonUtils.toMap(body);
                    arguments = parseArguments(methodInvocation, httpDataEntity, jsonElementMap);
                    return invoke(methodInvocation, arguments);
                }
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
