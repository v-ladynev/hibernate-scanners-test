package com.github.ladynev.scanners.fluent;

import java.io.File;
import java.net.URL;

/**
 * This class wraps URL for using it with collections. It doesn't use equals and hashcode of URL.
 *
 * @author V.Ladynev
 */
public class UrlWrapper {

    private final URL url;

    private final String externalForm;

    public UrlWrapper(URL url) {
        this.url = url;
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
