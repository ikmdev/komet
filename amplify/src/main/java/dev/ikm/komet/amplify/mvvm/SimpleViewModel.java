package dev.ikm.komet.amplify.mvvm;

import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A lightweight implementation of a ViewModel of the MVVM pattern.
 * <pre>
 *     reset - Will copy model values into property.
 *     save  - Will copy property values into the model value (value map).
 * </pre>
 */
public class SimpleViewModel implements ViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleViewModel.class);
    private ObservableMap<String, Property> singleValueMap = FXCollections.observableMap(new TreeMap<>());
    private ObservableMap<String, Observable> multiValueMap = FXCollections.observableMap(new TreeMap<>());
    private Map<String, Object> valueMap = FXCollections.observableMap(new TreeMap<>());

    /**
     * Copies value from valueMap to property. Updating view from model.
     * @param name property name
     * @param property
     */
    private void reloadProperty(String name, Property property) {
        Object value = valueMap.get(name);
        if (property instanceof IntegerProperty p) {
            p.set((int)valueMap.get(name));
        } else if(property instanceof LongProperty p) {
            p.set((long) value);
        } else if(property instanceof FloatProperty p) {
            p.set((float) value);
        } else if(property instanceof DoubleProperty p) {
            p.set((double) value);
        } else if(property instanceof BooleanProperty p) {
            p.set((boolean) value);
        } else if(property instanceof StringProperty p) {
            p.set((String) value);
        } else if(property instanceof ObjectProperty p) {
            p.set(value);
        }
    }

    /**
     * Copies value model to properties. ValueMap to Properties.
     * @return
     */
    public SimpleViewModel reset() {
        singleValueMap.forEach((name, propVal) -> {
            // copy values into properties
            reloadProperty(name, propVal);
        });
        multiValueMap.forEach((k, observableVal) -> {
            // copy values into properties
            if (observableVal instanceof Collection<?> collection) {
                Collection c2 = (Collection) valueMap.get(k);
                if (c2 != null) {
                    collection.clear();
                    collection.addAll(c2);
                } else {
                    collection.clear();
                }
            } else {
                throw new RuntimeException("Each value should be a Collection not a " + observableVal);
            }
        });
        return this;
    }

    /**
     * Copies Properties to ValueMap.
     * @return returns itself of type SimpleViewModel.
     */
    public SimpleViewModel save() {
        // single values
        singleValueMap.keySet().forEach( key -> {
            Property value = singleValueMap.get(key);
            // copy values into properties
            valueMap.put(key, value.getValue());
        });
        // multiple values (lists and sets)
        multiValueMap.keySet().forEach( key -> {
            Observable c = multiValueMap.get(key);
            if (c instanceof List collection) {
                valueMap.put(key, new ArrayList<>(collection));
            } else if (c instanceof Set collection) {
                valueMap.put(key, new TreeSet<>(collection));
            }
        });
        return this;
    }

    /**
     * Sets the model data (valueMap values). TODO: check type before putting it into valueMap. e.g. if StringProperty value should not allow an Integer.
     * @param name property name
     * @param value Raw value to be set as the committed data.
     * @return returns itself of type SimpleViewModel.
     */
    public SimpleViewModel setValue(String name, Object value) {
        if (singleValueMap.containsKey(name) || multiValueMap.containsKey(name)) {
            valueMap.put(name, value);
        }
        return this;
    }

    /**
     * Sets the Property to contain the new value. It does not set the model value.
     * @param name property name
     * @param value raw value
     * @return returns itself of type SimpleViewModel.
     */
    public SimpleViewModel setPropertyValue(String name, Object value) {
        if (value instanceof Collection<?> collection) {
            setPropertyValues(name, collection);
            //throw new RuntimeException("Setting property:%s value cannot be a Collection. Try calling setPropertyValues().".formatted(name));
            return this;
        }
        getProperty(name).setValue(value);
        return this;
    }
    public SimpleViewModel setPropertyValues(String name, Collection values) {
        Observable observable = multiValueMap.get(name);
        if (observable instanceof ObservableSet<?> observableSet) {
            observableSet.clear();
            if (values != null) {
                observableSet.addAll(new HashSet<>(values));
            }
        } else if (observable instanceof ObservableList<?> observableList) {
            observableList.clear();
            if (values != null) {
                observableList.addAll(new ArrayList<>(values));
            }
        } else {
            throw new RuntimeException("Not supported Observable Collection for property " + name);
        }
        return this;
    }
    /**
     * Gets the raw value from the model data.
     * @param name Name of the property
     * @return Raw value from the model data.
     */
    public <T> T getValue(String name) {
        return (T) valueMap.get(name);
    }

    public <U extends SimpleViewModel> U addProperty(String name, Property property) {
        if (singleValueMap.containsKey(name) || multiValueMap.containsKey(name)) {
            Object sVal = singleValueMap.get(name);
            Object mVal = multiValueMap.get(name);
            throw new RuntimeException("The property %s already exists. Property: %s".formatted(name, sVal !=null ? sVal : mVal));
        }
        singleValueMap.put(name, property);
        valueMap.put(name, property.getValue());
        return (U) this;
    }
    public <U extends SimpleViewModel> U addProperty(String name, String value) {
        return addProperty(name, new SimpleStringProperty(value));
    }
    public <U extends SimpleViewModel> U addProperty(String name, int value) {
        return addProperty(name, new SimpleIntegerProperty(value));
    }
    public <U extends SimpleViewModel> U addProperty(String name, long value) {
        return addProperty(name, new SimpleLongProperty(value));
    }
    public <U extends SimpleViewModel> U addProperty(String name, float value) {
        return addProperty(name, new SimpleFloatProperty(value));
    }
    public <U extends SimpleViewModel> U addProperty(String name, double value) {
        return addProperty(name, new SimpleDoubleProperty(value));
    }
    public <U extends SimpleViewModel> U addProperty(String name, boolean value) {
        return addProperty(name, new SimpleBooleanProperty(value));
    }
    public <U extends SimpleViewModel> U addProperty(String name, Collection value) {
        // if it's already an observable list or set just add it.
        if (value instanceof ObservableList<?> observableList) {
            multiValueMap.put(name, observableList);
            valueMap.put(name, new ArrayList<>(value));
        } else if (value instanceof ObservableSet observableSet) {
            multiValueMap.put(name, observableSet);
            valueMap.put(name, new TreeSet<>(value));
        } else {
            if (value instanceof Set set) {
                multiValueMap.put(name, FXCollections.observableSet(new TreeSet<>(set)));
                valueMap.put(name, new TreeSet<>(value));
            } else if (value instanceof List list) {
                multiValueMap.put(name, FXCollections.observableList(new ArrayList<>(value)));
                valueMap.put(name, new ArrayList<>(value));
            }
        }

        return (U) this;
    }
    public <U extends SimpleViewModel> U addProperty(String name, Object value) {
        singleValueMap.put(name, new SimpleObjectProperty(value));
        valueMap.put(name, value);
        return (U) this;
    }

    public <U extends SimpleViewModel> U addProperty(String name, Function<U, Collection> value) {
        return addProperty(name, value.apply((U)this));
    }
    public <T extends Property> T getProperty(String name) {
        return (T) singleValueMap.get(name);
    }
    public Property removeProperty(String name) {
        valueMap.remove(name);
        return singleValueMap.remove(name);
    }

    public Observable getObservableCollection(String name) {
        return multiValueMap.get(name);
    }
    public <T> ObservableList<T> getObservableList(String name) {
        return (ObservableList) multiValueMap.get(name);
    }
    public <T> ObservableSet<T> getObservableSet(String name) {
        return (ObservableSet) multiValueMap.get(name);
    }
    public Observable removeObservableCollection(String name) {
        return multiValueMap.remove(name);
    }
    public <T> List<T> getList(String name) {
        return (List) valueMap.get(name);
    }
    public <T> Set<T> getSet(String name) {
        return (Set) valueMap.get(name);
    }
    public <T> Collection<T> getCollection(String name) {
        return (Collection<T>) valueMap.get(name);
    }
    public void debugProperty(String name) {
        LOG.info(debugPropertyMessage(name));
    }
    public String debugPropertyMessage(String name) {
        if (getProperty(name) != null) {
            return "viewProperty:%s = %s | modelValue:%s = %s".formatted(name, getProperty(name).getValue(), name, getValue(name));
        } else if(getObservableCollection(name) != null) {
            return "viewProperty:%s = %s | modelValue:%s = %s".formatted(name, getObservableCollection(name), name, getCollection(name));
        }

        return "Unknown viewProperty:%s    ".formatted(name);

    }
    @Override
    public String toString() {
        return "SimpleViewModel {\n" +
                singleValueMap
                        .keySet()
                        .stream()
                        .map(name -> " " + debugPropertyMessage(name) + "\n")
                        .collect(Collectors.joining()) +
                multiValueMap
                        .keySet()
                        .stream()
                        .map(name -> " " + debugPropertyMessage(name) + "\n")
                        .collect(Collectors.joining()) +
                '}';
    }
    public static void main(String[] args){

        ViewModel personVm = new SimpleViewModel()
                .addProperty("firstName", "Fred")
                .addProperty("age", 54l)
                .addProperty("height", 123)
                .addProperty("colors", Set.of("red", "blue"))
                .addProperty("foods", List.of("bbq", "chips", "bbq"))
                .addProperty("thing", new Object(){
                    @Override
                    public String toString() {
                        return "thing ";
                    }
                })
                .addProperty("mpg", 20.5f);
        log("--------------");
        log("Creation personVm \n" + personVm);

        log("--------------");
        personVm.setPropertyValue("firstName", "Mary");
        log("before save " + personVm.debugPropertyMessage("firstName"));
        personVm.save();
        log("after save " + personVm.debugPropertyMessage("firstName"));
        log("--------------");
        personVm.setPropertyValue("age", 20);
        log("before save " + personVm.debugPropertyMessage("age"));
        personVm.save();
        log("after save " + personVm.debugPropertyMessage("age"));
        log("--------------");
        personVm.setPropertyValue("height", 555);
        log("before save " + personVm.debugPropertyMessage("height"));
        personVm.save();
        log("after save " + personVm.debugPropertyMessage("height"));
        log("--------------");
        personVm.setPropertyValue("colors", Set.of("green"));
        log("before save " + personVm.debugPropertyMessage("colors"));
        personVm.save();
        log("after save " + personVm.debugPropertyMessage("colors"));

        // changing ("bbq", "chips", "bbq") TO ("corn", "crabs")
        personVm.setPropertyValues("foods", Set.of("corn", "crabs"));
        log("before save " + personVm.debugPropertyMessage("foods"));
        personVm.save(); // commit data from
        log("after save  " + personVm.debugPropertyMessage("foods"));


        log("--------------");

        ViewModel personVm2 = new SimpleViewModel()
                .addProperty("firstName", "Fred")
                .addProperty("age", 54l)
                .addProperty("height", 123)
                .addProperty("colors", Set.of("red", "blue"))
                .addProperty("foods", List.of("bbq", "chips", "bbq"))
                .addProperty("thing", new Object(){
                    @Override
                    public String toString() {
                        return "thing ";
                    }
                })
                .addProperty("mpg", 20.5f);


        personVm2.setPropertyValue("firstName", "Mary");
        personVm2.setPropertyValue("age", 20);
        personVm2.setPropertyValue("height", 555);
        personVm2.setPropertyValue("colors", Set.of("green"));
        personVm2.setPropertyValues("foods", Set.of("corn", "crabs"));
        personVm2.setPropertyValue("thing", new Object(){
            @Override
            public String toString() {
                return "thing 2";
            }
        });
        log("before reset personVm2 \n" + personVm2);
        personVm2.reset();
        log("after reset  personVm2 \n" + personVm2);

    }
    private static void log(String message) {
        System.out.println(message);
    }
}
