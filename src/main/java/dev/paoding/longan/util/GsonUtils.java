package dev.paoding.longan.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import dev.paoding.longan.annotation.Json;
import dev.paoding.longan.service.UnexpectedJsonException;
import dev.paoding.longan.service.I18nSyntaxException;
import dev.paoding.longan.service.TypeFormatException;
import dev.paoding.longan.channel.http.TimeZoneThreadLocal;
import dev.paoding.longan.data.jpa.BeanProxyTypeAdapterFactory;

import java.io.Reader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GsonUtils {
    private static final String TYPE_FORMAT_EXCEPTION_MESSAGE = "Expected %s format, '%s' could not be parsed.";
    private static final String TYPE_FORMAT_NUMBER = "number";
    private static final Gson gson;
    private static final Map<String, DateTimeFormatter> formatterMap = new ConcurrentHashMap<>();
    private static final Map<String, ZoneId> zoneIdMap = new ConcurrentHashMap<>();

    static {
        Set<String> idSet = ZoneId.getAvailableZoneIds();
        for (String id : idSet) {
            ZoneId zoneId = ZoneId.of(id);
            zoneIdMap.put(id, zoneId);
            formatterMap.put(id, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(zoneId));
        }
        zoneIdMap.put("default", ZoneId.systemDefault());
        formatterMap.put("default", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()));

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.addSerializationExclusionStrategy(new ExclusionStrategy() {

            @Override
            public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                Json json = fieldAttributes.getAnnotation(Json.class);
                return json != null && !json.serialize();
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        });
        gsonBuilder.registerTypeAdapter(Double.class,
                (JsonSerializer<Double>) (src, typeOfSrc, context) -> {
                    BigDecimal value = BigDecimal.valueOf(src);
                    return new JsonPrimitive(value);
                });
        gsonBuilder.registerTypeAdapter(Class.class, new JsonSerializer<Class>() {
            @Override
            public JsonElement serialize(Class clazz, Type type, JsonSerializationContext jsonSerializationContext) {
                return new JsonPrimitive(clazz.getName());
            }
        });
        gsonBuilder.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
            private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            @Override
            public JsonElement serialize(Date date, Type type, JsonSerializationContext jsonSerializationContext) {
                String datetime = simpleDateFormat.format(date);
                return new JsonPrimitive(datetime);
            }
        });
        gsonBuilder.registerTypeAdapter(Instant.class, new JsonSerializer<Instant>() {
            @Override
            public JsonElement serialize(Instant instant, Type type, JsonSerializationContext jsonSerializationContext) {
                String datetime = formatterMap.get(TimeZoneThreadLocal.get()).format(instant);
                return new JsonPrimitive(datetime);
            }
        });

        gsonBuilder.registerTypeAdapter(Timestamp.class, new JsonSerializer<Timestamp>() {
            @Override
            public JsonElement serialize(Timestamp timestamp, Type type, JsonSerializationContext jsonSerializationContext) {
                String datetime = formatterMap.get(TimeZoneThreadLocal.get()).format(timestamp.toInstant());
                return new JsonPrimitive(datetime);
            }
        });
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            @Override
            public JsonElement serialize(LocalDateTime localDateTime, Type type, JsonSerializationContext jsonSerializationContext) {
                String datetime = localDateTime.format(formatter);
                return new JsonPrimitive(datetime);
            }
        });
        gsonBuilder.registerTypeAdapter(LocalTime.class, new JsonSerializer<LocalTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            @Override
            public JsonElement serialize(LocalTime localTime, Type type, JsonSerializationContext jsonSerializationContext) {
                String datetime = localTime.format(formatter);
                return new JsonPrimitive(datetime);
            }
        });
        gsonBuilder.registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public JsonElement serialize(LocalDate localDate, Type type, JsonSerializationContext jsonSerializationContext) {
                String datetime = localDate.format(formatter);
                return new JsonPrimitive(datetime);
            }
        });
        gsonBuilder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            @Override
            public Date deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                String datetime = json.getAsJsonPrimitive().getAsString();
                try {
                    return simpleDateFormat.parse(datetime);
                } catch (ParseException e) {
                    throw new TypeFormatException(String.format(TYPE_FORMAT_EXCEPTION_MESSAGE, "'yyyy-MM-dd HH:mm:ss'", json.getAsString()));
                }
            }
        });
        gsonBuilder.registerTypeAdapter(Instant.class, (JsonDeserializer<Instant>) (json, type, jsonDeserializationContext) -> {
            String timeZone = TimeZoneThreadLocal.get();
            String datetime = json.getAsJsonPrimitive().getAsString();
            return LocalDateTime.parse(datetime, formatterMap.get(timeZone)).atZone(zoneIdMap.get(timeZone)).toInstant();
        });

        gsonBuilder.registerTypeAdapter(Timestamp.class, (JsonDeserializer<Timestamp>) (json, type, jsonDeserializationContext) -> {
            String timeZone = TimeZoneThreadLocal.get();
            String datetime = json.getAsJsonPrimitive().getAsString();
            return Timestamp.from(LocalDateTime.parse(datetime, formatterMap.get(timeZone)).atZone(zoneIdMap.get(timeZone)).toInstant());
        });
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) -> {
            String datetime = json.getAsJsonPrimitive().getAsString();
            try {
                return DateTimeUtils.parseDateTime(datetime);
            } catch (DateTimeParseException e) {
                throw new TypeFormatException(String.format(TYPE_FORMAT_EXCEPTION_MESSAGE, "'yyyy-MM-dd HH:mm:ss'", json.getAsString()));
            }
        });
        gsonBuilder.registerTypeAdapter(LocalTime.class, (JsonDeserializer<LocalTime>) (json, type, jsonDeserializationContext) -> {
            String datetime = json.getAsJsonPrimitive().getAsString();
            try {
                return DateTimeUtils.parseTime(datetime);
            } catch (DateTimeParseException e) {
                throw new TypeFormatException(String.format(TYPE_FORMAT_EXCEPTION_MESSAGE, "'HH:mm:ss'", json.getAsString()));
            }
        });
        gsonBuilder.registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, type, jsonDeserializationContext) -> {
            String datetime = json.getAsJsonPrimitive().getAsString();
            try {
                return DateTimeUtils.parseDate(datetime);
            } catch (DateTimeParseException e) {
                throw new TypeFormatException(String.format(TYPE_FORMAT_EXCEPTION_MESSAGE, "'yyyy-MM-dd'", json.getAsString()));
            }
        });
        gsonBuilder.registerTypeAdapter(Long.class, (JsonDeserializer<Long>) (json, type, jsonDeserializationContext) -> {
            try {
                return json.getAsLong();
            } catch (NumberFormatException e) {
                throw new TypeFormatException(String.format(TYPE_FORMAT_EXCEPTION_MESSAGE, TYPE_FORMAT_NUMBER, json.getAsString()));
            }
        });
        gsonBuilder.registerTypeAdapter(Integer.class, (JsonDeserializer<Integer>) (json, type, jsonDeserializationContext) -> {
            try {
                return json.getAsInt();
            } catch (NumberFormatException e) {
                throw new TypeFormatException(String.format(TYPE_FORMAT_EXCEPTION_MESSAGE, TYPE_FORMAT_NUMBER, json.getAsString()));
            }
        });
        gsonBuilder.registerTypeAdapter(Short.class, (JsonDeserializer<Short>) (json, type, jsonDeserializationContext) -> {
            try {
                return json.getAsShort();
            } catch (NumberFormatException e) {
                throw new TypeFormatException(String.format(TYPE_FORMAT_EXCEPTION_MESSAGE, TYPE_FORMAT_NUMBER, json.getAsString()));
            }
        });
        gsonBuilder.registerTypeAdapter(Double.class, (JsonDeserializer<Double>) (json, type, jsonDeserializationContext) -> {
            try {
                return json.getAsDouble();
            } catch (NumberFormatException e) {
                throw new TypeFormatException(String.format(TYPE_FORMAT_EXCEPTION_MESSAGE, TYPE_FORMAT_NUMBER, json.getAsString()));
            }
        });
        gsonBuilder.registerTypeAdapter(Float.class, (JsonDeserializer<Float>) (json, type, jsonDeserializationContext) -> {
            try {
                return json.getAsFloat();
            } catch (NumberFormatException e) {
                throw new TypeFormatException(String.format(TYPE_FORMAT_EXCEPTION_MESSAGE, TYPE_FORMAT_NUMBER, json.getAsString()));
            }
        });
        gsonBuilder.registerTypeAdapter(long.class, (JsonDeserializer<Long>) (json, type, jsonDeserializationContext) -> {
            try {
                return json.getAsLong();
            } catch (NumberFormatException e) {
                throw new TypeFormatException(String.format(TYPE_FORMAT_EXCEPTION_MESSAGE, TYPE_FORMAT_NUMBER, json.getAsString()));
            }
        });
        gsonBuilder.registerTypeAdapter(int.class, (JsonDeserializer<Integer>) (json, type, jsonDeserializationContext) -> {
            try {
                return json.getAsInt();
            } catch (NumberFormatException e) {
                throw new TypeFormatException(String.format(TYPE_FORMAT_EXCEPTION_MESSAGE, TYPE_FORMAT_NUMBER, json.getAsString()));
            }
        });
        gsonBuilder.registerTypeAdapter(short.class, (JsonDeserializer<Short>) (json, type, jsonDeserializationContext) -> {
            try {
                return json.getAsShort();
            } catch (NumberFormatException e) {
                throw new TypeFormatException(String.format(TYPE_FORMAT_EXCEPTION_MESSAGE, TYPE_FORMAT_NUMBER, json.getAsString()));
            }
        });
        gsonBuilder.registerTypeAdapter(double.class, (JsonDeserializer<Double>) (json, type, jsonDeserializationContext) -> {
            try {
                return json.getAsDouble();
            } catch (NumberFormatException e) {
                throw new TypeFormatException(String.format(TYPE_FORMAT_EXCEPTION_MESSAGE, TYPE_FORMAT_NUMBER, json.getAsString()));
            }
        });
        gsonBuilder.registerTypeAdapter(float.class, (JsonDeserializer<Float>) (json, type, jsonDeserializationContext) -> {
            try {
                return json.getAsFloat();
            } catch (NumberFormatException e) {
                throw new TypeFormatException(String.format(TYPE_FORMAT_EXCEPTION_MESSAGE, TYPE_FORMAT_NUMBER, json.getAsString()));
            }
        });

        gsonBuilder.registerTypeAdapterFactory(new BeanProxyTypeAdapterFactory());
        gsonBuilder.setPrettyPrinting();

        gson = gsonBuilder.create();
    }

    public static Gson getGson() {
        return gson;
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static <T> T fromJson(Reader reader, Type type) {
        return gson.fromJson(reader, type);
    }

    public static <T> T fromJson(JsonElement jsonElement, Type type) {
        try {
            return gson.fromJson(jsonElement, type);
        } catch (JsonSyntaxException e) {
            throw new UnexpectedJsonException(e.getCause().getMessage());
        }

    }

    public static <T> T fromJson(JsonElement jsonElement, Class<T> clazz) {
        return gson.fromJson(jsonElement, clazz);
    }

    public static <T> T fromJson(String json, Type type) {
        return gson.fromJson(json, type);
    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static Map<String, JsonElement> toMap(String json) {
        try {
            return gson.fromJson(json, new TypeToken<Map<String, JsonElement>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            throw new UnexpectedJsonException();
        }
    }

    public static Map<String, String> toLocaleMap(String json) {
        try {
            return gson.fromJson(json, new TypeToken<Map<String, String>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            throw new I18nSyntaxException(json);
        }
    }

    /**
     * {"zh_CN":"手机","en_US":"phone"}
     *
     * @param json
     * @return
     */
    public static Map<String, String> toSimpleMap(String json) {
        return gson.fromJson(json, new TypeToken<Map<String, String>>() {
        }.getType());
    }

    /**
     * {"zh_CN":{"颜色","重量","尺寸"}}
     *
     * @param json
     * @return
     */
    public static Map<String, List<String>> toMapValueSimpleList(String json) {
        return gson.fromJson(json, new TypeToken<Map<String, List<String>>>() {
        }.getType());
    }

    /**
     * {"zh_CN":{"颜色":"红色"，"重量":"12g"，"尺寸":"39"}}
     *
     * @param json
     * @return
     */
    public static Map<String, Map<String, String>> toMapValueSimpleMap(String json) {
        return gson.fromJson(json, new TypeToken<Map<String, Map<String, String>>>() {
        }.getType());
    }
}
