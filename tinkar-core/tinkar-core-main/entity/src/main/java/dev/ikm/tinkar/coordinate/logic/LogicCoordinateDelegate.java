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

/**
 *
 * 
 */
public interface LogicCoordinateDelegate extends LogicCoordinate {
   /**
    * Gets the logic coordinate.
    *
    * @return a LogicCoordinate that specifies how to manage the retrieval and display of logic information.
    */
   LogicCoordinate getLogicCoordinate();

   @Override
   default int classifierNid() {
      return getLogicCoordinate().classifierNid();
   }

   @Override
   default int rootNid() {
      return getLogicCoordinate().rootNid();
   }

   @Override
   default int descriptionLogicProfileNid() {
      return getLogicCoordinate().descriptionLogicProfileNid();
   }

   @Override
   default int inferredAxiomsPatternNid() {
      return getLogicCoordinate().inferredAxiomsPatternNid();
   }

   @Override
   default int statedAxiomsPatternNid() {
      return getLogicCoordinate().statedAxiomsPatternNid();
   }

   @Override
   default int conceptMemberPatternNid() {
      return getLogicCoordinate().conceptMemberPatternNid();
   }

   @Override
   default int statedNavigationPatternNid() {
      return getLogicCoordinate().statedNavigationPatternNid();
   }

   @Override
   default int inferredNavigationPatternNid() { return getLogicCoordinate().statedNavigationPatternNid(); }

   @Override
   default LogicCoordinateRecord toLogicCoordinateRecord() {
      return getLogicCoordinate().toLogicCoordinateRecord();
   }

}
