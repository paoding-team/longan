package dev.paoding.longan.doc;

import dev.paoding.longan.util.GsonUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public class CodeFactory {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("使用方法：java -Dfile.encoding=utf8 -jar chili.jar 文档接口地址 终端");
            System.out.println("-接口地址 例如：http://localhost:8001/doc/");
            System.out.println("-终端 例如：USER_APP");
            System.out.println("使用例子：java -Dfile.encoding=utf8 -jar chili.jar http://localhost:8001/doc/ USER_APP");
            return;
        }
        boolean isFlutterProject = false;
        File file = new File(".");
        for (String s : file.list()) {
            if (s.equals("pubspec.yaml")) {
                isFlutterProject = true;
                break;
            }
        }
        if (!isFlutterProject) {
            System.out.println("只能在 Flutter 项目根目录下运行");
            return;
        }
        String url = args[0];
        String client = args[1];
//        String url = "http://localhost:8001/doc/";
//        String client = "USER_APP";
        if (url.endsWith("/")) {
            url += "doc.json";
        } else {
            url += "/doc.json";
        }
        System.out.println("下载接口文档数据...");
        String json = doGet(url);
        Document document = GsonUtils.fromJson(json, Document.class);
        System.out.println("生成 model 类...");
        writeModel(document);
        System.out.println("生成 service 类...");
        writeService(document, client);
        System.out.println("所有类生成完成");
    }


    public static void writeService(Document document, String client) throws IOException {
        document.getNodes().get(client).getChildren().forEach(it -> {
            try {
                writeService(document, it);
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

    public static void writeService(Document document, MetaNode metaNode) throws IOException {
        String typeName = getTypeName(metaNode.getLink());
        System.out.println(typeName);
        StringBuilder sb = new StringBuilder();
        sb.append("import 'package:chili/chili.dart';\n" + "import '../models/model.dart';");
        sb.append("\n\nclass " + typeName + "Service {");
        metaNode.getChildren().forEach(it -> {
            String methodName = getMethodName(it.getLink());
            MetaMethod metaMethod = document.getMethods().get(it.getLink());

            MetaRequest metaRequest = metaMethod.getRequest();
            metaRequest.getParams().forEach(requestParam -> {

            });

            MetaResponse metaResponse = metaMethod.getResponse();
            MetaParam responseParam = metaResponse.getParam();
            sb.append("\n\n\t");
            sb.append("\n\t///" + metaMethod.getAlias());
            sb.append("\n\t");
            StringBuilder convertSb = new StringBuilder();
            if (responseParam.isTypeModel()) {
                sb.append("static Future<" + typeName + "> " + methodName + "(");
                convertSb.append(typeName + ".fromJson(_)");
            } else if (responseParam.isActualTypeModel()) {
                sb.append("static Future<List<" + typeName + ">> " + methodName + "(");
                convertSb.append(typeName + ".fromJson(_)");
            } else {
                sb.append("static Future<" + responseParam.getDartType() + "> " + methodName + "(");
                if (responseParam.getDartType().equals("bool")) {
                    convertSb.append("_");
                } else if (responseParam.getDartType().equals("void")) {
                    convertSb.append("{}");
                } else if (responseParam.getDartType().equals("int")) {
                    convertSb.append("_");
                } else if (responseParam.getDartType().equals("String")) {
                    convertSb.append("_");
                }
            }
            AtomicBoolean isUploadMethod = new AtomicBoolean(false);
            StringBuilder paramMap = new StringBuilder();
            if (metaRequest.getParams().size() > 0) {
                sb.append("{");
            }
            metaRequest.getParams().forEach(requestParam -> {
                if (requestParam.isTypeModel()) {
                    if (requestParam.getJavaType().equals("dev.paoding.longan.sql.Between")) {
                        requestParam.setDartType("Between<" + requestParam.getChildren().get(0).getDartType() + ">");
                    } else {
                        requestParam.setDartType(getTypeName(requestParam.getJavaType()));
                    }
                } else if (requestParam.isActualTypeModel()) {
                    requestParam.setDartType(requestParam.getDartType() + "<" + getTypeName(requestParam.getActualJavaType()) + ">");
                } else if (requestParam.getDartType().equals("List")) {
                    requestParam.setDartType("List<" + requestParam.getActualDartType() + ">");
                } else if (requestParam.getDartType().equals("MultipartFile")) {
                    requestParam.setDartType("String");
                    requestParam.setName("filePath");
                    isUploadMethod.set(true);
                }
                boolean required = false;
                if(requestParam.getNotBlank()!= null && requestParam.getNotBlank()){
                    required = true;
                }
                if(requestParam.getNotEmpty() != null && requestParam.getNotEmpty()){
                    required = true;
                }
                if(requestParam.isNotNull()){
                    required = true;
                }
                sb.append((required ? "required " + requestParam.getDartType() : requestParam.getDartType() + "?") + " " + requestParam.getName() + ", ");
                paramMap.append("'" + requestParam.getName() + "': " + requestParam.getName() + ", ");
            });
            if (metaRequest.getParams().size() > 0) {
                sb.append("}");
            }
            sb.append(") {");
            if (isUploadMethod.get()) {
                sb.append("\n\t\treturn RpcClient.upload('" + it.getLink() + "', filePath).then((_) => " + convertSb + ");");
            } else {
                sb.append("\n\t\treturn RpcClient.post('" + it.getLink() + "', {" + paramMap + "}).then((_) => " + convertSb + ");");
            }
            sb.append("\n\t}");
        });
        sb.append("\n}");

        String fileName = toColumnName(typeName) + "_service.dart";
        File file = new File("lib/services", fileName);
//        File file = new File("/Users/wayne/Codes/AndroidStudioProjects/chili/example/lib/services", fileName);
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(sb.toString());
        fileWriter.close();
    }

    public static void writeModel(Document document) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("export 'between.dart';\n");
        writeBetween();
        document.getModels().values().forEach(it -> {
            sb.append("export '" + toColumnName(it.getSimpleName()) + ".dart';\n");
            try {
                writeModel(it);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        File file = new File("lib/models", "model.dart");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(sb.toString());
        fileWriter.close();
    }

    public static void writeBetween() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("class Between<T> {");
        sb.append("\n\tT start;");
        sb.append("\n\tT end;");
        sb.append("\n\n\tBetween({required this.start, required this.end});");
        sb.append("\n}");
        File file = new File("lib/models", "between.dart");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(sb.toString());
        fileWriter.close();
    }

    public static void writeModel(MetaModel metaModel) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("import 'package:chili/chili.dart';\n");
        sb.append("import 'model.dart';\n");
        sb.append("\n/// " + metaModel.getAlias());
        if (!metaModel.getDescription().isEmpty()) {
            sb.append(", " + metaModel.getDescription() + ".");
        }
        sb.append("\n///");
        metaModel.getFields().forEach(it -> {
            sb.append("\n/// [" + it.getName() + "] " + it.getAlias());
            if (it.isNotNull()) {
                sb.append(", 不为空");
            }
            if (it.getDescription() != null) {
                sb.append(", " + it.getDescription());
            }
        });
        sb.append("\nclass " + metaModel.getSimpleName() + " {\n");
        metaModel.getFields().forEach(it -> {
//            if(!it.getAlias().isEmpty()) {
//                sb.append("\t///" + it.getAlias() + "\n");
//            }
            if (it.isTypeModel()) {
                it.setDartType(getTypeName(it.getJavaType()));
            } else if (it.isActualTypeModel()) {
                it.setDartType(it.getDartType() + "<" + getTypeName(it.getActualJavaType()) + ">");
            }
            sb.append("\t" + it.getDartType() + "? " + it.getName() + ";\n");
        });

        sb.append("\n\t" + metaModel.getSimpleName() + "({");
        metaModel.getFields().forEach(it -> {
            sb.append("\n\t\tthis." + it.getName() + ", ");
//            if (it.getNullable()) {
//                sb.append("\n\t\tthis." + it.getName() + ", ");
//            } else {
//                if (it.getName().equals("id") && it.getDartType().equals("int")) {
//                    sb.append("\n\t\tthis." + it.getName() + " = 0, ");
//                } else {
//                    sb.append("\n\t\tthis." + it.getName() + ", ");
//                }
//            }
        });
        sb.append("\n\t});");


        sb.append("\n\n\tstatic _from(Map<dynamic, dynamic> json) {");
        sb.append("\n\t\treturn " + metaModel.getSimpleName() + "(");
        metaModel.getFields().forEach(it -> {
            if (it.getJavaType().equals("java.time.LocalDate")) {
                sb.append("\n\t\t\t" + it.getName() + ": TimeUtils.parseDate(json['" + it.getName() + "']),");
            } else if (it.getJavaType().equals("java.time.LocalDateTime")) {
                sb.append("\n\t\t\t" + it.getName() + ": TimeUtils.parseDateTime(json['" + it.getName() + "']),");
            } else if (it.getJavaType().equals("java.time.LocalTime")) {
                sb.append("\n\t\t\t" + it.getName() + ": TimeUtils.parseTime(json['" + it.getName() + "']),");
            } else if (it.isTypeModel()) {
                sb.append("\n\t\t\t" + it.getName() + ": " + getTypeName(it.getJavaType()) + ".fromJson(json['" + it.getName() + "']),");
            } else if (it.isActualTypeModel()) {
                sb.append("\n\t\t\t" + it.getName() + ": " + getTypeName(it.getActualJavaType()) + ".fromJson(json['" + it.getName() + "']),");
            } else {
                sb.append("\n\t\t\t" + it.getName() + ": json['" + it.getName() + "'],");
            }
        });
        sb.append("\n\t\t);");
        sb.append("\n\t}");

        sb.append("\n\n\tstatic fromJson(dynamic json) {");
        sb.append("\n\t\tif (json == null) return null;");
        sb.append("\n\t\tif (json is Map) {");
        sb.append("\n\t\t\treturn _from(json);");
        sb.append("\n\t\t} else if (json is List) {");
        sb.append("\n\t\t\treturn json.from<" + metaModel.getSimpleName() + ">(_from);");
        sb.append("\n\t\t}");
        sb.append("\n\t}");

        sb.append("\n\n\tMap<String, dynamic> toJson() => {");
        metaModel.getFields().forEach(it -> {
            sb.append("\n\t\t'" + it.getName() + "': " + it.getName() + ",");
        });
        sb.append("\n\t};");


        sb.append("\n}");
        String fileName = toColumnName(metaModel.getSimpleName()) + ".dart";
        File file = new File("lib/models", fileName);
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(sb.toString());
        fileWriter.close();
    }


    public static String getTypeName(String name) {
        String[] array = name.split("\\.");
        return array[array.length - 1];
    }

    public static String getMethodName(String name) {
        String[] array = name.split("/");
        return array[array.length - 1];
    }

    public static String toColumnName(String name) {
        StringBuilder sb = new StringBuilder();
        sb.append(name.charAt(0));
        for (int i = 1; i < name.length(); i++) {
            char chr = name.charAt(i);
            if (Character.isUpperCase(chr)) {
                sb.append("_");
            }
            sb.append(chr);
        }
        return sb.toString().toLowerCase();
    }


    public static String doGet(String httpurl) {
        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader br = null;
        String result = null;// 返回结果字符串
        try {
            // 创建远程url连接对象
            URL url = new URL(httpurl);
            // 通过远程url连接对象打开一个连接，强转成httpURLConnection类
            connection = (HttpURLConnection) url.openConnection();
            // 设置连接方式：get
            connection.setRequestMethod("GET");
            // 设置连接主机服务器的超时时间：15000毫秒
            connection.setConnectTimeout(15000);
            // 设置读取远程返回的数据时间：60000毫秒
            connection.setReadTimeout(60000);
            // 发送请求
            connection.connect();
            // 通过connection连接，获取输入流
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                // 封装输入流is，并指定字符集
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                // 存放数据
                StringBuffer sbf = new StringBuffer();
                String temp = null;
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                    sbf.append("\r\n");
                }
                result = sbf.toString();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            connection.disconnect();// 关闭远程连接
        }

        return result;
    }
}
