package com.github.ladynev.scanners.fluent;

import java.io.DataInput;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;

import com.impetus.annovention.resource.ResourceIterator;

/**
 * Based on <a href="https://github.com/rmuller/infomas-asl/tree/master/annotation-detector">
 * annotation-detector</a> author <a href="mailto:rmuller@xiam.nl">Ronald K. Muller</a>
 *
 * @author V.Ladynev
 */
public class ClassParser {

    // Constant Pool type tags
    private static final int CP_UTF8 = 1;

    private static final int CP_INTEGER = 3;

    private static final int CP_FLOAT = 4;

    private static final int CP_LONG = 5;

    private static final int CP_DOUBLE = 6;

    private static final int CP_CLASS = 7;

    private static final int CP_STRING = 8;

    private static final int CP_REF_FIELD = 9;

    private static final int CP_REF_METHOD = 10;

    private static final int CP_REF_INTERFACE = 11;

    private static final int CP_NAME_AND_TYPE = 12;

    private static final int CP_METHOD_HANDLE = 15;

    private static final int CP_METHOD_TYPE = 16;

    private static final int CP_INVOKE_DYNAMIC = 18;

    // AnnotationElementValue

    private static final int BYTE = 'B';

    private static final int CHAR = 'C';

    private static final int DOUBLE = 'D';

    private static final int FLOAT = 'F';

    private static final int INT = 'I';

    private static final int LONG = 'J';

    private static final int SHORT = 'S';

    private static final int BOOLEAN = 'Z';

    // used for AnnotationElement only

    private static final int STRING = 's';

    private static final int ENUM = 'e';

    private static final int CLASS = 'c';

    private static final int ANNOTATION = '@';

    private static final int ARRAY = '[';

    // The buffer is reused during the life cycle of this AnnotationDetector instance
    private final ClassFileBuffer cpBuffer = new ClassFileBuffer();

    // Reusing the constantPool is not needed for better performance
    private Object[] constantPool;

    private final String annotationDescriptor;

    public ClassParser(Class<? extends Annotation> annotation) {
        annotationDescriptor = ClassUtils.toDescriptor(annotation);
    }

    private void detect(final ResourceIterator iterator) throws IOException {
        InputStream stream;
        while ((stream = iterator.next()) != null) {
            try {
                cpBuffer.readFrom(stream);
                if (hasCafebabe(cpBuffer)) {
                    detect(cpBuffer);
                } // else ignore
            } catch (Throwable t) {
                // catch all errors
                if (!(stream instanceof FileInputStream)) {
                    // in case of an error we close the ZIP File here
                    stream.close();
                }
            } finally {
                // closing InputStream from ZIP Entry is handled by ZipFileIterator
                if (stream instanceof FileInputStream) {
                    stream.close();
                }
            }
        }
    }

    private boolean hasCafebabe(final ClassFileBuffer buffer) throws IOException {
        return buffer.size() > 4 && buffer.readInt() == 0xCAFEBABE;
    }

    /**
     * Inspect the given (Java) class file in streaming mode.
     */
    private void detect(final DataInput di) throws IOException {
        readVersion(di);
        readConstantPoolEntries(di);
        readAccessFlags(di);
        readThisClass(di);
        readSuperClass(di);
        readInterfaces(di);
        readFields(di);
        readMethods(di);
        readAttributes(di, 'T');
    }

    private void readVersion(final DataInput di) throws IOException {
        // sequence: minor version, major version (argument_index is 1-based)
        di.skipBytes(4);
    }

    private void readConstantPoolEntries(final DataInput di) throws IOException {
        final int count = di.readUnsignedShort();
        constantPool = new Object[count];
        for (int i = 1; i < count; ++i) {
            if (readConstantPoolEntry(di, i)) {
                // double slot
                ++i;
            }
        }
    }

    /**
     * Return {@code true} if a double slot is read (in case of Double or Long constant).
     */
    private boolean readConstantPoolEntry(final DataInput di, final int index) throws IOException {
        final int tag = di.readUnsignedByte();
        switch (tag) {
        case CP_METHOD_TYPE:
            di.skipBytes(2); // readUnsignedShort()
            return false;
        case CP_METHOD_HANDLE:
            di.skipBytes(3);
            return false;
        case CP_INTEGER:
        case CP_FLOAT:
        case CP_REF_FIELD:
        case CP_REF_METHOD:
        case CP_REF_INTERFACE:
        case CP_NAME_AND_TYPE:
        case CP_INVOKE_DYNAMIC:
            di.skipBytes(4); // readInt() / readFloat() / readUnsignedShort() * 2
            return false;
        case CP_LONG:
        case CP_DOUBLE:
            di.skipBytes(8); // readLong() / readDouble()
            return true;
        case CP_UTF8:
            constantPool[index] = di.readUTF();
            return false;
        case CP_CLASS:
        case CP_STRING:
            // reference to CP_UTF8 entry. The referenced index can have a higher number!
            constantPool[index] = di.readUnsignedShort();
            return false;
        default:
            throw new ClassFormatError("Unkown tag value for constant pool entry: " + tag);
        }
    }

    private void readAccessFlags(final DataInput di) throws IOException {
        di.skipBytes(2); // u2
    }

    private void readThisClass(final DataInput di) throws IOException {
        typeName = resolveUtf8(di);
    }

    private void readSuperClass(final DataInput di) throws IOException {
        di.skipBytes(2); // u2
    }

    private void readInterfaces(final DataInput di) throws IOException {
        final int count = di.readUnsignedShort();
        di.skipBytes(count * 2); // count * u2
    }

    private void readFields(final DataInput di) throws IOException {
        final int count = di.readUnsignedShort();

        for (int i = 0; i < count; ++i) {
            readAccessFlags(di);
            memberName = resolveUtf8(di);
            di.skipBytes(2); // unsigned short
            readAttributes(di, 'F');
        }
    }

    private void readMethods(final DataInput di) throws IOException {
        final int count = di.readUnsignedShort();

        for (int i = 0; i < count; ++i) {
            readAccessFlags(di);
            memberName = resolveUtf8(di);
            di.skipBytes(2); // unsigned short
            readAttributes(di, 'M');
        }
    }

    private void readAttributes(final DataInput di, final char reporterType) throws IOException {
        final int count = di.readUnsignedShort();

        for (int i = 0; i < count; ++i) {
            final String name = resolveUtf8(di);
            // in bytes, use this to skip the attribute info block
            final int length = di.readInt();
            if ("RuntimeVisibleAnnotations".equals(name)
                    || "RuntimeInvisibleAnnotations".equals(name)) {
                checkTypeAnnotations(di, reporterType);
            } else {
                di.skipBytes(length);
            }
        }
    }

    private boolean checkTypeAnnotations(final DataInput di, final char reporterType)
            throws IOException {
        // the number of Runtime(In)VisibleAnnotations
        final int count = di.readUnsignedShort();

        for (int i = 0; i < count; ++i) {
            String descriptor = readAnnotation(di);
            if (StringUtils.equal(descriptor, annotationDescriptor)) {
                return true;
            }
        }

        return false;
    }

    private String readAnnotation(final DataInput di) throws IOException {
        final String rawTypeName = resolveUtf8(di);
        // num_element_value_pairs
        final int count = di.readUnsignedShort();

        for (int i = 0; i < count; ++i) {
            di.skipBytes(2);
            readAnnotationElementValue(di);
        }
        return rawTypeName;
    }

    private void readAnnotationElementValue(final DataInput di) throws IOException {
        final int tag = di.readUnsignedByte();

        switch (tag) {
        case BYTE:
        case CHAR:
        case DOUBLE:
        case FLOAT:
        case INT:
        case LONG:
        case SHORT:
        case BOOLEAN:
        case STRING:
            di.skipBytes(2);
            break;
        case ENUM:
            di.skipBytes(4); // 2 * u2
            break;
        case CLASS:
            di.skipBytes(2);
            break;
        case ANNOTATION:
            readAnnotation(di);
            break;
        case ARRAY:
            final int count = di.readUnsignedShort();
            for (int i = 0; i < count; ++i) {
                readAnnotationElementValue(di);
            }
            break;
        default:
            throw new ClassFormatError("Not a valid annotation element type tag: 0x"
                    + Integer.toHexString(tag));
        }
    }

    /**
     * Look up the String value, identified by the u2 index value from constant pool (direct or
     * indirect).
     */
    private String resolveUtf8(final DataInput di) throws IOException {
        int index = di.readUnsignedShort();
        Object value = constantPool[index];
        return (String) (value instanceof Integer ? constantPool[(Integer) value] : value);
    }

}
