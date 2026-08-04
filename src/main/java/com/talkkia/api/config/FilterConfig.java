package com.talkkia.api.config;

import com.talkkia.api.handler.TalkkiaLoginFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @Author: Ethan
 * @Date: 2024/9/17
 * @Description: register filter
 */
@Configuration
public class FilterConfig {

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Bean
    public FilterRegistrationBean<TalkkiaLoginFilter> customFilterRegistrationBean() {
        FilterRegistrationBean<TalkkiaLoginFilter> registrationBean = new FilterRegistrationBean<>();
        // Set the filter instance
        registrationBean.setFilter(new TalkkiaLoginFilter(jwtTokenUtil));
        // Define URL patterns to which the filter should be applied
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
