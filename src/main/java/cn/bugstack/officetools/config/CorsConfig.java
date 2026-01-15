package cn.bugstack.officetools.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * CORS 跨域配置
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String[] allowedMethods;

    @Value("${app.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    @Value("${app.cors.allow-all-origins:false}")
    private boolean allowAllOrigins;

    @Value("${app.cors.allowed-origins:}")
    private String[] allowedOrigins;

    /**
     * CORS 配置 - 通过配置文件控制
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        if (allowAllOrigins) {
            // 允许所有域名（开发环境）
            config.addAllowedOriginPattern("*");
            config.setAllowCredentials(false);  // 允许所有源时必须设为 false
        } else {
            // 只允许配置的域名（生产环境）
            if (allowedOrigins == null || allowedOrigins.length == 0) {
                throw new IllegalStateException("CORS 配置错误：allowAllOrigins=false 时必须配置 allowedOrigins");
            }
            config.setAllowedOrigins(Arrays.asList(allowedOrigins));
            config.setAllowCredentials(allowCredentials);
        }

        config.setAllowedMethods(Arrays.asList(allowedMethods));
        config.addAllowedHeader(allowedHeaders);
        config.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
