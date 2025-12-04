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
package dev.ikm.tinkar.coordinate.logic;

import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.Encoder;
import dev.ikm.tinkar.common.binary.EncoderOutput;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.ImmutableCoordinate;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.Objects;

@RecordBuilder
public record LogicCoordinateRecord(int classifierNid,
                                    int descriptionLogicProfileNid,
                                    int inferredAxiomsPatternNid,
                                    int statedAxiomsPatternNid,
                                    int conceptMemberPatternNid,
                                    int statedNavigationPatternNid,
                                    int inferredNavigationPatternNid,
                                    int rootNid)
        implements LogicCoordinate, ImmutableCoordinate, LogicCoordinateRecordBuilder.With {

    public static LogicCoordinateRecord make(int classifierNid,
                                             int descriptionLogicProfileNid,
                                             int inferredAxiomsPatternNid,
                                             int statedAxiomsPatternNid,
                                             int conceptMemberPatternNid,
                                             int statedNavigationPatternNid,
                                             int inferredNavigationPatternNid,
                                             int rootNid) {
        return new LogicCoordinateRecord(classifierNid, descriptionLogicProfileNid,
                inferredAxiomsPatternNid, statedAxiomsPatternNid, conceptMemberPatternNid, statedNavigationPatternNid,
                inferredNavigationPatternNid, rootNid);
    }

    public static LogicCoordinateRecord make(ConceptFacade classifier,
                                             ConceptFacade descriptionLogicProfile,
                                             PatternFacade inferredAxiomsPattern,
                                             PatternFacade statedAxiomsPattern,
                                             PatternFacade conceptMemberPattern,
                                             PatternFacade statedNavigationPattern,
                                             PatternFacade inferredNavigationPattern,
                                             ConceptFacade root) {
        return new LogicCoordinateRecord(classifier.nid(), descriptionLogicProfile.nid(),
                inferredAxiomsPattern.nid(), statedAxiomsPattern.nid(), conceptMemberPattern.nid(), statedNavigationPattern.nid(),
                inferredNavigationPattern.nid(), root.nid());
    }

    @Decoder
    public static LogicCoordinateRecord decode(DecoderInput in) {
        switch (Encodable.checkVersion(in)) {
            default:
                return new LogicCoordinateRecord(in.readNid(), in.readNid(), in.readNid(), in.readNid(),
                        in.readNid(), in.readNid(), in.readNid(), in.readNid());
        }
    }

    @Override
    @Encoder
    public void encode(EncoderOutput out) {
        out.writeNid(this.classifierNid);
        out.writeNid(this.descriptionLogicProfileNid);
        out.writeNid(this.inferredAxiomsPatternNid);
        out.writeNid(this.statedAxiomsPatternNid);
        out.writeNid(this.conceptMemberPatternNid);
        out.writeNid(this.statedNavigationPatternNid);
        out.writeNid(this.inferredNavigationPatternNid);
        out.writeNid(this.rootNid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogicCoordinate that)) return false;
        return classifierNid() == that.classifierNid() &&
                descriptionLogicProfileNid() == that.descriptionLogicProfileNid() &&
                inferredAxiomsPatternNid() == that.inferredAxiomsPatternNid() &&
                statedAxiomsPatternNid() == that.statedAxiomsPatternNid() &&
                conceptMemberPatternNid() == that.conceptMemberPatternNid() &&
                statedNavigationPatternNid() == that.statedNavigationPatternNid() &&
                inferredNavigationPatternNid() == that.inferredNavigationPatternNid() &&
                rootNid() == that.rootNid();
    }

    @Override
    public int hashCode() {
        return Objects.hash(classifierNid(), descriptionLogicProfileNid(), inferredAxiomsPatternNid(),
                statedAxiomsPatternNid(), conceptMemberPatternNid(), statedNavigationPatternNid(),
                inferredNavigationPatternNid(), rootNid());
    }

    @Override
    public String toString() {
        return "LogicCoordinateImpl{" +
                "stated axioms: " + PrimitiveData.text(this.statedAxiomsPatternNid) + "<" + this.statedAxiomsPatternNid + ">,\n" +
                "inferred axioms: " + PrimitiveData.text(this.inferredAxiomsPatternNid) + "<" + this.inferredAxiomsPatternNid + ">, \n" +
                "profile: " + PrimitiveData.text(this.descriptionLogicProfileNid) + "<" + this.descriptionLogicProfileNid + ">, \n" +
                "classifier: " + PrimitiveData.text(this.classifierNid) + "<" + this.classifierNid + ">, \n" +
                "concept members: " + PrimitiveData.text(this.conceptMemberPatternNid) + "<" + this.conceptMemberPatternNid + ">, \n" +
                "stated navigation: " + PrimitiveData.text(this.statedNavigationPatternNid) + "<" + this.statedNavigationPatternNid + ">, \n" +
                "inferred navigation: " + PrimitiveData.text(this.inferredNavigationPatternNid) + "<" + this.inferredNavigationPatternNid + ">, \n" +
                "root:" + PrimitiveData.text(this.rootNid) + "<" + this.rootNid + ">,\n" +
                "}";
    }

    @Override
    public LogicCoordinateRecord toLogicCoordinateRecord() {
        return this;
    }


}
