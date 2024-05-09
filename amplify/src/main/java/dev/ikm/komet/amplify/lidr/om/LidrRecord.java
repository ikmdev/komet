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
package dev.ikm.komet.amplify.lidr.om;

import dev.ikm.tinkar.common.id.PublicId;

import java.util.Set;

public record LidrRecord(PublicId lidrRecordId,
                         PublicId testPerformedId,
                         PublicId dataResultsTypeId, /* Ord - 3bf24a2e-7c1d-3cad-84e9-bdda58df5905 */
                         AnalyteRecord analyte,
                         Set<TargetRecord> targets,
                         Set<SpecimenRecord> specimens,
                         Set<ResultConformanceRecord> resultConformances) {

    public static final int IDX_TEST_PERFORMED = 0;
    public static final int IDX_DATA_RESULTS_TYPE = 1;
    public static final int IDX_ANALYTES = 2;
    public static final int IDX_TARGETS = 3;
    public static final int IDX_SPECIMENS = 4;
    public static final int IDX_RESULT_CONFORMANCES = 5;

}
