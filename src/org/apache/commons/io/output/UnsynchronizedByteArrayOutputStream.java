package src.org.apache.commons.io.output;

import java.io.ByteArrayOutputStream;

/**
 * Minimal compatibility shim for Apache Commons IO's UnsynchronizedByteArrayOutputStream.
 *
 * POI may reference this class at runtime if commons-io is not present in the classpath.
 * To avoid forcing an external dependency in this project, provide a thin wrapper that
 * extends ByteArrayOutputStream and exposes the commonly used constructors/methods.
 */
public class UnsynchronizedByteArrayOutputStream extends ByteArrayOutputStream {

    public UnsynchronizedByteArrayOutputStream() {
        super();
    }

    public UnsynchronizedByteArrayOutputStream(int size) {
        super(size);
    }

    // All useful methods (toByteArray, writeTo, reset, size) are inherited from ByteArrayOutputStream.
}
