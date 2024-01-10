package dev.paoding.longan.channel.dubbo;

import org.apache.dubbo.rpc.*;

public class DubboFilter implements Filter, Filter.Listener {
    private DubboInterceptor dubboInterceptor;

    public void setDubboInterceptor(DubboInterceptor dubboInterceptor) {
        this.dubboInterceptor = dubboInterceptor;
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String serviceName = invocation.getServiceModel().getServiceKey();
        String methodName = invocation.getMethodName();
        Class<?>[] parameterTypes = invocation.getParameterTypes();
        Object[] arguments = invocation.getArguments();

        dubboInterceptor.validate(serviceName, methodName, parameterTypes, arguments);

        Result result = invoker.invoke(invocation);
        Object value = result.getValue();
        if (value != null) {
            value = dubboInterceptor.filter(serviceName, methodName, parameterTypes, value);
            result.setValue(value);
        }
        return result;
    }

    @Override
    public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {

    }

    @Override
    public void onError(Throwable t, Invoker<?> invoker, Invocation invocation) {

    }
}
