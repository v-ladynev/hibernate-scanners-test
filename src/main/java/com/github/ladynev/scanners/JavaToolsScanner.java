package com.github.ladynev.scanners;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.hibernate.bytecode.spi.ByteCodeHelper;

/**
 *
 * @author V.Ladynev
 */
public class JavaToolsScanner implements IScanner {

    private final ClassLoader loader;

    public JavaToolsScanner() {
        loader = Thread.currentThread().getContextClassLoader();
    }

    @Override
    public List<Class<?>> scan(String packageToScan, List<Class<?>> result) throws IOException {

        StandardJavaFileManager fileManager = ToolProvider.getSystemJavaCompiler()
                .getStandardFileManager(null, null, null);

        Iterable<JavaFileObject> files = fileManager.list(StandardLocation.CLASS_PATH,
                packageToScan, Collections.singleton(JavaFileObject.Kind.CLASS), true);

        for (JavaFileObject file : files) {

            Class<?> clazz = null;
            try {
                clazz = toClass(null, file.openInputStream());
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }

            if (clazz.isAnnotationPresent(Entity.class)) {
                result.add(clazz);
            }

        }

        return result;
    }

    private Class<?> toClass(String name, InputStream stream) throws IOException,
    ClassNotFoundException {
        final byte[] bytecode = ByteCodeHelper.readByteCode(stream);

        Class<?> result = new ClassLoader(loader) {

            public Class<?> getClass(String name) throws ClassNotFoundException {
                return defineClass(name, bytecode, 0, bytecode.length);
            }
        }.getClass(name);

        return loader.loadClass(result.getName());
    }

}
