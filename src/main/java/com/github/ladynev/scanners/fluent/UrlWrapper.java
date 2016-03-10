package com.github.ladynev.scanners.fluent;

import java.io.File;
import java.net.URL;

/**
 * This class wraps URL and ClassLoader for using it with collections. The same URLs with distinct
 * class loaders are considered as equal. It doesn't use equals and hashcode of URL.
 *
 * @author V.Ladynev
 */
public class UrlWrapper {

    private final URL url;

    private final String externalForm;

    private final ClassLoader loader;

    public UrlWrapper(URL url, ClassLoader loader) {
        this.url = url;
        this.loader = loader;
        externalForm = url.toExternalForm();
    }

    public URL getUrl() {
        return url;
    }

    public boolean isFile() {
        return ClassUtils.isFile(url);
    }

    public File getFile() {
        return new File(url.getFile());
    }

    public ClassLoader getLoader() {
        return loader;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof UrlWrapper ? externalForm.equals(((UrlWrapper) obj).externalForm)
                : false;
    }

    @Override
    public int hashCode() {
        return externalForm.hashCode();
    }

    @Override
    public String toString() {
        return url.toString();
    }

    public String getExternalForm() {
        return externalForm;
    }

}
