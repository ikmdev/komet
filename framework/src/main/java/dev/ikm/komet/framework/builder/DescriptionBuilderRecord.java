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

import dev.ikm.komet.terms.KometTerm;
import io.soabase.recordbuilder.core.RecordBuilder;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.KernelTerm;

@RecordBuilder
public record DescriptionBuilderRecord(ConceptFacade language, String text,
                                       ConceptFacade descriptionType,
                                       ConceptFacade caseSensitivity,
                                       AcceptabilityRecord... acceptabilityRecords)
        implements DescriptionBuilderRecordBuilder.With {
    public static DescriptionBuilderRecord makeRegularName(String text) {
        return new DescriptionBuilderRecord(KernelTerm.ENGLISH_LANGUAGE,
                text,
                KernelTerm.REGULAR_NAME_DESCRIPTION_TYPE,
                KernelTerm.DESCRIPTION_NOT_CASE_SENSITIVE,
                new AcceptabilityRecord(KernelTerm.US_DIALECT_PATTERN, KernelTerm.PREFERRED),
                new AcceptabilityRecord(KernelTerm.GB_DIALECT_PATTERN, KernelTerm.PREFERRED));
    }

    public static DescriptionBuilderRecord makeFullyQualifiedName(String text) {
        return new DescriptionBuilderRecord(KernelTerm.ENGLISH_LANGUAGE,
                text,
                KernelTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE,
                KernelTerm.DESCRIPTION_NOT_CASE_SENSITIVE,
                new AcceptabilityRecord(KernelTerm.US_DIALECT_PATTERN, KernelTerm.PREFERRED),
                new AcceptabilityRecord(KernelTerm.GB_DIALECT_PATTERN, KernelTerm.PREFERRED));
    }

    public static DescriptionBuilderRecord makeSynonym(String text) {
        return new DescriptionBuilderRecord(KernelTerm.ENGLISH_LANGUAGE,
                text,
                KernelTerm.REGULAR_NAME_DESCRIPTION_TYPE,
                KernelTerm.DESCRIPTION_NOT_CASE_SENSITIVE,
                new AcceptabilityRecord(KernelTerm.US_DIALECT_PATTERN, KometTerm.ACCEPTABLE),
                new AcceptabilityRecord(KernelTerm.GB_DIALECT_PATTERN, KometTerm.ACCEPTABLE));
    }

}
