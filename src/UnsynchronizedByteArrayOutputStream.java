package src;

import java.io.ByteArrayOutputStream;

/**
 * Minimal compatibility shim for Apache Commons IO's UnsynchronizedByteArrayOutputStream.
 *
 * Apache POI may reference this class at runtime. If commons-io is not on the classpath,
 * this lightweight wrapper avoids a NoClassDefFoundError by delegating to the JDK's
 * ByteArrayOutputStream.
 */
public class UnsynchronizedByteArrayOutputStream extends ByteArrayOutputStream {

    public UnsynchronizedByteArrayOutputStream() {
        super();
    }

    public UnsynchronizedByteArrayOutputStream(int size) {
        super(size);
    }

    // All typical methods (toByteArray, writeTo, reset, size) are inherited.
}
