package net.scrape.containers.processors;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.engine.DefaultProducerTemplate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import net.scrape.containers.config.WebsiteConfig;
import reactor.core.publisher.Mono;

/*
 *
 * @author jayendravikramsingh
 *
 * 22/08/20
 */

@Service
public class WebsiteScrapperProcessor implements Processor {
    
    private static final Logger log = LoggerFactory.getLogger(WebsiteScrapperProcessor.class);
    private static final String TEXT_BOX = "contno";
    private static final String SUBMIT = "CONTButton1";
    private static final String TYPE = "drpimpexp";
    
    @Autowired
    private CamelContext camelContext;
    
    @Autowired
    private WebsiteConfig websiteConfig;
    
    @Override
    public void process(Exchange exchange) throws Exception {
        String containerId = exchange.getIn().getBody(String.class);
        log.info("id -> {}", containerId);
        WebClient client = WebClient.create();
        String body = client.post().uri(websiteConfig.getUrl())
                            .body(BodyInserters.fromFormData(this.returnFormData(containerId))).retrieve()
                            .onStatus(anyOtherThan2xx, clientResponse -> {
                                log.error("Received status :{}, body: {}", clientResponse.statusCode(),
                                          clientResponse.bodyToMono(String.class));
                                return Mono.empty();
                            }).bodyToMono(String.class).block();
        Document page = Jsoup.parse(body);
        Element div = websiteConfig.getResult().keySet().stream().map(page::getElementById).filter(Objects::nonNull)
                                   .findFirst().orElseThrow();
        Element tbody = div.getElementsByTag("tbody").get(0);
        //@formatter:off
        Map<String, String> data = tbody.getElementsByTag("tr").stream()
                                        .map(tr -> tr.getElementsByTag("td")
                                                     .stream()
                                                     .peek(td -> log.debug("{}", td.text()))
                                                     .map(td -> td.text()))
                                        .flatMap(dataStream -> dataStream.sorted()).collect(Collectors.toMap(val -> {
                    Optional<String> first = websiteConfig.getResult().get(div.id()).stream()
                                                          .filter(head -> val.contains(head)).findFirst();
                    return first.isPresent() ? first.get().trim() : "";
                }, val -> {
                    Optional<String> first = websiteConfig.getResult().get(div.id()).stream()
                                                          .filter(head -> val.contains(head)).findFirst();
                    return first.isPresent() ? val.replace(first.get().trim(), "") : val;
                }));
        //@formatter:on
        data.put("Container Number", containerId);
        log.info("Result -> {} and target route : {}", data.toString(), div.id());
        exchange.getIn().setBody(data);
        exchange.getIn().setHeader("output-type", div.id());
        websiteConfig.triggerRoute(data, div.id());
    }
    
    public MultiValueMap<String, String> returnFormData(String containerId) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(TEXT_BOX, containerId.trim());
        map.add(SUBMIT, "Submit query");
        map.add(TYPE, "Any");
        map.setAll(websiteConfig.getFormData());
        return map;
    }
    
    private Predicate<HttpStatus> anyOtherThan2xx = response -> !response.is2xxSuccessful();
}
