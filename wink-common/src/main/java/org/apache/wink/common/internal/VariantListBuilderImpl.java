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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Variant.VariantListBuilder;

public class VariantListBuilderImpl extends VariantListBuilder {

    private List<MediaType> mediaTypes;
    private List<Locale>    languages;
    private List<String>    encodings;
    private List<Variant>   variants;

    public VariantListBuilderImpl() {
        variants = null;
        mediaTypes = new ArrayList<MediaType>();
        languages = new ArrayList<Locale>();
        encodings = new ArrayList<String>();
    }

    private void reset() {
        encodings.clear();
        languages.clear();
        mediaTypes.clear();
    }

    @Override
    public VariantListBuilder add() {
        if (variants == null) {
            variants = new ArrayList<Variant>();
        }

        verifyNonEmpty();

        for (MediaType mediaType : mediaTypes) {
            for (Locale language : languages) {
                for (String encoding : encodings) {
                    variants.add(new Variant(mediaType, language, encoding));
                }
            }
        }
        reset();
        return this;
    }

    private void verifyNonEmpty() {
        if (mediaTypes.size() == 0) {
            mediaTypes.add(null);
        }
        if (languages.size() == 0) {
            languages.add(null);
        }
        if (encodings.size() == 0) {
            encodings.add(null);
        }
    }

    @Override
    public List<Variant> build() {
        if (variants == null) {
            variants = new ArrayList<Variant>();
        }
        List<Variant> list = variants;
        variants = null;
        return list;
    }

    @Override
    public VariantListBuilder encodings(String... encodings) {
        this.encodings.clear();
        for (String encoding : encodings) {
            this.encodings.add(encoding);
        }
        return this;
    }

    @Override
    public VariantListBuilder languages(Locale... languages) {
        this.languages.clear();
        for (Locale language : languages) {
            this.languages.add(language);
        }
        return this;
    }

    @Override
    public VariantListBuilder mediaTypes(MediaType... mediaTypes) {
        this.mediaTypes.clear();
        for (MediaType mediaType : mediaTypes) {
            this.mediaTypes.add(mediaType);
        }
        return this;
    }

}
