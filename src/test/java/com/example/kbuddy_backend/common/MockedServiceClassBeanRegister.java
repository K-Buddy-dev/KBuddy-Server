package com.example.kbuddy_backend.common;

import static org.mockito.Mockito.mock;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

import com.example.kbuddy_backend.livechat.service.CounselorProfileService;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

public class MockedServiceClassBeanRegister implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry registry) throws BeansException {
        for (String className : getServiceLayerObjectClassNames()) {
            try {
                Class<?> cls = Class.forName(className);
                registry.registerBeanDefinition(beanName(cls), beanDefinition(cls));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        BeanDefinitionRegistryPostProcessor.super.postProcessBeanFactory(beanFactory);
    }

    private Set<String> getServiceLayerObjectClassNames() {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(Service.class));
        return provider.findCandidateComponents("com.example.kbuddy_backend")
                .stream()
                /*
                 * CounselorProfileService에는 @PersistenceContext EntityManager가 있습니다.
                 *
                 * @WebMvcTest는 JPA의 EntityManagerFactory를 생성하지 않으므로,
                 * 기존 방식으로 이 Service를 Bean 등록하면 테스트 Context가 실패합니다.
                 *
                 * 프로덕션 Service는 변경하지 않고,
                 * 공통 MVC 테스트의 자동 Service 등록 대상에서만 제외합니다.
                 */
                .filter(beanDefinition ->
                        !CounselorProfileService.class.getName().equals(beanDefinition.getBeanClassName()))
                .map(BeanDefinition::getBeanClassName)
                .collect(Collectors.toSet());
    }

    private String beanName(final Class<?> cls) {
        final String clsName = cls.getSimpleName();
        return clsName.toLowerCase().charAt(0) + clsName.substring(1);
    }

    private <T> AbstractBeanDefinition beanDefinition(final Class<T> cls) {
        return rootBeanDefinition(cls, () -> mock(cls)).getBeanDefinition();
    }
}
