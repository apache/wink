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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

import org.apache.wink.common.internal.uri.UriEncoder;
import org.apache.wink.common.internal.utils.StringUtils;

public class PathSegmentImpl implements PathSegment, Cloneable, Comparable<PathSegment> {

    private static final String            MATRIX_DELIMITER = ";"; //$NON-NLS-1$
    private String                         path;
    private MultivaluedMap<String, String> matrixParams;

    private PathSegmentImpl() {
        this.path = null;
        this.matrixParams = null;
    }

    public PathSegmentImpl(String path) {
        if (path == null) {
            throw new NullPointerException("path"); //$NON-NLS-1$
        }
        constructParts(path);
    }

    public PathSegmentImpl(String path, String matrix) {
        if (path == null) {
            throw new NullPointerException("path"); //$NON-NLS-1$
        }
        this.path = path;
        constructMatrixParams(extractMatrixParams(matrix));
    }

    public PathSegmentImpl(String path, MultivaluedMap<String, String> matrixParams) {
        if (path == null) {
            throw new NullPointerException("path"); //$NON-NLS-1$
        }
        this.path = path;
        this.matrixParams = matrixParams;
    }

    public MultivaluedMap<String, String> getMatrixParameters() {
        if (matrixParams == null) {
            matrixParams = new MultivaluedMapImpl<String, String>();
        }
        return matrixParams;
    }

    public void clearMatrixParameter(String name) {
        MultivaluedMap<String, String> matrixParameters = getMatrixParameters();
        matrixParameters.remove(name);
    }

    public void clearAllMatrixParameters() {
        MultivaluedMap<String, String> matrixParameters = getMatrixParameters();
        matrixParameters.clear();
    }

    public void setMatrixParameters(String matrix) {
        constructMatrixParams(extractMatrixParams(matrix));
    }

    public String getPath() {
        return path;
    }

    private void constructParts(String path) {
        String[] parts = extractMatrixParams(path);
        this.path = parts[0];
        constructMatrixParams(parts, 1);
    }

    private String[] extractMatrixParams(String path) {
        return StringUtils.fastSplitTemplate(path, MATRIX_DELIMITER);
    }

    private void constructMatrixParams(String[] matrixParamsArray) {
        constructMatrixParams(matrixParamsArray, 0);
    }

    private void constructMatrixParams(String[] matrixParamsArray, int offset) {
        getMatrixParameters().clear();
        for (int i = offset; i < matrixParamsArray.length; ++i) {
            String matrixParam = matrixParamsArray[i];
            int index = matrixParam.indexOf('=');
            if (index == -1) {
                // the matrix param is actually being removed
                // (see http://www.w3.org/DesignIssues/MatrixURIs.html)
                // so do not add this to the map
                continue;
            }
            String name = matrixParam.substring(0, index);
            String value = matrixParam.substring(index + 1);
            matrixParams.add(name, value);
        }
    }

    @Override
    public PathSegmentImpl clone() {
        try {
            PathSegmentImpl ps = (PathSegmentImpl)super.clone();
            if (matrixParams != null) {
                ps.matrixParams = ((MultivaluedMapImpl<String, String>)matrixParams).clone();
            }
            return ps;
        } catch (CloneNotSupportedException e) {
            // shouldn't happen
            throw new WebApplicationException(e);
        }
    }

    public static PathSegmentImpl decode(PathSegment segment) {
        PathSegmentImpl clone = new PathSegmentImpl();
        clone.path = UriEncoder.decodeString(segment.getPath());
        clone.matrixParams = UriEncoder.decodeMultivaluedMap(segment.getMatrixParameters(), true);
        return clone;
    }

    @Override
    public String toString() {
        MultivaluedMap<String, String> matrixParameters = getMatrixParameters();
        String parameters = MultivaluedMapImpl.toString(matrixParameters, ";"); //$NON-NLS-1$
        String delim = (matrixParameters.isEmpty() ? "" : ";"); //$NON-NLS-1$ //$NON-NLS-2$
        String result = getPath() + delim + parameters;
        return result;
    }

    public static String toString(List<PathSegment> segments) {
        StringBuilder builder = new StringBuilder();
        String delim = ""; //$NON-NLS-1$
        for (PathSegment segment : segments) {
            builder.append(delim);
            builder.append(segment.toString());
            delim = "/"; //$NON-NLS-1$
        }
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((matrixParams == null) ? 0 : matrixParams.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PathSegmentImpl other = (PathSegmentImpl)obj;
        if (matrixParams == null) {
            if (other.matrixParams != null)
                return false;
        } else if (!matrixParams.equals(other.matrixParams))
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        return true;
    }

    public int compareTo(PathSegment o) {
        return path.compareTo(o.getPath());
    }

}
