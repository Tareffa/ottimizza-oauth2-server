package br.com.ottimizza.application;

// import org.apache.coyote.http2.Http2Protocol;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.thymeleaf.extras.springsecurity4.dialect.SpringSecurityDialect;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class SpringbootOauth2ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootOauth2ServerApplication.class, args);
	}

	@Bean
	public SpringSecurityDialect securityDialect() {
		return new SpringSecurityDialect();
	}
	
	// @Bean
	// public CookieSerializer cookieSerializer() {
	// DefaultCookieSerializer serializer = new DefaultCookieSerializer();
	// serializer.setCookieName("JSESSIONID");
	// serializer.setCookiePath("/");
	// serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
	// return serializer;
	// }

	// @Bean
	// public ConfigurableServletWebServerFactory tomcatCustomizer() {
	// 	TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
	// 	factory.addConnectorCustomizers(connector -> connector.addUpgradeProtocol(new Http2Protocol()));
	// 	return factory;
	// }

}
