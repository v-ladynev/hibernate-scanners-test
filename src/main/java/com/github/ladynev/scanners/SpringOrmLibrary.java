package com.github.ladynev.scanners;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import com.github.ladynev.scanners.util.ScannerAdapter;

/**
 *
 * @author V.Ladynev
 */
public class SpringOrmLibrary extends ScannerAdapter {

    private static final String RESOURCE_PATTERN = "/**/*.class";

    @Override
    public List<Class<?>> scan(String packageToScan) throws Exception {
        final List<Class<?>> result = new ArrayList<Class<?>>();

        AnnotationTypeFilter filter = new AnnotationTypeFilter(getAnnotation(), false);

        PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver(
                getLoader());

        String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                + ClassUtils.convertClassNameToResourcePath(packageToScan) + RESOURCE_PATTERN;

        Resource[] resources = resourceLoader.getResources(pattern);
        MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourceLoader);
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                MetadataReader reader = readerFactory.getMetadataReader(resource);
                String className = reader.getClassMetadata().getClassName();
                if (filter.match(reader, readerFactory)) {
                    result.add(ClassUtils.resolveClassName(className, getLoader()));
                }
            }
        }

        return result;
    }

}
