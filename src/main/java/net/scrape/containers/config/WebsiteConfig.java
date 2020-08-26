package net.scrape.containers.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.PostConstruct;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.engine.DefaultProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/*
 *
 * @author jayendravikramsingh
 *
 * 22/08/20
 */
@Configuration
@ConfigurationProperties(prefix = "website")
public class WebsiteConfig {
    
    @Autowired
    private CamelContext camelContext;
    
    private Map<String, ProducerTemplate> routeTemplates;
    
    private static final Logger log = LoggerFactory.getLogger(WebsiteConfig.class);
    
    private String url;
    
    private Map<String, String> formData;
    
    private Map<String, List<String>> result;
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public Map<String, String> getFormData() {
        return Collections.unmodifiableMap(formData);
    }
    
    public void setFormData(Map<String, String> formData) {
        this.formData = formData;
    }
    
    public Map<String, List<String>> getResult() {
        return Collections.unmodifiableMap(result);
    }
    
    public void setResult(Map<String, List<String>> result) {
        this.result = result;
    }
    
    public String setValueInMap(String key, String value) {
        log.info("Setting key : {}, value: {}", key, value);
        if (Objects.isNull(formData)) {
            this.formData = new HashMap<>();
        }
        return formData.put(key, value);
    }
    
    @PostConstruct
    public void init() {
        log.info("Result map : {}", result.toString());
        log.info(Optional.ofNullable(formData).flatMap(map -> Optional.of(map.toString()))
                         .orElse("Input form data is empty"));
        routeTemplates = new HashMap<>();
        result.keySet().stream().forEach(routeSuffix -> {
            DefaultProducerTemplate template = DefaultProducerTemplate
                    .newInstance(camelContext, "direct:" + routeSuffix);
            template.start();
            routeTemplates.put(routeSuffix, template);
        });
        
    }
    
    public void triggerRoute(Map<String, String> data, String route) {
        ProducerTemplate producer = routeTemplates
                .getOrDefault(route, DefaultProducerTemplate.newInstance(camelContext, "mock:default"));
        producer.sendBody(data);
    }
}
