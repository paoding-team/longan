package dev.paoding.longan.data.jpa;

import dev.paoding.longan.data.Between;
import dev.paoding.longan.service.SystemException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class Example<T> {
    private ExampleMatcher exampleMatcher;
    private T entity;
    private String prefix;
    private String role;
    private boolean empty = true;
    private boolean disabled = false;
    private String joinField;
    private Set<String> validFiledSet;
    private Class<T> type;

    public Example(T... t) {
        type = (Class<T>) t.getClass().getComponentType();
    }

    public Example<T> with(T entity) {
        if (entity == null) {
            try {
                entity = type.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new SystemException(e.getMessage());
            }
        }
        this.entity = entity;
        this.exampleMatcher = new ExampleMatcher();
        this.prefix = SqlParser.toDatabaseName(type.getSimpleName()) + "_";
        this.validFiledSet = toSet(entity);

        return this;
    }

    public static <T> Example<T> of(T entity) {
        return of(entity, new ExampleMatcher());
    }

    private static <T> Example<T> of(T entity, ExampleMatcher exampleMatcher) {
        Example<T> example = new Example<T>();
        example.entity = entity;
        example.exampleMatcher = exampleMatcher;
        if (entity != null) {
            example.prefix = SqlParser.toDatabaseName(entity.getClass().getSimpleName()) + "_";
            example.validFiledSet = toSet(entity);
        } else {
            example.empty = true;
            example.validFiledSet = new HashSet<>();
        }

        return example;
    }


    private static Set<String> toSet(Object bean) {
        Set<String> set = new HashSet<>();
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        try {
            for (Field field : declaredFields) {
                if (field.getModifiers() == Modifier.PRIVATE || field.getModifiers() == Modifier.PUBLIC || field.getModifiers() == Modifier.PROTECTED) {
                    field.setAccessible(true);
                    Object value = field.get(bean);
                    if (value != null) {
                        set.add(field.getName());
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return set;
    }

    public String getRole() {
        return role;
    }

    public Example<T> joinField(String joinField) {
        this.joinField = joinField;
        return this;
    }

    public Example<T> role(String role) {
        this.role = role;
        return this;
    }

    public boolean isEmpty() {
        return empty;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public T getEntity() {
        return entity;
    }

    private boolean existed(String field) {
        if (validFiledSet.contains(field)) {
            empty = false;
            return true;
        }
        return false;
    }

    public Example<T> is(String field) {
        if (field.equals("id")) {
            disabled = validFiledSet.contains(field);
        }
        if (existed(field)) {
            exampleMatcher.add(Matcher.of("Is", field));
        }
        return this;
    }

    public Example<T> equals(String field) {
        return is(field);
    }

    public Example<T> lessThan(String field) {
        if (existed(field)) {
            exampleMatcher.add(Matcher.of("LessThan", field));
        }
        return this;
    }

    public Example<T> lessThanEqual(String field) {
        if (existed(field)) {
            exampleMatcher.add(Matcher.of("LessThanEqual", field));
        }
        return this;
    }

    public Example<T> greaterThan(String field) {
        if (existed(field)) {
            exampleMatcher.add(Matcher.of("GreaterThan", field));
        }
        return this;
    }

    public Example<T> greaterThanEqual(String field) {
        if (existed(field)) {
            exampleMatcher.add(Matcher.of("GreaterThanEqual", field));
        }
        return this;
    }

    public Example<T> after(String field) {
        if (existed(field)) {
            exampleMatcher.add(Matcher.of("After", field));
        }
        return this;
    }

    public Example<T> before(String field) {
        if (existed(field)) {
            exampleMatcher.add(Matcher.of("Before", field));
        }
        return this;
    }

    public Example<T> isNull(String field) {
//        validate(field);
        empty = false;
        exampleMatcher.add(Matcher.of("IsNull", field));
        return this;
    }

    public Example<T> isNotNull(String field) {
//        validate(field);
        empty = false;
        exampleMatcher.add(Matcher.of("IsNotNull", field));
        return this;
    }

    public Example<T> notNull(String field) {
//        validate(field);
        return isNotNull(field);
    }

    public Example<T> startingWith(String field) {
        if (existed(field)) {
            exampleMatcher.add(Matcher.of("StartingWith", field));
        }
        return this;
    }

    public Example<T> endingWith(String field) {
        if (existed(field)) {
            exampleMatcher.add(Matcher.of("EndingWith", field));
        }
        return this;
    }

    public Example<T> containing(String field) {
        if (existed(field)) {
            exampleMatcher.add(Matcher.of("Containing", field));
        }
        return this;
    }

    public Example<T> between(Between<?> between) {
        empty = false;
        if (between != null && between.getStart() != null && between.getEnd() != null) {
            exampleMatcher.add(Matcher.of(between));
        }
        return this;
    }

    public Example<T> not(String field) {
        if (existed(field)) {
            exampleMatcher.add(Matcher.of("Not", field));
        }
        return this;
    }

    public Example<T> in(String field) {
        if (existed(field)) {
            exampleMatcher.add(Matcher.of("In", field));
        }
        return this;
    }

    public Example<T> notIn(String field) {
        if (existed(field)) {
            exampleMatcher.add(Matcher.of("NotIn", field));
        }
        return this;
    }

    public Example<T> isTrue(String field) {
//        validate(field);
        empty = false;
        exampleMatcher.add(Matcher.of("IsTrue", field));
        return this;
    }

    public Example<T> isFalse(String field) {
//        validate(field);
        empty = false;
        exampleMatcher.add(Matcher.of("IsFalse", field));
        return this;
    }

    public String getJoinField() {
        return this.joinField;
    }

    public MatchResult match() {
        if (this.entity == null) {
            return MatchResult.empty();
        }
        return exampleMatcher.match("", "", this.entity);
    }

    public MatchResult match(String tableAlias) {
        if (this.entity == null) {
            return MatchResult.empty();
        }
        return exampleMatcher.match(tableAlias + ".", prefix, this.entity);
    }

    public MatchResult match(String tableAlias, String prefix) {
        if (this.entity == null) {
            return MatchResult.empty();
        }
        return exampleMatcher.match(tableAlias + ".", prefix + "_", this.entity);
    }
}
