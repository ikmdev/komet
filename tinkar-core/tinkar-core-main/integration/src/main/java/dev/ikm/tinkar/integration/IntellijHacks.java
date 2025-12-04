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
package dev.ikm.tinkar.integration;

/**
 * Class to read stuff in the module-info.java file, that the tests will use, since
 * IntelliJ colors them read and says things like:
 * <p>
 * "Package 'dev.ikm.tinkar.common.service' is declared in module 'dev.ikm.tinkar.common', but module 'dev.ikm.tinkar.integration' does not read it"
 * <p>
 *
 * @TODO Invalidating the IntelliJ cache and restarting may have fixed need for this class, but leaving it in for now...
 */

import dev.ikm.tinkar.common.service.DataServiceController;
import dev.ikm.tinkar.common.service.DefaultDescriptionForNidService;
import dev.ikm.tinkar.common.service.PublicIdService;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.StampService;

public class IntellijHacks {
    DataServiceController dataServiceController = null;
    DefaultDescriptionForNidService defaultDescriptionForNidService = null;
    PublicIdService publicIdService = null;
    EntityService entityService = null;
    StampService stampService = null;

}
