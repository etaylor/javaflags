package com.ajitgeorge.flags;

import com.google.common.base.Predicate;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

/**
 * TODO - generate usage
 * TODO - don't require '='
 * TODO - support booleans
 * TODO - support ints
 * TODO - support yes/no, true/false for booleans
 * TODO = support unspecified value for boolean flag means true
 */
public class Flags {
    private Reflections reflections;
    private Map<Class, Setter> setters = Setters.all();

    public Flags(String packagePrefix) {
        reflections = new Reflections(packagePrefix, new FieldAnnotationsScanner());
    }

    public List<String> parse(String[] argv) {
        List<String> nonFlagArguments = newArrayList();

        Set<Field> flaggedFields = reflections.getFieldsAnnotatedWith(Flag.class);

        for (String s : argv) {
            if (s.startsWith("--")) {
                String[] parts = s.split("=", 2);
                final String name = parts[0].substring(2);
                String value = parts[1];

                Iterable<Field> fields = filter(flaggedFields, new Predicate<Field>() {
                    @Override
                    public boolean apply(Field elem) {
                        return elem.getAnnotation(Flag.class).value().equals(name);
                    }
                });
                for (Field field : fields) {
                    try {
                        Setter setter = setters.get(field.getType());

                        if (setter == null) {
                            throw new IllegalArgumentException("flagged field is of unknown type " + field.getType());
                        }

                        setter.set(field, value);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                nonFlagArguments.add(s);
            }
        }
        return nonFlagArguments;
    }
}