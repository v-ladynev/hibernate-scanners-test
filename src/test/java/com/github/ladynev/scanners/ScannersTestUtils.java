package com.github.ladynev.scanners;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import com.github.ladynev.scanners.util.ScannersUtils;
import com.google.common.io.Closer;
import com.google.common.io.Resources;

public final class ScannersTestUtils {

    private ScannersTestUtils() {

    }

    public static void writeJarFile(File jarFile, Class<?>... classes) throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        Closer closer = Closer.create();
        try {
            FileOutputStream fileOut = closer.register(new FileOutputStream(jarFile));
            JarOutputStream jarOut = closer.register(new JarOutputStream(fileOut, manifest));

            for (Class<?> clazz : classes) {
                String classResource = ScannersUtils.classAsResource(clazz.getName());
                jarOut.putNextEntry(new ZipEntry(classResource));
                Resources.copy(ScannersTestUtils.class.getResource(ScannersUtils
                        .resourcePathFromRoot(classResource)), jarOut);
                jarOut.closeEntry();
            }

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

}
