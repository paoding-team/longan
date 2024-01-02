package dev.paoding.longan.util;

public class StringUtils {
    public static String lowerFirst(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    public static String upperFirst(String str) {
        char[] chars = str.toCharArray();
        chars[0] -= 32;
        return String.valueOf(chars);
    }

    public static String lower(String name) {
        return underline(name).toLowerCase();
    }

    public static String upper(String name) {
        return underline(name).toUpperCase();
    }

    public static String lowerCamel(String underscoreName) {
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toLowerCase(underscoreName.charAt(0)));
        return toCamel(underscoreName, sb);
    }

    private static String toCamel(String underscoreName, StringBuilder sb) {
        for (int i = 1; i < underscoreName.length(); i++) {
            char chr = underscoreName.charAt(i);
            if (chr == '_') {
                i++;
                sb.append(Character.toUpperCase(underscoreName.charAt(i)));
            } else {
                sb.append(Character.toLowerCase(underscoreName.charAt(i)));
            }
        }
        return sb.toString();
    }

    public static String upperCamel(String underscoreName) {
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toUpperCase(underscoreName.charAt(0)));
        return toCamel(underscoreName, sb);
    }

    /**
     * 将驼峰命名转换小写下划线命名法
     *
     * @param camelName
     * @return
     */
    public static String lowerUnderscore(String camelName) {
        return underscore(camelName).toLowerCase();
    }

    /**
     * 将驼峰命名转换大写写下划线命名法
     *
     * @param camelName
     * @return
     */
    public static String upperUnderscore(String camelName) {
        return underscore(camelName).toUpperCase();
    }

    public static String underscore(String camelName) {
        StringBuilder sb = new StringBuilder();
        sb.append(camelName.charAt(0));
        for (int i = 1; i < camelName.length(); i++) {
            char chr = camelName.charAt(i);
            if (Character.isUpperCase(chr)) {
                sb.append("_");
            }
            sb.append(chr);
        }
        return sb.toString();
    }

    public static String underline(String name) {
        return underscore(name);
    }

//    public static String toColumnName(String name) {
//        StringBuilder sb = new StringBuilder();
//        sb.append(name.charAt(0));
//        for (int i = 1; i < name.length(); i++) {
//            char chr = name.charAt(i);
//            if (Character.isUpperCase(chr)) {
//                sb.append("_");
//            }
//            sb.append(chr);
//        }
//        return sb.toString().toLowerCase();
//    }
}
