/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 *******************************************************************************/

package org.apache.wink.common.internal.providers.entity.csv;

import org.apache.wink.common.model.csv.CsvDeserializer;
import org.apache.wink.common.model.csv.CsvSerializer;

// TODO Eli this class should be removed from the SDK (replaced with the CSV providers)
/**
 * Defines a descriptor provider for the CsvRepresentation. The provider should
 * return two descriptors: <tt>CSVDeserializationDescriptor<String[]></tt> and
 * <tt>CSVSerializationDescriptor<String[]></tt>. In case the implementation
 * decides not to implement one of these methods, it should throw
 * <tt>java.lang.UnsupportedOperationException</tt>.
 * 
 * @see org.apache.wink.common.model.csv.representation.csv.CsvDeserializer
 * @see org.apache.wink.common.model.csv.representation.csv.CsvSerializer
 */
public interface CsvDescriptorProvider {

    /**
     * returns the descriptor that is responsible to deserialize data from the
     * CSV to data object if provider decides not to implement this method, it
     * must throw <tt>java.lang.UnsupportedOperationException</tt>
     * 
     * @return CSVDeserializationDescriptor<String[]>
     */
    CsvDeserializer getDeserializationDescriptor();

    /**
     * returns the descriptor that is responsible to serialize from data object
     * to the CSV if provider decides not to implement this method, it must
     * throw <tt>java.lang.UnsupportedOperationException</tt>
     * 
     * @return CSVSerializationDescriptor<String[]>
     */
    CsvSerializer getSerializationDescriptor();

}
