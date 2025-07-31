package io.github.hejun.neutron.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * MyBatis-Plus 配置
 *
 * @author HeJun
 */
@Configuration(proxyBeanMethods = false)
public class MyBatisPlusConfig {

	@Bean
	public PaginationInnerInterceptor paginationInterceptor() {
		return new PaginationInnerInterceptor();
	}

	@Bean
	public MybatisPlusInterceptor mybatisPlusInterceptor(List<InnerInterceptor> interceptors) {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
		if (interceptors != null) {
			interceptors.forEach(interceptor::addInnerInterceptor);
		}
		return interceptor;
	}

}
