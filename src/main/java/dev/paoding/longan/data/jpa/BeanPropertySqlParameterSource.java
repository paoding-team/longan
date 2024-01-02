package dev.paoding.longan.data.jpa;


import dev.paoding.longan.data.Entity;
import dev.paoding.longan.util.EntityUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.namedparam.AbstractSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link SqlParameterSource} implementation that obtains parameter values
 * from bean properties of a given JavaBean object. The names of the bean
 * properties have to match the parameter names.
 *
 * <p>Uses a Spring BeanWrapper for bean property access underneath.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @see NamedParameterJdbcTemplate
 * @see BeanWrapper
 * @since 2.0
 */
public class BeanPropertySqlParameterSource extends AbstractSqlParameterSource {

    private final BeanWrapper beanWrapper;

    @Nullable
    private String[] propertyNames;


    /**
     * Create a new BeanPropertySqlParameterSource for the given bean.
     *
     * @param object the bean instance to wrap
     */
    public BeanPropertySqlParameterSource(Object object) {
        this.beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(object);
    }


    @Override
    public boolean hasValue(String paramName) {
        return this.beanWrapper.isReadableProperty(paramName);
    }

    @Override
    @Nullable
    public Object getValue(String paramName) throws IllegalArgumentException {
        try {
            Class<?> type = beanWrapper.getPropertyDescriptor(paramName).getPropertyType();
            Object value = beanWrapper.getPropertyValue(paramName);
            if (value == null) {
                return null;
            }
            if (type.isAnnotationPresent(Entity.class)) {
                return EntityUtils.getId(value);
            } else if (Enum.class.isAssignableFrom(type)) {
                return value.toString();
            } else if (Instant.class.isAssignableFrom(type)) {
                return Timestamp.from((Instant) value);
            } else {
                return value;
            }
        } catch (NotReadablePropertyException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    /**
     * Derives a default SQL type from the corresponding property type.
     *
     * @see org.springframework.jdbc.core.StatementCreatorUtils#javaTypeToSqlParameterType
     */
    @Override
    public int getSqlType(String paramName) {
        int sqlType = super.getSqlType(paramName);
        if (sqlType != TYPE_UNKNOWN) {
            return sqlType;
        }
        Class<?> propType = this.beanWrapper.getPropertyType(paramName);
        return StatementCreatorUtils.javaTypeToSqlParameterType(propType);
    }

    @Override
    @Nullable
    public String[] getParameterNames() {
        return getReadablePropertyNames();
    }

    /**
     * Provide access to the property names of the wrapped bean.
     * Uses support provided in the {@link PropertyAccessor} interface.
     *
     * @return an array containing all the known property names
     */
    public String[] getReadablePropertyNames() {
        if (this.propertyNames == null) {
            List<String> names = new ArrayList<>();
            PropertyDescriptor[] props = this.beanWrapper.getPropertyDescriptors();
            for (PropertyDescriptor pd : props) {
                if (this.beanWrapper.isReadableProperty(pd.getName())) {
                    names.add(pd.getName());
                }
            }
            this.propertyNames = StringUtils.toStringArray(names);
        }
        return this.propertyNames;
    }

}