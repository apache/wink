/*
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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.wink.jaxrs.test.context.uriinfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/context/uriinfo/detailed")
public class UriInfoDetailedMethods {

    @GET
    public Response getUriInfo(@Context UriInfo uriInfo, @QueryParam("reqInfo") String requestInfo) {
        if ("getAbsolutePath".equals(requestInfo)) {
            return Response.ok(uriInfo.getAbsolutePath().toString()).build();
        } else if ("getAbsolutePathBuilder".equals(requestInfo)) {
            return Response.ok(uriInfo.getAbsolutePathBuilder().host("abcd")
                .build("unusedTemplateValue1").toString()).build();
        } else if ("getBaseUri".equals(requestInfo)) {
            return Response.ok(uriInfo.getBaseUri().toString()).build();
        } else if ("getBaseUriBuilder".equals(requestInfo)) {
            return Response.ok(uriInfo.getBaseUriBuilder().host("abcd")
                .build("unusedTemplateValue1").toString()).build();
        } else if ("getPath".equals(requestInfo)) {
            return Response.ok(uriInfo.getPath()).build();
        } else if ("getPathDecodedTrue".equals(requestInfo)) {
            return Response.ok(uriInfo.getPath(true)).build();
        } else if ("getPathDecodedFalse".equals(requestInfo)) {
            return Response.ok(uriInfo.getPath(false)).build();
        } else if ("getMatchedResources".equals(requestInfo)) {
            List<Object> matchedResources = uriInfo.getMatchedResources();
            String resourceClassNames = "";
            for (Object o : matchedResources) {
                Class<?> c = (Class<?>)o.getClass();
                resourceClassNames += c.getName() + ":";
            }
            return Response.ok(resourceClassNames).build();
            /*
             * check if this should be the actual instances or just the classes
             */
        } else if ("getMatchedURIs".equals(requestInfo)) {
            List<String> matchedURIs = uriInfo.getMatchedURIs();
            String retStr = "";
            for (String s : matchedURIs) {
                retStr += s + ":";
            }
            return Response.ok(retStr).build();
        } else if ("getMatchedURIsDecodedTrue".equals(requestInfo)) {
            List<String> matchedURIs = uriInfo.getMatchedURIs(true);
            String retStr = "";
            for (String s : matchedURIs) {
                retStr += s + ":";
            }
            return Response.ok(retStr).build();
        } else if ("getMatchedURIsDecodedFalse".equals(requestInfo)) {
            List<String> matchedURIs = uriInfo.getMatchedURIs(false);
            String retStr = "";
            for (String s : matchedURIs) {
                retStr += s + ":";
            }
            return Response.ok(retStr).build();
        } else if ("getPathParameters".equals(requestInfo)) {
            MultivaluedMap<String, String> params = uriInfo.getPathParameters();
            List<String> keys = new ArrayList<String>(params.keySet());
            Collections.sort(keys);
            /*
             * may want to test for {test}/{test}
             */
            String retStr = "";
            for (String k : keys) {
                retStr += k + "=";

                List<String> values = params.get(k);
                for (String v : values) {
                    retStr += v + ":";
                }
            }
            return Response.ok(retStr).build();
        } else if ("getPathParametersDecodedTrue".equals(requestInfo)) {
            MultivaluedMap<String, String> params = uriInfo.getPathParameters(true);
            List<String> keys = new ArrayList<String>(params.keySet());
            Collections.sort(keys);
            /*
             * may want to test for {test}/{test}
             */
            String retStr = "";
            for (String k : keys) {
                retStr += k + "=";

                List<String> values = params.get(k);
                for (String v : values) {
                    retStr += v + ":";
                }
            }
            return Response.ok(retStr).build();
        } else if ("getPathParametersDecodedFalse".equals(requestInfo)) {
            MultivaluedMap<String, String> params = uriInfo.getPathParameters(false);
            List<String> keys = new ArrayList<String>(params.keySet());
            Collections.sort(keys);
            /*
             * may want to test for {test}/{test}
             */
            String retStr = "";
            for (String k : keys) {
                retStr += k + "=";

                List<String> values = params.get(k);
                for (String v : values) {
                    retStr += v + ":";
                }
            }
            return Response.ok(retStr).build();
        } else if ("getPathSegments".equals(requestInfo)) {
            List<PathSegment> params = uriInfo.getPathSegments();
            String retStr = "";
            for (PathSegment p : params) {
                retStr += p.getPath() + "#";
                MultivaluedMap<String, String> matrixParams = p.getMatrixParameters();

                List<String> keys = new ArrayList<String>(matrixParams.keySet());
                Collections.sort(keys);
                for (String k : keys) {
                    retStr += k + "=";
                    List<String> values = matrixParams.get(k);
                    for (String v : values) {
                        retStr += v + ":";
                    }
                }
                retStr += ":";
            }
            return Response.ok(retStr).build();
        } else if ("getPathSegmentsDecodedFalse".equals(requestInfo)) {
            List<PathSegment> params = uriInfo.getPathSegments(false);
            String retStr = "";
            for (PathSegment p : params) {
                retStr += p.getPath() + "#";
                MultivaluedMap<String, String> matrixParams = p.getMatrixParameters();

                List<String> keys = new ArrayList<String>(matrixParams.keySet());
                Collections.sort(keys);
                for (String k : keys) {
                    retStr += k + "=";
                    List<String> values = matrixParams.get(k);
                    for (String v : values) {
                        retStr += v + ":";
                    }
                }
                retStr += ":";
            }
            return Response.ok(retStr).build();
        } else if ("getQueryParameters".equals(requestInfo)) {
            MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
            String retStr = "";
            List<String> keys = new ArrayList<String>(params.keySet());
            Collections.sort(keys);
            for (String k : keys) {
                retStr += k + "=";

                List<String> values = params.get(k);
                for (String v : values) {
                    retStr += v + ":";
                }
            }
            return Response.ok(retStr).build();
        } else if ("getRequestUri".equals(requestInfo)) {
            return Response.ok(uriInfo.getRequestUri().toString()).build();
        } else if ("getRequestUriBuilder".equals(requestInfo)) {
            return Response.ok(uriInfo.getRequestUriBuilder().host("abcd")
                .build("unusedTemplateValue1").toString()).build();
        }

        return Response.serverError().build();
    }

    @Path("decoded/{path}")
    @GET
    @Encoded
    public Response getUriInfoPathDecoded(@Context UriInfo uriInfo,
                                          @QueryParam("decoded") boolean decoded,
                                          @PathParam("path") String path) {
        return Response.ok(uriInfo.getPath(decoded)).build();
    }

    @Path("pathparamsone{p1:.*}")
    @GET
    @Encoded
    public Response getUriInfoPathParametersOne(@Context UriInfo uriInfo,
                                                @QueryParam("decoded") Boolean decoded,
                                                @PathParam("p1") String path) {
        MultivaluedMap<String, String> params = null;
        if (decoded == null) {
            params = uriInfo.getPathParameters();
        } else if (decoded == true) {
            params = uriInfo.getPathParameters(true);
        } else if (decoded == false) {
            params = uriInfo.getPathParameters(false);
        }
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        /*
         * may want to test for {test}/{test}
         */
        String retStr = "";
        for (String k : keys) {
            retStr += k + "=";

            List<String> values = params.get(k);
            for (String v : values) {
                retStr += v + ":";
            }
        }
        return Response.ok(retStr).build();
    }

    @Path("pathparamsmany/{p1}/{p2}{p3:.*}")
    @GET
    @Encoded
    public Response getUriInfoPathParametersMany(@Context UriInfo uriInfo,
                                                 @QueryParam("decoded") Boolean decoded,
                                                 @PathParam("p1") String p1,
                                                 @PathParam("p2") String p2,
                                                 @PathParam("p3") String p3) {
        MultivaluedMap<String, String> params = null;
        if (decoded == null) {
            params = uriInfo.getPathParameters();
        } else if (decoded == true) {
            params = uriInfo.getPathParameters(true);
        } else if (decoded == false) {
            params = uriInfo.getPathParameters(false);
        }
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        /*
         * may want to test for {test}/{test}
         */
        String retStr = "";
        for (String k : keys) {
            retStr += k + "=";

            List<String> values = params.get(k);
            for (String v : values) {
                retStr += v + ":";
            }
        }
        return Response.ok(retStr).build();
    }

    @Path("matchedurisdecoded/{path}")
    @GET
    @Encoded
    public Response getMatchedUrisDecoded(@Context UriInfo uriInfo,
                                          @QueryParam("decoded") Boolean decoded,
                                          @PathParam("path") String path) {
        return Response.ok(uriInfo.getPath(decoded)).build();
    }

    @Path("absolutepathbuilder")
    @GET
    public Response getAbsoluteUriBuilder(@Context UriInfo uriInfo,
                                          @QueryParam("reqInfo") String requestInfo) {
        if ("getAbsolutePath".equals(requestInfo)) {
            return Response.ok(uriInfo.getAbsolutePath().toString()).build();
        }
        return Response.serverError().build();
    }

    @Path("matchedresources")
    public MatchedResourcesSubResource getMatchedResourcesSubresource(@Context UriInfo uriInfo) {
        List<Object> matchedResources = uriInfo.getMatchedResources();
        String resourceClassNames = "";
        for (Object o : matchedResources) {
            Class<?> c = (Class<?>)o.getClass();
            resourceClassNames += c.getName() + ":";
        }
        return new MatchedResourcesSubResource(resourceClassNames, uriInfo);
    }

    @Path("matcheduris")
    public MatchedURIsSubResource getMatchedURIsSubresource(@Context UriInfo uriInfo) {
        List<String> matchedURIs = uriInfo.getMatchedURIs();
        String retStr = "";
        for (String s : matchedURIs) {
            retStr += s + ":";
        }
        return new MatchedURIsSubResource(retStr, uriInfo);
    }

    @Path("queryparams")
    @GET
    @Encoded
    public Response getUriInfoQueryParameters(@Context UriInfo uriInfo,
                                              @QueryParam("decoded") Boolean decoded) {
        MultivaluedMap<String, String> queryParams = null;
        if (decoded == null) {
            queryParams = uriInfo.getQueryParameters();
        } else if (decoded == true) {
            queryParams = uriInfo.getQueryParameters(true);
        } else if (decoded == false) {
            queryParams = uriInfo.getQueryParameters(false);
        }
        String retStr = "";
        List<String> keys = new ArrayList<String>(queryParams.keySet());
        Collections.sort(keys);
        for (String k : keys) {
            retStr += k + "=";

            List<String> values = queryParams.get(k);
            for (String v : values) {
                retStr += v + ":";
            }
        }
        return Response.ok(retStr).build();
    }
}
