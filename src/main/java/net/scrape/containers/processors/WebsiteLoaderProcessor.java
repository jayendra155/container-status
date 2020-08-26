package net.scrape.containers.processors;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import net.scrape.containers.config.WebsiteConfig;

/*
 *
 * @author jayendravikramsingh
 *
 * 25/08/20
 */
@Service
public class WebsiteLoaderProcessor implements Processor {
    
    private static final Logger log = LoggerFactory.getLogger(WebsiteLoaderProcessor.class);
    private HtmlPage page;
    
    @Autowired
    private WebsiteConfig websiteConfig;
    
    @Value("${website.loader.required-headers}")
    private String requiredHeaders;
    
    WebClient client = new WebClient();
    
    @Override
    public void process(Exchange exchange) throws Exception {
        if (Objects.isNull(page)) {
            log.info("Loading fresh page for state data. URL : {}", websiteConfig.getUrl());
            client.getOptions().setCssEnabled(false);
            client.getOptions().setJavaScriptEnabled(false);
            page = client.getPage(websiteConfig.getUrl());
            Set<String> keys = Arrays.stream(requiredHeaders.split(",")).collect(Collectors.toSet());
            keys.forEach(key -> {
                String[] split = key.split("\\.");
                DomElement elementById = page.getElementById(split[split.length-1]);
                log.info("Key : {}, Element : {}", split[split.length-1], elementById.getAttribute("value"));
                websiteConfig.setValueInMap(split[split.length-1], elementById.getAttribute("value"));
            });
        }
    }
}
