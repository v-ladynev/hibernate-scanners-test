package com.github.ladynev.scanners.fluent;

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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UrlWrapper && externalForm.equals(((UrlWrapper) obj).externalForm);
    }

    @Override
    public int hashCode() {
        return externalForm.hashCode();
    }

    @Override
    public String toString() {
        return url.toString();
    }

}
