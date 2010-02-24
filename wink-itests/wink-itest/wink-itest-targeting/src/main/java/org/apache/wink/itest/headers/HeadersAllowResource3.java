package org.apache.wink.itest.headers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("headersallow3")
public class HeadersAllowResource3 {

    public static class SubLocatorResource {
        @GET
        public String getMore() {
            return "foo";
        }
    }

    @Path("/sublocator")
    public SubLocatorResource getMore() {
        return new SubLocatorResource();
    }
}
