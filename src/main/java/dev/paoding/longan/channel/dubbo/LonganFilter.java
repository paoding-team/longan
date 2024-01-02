package dev.paoding.longan.channel.dubbo;

import org.apache.dubbo.rpc.*;

public class LonganFilter implements Filter {
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
        Object value = dubboInterceptor.filter(serviceName, methodName, parameterTypes, result.getValue());
        result.setValue(value);
        return result;
    }
}
