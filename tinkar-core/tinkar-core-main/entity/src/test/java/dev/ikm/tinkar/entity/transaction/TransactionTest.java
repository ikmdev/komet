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
package dev.ikm.tinkar.entity.transaction;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TransactionTest {
    /*
    NOTE: method addComponent(int entityNid) is not being used anywhere, but in this Test Class so far.
     */

    private static final Logger LOG = LoggerFactory.getLogger(TransactionTest.class);

    @Test
    @Order(1)
    /*
    Test with value Not equals to Zero
     */
    public void testAddComponentNotZero() {
        LOG.warn("Test addComponent(entityNid) Not Zero");

        Transaction transaction = new Transaction();
        int actualEntityNid = 6;
        int expectedEntityNid;

        expectedEntityNid = actualEntityNid;
        transaction.addComponent(actualEntityNid);
        assertEquals(expectedEntityNid, actualEntityNid, "entityNid not equals to 0");
    }

    @Test
    @Order(2)
    /*
    Test expecting Exception
     */
    public void testAddComponentWithZeroFail() {
        LOG.warn("Test addComponent(entityNid) throws IllegalStateException when entityNid = 0");

        Transaction transaction = new Transaction();
        assertThrows(IllegalStateException.class, () -> transaction.addComponent(0));
    }

    @Test
    @Order(3)
    /*
    Test with Negative value
     */
    public void testAddComponentWithNegative() {
        LOG.warn("Test addComponent(entityNid) with Negative Values");

        Transaction transaction = new Transaction();
        int actualEntityNid = -1;
        int expectedEntityNid;

        expectedEntityNid = actualEntityNid;
        transaction.addComponent(actualEntityNid);
        assertEquals(expectedEntityNid, actualEntityNid, "entityNid with Negative Values");
    }

}
