package dev.paoding.longan.core;

import dev.paoding.longan.annotation.EnableI18n;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Internationalization implements ImportSelector {
    private final static ThreadLocal<String> languageThreadLocal = new ThreadLocal<>();
    private static final Set<String> supports = new HashSet<>();
    private static boolean enabled;
    private static String value;

    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        enabled = true;
        Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(EnableI18n.class.getName());
        if (attributes == null) {
            return new String[0];
        }

        value = (String) attributes.get("value");
        supports.add(value);
        String[] array = (String[]) attributes.get("supports");
        Collections.addAll(supports, array);
        return new String[0];
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setLanguage(String language) {
        if (language == null || language.trim().isEmpty() || !supports.contains(language)) {
            languageThreadLocal.set(value);
        } else {
            languageThreadLocal.set(language);
        }
    }

    public static String getLanguage() {
        return languageThreadLocal.get();
    }

    public static void remove() {
        languageThreadLocal.remove();
    }
}
