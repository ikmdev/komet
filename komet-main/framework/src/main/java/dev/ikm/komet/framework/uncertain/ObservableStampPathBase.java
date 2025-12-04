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
package dev.ikm.komet.framework.uncertain;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import dev.ikm.komet.framework.view.ObservableCoordinateAbstract;
import dev.ikm.tinkar.coordinate.stamp.StampPath;
import dev.ikm.tinkar.coordinate.stamp.StampPathImmutable;
import dev.ikm.tinkar.coordinate.stamp.StampPositionRecord;
import dev.ikm.tinkar.terms.ConceptFacade;

public abstract class ObservableStampPathBase
        extends ObservableCoordinateAbstract<StampPathImmutable>
        implements ObservableStampPath {

    /** The path concept property. */
    private final ObjectProperty<ConceptFacade> pathConceptProperty;

    private final SetProperty<StampPositionRecord> pathOriginsProperty;

    private final ListProperty<StampPositionRecord> pathOriginsAsListProperty;

    /**
     * Note that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each method reference will create
     * a new object, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     */
    private final ChangeListener<ConceptFacade> pathConceptChangedListener = this::pathConceptChanged;
    private final SetChangeListener<StampPositionRecord> pathOriginsSetChanged = this::pathOriginsSetChanged;
    private final ListChangeListener<StampPositionRecord> pathOriginsListChangedListener = this::pathOriginsListChanged;

    //~--- constructors --------------------------------------------------------

    protected ObservableStampPathBase(StampPath stampPath, String coordinateName) {
        super(stampPath.toStampPathImmutable(), coordinateName);
        this.pathConceptProperty = makePathConceptProperty(stampPath);

        this.pathOriginsProperty = makePathOriginsProperty(stampPath);

        this.pathOriginsAsListProperty = makePathOriginsAsListProperty(stampPath);

        addListeners();
    }

    protected abstract ListProperty<StampPositionRecord> makePathOriginsAsListProperty(StampPath stampPath);

    protected abstract SetProperty<StampPositionRecord> makePathOriginsProperty(StampPath stampPath);

    protected abstract ObjectProperty<ConceptFacade> makePathConceptProperty(StampPath stampPath);

    @Override
    protected final void addListeners() {
        this.pathConceptProperty.addListener(this.pathConceptChangedListener);
        this.pathOriginsProperty.addListener(this.pathOriginsSetChanged);
        this.pathOriginsAsListProperty.addListener(this.pathOriginsListChangedListener);
    }

    @Override
    protected final void removeListeners() {
        this.pathConceptProperty.removeListener(this.pathConceptChangedListener);
        this.pathOriginsProperty.removeListener(this.pathOriginsSetChanged);
        this.pathOriginsAsListProperty.removeListener(this.pathOriginsListChangedListener);
    }

    private void pathOriginsSetChanged(SetChangeListener.Change<? extends StampPositionRecord> c) {
        this.setValue(StampPathImmutable.make(pathConcept().nid(),
                Sets.immutable.withAll(c.getSet())));
    }

    private void pathOriginsListChanged(ListChangeListener.Change<? extends StampPositionRecord> c) {
        this.setValue(StampPathImmutable.make(pathConcept().nid(),
                Sets.immutable.withAll(c.getList())));
    }

    private void pathConceptChanged(ObservableValue<? extends ConceptFacade> observablePathConcept,
                                    ConceptFacade oldPathConcept,
                                    ConceptFacade newPathConcept) {
        this.setValue(StampPathImmutable.make(newPathConcept.nid(),
                getPathOrigins()));
    }

    @Override
    public ObjectProperty<ConceptFacade> pathConceptProperty() {
        return this.pathConceptProperty;
    }


    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "ObservableStampPathImpl{" + this.getValue().toString() + '}';
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return this.getValue().equals(obj);
    }

    @Override
    public SetProperty<StampPositionRecord> pathOriginsProperty() {
        return this.pathOriginsProperty;
    }

    @Override
    public final ListProperty<StampPositionRecord> pathOriginsAsListProperty() {
        return this.pathOriginsAsListProperty;
    }

    @Override
    public final StampPath getStampPath() {
        return this.getValue();
    }

    @Override
    public final int pathConceptNid() {
        return this.getValue().pathConceptNid();
    }

    @Override
    public final ImmutableSet<StampPositionRecord> getPathOrigins() {
        return this.getValue().getPathOrigins();
    }

    @Override
    public final StampPathImmutable toStampPathImmutable() {
        return getValue();
    }


}

