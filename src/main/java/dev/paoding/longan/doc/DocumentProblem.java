package dev.paoding.longan.doc;

import dev.paoding.longan.annotation.Param;
import javassist.*;
import javassist.NotFoundException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DocumentProblem {
    private String cause;
    private String type;
    private Param param;
    private Method method;
    private Field field;

    public DocumentProblem(String cause, Method method) {
        this.cause = cause;
        this.method = method;
        this.type = "Request";
    }

    public DocumentProblem(String cause,String type, Method method) {
        this.cause = cause;
        this.method = method;
        this.type = type;
    }

    public DocumentProblem(String cause, Param param, Method method) {
        this.cause = cause;
        this.param = param;
        this.method = method;
        this.type = "Request";
    }

    public DocumentProblem(String cause, Field field) {
        this.cause = cause;
        this.field = field;
        this.type = "Column";
    }

    @Override
    public String toString() {
        if(type.endsWith("Column")){
            StringBuilder sb = new StringBuilder();
            sb.append("\n\t--------------------------------------------------------------------");
            sb.append("\n\ttype: " + type);
            sb.append("\n\tcause: " + cause);
            if (param != null) {
                sb.append("\n\tparam: " + param.name());
            }
            sb.append("\n\tfield: " + field.getDeclaringClass().getName() + "." + field.getName() + "(" + field.getDeclaringClass().getSimpleName() + ".java:" + 1 + ")");
            sb.append("\n\t--------------------------------------------------------------------");
            return sb.toString();
        }else {
            int lineNumber = getLineNumber(method);
            StringBuilder sb = new StringBuilder();
            sb.append("\n\t--------------------------------------------------------------------");
            sb.append("\n\ttype: " + type);
            sb.append("\n\tcause: " + cause);
            if (param != null) {
                sb.append("\n\tparam: " + param.name());
            }
            sb.append("\n\tmethod: " + method.getDeclaringClass().getName() + "." + method.getName() + "(" + method.getDeclaringClass().getSimpleName() + ".java:" + lineNumber + ")");
            sb.append("\n\t--------------------------------------------------------------------");
            return sb.toString();
        }
    }

    private int getLineNumber(Method method) {
        ClassPool pool = ClassPool.getDefault();
        try {
            CtClass cc = pool.get(method.getDeclaringClass().getName());
            CtMethod methodX = cc.getDeclaredMethod(method.getName());
            return methodX.getMethodInfo().getLineNumber(0) - 1;
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }


}
