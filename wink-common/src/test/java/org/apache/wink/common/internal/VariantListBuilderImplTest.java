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

package org.apache.wink.common.internal;

import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertArrayEquals;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Variant.VariantListBuilder;

import org.apache.wink.common.internal.VariantListBuilderImpl;

import junit.framework.TestCase;

public class VariantListBuilderImplTest extends TestCase {

    public void testBuildMediaTypes() {
        Variant variant1 = new Variant(MediaType.APPLICATION_ATOM_XML_TYPE, null, null);
        Variant variant2 = new Variant(MediaType.APPLICATION_JSON_TYPE, null, null);

        VariantListBuilder builder = new VariantListBuilderImpl();
        List<Variant> list = builder.mediaTypes(MediaType.APPLICATION_ATOM_XML_TYPE).add().build();
        assertList(new Variant[] {variant1}, list);

        list =
            builder
                .mediaTypes(MediaType.APPLICATION_ATOM_XML_TYPE, MediaType.APPLICATION_JSON_TYPE)
                .add().build();
        assertList(new Variant[] {variant1, variant2}, list);
    }

    public void testBuildLanguages() {
        Variant variant1 = new Variant(null, Locale.ENGLISH, null);
        Variant variant2 = new Variant(null, Locale.CHINESE, null);

        VariantListBuilder builder = new VariantListBuilderImpl();
        List<Variant> list = builder.languages(Locale.ENGLISH).add().build();
        assertList(new Variant[] {variant1}, list);

        list = builder.languages(Locale.ENGLISH, Locale.CHINESE).add().build();
        assertList(new Variant[] {variant1, variant2}, list);
    }

    public void testBuildEncodings() {
        Variant variant1 = new Variant(null, null, "UTF-8");
        Variant variant2 = new Variant(null, null, "UTF-16");

        VariantListBuilder builder = new VariantListBuilderImpl();
        List<Variant> list = builder.encodings("UTF-8").add().build();
        assertList(new Variant[] {variant1}, list);

        list = builder.encodings("UTF-8", "UTF-16").add().build();
        assertList(new Variant[] {variant1, variant2}, list);
    }

    public void testBuildMediaTypesAndLanguages() {
        Variant variant1 = new Variant(MediaType.APPLICATION_ATOM_XML_TYPE, Locale.ENGLISH, null);
        Variant variant2 = new Variant(MediaType.APPLICATION_ATOM_XML_TYPE, Locale.CHINESE, null);
        Variant variant3 = new Variant(MediaType.APPLICATION_JSON_TYPE, Locale.ENGLISH, null);
        Variant variant4 = new Variant(MediaType.APPLICATION_JSON_TYPE, Locale.CHINESE, null);

        VariantListBuilder builder = new VariantListBuilderImpl();
        List<Variant> list = null;

        list =
            builder.mediaTypes(MediaType.APPLICATION_ATOM_XML_TYPE).languages(Locale.ENGLISH).add()
                .build();
        assertList(new Variant[] {variant1}, list);

        list =
            builder.mediaTypes(MediaType.APPLICATION_ATOM_XML_TYPE).languages(Locale.CHINESE).add()
                .build();
        assertList(new Variant[] {variant2}, list);

        list =
            builder.mediaTypes(MediaType.APPLICATION_ATOM_XML_TYPE).languages(Locale.ENGLISH,
                                                                              Locale.CHINESE).add()
                .build();
        assertList(new Variant[] {variant1, variant2}, list);

        list =
            builder
                .mediaTypes(MediaType.APPLICATION_ATOM_XML_TYPE, MediaType.APPLICATION_JSON_TYPE)
                .languages(Locale.ENGLISH).add().build();
        assertList(new Variant[] {variant1, variant3}, list);

        list =
            builder
                .mediaTypes(MediaType.APPLICATION_ATOM_XML_TYPE, MediaType.APPLICATION_JSON_TYPE)
                .languages(Locale.ENGLISH, Locale.CHINESE).add().build();
        assertList(new Variant[] {variant1, variant2, variant3, variant4}, list);
    }

    public void testBuildMediaTypesAndEncodings() {
        Variant variant1 = new Variant(MediaType.APPLICATION_ATOM_XML_TYPE, null, "UTF-8");
        Variant variant2 = new Variant(MediaType.APPLICATION_ATOM_XML_TYPE, null, "UTF-16");
        Variant variant3 = new Variant(MediaType.APPLICATION_JSON_TYPE, null, "UTF-8");
        Variant variant4 = new Variant(MediaType.APPLICATION_JSON_TYPE, null, "UTF-16");

        VariantListBuilder builder = new VariantListBuilderImpl();
        List<Variant> list = null;

        list =
            builder.mediaTypes(MediaType.APPLICATION_ATOM_XML_TYPE).encodings("UTF-8").add()
                .build();
        assertList(new Variant[] {variant1}, list);

        list =
            builder.mediaTypes(MediaType.APPLICATION_ATOM_XML_TYPE).encodings("UTF-16").add()
                .build();
        assertList(new Variant[] {variant2}, list);

        list =
            builder.mediaTypes(MediaType.APPLICATION_ATOM_XML_TYPE).encodings("UTF-8", "UTF-16")
                .add().build();
        assertList(new Variant[] {variant1, variant2}, list);

        list =
            builder
                .mediaTypes(MediaType.APPLICATION_ATOM_XML_TYPE, MediaType.APPLICATION_JSON_TYPE)
                .encodings("UTF-8").add().build();
        assertList(new Variant[] {variant1, variant3}, list);

        list =
            builder
                .mediaTypes(MediaType.APPLICATION_ATOM_XML_TYPE, MediaType.APPLICATION_JSON_TYPE)
                .encodings("UTF-8", "UTF-16").add().build();
        assertList(new Variant[] {variant1, variant2, variant3, variant4}, list);
    }

    public void testBuildLanguagesAndEncodings() {
        Variant variant1 = new Variant(null, Locale.ENGLISH, "UTF-8");
        Variant variant2 = new Variant(null, Locale.ENGLISH, "UTF-16");
        Variant variant3 = new Variant(null, Locale.CHINESE, "UTF-8");
        Variant variant4 = new Variant(null, Locale.CHINESE, "UTF-16");

        VariantListBuilder builder = new VariantListBuilderImpl();
        List<Variant> list = null;

        list = builder.languages(Locale.ENGLISH).encodings("UTF-8").add().build();
        assertList(new Variant[] {variant1}, list);

        list = builder.languages(Locale.ENGLISH).encodings("UTF-16").add().build();
        assertList(new Variant[] {variant2}, list);

        list = builder.languages(Locale.ENGLISH).encodings("UTF-8", "UTF-16").add().build();
        assertList(new Variant[] {variant1, variant2}, list);

        list = builder.languages(Locale.ENGLISH, Locale.CHINESE).encodings("UTF-8").add().build();
        assertList(new Variant[] {variant1, variant3}, list);

        list =
            builder.languages(Locale.ENGLISH, Locale.CHINESE).encodings("UTF-8", "UTF-16").add()
                .build();
        assertList(new Variant[] {variant1, variant2, variant3, variant4}, list);
    }

    public void testBuildMediaTypesAndLanguagesAndEncodings() {
        Variant variant1 =
            new Variant(MediaType.APPLICATION_ATOM_XML_TYPE, Locale.ENGLISH, "UTF-8");
        Variant variant2 =
            new Variant(MediaType.APPLICATION_ATOM_XML_TYPE, Locale.ENGLISH, "UTF-16");
        Variant variant3 =
            new Variant(MediaType.APPLICATION_ATOM_XML_TYPE, Locale.CHINESE, "UTF-8");
        Variant variant4 =
            new Variant(MediaType.APPLICATION_ATOM_XML_TYPE, Locale.CHINESE, "UTF-16");
        Variant variant5 = new Variant(MediaType.APPLICATION_JSON_TYPE, Locale.ENGLISH, "UTF-8");
        Variant variant6 = new Variant(MediaType.APPLICATION_JSON_TYPE, Locale.ENGLISH, "UTF-16");
        Variant variant7 = new Variant(MediaType.APPLICATION_JSON_TYPE, Locale.CHINESE, "UTF-8");
        Variant variant8 = new Variant(MediaType.APPLICATION_JSON_TYPE, Locale.CHINESE, "UTF-16");

        VariantListBuilder builder = new VariantListBuilderImpl();
        List<Variant> list = null;

        list =
            builder.mediaTypes(MediaType.APPLICATION_ATOM_XML_TYPE).languages(Locale.ENGLISH)
                .encodings("UTF-8").add().build();
        assertList(new Variant[] {variant1}, list);

        list =
            builder.mediaTypes(MediaType.APPLICATION_ATOM_XML_TYPE).languages(Locale.ENGLISH)
                .encodings("UTF-16").add().build();
        assertList(new Variant[] {variant2}, list);

        list =
            builder.mediaTypes(MediaType.APPLICATION_ATOM_XML_TYPE).languages(Locale.ENGLISH)
                .encodings("UTF-8", "UTF-16").add().build();
        assertList(new Variant[] {variant1, variant2}, list);

        list =
            builder.mediaTypes(MediaType.APPLICATION_ATOM_XML_TYPE).languages(Locale.ENGLISH,
                                                                              Locale.CHINESE)
                .encodings("UTF-8", "UTF-16").add().build();
        assertList(new Variant[] {variant1, variant2, variant3, variant4}, list);

        list =
            builder
                .mediaTypes(MediaType.APPLICATION_ATOM_XML_TYPE, MediaType.APPLICATION_JSON_TYPE)
                .languages(Locale.ENGLISH, Locale.CHINESE).encodings("UTF-8", "UTF-16").add()
                .build();
        assertList(new Variant[] {variant1, variant2, variant3, variant4, variant5, variant6,
            variant7, variant8}, list);
    }

    public void testBuildAllNull() {
        VariantListBuilder builder = new VariantListBuilderImpl();
        List<Variant> list = builder.build();
        assertEquals(0, list.size());

        try {
            builder.add();
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    private void assertList(Variant[] expected, List<Variant> actual) {
        assertArrayEquals(expected, actual.toArray());
    }
}
