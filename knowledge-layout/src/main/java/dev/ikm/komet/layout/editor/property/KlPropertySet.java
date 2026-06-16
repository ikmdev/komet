package dev.ikm.komet.layout.editor.property;

import dev.ikm.komet.preferences.KometPreferences;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Base class for the set of configurable properties a Pattern (or, in the future, an area) factory
 * exposes. A concrete subclass simply declares plain JavaFX properties with the usual
 * {@code xxxProperty()}/{@code getXxx()}/{@code setXxx()} accessors — there is no per-property
 * descriptor class to write. This class discovers those properties reflectively and, from that
 * single declaration, drives both the editor UI (a control is chosen per property type, Scene
 * Builder style) and persistence.
 *
 * <p><b>Where state lives.</b> A factory is a stateless, {@code ServiceLoader}-discovered singleton,
 * so it can only <em>declare</em> what properties exist (by returning a fresh subclass instance from
 * its {@code createProperties()} method). The actual per-pattern values live in the
 * {@link KlPropertySet} instance held by the editor model, which is also the instance the journal
 * control binds to at render time.
 *
 * <p><b>Discovery contract.</b> Every {@code public}, no-argument method whose name ends in
 * {@code Property} and whose return type is a JavaFX {@link Property} is treated as an editable
 * property. The property name is the method name minus the {@code Property} suffix. Labels and
 * ordering are derived from the name unless overridden with {@link KlProperty}.
 *
 * <p>Because discovery only touches public methods of public classes, no {@code opens} directive is
 * required for subclasses that live in an exported package.
 */
public abstract class KlPropertySet {

    private static final Logger LOG = LoggerFactory.getLogger(KlPropertySet.class);

    private static final String PROPERTY_SUFFIX = "Property";

    /**
     * Prefix for the preference keys written by this class, namespacing factory-property values so
     * they never collide with the fixed keys the model writes (title, columns, etc.).
     */
    private static final String KEY_PREFIX = "kl-factory-prop-";

    /**
     * A single property discovered on a {@link KlPropertySet}: the data needed to render and bind a
     * control for it. This is a transient view produced by reflection, not something a factory
     * author declares.
     *
     * @param name        the property name (method name without the {@code Property} suffix)
     * @param displayName the label to show in the editor
     * @param valueType   the boxed value type ({@code Boolean}, {@code Integer}, an enum class, ...)
     * @param choices     the fixed set of allowed values declared via {@link KlProperty}, or empty
     *                    for an unconstrained property
     * @param property    the live JavaFX property to bind a control to
     */
    public record KlPropertyItem(String name, String displayName, Class<?> valueType,
                                 List<?> choices, Property<?> property) {
    }

    /**
     * Reflectively discovers the editable properties on this set, ordered for display.
     *
     * @return the discovered properties, ordered alphabetically by display label
     */
    public List<KlPropertyItem> discoverProperties() {
        List<KlPropertyItem> items = new ArrayList<>();

        for (Method method : getClass().getMethods()) {
            if (method.getParameterCount() != 0) {
                continue;
            }
            if (!Property.class.isAssignableFrom(method.getReturnType())) {
                continue;
            }
            String methodName = method.getName();
            if (!methodName.endsWith(PROPERTY_SUFFIX) || methodName.length() == PROPERTY_SUFFIX.length()) {
                continue;
            }

            String name = methodName.substring(0, methodName.length() - PROPERTY_SUFFIX.length());
            KlProperty hint = resolveHint(method, name);

            try {
                Property<?> property = (Property<?>) method.invoke(this);
                if (property == null) {
                    continue;
                }

                String displayName = hint != null && !hint.label().isEmpty() ? hint.label() : humanize(name);
                Class<?> valueType = valueTypeOf(method, property);
                List<?> choices = choicesOf(hint);

                items.add(new KlPropertyItem(name, displayName, valueType, choices, property));
            } catch (ReflectiveOperationException e) {
                LOG.error("Could not read property from method {}", methodName, e);
            }
        }

        items.sort(Comparator.comparing(KlPropertyItem::displayName));
        return items;
    }

    /**
     * Saves every discovered property into the given preferences node under namespaced keys.
     *
     * @param preferences the preferences node to write into
     */
    public void save(KometPreferences preferences) {
        for (KlPropertyItem item : discoverProperties()) {
            String key = KEY_PREFIX + item.name();
            Object value = item.property().getValue();
            if (value == null) {
                continue;
            }
            Class<?> type = item.valueType();

            if (type == Boolean.class) {
                preferences.putBoolean(key, (Boolean) value);
            } else if (type == Integer.class) {
                preferences.putInt(key, ((Number) value).intValue());
            } else if (type == Long.class) {
                preferences.putLong(key, ((Number) value).longValue());
            } else if (type == Float.class || type == Double.class) {
                preferences.putDouble(key, ((Number) value).doubleValue());
            } else if (type == String.class) {
                preferences.put(key, (String) value);
            } else if (Enum.class.isAssignableFrom(type)) {
                preferences.put(key, ((Enum<?>) value).name());
            } else {
                LOG.warn("Unsupported property type {} for key {}; not saved", type, key);
            }
        }
    }

    /**
     * Loads every discovered property from the given preferences node. Missing values leave the
     * property at its default.
     *
     * @param preferences the preferences node to read from
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void load(KometPreferences preferences) {
        for (KlPropertyItem item : discoverProperties()) {
            String key = KEY_PREFIX + item.name();
            Class<?> type = item.valueType();
            Property property = item.property();

            if (type == Boolean.class) {
                preferences.getBoolean(key).ifPresent(property::setValue);
            } else if (type == Integer.class) {
                OptionalInt value = preferences.getInt(key);
                if (value.isPresent()) {
                    property.setValue(value.getAsInt());
                }
            } else if (type == Long.class) {
                OptionalLong value = preferences.getLong(key);
                if (value.isPresent()) {
                    property.setValue(value.getAsLong());
                }
            } else if (type == Float.class) {
                OptionalDouble value = preferences.getDouble(key);
                if (value.isPresent()) {
                    property.setValue((float) value.getAsDouble());
                }
            } else if (type == Double.class) {
                OptionalDouble value = preferences.getDouble(key);
                if (value.isPresent()) {
                    property.setValue(value.getAsDouble());
                }
            } else if (type == String.class) {
                preferences.get(key).ifPresent(property::setValue);
            } else if (Enum.class.isAssignableFrom(type)) {
                preferences.get(key).ifPresent(name -> property.setValue(Enum.valueOf((Class) type, name)));
            } else {
                LOG.warn("Unsupported property type {} for key {}; not loaded", type, key);
            }
        }
    }

    /**
     * Resolves the {@link KlProperty} hint for a property, accepting it on either the accessor method
     * or the backing field of the same name. The accessor takes precedence when both are annotated.
     */
    private KlProperty resolveHint(Method method, String name) {
        KlProperty methodHint = method.getAnnotation(KlProperty.class);
        if (methodHint != null) {
            return methodHint;
        }
        return fieldHint(name);
    }

    /**
     * Looks up the {@link KlProperty} annotation on the backing field named {@code name}, walking up
     * the class hierarchy. Only annotation metadata is read, so no reflective field access (and thus
     * no module {@code opens}) is required.
     */
    private KlProperty fieldHint(String name) {
        Class<?> type = getClass();
        while (type != null && type != KlPropertySet.class) {
            try {
                return type.getDeclaredField(name).getAnnotation(KlProperty.class);
            } catch (NoSuchFieldException e) {
                type = type.getSuperclass();
            }
        }
        return null;
    }

    /**
     * Extracts the fixed set of allowed values declared on a property's {@link KlProperty}
     * annotation, if any. Returns an empty list when the property is unconstrained.
     */
    private static List<?> choicesOf(KlProperty hint) {
        if (hint == null) {
            return List.of();
        }
        if (hint.intChoices().length > 0) {
            List<Integer> choices = new ArrayList<>(hint.intChoices().length);
            for (int choice : hint.intChoices()) {
                choices.add(choice);
            }
            return choices;
        }
        return List.of();
    }

    /**
     * Determines the boxed value type of a discovered property. Primitive-typed JavaFX properties
     * map to their boxed types; an {@code ObjectProperty<T>} is resolved from its generic type
     * argument, falling back to the current value's class.
     */
    private static Class<?> valueTypeOf(Method method, Property<?> property) {
        if (property instanceof BooleanProperty) {
            return Boolean.class;
        }
        if (property instanceof IntegerProperty) {
            return Integer.class;
        }
        if (property instanceof LongProperty) {
            return Long.class;
        }
        if (property instanceof FloatProperty) {
            return Float.class;
        }
        if (property instanceof DoubleProperty) {
            return Double.class;
        }
        if (property instanceof StringProperty) {
            return String.class;
        }

        Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType parameterizedType
                && parameterizedType.getActualTypeArguments().length == 1
                && parameterizedType.getActualTypeArguments()[0] instanceof Class<?> typeArgument) {
            return typeArgument;
        }

        Object value = property.getValue();
        return value != null ? value.getClass() : Object.class;
    }

    /**
     * Turns a camelCase property name into a human-friendly label, e.g. {@code headerVisible}
     * becomes {@code "Header Visible"}.
     */
    private static String humanize(String name) {
        StringBuilder builder = new StringBuilder(name.length() + 4);
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i == 0) {
                builder.append(Character.toUpperCase(c));
            } else {
                if (Character.isUpperCase(c) && !Character.isUpperCase(name.charAt(i - 1))) {
                    builder.append(' ');
                }
                builder.append(c);
            }
        }
        return builder.toString();
    }
}