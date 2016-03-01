package com.github.ladynev.scanners;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.github.ladynev.scanners.util.ScannersUtils;
import com.github.ladynev.scanners.util.ScannerAdapter;

/**
 *
 * @author V.Ladynev
 */
public class SpringLibrary extends ScannerAdapter {

    @Override
    public List<Class<?>> scan(String packageToScan) throws Exception {
        List<Class<?>> result = new ArrayList<Class<?>>();

        final boolean useDefaultFilters = false;
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
                useDefaultFilters);
        provider.addIncludeFilter(new AnnotationTypeFilter(getAnnotation()));
        provider.setResourceLoader(new DefaultResourceLoader(getLoader()));

        Set<BeanDefinition> classes = provider.findCandidateComponents(packageToScan);
        for (BeanDefinition bean : classes) {
            result.add(ScannersUtils.toClass(bean.getBeanClassName(), getLoader()));
        }

        return result;
    }

}
