package com.github.ladynev.scanners;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.github.ladynev.scanners.util.ClassUtils;

/**
 *
 * @author V.Ladynev
 */
public class SpringLibrary implements IScanner {

    @Override
    public List<Class<?>> scan(String packageToScan) throws Exception {
        List<Class<?>> result = new ArrayList<Class<?>>();

        final boolean useDefaultFilters = false;
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
                useDefaultFilters);
        provider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

        Set<BeanDefinition> classes = provider.findCandidateComponents(packageToScan);
        for (BeanDefinition bean : classes) {
            result.add(ClassUtils.toClass(bean.getBeanClassName()));
        }

        return result;
    }

}
