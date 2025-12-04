/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.tinkar.entity;

import dev.ikm.tinkar.component.Component;
import dev.ikm.tinkar.component.FieldDataType;
import dev.ikm.tinkar.component.SemanticVersion;
import dev.ikm.tinkar.component.graph.DiTree;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpression;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.SemanticFacade;
import org.eclipse.collections.api.list.ImmutableList;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A version of a SemanticEntity.
 * <p>
 * A semantic is an assertion that is patterned by a {@link PatternEntity}. Each semantic version references:
 * - the pattern that defines the meaning and data types of its fields; and
 * - the component (concept, semantic, pattern, or other entity) that the assertion is about (the referenced component).
 * <p>
 * Implementations expose the raw field values via {@link #fieldValues()} and provide a set of convenience conversion
 * methods (for example, {@link #fieldAsInt(int)}, {@link #fieldAsLong(int)}, {@link #fieldAsLogicalExpression(int)})
 * that perform exact, checked conversions. These conversions follow the rules below:
 * - A null field value results in a NullPointerException.
 * - If the type is unsupported for the requested conversion, an IllegalArgumentException is thrown.
 * - For numeric conversions, overflow or loss of precision results in an ArithmeticException. Non-finite floating
 *   point values also result in an ArithmeticException.
 * <p>
 * The {@link #entity()} and {@link #chronology()} methods both return the owning {@link SemanticEntity}. The default
 * implementation of {@link #entity()} delegates to {@link #chronology()}.
 */
public interface SemanticEntityVersion extends EntityVersion, SemanticVersion {
    /**
     * Returns the owning semantic chronology for this version.
     * This method is equivalent to {@link #chronology()} and exists for API symmetry with other entity
     * version types. The default implementation delegates to {@link #chronology()}.
     *
     * @return the {@link SemanticEntity} that contains this version (never null)
     */
    @Override
    default SemanticEntity entity() {
        return chronology();
    }

    /**
     * The owning semantic chronology for this version.
     *
     * @return the {@link SemanticEntity} that contains this version (never null)
     */
    @Override
    SemanticEntity chronology();

    /**
     * The component that this semantic is about.
     * <p>
     * Equivalent to resolving {@link #referencedComponentNid()} through the {@link Entity#provider()}.
     *
     * @return the referenced component as an {@link EntityFacade}
     */
    default EntityFacade referencedComponent() {
        return EntityFacade.make(referencedComponentNid());
    }

    /**
     * The nid of the component that this semantic references (is about).
     *
     * @return the nid of the referenced component
     */
    default int referencedComponentNid() {
        return chronology().referencedComponentNid();
    }

    /**
     * The pattern that defines the meaning and data types of the fields for this semantic.
     * <p>
     * Equivalent to resolving {@link #patternNid()} through the {@link Entity#provider()}.
     *
     * @return the {@link PatternEntity} for this semantic
     */
    default PatternEntity pattern() {
        return Entity.provider().getEntityFast(patternNid());
    }

    /**
     * The nid of the {@link PatternEntity} that constrains this semantic's fields.
     *
     * @return the nid of the pattern for this semantic
     */
    default int patternNid() {
        return chronology().patternNid();
    }

    /**
     * Returns the inferred {@link FieldDataType} for the value at the given field index.
     * This is derived from the runtime type of the value in {@link #fieldValues()}.
     *
     * @param fieldIndex zero-based index into {@link #fieldValues()}
     * @return the data type for the specified field
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    default FieldDataType fieldDataType(int fieldIndex) {
        return FieldDataType.getFieldDataType(fieldValues().get(fieldIndex));
    }

    /**
     * The raw field values for this semantic version, ordered according to its {@link #pattern()}.
     * Values may include primitive wrappers, numbers, strings, nids, or entity facades depending on the pattern.
     *
     * @return an immutable list of field values (never null)
     */
    @Override
    ImmutableList<Object> fieldValues();

    /**
     * Returns the field objects for this version, aligned with the supplied pattern version.
     * The supplied {@code patternVersion} is used to interpret and label the values from {@link #fieldValues()}.
     *
     * @return an immutable list of fields corresponding to this semantic version
     */
    ImmutableList<? extends Field> fields();


/**
     * Returns the value at the specified field index as an int with exact, checked conversion.
     * <p>
     * Accepts integral numeric types (Byte, Short, Integer, Long, BigInteger, BigDecimal with scale 0),
     * floating point numbers that represent an exact integer within int range, and numeric strings.
     * <p>
     * Throws:
     * - NullPointerException if the field value is null
     * - ArithmeticException on overflow, non-finite values, or loss of precision
     * - IllegalArgumentException if the type cannot be converted
     *
     * @param index zero-based field index
     * @return the field value as an int
     */
    default int fieldAsInt(int index) {
        return switch (fieldValues().get(index)) {
            case null -> throw new NullPointerException("Field value is null");
            case Integer i -> i;

            case Long l -> Math.toIntExact(l); // throws on overflow

            case Short s -> s.intValue();

            case Byte b -> b.intValue();

            case BigInteger bi when bi.bitLength() <= 31 -> bi.intValueExact(); // safe (<= 31 bits)
            case BigInteger bi -> throw new ArithmeticException("Overflow converting BigInteger to int: " + bi);

            case BigDecimal bd when bd.scale() == 0 && bd.compareTo(BigDecimal.valueOf(Integer.MIN_VALUE)) >= 0
                    && bd.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) <= 0 -> bd.intValueExact();
            case BigDecimal bd when bd.scale() != 0 ->
                    throw new ArithmeticException("Loss of precision converting BigDecimal to int: " + bd);
            case BigDecimal bd -> throw new ArithmeticException("Overflow converting BigDecimal to int: " + bd);

            case Float f when (f.isNaN() || f.isInfinite()) -> throw new ArithmeticException("Non-finite float: " + f);
            case Float f when (f < Integer.MIN_VALUE || f > Integer.MAX_VALUE) ->
                    throw new ArithmeticException("Overflow converting float to int: " + f);
            case Float f when (f.floatValue() != Math.rint(f)) ->
                    throw new ArithmeticException("Loss of precision converting float to int: " + f);
            case Float f -> f.intValue();

            case Double d when (d.isNaN() || d.isInfinite()) ->
                    throw new ArithmeticException("Non-finite double: " + d);
            case Double d when (d < Integer.MIN_VALUE || d > Integer.MAX_VALUE) ->
                    throw new ArithmeticException("Overflow converting double to int: " + d);
            case Double d when (d.doubleValue() != Math.rint(d)) ->
                    throw new ArithmeticException("Loss of precision converting double to int: " + d);
            case Double d -> d.intValue();

            case String s -> {
                // Accept optional '+' and whitespace; delegate to Integer.parseInt for exactness
                String t = s.trim();
                // If it has a decimal point/exponent, reject as loss of precision
                if (t.matches("[+-]?\\d+")) {
                    yield Integer.parseInt(t);
                }
                throw new ArithmeticException("Loss of precision converting String to int: " + s);
            }

            case Number n -> { // fallback for other Number subclasses (e.g., AtomicInteger, AtomicLong)
                if (n instanceof AtomicInteger ai) {
                    yield ai.get();
                }
                if (n instanceof AtomicLong al) {
                    yield Math.toIntExact(al.get());
                }
                // Last-resort exactness check via long
                long lv = n.longValue();
                if (n.doubleValue() != (double) lv) {
                    throw new ArithmeticException("Loss of precision converting " + n.getClass().getSimpleName() + " to int: " + n);
                }
                yield Math.toIntExact(lv);
            }

            case Object obj ->
                    throw new IllegalArgumentException("Unsupported type for int conversion: " + obj.getClass().getSimpleName() + ": " + obj);
        };
    }


/**
     * Returns the value at the specified field index as a long with exact, checked conversion.
     * <p>
     * Accepts integral numeric types (Byte, Short, Integer, Long, BigInteger, BigDecimal with scale 0),
     * floating point numbers that represent an exact integer within long range, and numeric strings.
     * <p>
     * Throws:
     * - NullPointerException if the field value is null
     * - ArithmeticException on overflow, non-finite values, or loss of precision
     * - IllegalArgumentException if the type cannot be converted
     *
     * @param index zero-based field index
     * @return the field value as a long
     */
    default long fieldAsLong(int index) {
        return switch (fieldValues().get(index)) {
            case null -> throw new NullPointerException("Field value is null");
            case Long l -> l;

            case Integer i -> i.longValue();
            case Short s -> s.longValue();
            case Byte b -> b.longValue();

            case BigInteger bi -> bi.longValueExact();

            case BigDecimal bd
                    when bd.scale() == 0
                    && bd.compareTo(BigDecimal.valueOf(Long.MIN_VALUE)) >= 0
                    && bd.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) <= 0 -> bd.longValueExact();
            case BigDecimal bd when bd.scale() != 0 ->
                    throw new ArithmeticException("Loss of precision converting BigDecimal to long: " + bd);
            case BigDecimal bd ->
                    throw new ArithmeticException("Overflow converting BigDecimal to long: " + bd);

            case Float f when (f.isNaN() || f.isInfinite()) -> throw new ArithmeticException("Non-finite float: " + f);
            case Float f when (f < Long.MIN_VALUE || f > Long.MAX_VALUE) ->
                    throw new ArithmeticException("Overflow converting float to long: " + f);
            case Float f when (f.floatValue() != Math.rint(f)) ->
                    throw new ArithmeticException("Loss of precision converting float to long: " + f);
            case Float f -> f.longValue();

            case Double d when (d.isNaN() || d.isInfinite()) ->
                    throw new ArithmeticException("Non-finite double: " + d);
            case Double d when (d < Long.MIN_VALUE || d > Long.MAX_VALUE) ->
                    throw new ArithmeticException("Overflow converting double to long: " + d);
            case Double d when (d.doubleValue() != Math.rint(d)) ->
                    throw new ArithmeticException("Loss of precision converting double to long: " + d);
            case Double d -> d.longValue();

            case String s -> {
                String t = s.trim();
                if (t.matches("[+-]?\\d+")) {
                    yield Long.parseLong(t);
                }
                throw new ArithmeticException("Loss of precision converting String to long: " + s);
            }

            case AtomicLong al -> al.get();
            case AtomicInteger ai -> ai.get();

            case Number n -> {
                double dv = n.doubleValue();
                long lv = n.longValue();
                if (Double.isNaN(dv) || Double.isInfinite(dv)) {
                    throw new ArithmeticException("Non-finite number: " + n);
                }
                // Ensure an exact integer value and in-range for long
                if (dv != (double) lv) {
                    throw new ArithmeticException("Loss of precision converting " + n.getClass().getSimpleName() + " to long: " + n);
                }
                yield lv;
            }
            case Object obj ->
                    throw new IllegalArgumentException("Unsupported type for int conversion: " + obj.getClass().getSimpleName() + ": " + obj);
        };

    }

/**
     * Returns the value at the specified field index as a double.
     * <p>
     * Accepts numeric types and numeric strings. BigDecimal and BigInteger are converted using their
     * doubleValue with no additional scaling, which may lose precision for large values.
     * <p>
     * Throws:
     * - NullPointerException if the field value is null
     * - IllegalArgumentException if the type cannot be converted
     *
     * @param index zero-based field index
     * @return the field value as a double (may be NaN or ±Infinity if present in the source)
     */
    default double fieldAsDouble(int index) {
        Object v = fieldValues().get(index);
        return switch (v) {
            case null -> throw new NullPointerException("Field value is null");
            case Double d -> {
                if (d.isNaN() || d.isInfinite()) throw new ArithmeticException("Non-finite double: " + d);
                yield d;
            }
            case Float f -> {
                if (f.isNaN() || f.isInfinite()) throw new ArithmeticException("Non-finite float: " + f);
                yield (double) f;
            }
            case Integer i -> (double) i;
            case Long l -> (double) l;
            case Short s -> (double) s;
            case Byte b -> (double) b;

            case BigInteger bi -> bi.doubleValue(); // may lose precision, but representable as double
            case BigDecimal bd -> bd.doubleValue(); // may lose precision

            case String s -> {
                String t = s.trim();
                double d = Double.parseDouble(t);
                if (Double.isNaN(d) || Double.isInfinite(d)) {
                    throw new ArithmeticException("Non-finite parsed double: " + s);
                }
                yield d;
            }

            case AtomicInteger ai -> (double) ai.get();
            case AtomicLong al -> (double) al.get();

            case Number n -> {
                double d = n.doubleValue();
                if (Double.isNaN(d) || Double.isInfinite(d)) {
                    throw new ArithmeticException("Non-finite number: " + n);
                }
                yield d;
            }

            default -> throw new IllegalArgumentException("Unsupported type for double conversion: " +
                    v.getClass().getSimpleName() + ": " + v);
        };
    }

/**
     * Returns the value at the specified field index as a float.
     * <p>
     * Accepts numeric types and numeric strings. BigDecimal and BigInteger are converted using their
     * floatValue with no additional scaling, which may lose precision for large values.
     * <p>
     * Throws:
     * - NullPointerException if the field value is null
     * - IllegalArgumentException if the type cannot be converted
     *
     * @param index zero-based field index
     * @return the field value as a float (may be NaN or ±Infinity if present in the source)
     */
    default float fieldAsFloat(int index) {
        Object v = fieldValues().get(index);
        return switch (v) {
            case null -> throw new NullPointerException("Field value is null");
            case Float f -> {
                if (f.isNaN() || f.isInfinite()) throw new ArithmeticException("Non-finite float: " + f);
                yield f;
            }
            case Double d -> {
                if (d.isNaN() || d.isInfinite()) throw new ArithmeticException("Non-finite double: " + d);
                float f = d.floatValue();
                // overflow to +/-Infinity check
                if (Float.isInfinite(f) && !Double.isInfinite(d)) {
                    throw new ArithmeticException("Overflow converting double to float: " + d);
                }
                yield f;
            }
            case Integer i -> (float) i.intValue();
            case Long l -> {
                float f = (float) (long) l;
                if (Float.isInfinite(f)) throw new ArithmeticException("Overflow converting long to float: " + l);
                yield f;
            }
            case Short s -> (float) s.shortValue();
            case Byte b -> (float) b.byteValue();

            case BigInteger bi -> {
                float f = bi.floatValue();
                if (Float.isInfinite(f))
                    throw new ArithmeticException("Overflow converting BigInteger to float: " + bi);
                yield f;
            }
            case BigDecimal bd -> {
                float f = bd.floatValue();
                if (Float.isInfinite(f))
                    throw new ArithmeticException("Overflow converting BigDecimal to float: " + bd);
                yield f;
            }

            case String s -> {
                String t = s.trim();
                float f = Float.parseFloat(t);
                if (Float.isNaN(f) || Float.isInfinite(f)) {
                    throw new ArithmeticException("Non-finite parsed float: " + s);
                }
                yield f;
            }

            case AtomicInteger ai -> (float) ai.get();
            case AtomicLong al -> {
                long l = al.get();
                float f = (float) l;
                if (Float.isInfinite(f)) throw new ArithmeticException("Overflow converting AtomicLong to float: " + l);
                yield f;
            }

            case Number n -> {
                float f = n.floatValue();
                if (Float.isNaN(f) || Float.isInfinite(f)) {
                    throw new ArithmeticException("Non-finite number: " + n);
                }
                yield f;
            }

            default -> throw new IllegalArgumentException("Unsupported type for float conversion: " +
                    v.getClass().getSimpleName() + ": " + v);
        };
    }

/**
     * Returns the value at the specified field index as a BigInteger.
     * <p>
     * Accepts integral numeric types (Byte, Short, Integer, Long, BigInteger, BigDecimal with scale 0),
     * floating point numbers that represent an exact integer, and numeric strings.
     * <p>
     * Throws:
     * - NullPointerException if the field value is null
     * - ArithmeticException on non-integer floating point or BigDecimal inputs
     * - IllegalArgumentException if the type cannot be converted
     *
     * @param index zero-based field index
     * @return the field value as a BigInteger
     */
    default BigInteger fieldAsBigInteger(int index) {
        Object v = fieldValues().get(index);
        return switch (v) {
            case null -> throw new NullPointerException("Field value is null");

            case BigInteger bi -> bi;

            case Integer i -> BigInteger.valueOf(i.longValue());
            case Long l -> BigInteger.valueOf(l);
            case Short s -> BigInteger.valueOf(s.longValue());
            case Byte b -> BigInteger.valueOf(b.longValue());

            case BigDecimal bd when bd.scale() == 0 -> bd.toBigIntegerExact();
            case BigDecimal bd ->
                    throw new ArithmeticException("Loss of precision converting BigDecimal to BigInteger: " + bd);

            case Float f -> {
                if (f.isNaN() || f.isInfinite()) throw new ArithmeticException("Non-finite float: " + f);
                if (f.floatValue() != Math.rint(f)) {
                    throw new ArithmeticException("Loss of precision converting float to BigInteger: " + f);
                }
                yield BigInteger.valueOf(f.longValue());
            }
            case Double d -> {
                if (d.isNaN() || d.isInfinite()) throw new ArithmeticException("Non-finite double: " + d);
                if (d.doubleValue() != Math.rint(d)) {
                    throw new ArithmeticException("Loss of precision converting double to BigInteger: " + d);
                }
                yield BigInteger.valueOf(d.longValue());
            }

            case String s -> {
                String t = s.trim();
                if (!t.matches("[+-]?\\d+")) {
                    throw new ArithmeticException("Loss of precision converting String to BigInteger: " + s);
                }
                yield new BigInteger(t);
            }

            case AtomicInteger ai -> BigInteger.valueOf(ai.get());
            case AtomicLong al -> BigInteger.valueOf(al.get());

            default -> throw new IllegalArgumentException("Unsupported type for BigInteger conversion: " +
                    v.getClass().getSimpleName() + ": " + v);
        };
    }

/**
     * Returns the value at the specified field index as a BigDecimal.
     * <p>
     * Accepts all numeric types and numeric strings. Integral types are converted with scale 0.
     * Floating point inputs produce a BigDecimal via valueOf to preserve common representations.
     * <p>
     * Throws:
     * - NullPointerException if the field value is null
     * - IllegalArgumentException if the type cannot be converted
     *
     * @param index zero-based field index
     * @return the field value as a BigDecimal
     */
    default BigDecimal fieldAsBigDecimal(int index) {
        Object v = fieldValues().get(index);
        return switch (v) {
            case null -> throw new NullPointerException("Field value is null");

            case BigDecimal bd -> bd;

            case BigInteger bi -> new BigDecimal(bi);

            case Integer i -> BigDecimal.valueOf(i.longValue());
            case Long l -> BigDecimal.valueOf(l);
            case Short s -> BigDecimal.valueOf(s.longValue());
            case Byte b -> BigDecimal.valueOf(b.longValue());

            case Float f -> {
                if (f.isNaN() || f.isInfinite()) {
                    throw new ArithmeticException("Non-finite float: " + f);
                }
                yield BigDecimal.valueOf(f.doubleValue()); // via double; use new BigDecimal(String) if needed
            }
            case Double d -> {
                if (d.isNaN() || d.isInfinite()) {
                    throw new ArithmeticException("Non-finite double: " + d);
                }
                yield BigDecimal.valueOf(d);
            }

            case String s -> {
                String t = s.trim();
                yield new BigDecimal(t);
            }

            case AtomicInteger ai -> BigDecimal.valueOf(ai.get());
            case AtomicLong al -> BigDecimal.valueOf(al.get());

            case Number n -> BigDecimal.valueOf(n.doubleValue());

            default -> throw new IllegalArgumentException("Unsupported type for BigDecimal conversion: " +
                    v.getClass().getSimpleName() + ": " + v);
        };
    }

    // Boolean as int/long/double/float if needed
/**
     * Returns the value at the specified field index as a boolean.
     * <p>
     * Accepts Boolean values directly and common textual representations: "true"/"false",
     * "1"/"0", and "yes"/"no" (case-insensitive and trimmed). No other types are accepted.
     * <p>
     * Throws:
     * - NullPointerException if the field value is null
     * - IllegalArgumentException if the value cannot be interpreted as a boolean
     *
     * @param index zero-based field index
     * @return the field value as a boolean
     */
    default boolean fieldAsBoolean(int index) {
        Object v = fieldValues().get(index);
        return switch (v) {
            case null -> throw new NullPointerException("Field value is null");
            case Boolean b -> b;
            case String s -> {
                String t = s.trim().toLowerCase();
                if (t.equals("true") || t.equals("1") || t.equals("yes")) yield true;
                if (t.equals("false") || t.equals("0") || t.equals("no")) yield false;
                throw new IllegalArgumentException("Unsupported String for boolean: " + s);
            }
            default -> throw new IllegalArgumentException("Unsupported type for boolean conversion: " +
                    v.getClass().getSimpleName() + ": " + v);
        };
    }

/**
     * Returns the value at the specified field index as a String.
     * <p>
     * For numeric and boolean values, returns the default string representation. BigDecimal uses
     * toPlainString to avoid scientific notation. For EntityFacade, returns its XML fragment. Null
     * values return null. All other objects use toString().
     *
     * @param index zero-based field index
     * @return the field value rendered as a String, or null if the field is null
     */
    default String fieldAsString(int index) {
        Object v = fieldValues().get(index);
        return switch (v) {
            case null -> null;

            // Strings as-is (trim not applied to preserve original)
            case String s -> s;

            // Primitive wrappers
            case Integer i -> Integer.toString(i);
            case Long l -> Long.toString(l);
            case Short s -> Short.toString(s);
            case Byte b -> Byte.toString(b);
            case Boolean bool -> Boolean.toString(bool);
            case Character c -> Character.toString(c);

            // Floating point – preserve special values and default formatting
            case Float f -> {
                if (f.isNaN()) yield "NaN";
                if (f.equals(Float.POSITIVE_INFINITY)) yield "Infinity";
                if (f.equals(Float.NEGATIVE_INFINITY)) yield "-Infinity";
                yield Float.toString(f);
            }
            case Double d -> {
                if (d.isNaN()) yield "NaN";
                if (d.equals(Double.POSITIVE_INFINITY)) yield "Infinity";
                if (d.equals(Double.NEGATIVE_INFINITY)) yield "-Infinity";
                yield Double.toString(d);
            }

            // Big numbers
            case BigInteger bi -> bi.toString();
            case BigDecimal bd -> bd.toPlainString();

            // Atomics and Numbers
            case AtomicInteger ai -> Integer.toString(ai.get());
            case AtomicLong al -> Long.toString(al.get());
            case Number n -> n.toString();

            case EntityFacade ef -> ef.toXmlFragment();

            // Fallback to toString for any other object
            default -> v.toString();
        };
    }

/**
     * Returns the value at the specified field index as an EntityFacade.
     * <p>
     * Accepts EntityFacade directly, Component (resolved via publicId), or an int nid resolved via
     * EntityService. Any other type results in an error.
     * <p>
     * Throws:
     * - NullPointerException if the field value is null
     * - IllegalArgumentException if the value cannot be resolved to an entity
     *
     * @param index zero-based field index
     * @return the resolved EntityFacade
     */
    default EntityFacade fieldAsEntityFacade(int index) {
        Object v = fieldValues().get(index);
        return switch (v) {
            case null -> throw new NullPointerException("Field value is null");
            case EntityFacade e -> e;
            case Component c -> EntityService.get().getEntity(c.publicId()).get();
            case Integer i -> EntityService.get().getEntity(i).get();
            default -> throw new IllegalArgumentException("Unsupported type for Entity conversion: " +
                    v.getClass().getSimpleName() + ": " + v);
        };
    }

/**
     * Returns the value at the specified field index as a ConceptFacade.
     * <p>
     * Accepts ConceptFacade directly, or resolves an EntityFacade/nid to a ConceptFacade via EntityService.
     * If the resolved entity is not a concept, an error is thrown.
     *
     * @param index zero-based field index
     * @return the resolved ConceptFacade
     * @throws NullPointerException if the field value is null
     * @throws IllegalArgumentException if the value cannot be resolved to a ConceptFacade
     */
    default ConceptFacade fieldAsConceptFacade(int index) {
        Object v = fieldValues().get(index);
        return switch (v) {
            case null -> throw new NullPointerException("Field value is null");
            case ConceptFacade c -> c;
            case EntityFacade e when EntityService.get().getEntity(e).get() instanceof ConceptFacade conceptFacade -> conceptFacade;
            case Integer i when EntityService.get().getEntity(i).get() instanceof ConceptFacade conceptFacade -> conceptFacade;
            default -> throw new IllegalArgumentException("Unsupported type for Entity conversion: " +
                    v.getClass().getSimpleName() + ": " + v);
        };
    }

/**
     * Returns the value at the specified field index as a SemanticFacade.
     * <p>
     * Accepts SemanticFacade directly, or resolves an EntityFacade/nid to a SemanticFacade via EntityService.
     * If the resolved entity is not a semantic, an error is thrown.
     *
     * @param index zero-based field index
     * @return the resolved SemanticFacade
     * @throws NullPointerException if the field value is null
     * @throws IllegalArgumentException if the value cannot be resolved to a SemanticFacade
     */
    default SemanticFacade fieldAsSemanticFacade(int index) {
        Object v = fieldValues().get(index);
        return switch (v) {
            case null -> throw new NullPointerException("Field value is null");
            case SemanticFacade s -> s;
            case EntityFacade e when EntityService.get().getEntity(e).get() instanceof SemanticFacade semanticFacade -> semanticFacade;
            case Integer i when EntityService.get().getEntity(i).get() instanceof SemanticFacade semanticFacade -> semanticFacade;
            default -> throw new IllegalArgumentException("Unsupported type for Entity conversion: " +
                    v.getClass().getSimpleName() + ": " + v);
        };
    }

/**
     * Returns the value at the specified field index as a PatternFacade.
     * <p>
     * Accepts PatternFacade directly, or resolves an EntityFacade/nid to a PatternFacade via EntityService.
     * If the resolved entity is not a pattern, an error is thrown.
     *
     * @param index zero-based field index
     * @return the resolved PatternFacade
     * @throws NullPointerException if the field value is null
     * @throws IllegalArgumentException if the value cannot be resolved to a PatternFacade
     */
    default PatternFacade fieldAsPatternFacade(int index) {
        Object v = fieldValues().get(index);
        return switch (v) {
            case null -> throw new NullPointerException("Field value is null");
            case PatternFacade p -> p;
            case EntityFacade e when EntityService.get().getEntity(e).get() instanceof PatternFacade patternFacade -> patternFacade;
            case Integer i when EntityService.get().getEntity(i).get() instanceof PatternFacade patternFacade -> patternFacade;
            default -> throw new IllegalArgumentException("Unsupported type for Entity conversion: " +
                    v.getClass().getSimpleName() + ": " + v);
        };
    }

/**
     * Returns the value at the specified field index as a StampEntity.
     * <p>
     * Resolves an EntityFacade or nid via EntityService and requires the resolved entity to be a StampEntity.
     *
     * @param index zero-based field index
     * @return the resolved StampEntity
     * @throws NullPointerException if the field value is null
     * @throws IllegalArgumentException if the value cannot be resolved to a StampEntity
     */
    default StampEntity fieldAsStampEntity(int index) {
        Object v = fieldValues().get(index);
        return switch (v) {
            case null -> throw new NullPointerException("Field value is null");
            case EntityFacade e when EntityService.get().getEntity(e).get() instanceof StampEntity stampEntity -> stampEntity;
            case Integer i when EntityService.get().getEntity(i).get() instanceof StampEntity stampEntity -> stampEntity;
            default -> throw new IllegalArgumentException("Unsupported type for Entity conversion: " +
                    v.getClass().getSimpleName() + ": " + v);
        };
    }

/**
     * Returns the value at the specified field index as a DiTree of EntityVertex.
     * <p>
     * Expects the stored value to already be a DiTree; no resolution is performed.
     *
     * @param index zero-based field index
     * @return the DiTree value
     * @throws NullPointerException if the field value is null
     * @throws IllegalArgumentException if the field is not a DiTree
     */
    default DiTree<EntityVertex> fieldAsDiTree(int index) {
        Object v = fieldValues().get(index);
        return switch (v) {
            case null -> throw new NullPointerException("Field value is null");
            case DiTree<?> tree -> (DiTree<EntityVertex>) tree;
            default -> throw new IllegalArgumentException("Unsupported type for Entity conversion: " +
                    v.getClass().getSimpleName() + ": " + v);
        };
    }

/**
     * Returns the value at the specified field index as a LogicalExpression.
     * <p>
     * Expects the stored value to be a DiTree<EntityVertex> and wraps it in a LogicalExpression.
     * No additional validation or transformation of the tree is performed.
     *
     * @param index zero-based field index
     * @return a LogicalExpression constructed from the stored DiTree
     * @throws NullPointerException if the field value is null
     * @throws IllegalArgumentException if the field is not a DiTree
     */
    default LogicalExpression fieldAsLogicalExpression(int index) {
        Object v = fieldValues().get(index);
        return switch (v) {
            case null -> throw new NullPointerException("Field value is null");
            case DiTree<?> tree -> new LogicalExpression((DiTree<EntityVertex>) tree);
            default -> throw new IllegalArgumentException("Unsupported type for Entity conversion: " +
                    v.getClass().getSimpleName() + ": " + v);
        };
    }
}
