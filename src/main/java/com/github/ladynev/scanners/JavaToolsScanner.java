package com.github.ladynev.scanners;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.hibernate.bytecode.spi.ByteCodeHelper;

import com.github.ladynev.scanners.util.ScannerAdapter;

/**
 *
 * @author V.Ladynev
 */
public class JavaToolsScanner extends ScannerAdapter {

    private ClassLoaderWrapper loader;

    @Override
    public List<Class<?>> scan(String packageToScan) throws Exception {
        loader = new ClassLoaderWrapper(getLoader());

        List<Class<?>> result = new ArrayList<Class<?>>();

        StandardJavaFileManager fileManager = ToolProvider.getSystemJavaCompiler()
                .getStandardFileManager(null, null, null);

        Iterable<JavaFileObject> files = fileManager.list(StandardLocation.CLASS_PATH,
                packageToScan, Collections.singleton(JavaFileObject.Kind.CLASS), true);

        for (JavaFileObject file : files) {
            Class<?> clazz = toClass(file);
            if (isAnnotationPresent(clazz)) {
                result.add(clazz);
            }
        }

        return result;
    }

    private Class<?> toClass(JavaFileObject file) throws IOException, ClassNotFoundException {
        byte[] bytecode = ByteCodeHelper.readByteCode(file.openInputStream());

        Class<?> result = loader.getClass(null, bytecode);

        return loader.getParent().loadClass(result.getName());
    }

    private static class ClassLoaderWrapper extends ClassLoader {

        public ClassLoaderWrapper(ClassLoader parent) {
            super(parent);
        }

        public Class<?> getClass(String name, byte[] bytecode) throws ClassNotFoundException {
            return defineClass(name, bytecode, 0, bytecode.length);
        }

        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve)
                throws ClassNotFoundException {
            return super.loadClass(name, resolve);
        }

    }

}
