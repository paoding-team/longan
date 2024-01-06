package dev.paoding.longan.channel.http;


import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class HttpDataEntity {
    private static final String TYPE_FORMAT_EXCEPTION_MESSAGE = "Input to \"%s\" must be a valid %s literal, \"%s\" was not in a correct format.";
    private final Map<String, List<String>> textMap = new HashMap<>();
    private final Map<String, List<MultipartFile>> fileMap = new HashMap<>();

    public HttpDataEntity() {
    }

    public HttpDataEntity(String query) {
        if (query != null) {
            parse(query);
        }
    }

    public Set<String> getTextParameterNames() {
        return textMap.keySet();
    }

    public boolean containsParameterName(String parameterName) {
        return textMap.containsKey(parameterName) || fileMap.containsKey(parameterName);
    }

    public Object getValue(Parameter parameter, String name) {
        Class<?> parameterType = parameter.getType();
        if (parameterType.isPrimitive()) {
            return getPrimitive(parameterType, name);
        } else if (Collection.class.isAssignableFrom(parameterType)) {
            if (parameter.getParameterizedType() instanceof ParameterizedType parameterizedType) {
                Class<?> actualType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                if (List.class.isAssignableFrom(parameterType)) {
                    return getList(actualType, name);
                } else if (Set.class.isAssignableFrom(parameterType)) {
                    return getSet(actualType, name);
                }
            } else if (List.class.isAssignableFrom(parameterType)) {
                return this.getStringList(name);
            } else if (Set.class.isAssignableFrom(parameterType)) {
                return toSet(getStringList(name));
            }
        } else if (parameterType.isArray()) {
            Class<?> componentType = parameter.getType().getComponentType();
            return getArray(componentType, name);
        } else {
            return getObject(parameterType, name);
        }
        return null;
    }

    private void parse(String queryString) {
        String[] paramArray = queryString.split("&");
        for (String param : paramArray) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                put(keyValue[0], keyValue[1]);
            }
        }
    }

    public void putAll(Map<String, String> map) {
        map.forEach(this::put);
    }

    private String formatExceptionMessage(String name, String value, String type) {
        return String.format(TYPE_FORMAT_EXCEPTION_MESSAGE, name, type, value);
    }

    private short parsePrimitiveShort(String name, String value) {
        try {
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(formatExceptionMessage(name, value, "short"));
        }
    }

    private Short parseObjectShort(String name, String value) {
        try {
            return Short.valueOf(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(formatExceptionMessage(name, value, "short"));
        }
    }

    private short getPrimitiveShort(String name) {
        String value = getString(name);
        if (value != null) {
            return parsePrimitiveShort(name, value);
        }
        return 0;
    }

    private Short getObjectShort(String name) {
        String value = getString(name);
        if (value != null) {
            return parseObjectShort(name, value);
        }
        return null;
    }

    private short[] getPrimitiveShortArray(String name) {
        List<String> list = textMap.get(name);
        if (list != null) {
            short[] shortArray = new short[list.size()];
            for (int i = 0; i < list.size(); i++) {
                shortArray[i] = parsePrimitiveShort(name, list.get(i));
            }
            return shortArray;
        }
        return null;
    }

    private Short[] getObjectShortArray(String name) {
        List<String> list = textMap.get(name);
        if (list != null) {
            Short[] shortArray = new Short[list.size()];
            for (int i = 0; i < list.size(); i++) {
                shortArray[i] = parseObjectShort(name, list.get(i));
            }
            return shortArray;
        }
        return null;
    }

    private List<Short> getShortList(String name) {
        List<String> list = textMap.get(name);
        if (list != null) {
            List<Short> shortList = new ArrayList<>();
            for (String value : list) {
                shortList.add(parseObjectShort(name, value));
            }
            return shortList;
        }
        return null;
    }

    private int parsePrimitiveInteger(String name, String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(formatExceptionMessage(name, value, "integer"));
        }
    }

    private Integer parseObjectInteger(String name, String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(formatExceptionMessage(name, value, "integer"));
        }
    }

    private int getPrimitiveInteger(String name) {
        String value = getString(name);
        if (value != null) {
            return parsePrimitiveInteger(name, value);
        }
        return 0;
    }

    private Integer getObjectInteger(String name) {
        String value = getString(name);
        if (value != null) {
            return parseObjectInteger(name, value);
        }
        return null;
    }

    private int[] getPrimitiveIntegerArray(String name) {
        List<String> list = textMap.get(name);
        if (list != null) {
            int[] intArray = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                intArray[i] = parsePrimitiveInteger(name, list.get(i));
            }
            return intArray;
        }
        return null;
    }

    private Integer[] getObjectIntegerArray(String name) {
        List<String> list = textMap.get(name);
        if (list != null) {
            Integer[] integerArray = new Integer[list.size()];
            for (int i = 0; i < list.size(); i++) {
                integerArray[i] = parseObjectInteger(name, list.get(i));
            }
            return integerArray;
        }
        return null;
    }

    private List<Integer> getIntegerList(String name) {
        List<String> list = textMap.get(name);
        if (list != null) {
            List<Integer> integerList = new ArrayList<>();
            for (String value : list) {
                integerList.add(parseObjectInteger(name, value));
            }
            return integerList;
        }
        return null;
    }

    private long parsePrimitiveLong(String name, String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(formatExceptionMessage(name, value, "long"));
        }
    }

    private Long parseObjectLong(String name, String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(formatExceptionMessage(name, value, "long"));
        }
    }

    private long getPrimitiveLong(String name) {
        String value = getString(name);
        if (value != null) {
            return parsePrimitiveLong(name, value);
        }
        return 0;
    }

    private Long getObjectLong(String name) {
        String value = getString(name);
        if (value != null) {
            return parseObjectLong(name, value);
        }
        return null;
    }


    private long[] getPrimitiveLongArray(String name) {
        List<String> list = textMap.get(name);
        if (list != null) {
            long[] longArray = new long[list.size()];
            for (int i = 0; i < list.size(); i++) {
                longArray[i] = parsePrimitiveLong(name, list.get(i));
            }
            return longArray;
        }
        return null;
    }

    private Long[] getObjectLongArray(String name) {
        List<String> list = textMap.get(name);
        if (list != null) {
            Long[] longArray = new Long[list.size()];
            for (int i = 0; i < list.size(); i++) {
                longArray[i] = parseObjectLong(name, list.get(i));
            }
            return longArray;
        }
        return null;
    }

    private List<Long> getLongList(String name) {
        List<String> list = textMap.get(name);
        if (list != null) {
            List<Long> longList = new ArrayList<>();
            for (String value : list) {
                longList.add(parseObjectLong(name, value));
            }
            return longList;
        }
        return null;
    }

    private float parsePrimitiveFloat(String name, String value) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(formatExceptionMessage(name, value, "float"));
        }
    }

    private Float parseObjectFloat(String name, String value) {
        try {
            return Float.valueOf(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(formatExceptionMessage(name, value, "float"));
        }
    }

    private float getPrimitiveFloat(String name) {
        String value = getString(name);
        if (value != null) {
            return parsePrimitiveFloat(name, value);
        }
        return 0;
    }

    private Float getObjectFloat(String name) {
        String value = getString(name);
        if (value != null) {
            return parseObjectFloat(name, value);
        }
        return null;
    }

    private float[] getPrimitiveFloatArray(String name) {
        List<String> list = textMap.get(name);
        if (list != null) {
            float[] floatArray = new float[list.size()];
            for (int i = 0; i < list.size(); i++) {
                floatArray[i] = parsePrimitiveFloat(name, list.get(i));
            }
            return floatArray;
        }
        return null;
    }

    private Float[] getObjectFloatArray(String name) {
        List<String> list = textMap.get(name);
        if (list != null) {
            Float[] floatArray = new Float[list.size()];
            for (int i = 0; i < list.size(); i++) {
                floatArray[i] = parseObjectFloat(name, list.get(i));
            }
            return floatArray;
        }
        return null;
    }

    private List<Float> getFloatList(String name) {
        List<String> list = textMap.get(name);
        if (list != null) {
            List<Float> floatList = new ArrayList<>();
            for (String value : list) {
                floatList.add(parseObjectFloat(name, value));
            }
            return floatList;
        }
        return null;
    }

    private double parsePrimitiveDouble(String name, String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(formatExceptionMessage(name, value, "double"));
        }
    }

    private Double parseObjectDouble(String name, String value) {
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(formatExceptionMessage(name, value, "double"));
        }
    }

    private double getPrimitiveDouble(String name) {
        String value = getString(name);
        if (value != null) {
            return parsePrimitiveDouble(name, value);
        }
        return 0;
    }

    private Double getObjectDouble(String name) {
        String value = getString(name);
        if (value != null) {
            return parseObjectDouble(name, value);
        }
        return null;
    }

    private double[] getPrimitiveDoubleArray(String name) {
        List<String> list = textMap.get(name);
        if (list != null) {
            double[] doubleArray = new double[list.size()];
            for (int i = 0; i < list.size(); i++) {
                doubleArray[i] = parsePrimitiveDouble(name, list.get(i));
            }
            return doubleArray;
        }
        return null;
    }

    private Double[] getObjectDoubleArray(String name) {
        List<String> list = textMap.get(name);
        if (list != null) {
            Double[] doubleArray = new Double[list.size()];
            for (int i = 0; i < list.size(); i++) {
                doubleArray[i] = parseObjectDouble(name, list.get(i));
            }
            return doubleArray;
        }
        return null;
    }

    private List<Double> getDoubleList(String name) {
        List<String> list = textMap.get(name);
        if (list != null) {
            List<Double> doubleList = new ArrayList<>();
            for (String value : list) {
                doubleList.add(parseObjectDouble(name, value));
            }
            return doubleList;
        }
        return null;
    }

    private boolean parsePrimitiveBoolean(String name, String value) {
        try {
            return Boolean.parseBoolean(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(formatExceptionMessage(name, value, "boolean"));
        }
    }

    private Boolean parseObjectBoolean(String name, String value) {
        try {
            return Boolean.valueOf(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(formatExceptionMessage(name, value, "boolean"));
        }
    }

    private boolean getPrimitiveBoolean(String name) {
        String value = getString(name);
        if (value != null) {
            return parsePrimitiveBoolean(name, value);
        }
        return false;
    }

    private Boolean getObjectBoolean(String name) {
        String value = getString(name);
        if (value != null) {
            return parseObjectBoolean(name, value);
        }
        return false;
    }


    private boolean[] getPrimitiveBooleanArray(String name) {
        List<String> list = textMap.get(name);
        if (list != null) {
            boolean[] booleanArray = new boolean[list.size()];
            for (int i = 0; i < list.size(); i++) {
                booleanArray[i] = parsePrimitiveBoolean(name, list.get(i));
            }
            return booleanArray;
        }
        return null;
    }

    private Boolean[] getObjectBooleanArray(String name) {
        List<String> list = textMap.get(name);
        if (list != null) {
            Boolean[] booleanArray = new Boolean[list.size()];
            for (int i = 0; i < list.size(); i++) {
                booleanArray[i] = parseObjectBoolean(name, list.get(i));
            }
            return booleanArray;
        }
        return null;
    }

    private List<Boolean> getBooleanList(String name) {
        List<String> list = textMap.get(name);
        if (list != null) {
            List<Boolean> booleanList = new ArrayList<>();
            for (String value : list) {
                booleanList.add(parseObjectBoolean(name, value));
            }
            return booleanList;
        }
        return null;
    }

    private String getString(String name) {
        List<String> list = textMap.get(name);
        if (list != null) {
            return list.getFirst();
        }
        return null;
    }

    private String[] getStringArray(String name) {
        List<String> list = textMap.get(name);
        if (list != null) {
            String[] stringArray = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                stringArray[i] = list.get(i);
            }
            return stringArray;
        }
        return null;
    }

    private List<String> getStringList(String name) {
        return textMap.get(name);
    }

    public MultipartFile getMultipartFile(String name) {
        List<MultipartFile> list = fileMap.get(name);
        if (list != null) {
            return list.getFirst();
        }
        return null;
    }

    private MultipartFile[] getMultipartFileArray(String name) {
        List<MultipartFile> list = fileMap.get(name);
        if (list != null) {
            MultipartFile[] multipartFileArray = new MultipartFile[list.size()];
            for (int i = 0; i < list.size(); i++) {
                multipartFileArray[i] = list.get(i);
            }
            return multipartFileArray;
        }
        return null;
    }

    private List<MultipartFile> getMultipartFileList(String name) {
        List<MultipartFile> list = fileMap.get(name);
        if (list != null) {
            return list;
        }
        return null;
    }

    public void put(String name, String value) {
        if (!textMap.containsKey(name)) {
            textMap.put(name, new ArrayList<>());
        }
        textMap.get(name).add(value);
    }

    public void put(String name, MultipartFile file) {
        if (!fileMap.containsKey(name)) {
            fileMap.put(name, new ArrayList<>());
        }
        fileMap.get(name).add(file);
    }


    private Object getPrimitive(Class<?> parameterType, String name) {
        if (parameterType == int.class) {
            return this.getPrimitiveInteger(name);
        } else if (parameterType == long.class) {
            return this.getPrimitiveLong(name);
        } else if (parameterType == boolean.class) {
            return this.getPrimitiveBoolean(name);
        } else if (parameterType == double.class) {
            return this.getPrimitiveDouble(name);
        } else if (parameterType == float.class) {
            return this.getPrimitiveFloat(name);
        } else if (parameterType == short.class) {
            return this.getPrimitiveShort(name);
        }
        return null;
    }

    private Object getObject(Class<?> type, String name) {
        if (type == String.class) {
            return this.getString(name);
        } else if (type == Integer.class) {
            return this.getObjectInteger(name);
        } else if (type == Long.class) {
            return this.getObjectLong(name);
        } else if (type == Boolean.class) {
            return this.getObjectBoolean(name);
        } else if (type == Double.class) {
            return this.getObjectDouble(name);
        } else if (type == Float.class) {
            return this.getObjectFloat(name);
        } else if (type == Short.class) {
            return this.getObjectShort(name);
        } else if (type == MultipartFile.class) {
            return this.getMultipartFile(name);
        }
        return null;
    }

    private Object getArray(Class<?> type, String name) {
        if (type == String.class) {
            return this.getStringArray(name);
        } else if (type == Integer.class) {
            return this.getObjectIntegerArray(name);
        } else if (type == int.class) {
            return this.getPrimitiveIntegerArray(name);
        } else if (type == Long.class) {
            return this.getObjectLongArray(name);
        } else if (type == long.class) {
            return this.getPrimitiveLongArray(name);
        } else if (type == Boolean.class) {
            return this.getObjectBooleanArray(name);
        } else if (type == boolean.class) {
            return this.getPrimitiveBooleanArray(name);
        } else if (type == Double.class) {
            return this.getObjectDoubleArray(name);
        } else if (type == double.class) {
            return this.getPrimitiveDoubleArray(name);
        } else if (type == Float.class) {
            return this.getObjectFloatArray(name);
        } else if (type == float.class) {
            return this.getPrimitiveFloatArray(name);
        } else if (type == Short.class) {
            return this.getObjectShortArray(name);
        } else if (type == short.class) {
            return this.getPrimitiveShortArray(name);
        } else if (type == MultipartFile.class) {
            return this.getMultipartFileArray(name);
        }
        return null;
    }

    private List<?> getList(Class<?> type, String name) {
        if (type == String.class) {
            return this.getStringList(name);
        } else if (type == Integer.class) {
            return this.getIntegerList(name);
        } else if (type == Long.class) {
            return this.getLongList(name);
        } else if (type == Boolean.class) {
            return this.getBooleanList(name);
        } else if (type == Double.class) {
            return this.getDoubleList(name);
        } else if (type == Float.class) {
            return this.getFloatList(name);
        } else if (type == Short.class) {
            return this.getShortList(name);
        } else if (type == MultipartFile.class) {
            return this.getMultipartFileList(name);
        }
        return null;
    }

    private Set<?> getSet(Class<?> type, String name) {
        return toSet(getList(type, name));
    }

    private <T> Set<T> toSet(List<T> list) {
        if (list != null) {
            return new HashSet<>(list);
        }
        return null;
    }

    @Override
    public String toString() {
        return "HttpDataEntity{" +
                "textMap=" + textMap +
                ", fileMap=" + fileMap +
                '}';
    }
}
