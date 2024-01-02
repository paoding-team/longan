package dev.paoding.longan.channel.http;

import io.netty.util.AsciiString;

public class Http {

    public final static class Method {
        public final static String GET = "GET";
        public final static String POST = "POST";
        public static final String HEAD = "HEAD";
    }

    public final static class ContentType {
        public final static AsciiString APPLICATION_JAVASCRIPT = AsciiString.cached("application/javascript");
        public final static AsciiString IMAGE_JPEG = AsciiString.cached("image/jpeg");
        public final static AsciiString IMAGE_GIF = AsciiString.cached("image/gif");
        public final static AsciiString IMAGE_WEBP = AsciiString.cached("image/webp");
        public final static AsciiString IMAGE_PNG = AsciiString.cached("image/png");

    }

    public final static class Header {
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String CONTENT_LANGUAGE = "Content-Language";
    }
}
