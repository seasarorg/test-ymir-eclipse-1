package org.seasar.ymir.eclipse.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils {
    private StreamUtils() {
    }

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        in = new BufferedInputStream(in);
        out = new BufferedOutputStream(out);
        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static void close(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException ignore) {
            }
        }
    }

    public static void close(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException ignore) {
            }
        }
    }
}
