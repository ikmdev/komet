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
package dev.ikm.komet.framework.builder;

import io.soabase.recordbuilder.core.RecordBuilder;
import dev.ikm.komet.terms.KometConcept;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.KernelConcept;
import dev.ikm.tinkar.terms.KernelPattern;

@RecordBuilder
public record DescriptionBuilderRecord(ConceptFacade language, String text,
                                       ConceptFacade descriptionType,
                                       ConceptFacade caseSensitivity,
                                       AcceptabilityRecord... acceptabilityRecords)
        implements DescriptionBuilderRecordBuilder.With {
    public static DescriptionBuilderRecord makeRegularName(String text) {
        return new DescriptionBuilderRecord(KernelConcept.ENGLISH_LANGUAGE,
                text,
                KernelConcept.REGULAR_NAME_DESCRIPTION_TYPE,
                KernelConcept.DESCRIPTION_NOT_CASE_SENSITIVE,
                new AcceptabilityRecord(KernelPattern.US_DIALECT_PATTERN, KernelConcept.PREFERRED),
                new AcceptabilityRecord(KernelPattern.GB_DIALECT_PATTERN, KernelConcept.PREFERRED));
    }

    public static DescriptionBuilderRecord makeFullyQualifiedName(String text) {
        return new DescriptionBuilderRecord(KernelConcept.ENGLISH_LANGUAGE,
                text,
                KernelConcept.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE,
                KernelConcept.DESCRIPTION_NOT_CASE_SENSITIVE,
                new AcceptabilityRecord(KernelPattern.US_DIALECT_PATTERN, KernelConcept.PREFERRED),
                new AcceptabilityRecord(KernelPattern.GB_DIALECT_PATTERN, KernelConcept.PREFERRED));
    }

    public static DescriptionBuilderRecord makeSynonym(String text) {
        return new DescriptionBuilderRecord(KernelConcept.ENGLISH_LANGUAGE,
                text,
                KernelConcept.REGULAR_NAME_DESCRIPTION_TYPE,
                KernelConcept.DESCRIPTION_NOT_CASE_SENSITIVE,
                new AcceptabilityRecord(KernelPattern.US_DIALECT_PATTERN, KometConcept.ACCEPTABLE),
                new AcceptabilityRecord(KernelPattern.GB_DIALECT_PATTERN, KometConcept.ACCEPTABLE));
    }

}
