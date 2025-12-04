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
package dev.ikm.tinkar.integration.snomed.core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *  This Class can be used but most of the SNOMEDCT patterns.
 *  It reads the input text file and
 *  Sequence of columns in the input text file.
 *  Based on the initial input data the 1st 4 columns are same and hence can be abstracted.
 *
 */
public class SnomedCTData {

    private static final String ID = "id";
    private static final String EFFECTIVE_TIME = "effectiveTime";
    private static final String ACTIVE = "active";
    private static final String MODULE_ID = "moduleId";
    private UUID namespaceUUID;
    private Map<String, List<String>> dataMap;

    private int totalRows;
    private int totalColumns;

    public UUID getNamespaceUUID() {
        return namespaceUUID;
    }

    public void setNamespaceUUID(UUID namespaceUUID) {
        this.namespaceUUID = namespaceUUID;
    }

    public Map<String, List<String>> getDataMap() {
        return dataMap;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public int getTotalColumns() {
        return totalColumns;
    }

   /**
    * This method loads the line data in this bean.
    * @Param String
    * @Returns void
    * **/
    public void load(String lineData) {
        String[] columnValues = lineData.split("\t");
        List<String> columnsHeaders = new ArrayList<>(dataMap.keySet());
        for(int i = 0; i < columnValues.length; i++){
            List<String> values = dataMap.get(columnsHeaders.get(i));
            values.add(columnValues[i]);
        }
        totalRows++;
    }

    /**
     * This method loads the header line / column names from the 1st line of the data file.
     * @Param String
     * @Returns void
     * **/
    public void loadHeaders(String lineData) {
        String[] columnHeaders = lineData.split("\t");
        dataMap = new LinkedHashMap<>();
        for(int i = 0; i < columnHeaders.length ; i++){
            if(dataMap.get(columnHeaders[i]) == null){
                dataMap.put(columnHeaders[i],new ArrayList<>());
            }
        }
        totalColumns = columnHeaders.length;
        totalRows = 0;
    }

    /**
     * This method returns the string representation of the row data.
     * @Param int
     * @Returns String
     * **/
    public String toString(int rowNumber) {
        List<String> columns = new ArrayList<>(dataMap.keySet());
        StringBuilder sb = new StringBuilder();
        for(int coulmnNumber =0 ; coulmnNumber < totalColumns ; coulmnNumber++){
            List<String> values = dataMap.get(columns.get(coulmnNumber));
            sb.append(values.get(rowNumber));
        }
       return sb.toString();
    }

    /**
     * This method returns the string representation of the row data for the requested columns.
     * @Param int
     * @Param String []
     * @Returns String
     * **/
    public String toString(int rowNumber, String ... columns) {
        StringBuilder sb = new StringBuilder();
        for(int i=0 ; i < columns.length ; i++){
            List<String> values = dataMap.get(columns[i]);
            sb.append(values.get(rowNumber));
        }
        return sb.toString();
    }


    public String getID(int rowNumber){
        return dataMap.get(ID).get(rowNumber);
    }
    public int getActive(int rowNumber) {
        return Integer.parseInt(dataMap.get(ACTIVE).get(rowNumber));
    }

    public long getEffectiveTime(int rowNumber) {
        return Instant.ofEpochSecond(Long.parseLong(dataMap.get(EFFECTIVE_TIME).get(rowNumber))).toEpochMilli();
    }

    public String getModuleId(int rowNumber) {
        return dataMap.get(MODULE_ID).get(rowNumber);
    }
}
