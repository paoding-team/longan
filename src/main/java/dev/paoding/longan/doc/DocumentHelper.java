package dev.paoding.longan.doc;

import com.google.common.base.Joiner;
import dev.paoding.longan.data.jpa.Column;
import dev.paoding.longan.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class DocumentHelper {

    public static String getRequest(Class<?> model) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t@Request({\n");
        sb.append("\t\t@Param(name = \"" + model.getSimpleName() + "\", props = {\n");
        Field[] fields = model.getDeclaredFields();
        for (Field field : fields) {
            if (field.getModifiers() == Modifier.PRIVATE) {
                boolean nullable = true;
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    nullable = column.nullable();
                }
                sb.append("\t\t\t@Prop(name = \"" + field.getName() + "\"");
                if (!nullable) {
                    sb.append(", notNull = true");
                }
                sb.append("),\n");
            }
        }
        sb.append("\t\t}\n");
        sb.append("\t})");
        return sb.toString();
    }

    public static String getResponse(Class<?> model) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t@Response(includes = {");
        Field[] fields = model.getDeclaredFields();
        List nameList = new ArrayList<>();
        for (Field field : fields) {
            if (field.getModifiers() == Modifier.PRIVATE) {
                nameList.add("\"" + field.getName() + "\"");
            }
        }
        Joiner.on(", ").appendTo(sb, nameList);
        sb.append("})");
        return sb.toString();
    }

    public static String createSaveMethod(Class<?> model) {
        String className = model.getSimpleName();
        String objectName = StringUtils.lowerFirst(className);
        StringBuilder sb = new StringBuilder();
        sb.append("\t@RpcMethod\n");
        sb.append(getRequest(model));
        sb.append("\n");
        sb.append("\t@Response(includes = {\"id\"})");
        sb.append("\n\tpublic " + className + " save(" + className + " " + objectName + ") {\n");
        sb.append("\t\treturn " + objectName + "Repository.save(" + objectName + ");\n");
        sb.append("\t}");
        return sb.toString();
    }

    public static String createGetMethod(Class<?> model) {
        String className = model.getSimpleName();
        String objectName = StringUtils.lowerFirst(className);
        StringBuilder sb = new StringBuilder();
        sb.append("\t@RpcMethod(readOnly = true)\n");
        sb.append("\t@Request({\n\t\t\t@Param(name = \"id\", notNull = true)\n\t})");
        sb.append("\n");
        sb.append(getResponse(model));
        sb.append("\n\tpublic " + className + " get(Long id) {\n");
        sb.append("\t\treturn " + objectName + "Repository.get(id);\n");
        sb.append("\t}");
        return sb.toString();
    }

    public static String createFindMethod(Class<?> model) {
        String className = model.getSimpleName();
        String objectName = StringUtils.lowerFirst(className);
        StringBuilder sb = new StringBuilder();
        sb.append("\t@RpcMethod(readOnly = true)\n");
        sb.append(getRequest(model));
        sb.append("\n");
        sb.append(getResponse(model));
        sb.append("\n\tpublic List<" + className + "> find(" + className + " " + objectName + ", Pageable pageable) {\n");
        sb.append("\t\tExample<"+className+"> example = Example.of("+objectName+");\n");
        sb.append("\t\treturn " + objectName + "Repository.findAll(example, pageable);\n");
        sb.append("\t}");
        return sb.toString();
    }

    public static String createCountMethod(Class<?> model) {
        String className = model.getSimpleName();
        String objectName = StringUtils.lowerFirst(className);
        StringBuilder sb = new StringBuilder();
        sb.append("\t@RpcMethod(readOnly = true)\n");
        sb.append(getRequest(model));
        sb.append("\n");
        sb.append("\t@Response(alias = \"总数\")");
        sb.append("\n\tpublic long count(" + className + " " + objectName + ") {\n");
        sb.append("\t\tExample<"+className+"> example = Example.of("+objectName+");\n");
        sb.append("\t\treturn " + objectName + "Repository.count(example);\n");
        sb.append("\t}");
        return sb.toString();
    }
}
