package net.scrape.containers.config.camel.route;

import java.util.List;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.csv.CsvDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import net.scrape.containers.config.WebsiteConfig;
import net.scrape.containers.processors.WebsiteLoaderProcessor;
import net.scrape.containers.processors.WebsiteScrapperProcessor;

/*
 *
 * @author jayendravikramsingh
 *
 * 22/08/20
 */
@Service
public class FileRoutes extends RouteBuilder {
    
    @Autowired
    private WebsiteScrapperProcessor websiteScrapperProcessor;
    
    @Autowired
    private WebsiteLoaderProcessor websiteLoaderProcessor;
    
    @Autowired
    private WebsiteConfig websiteConfig;
    
    @Value("${file.path.from}")
    private String from;
    
    @Value("${file.path.to}")
    private String to;
    
    @Override
    public void configure() throws Exception {
        //        onException(Exception.class).continued(true).logStackTrace(true).maximumRedeliveries(0)
        //                                    .to(deadLetterChannel("mock:dlq").getDeadLetterUri());
        //@formatter:off
        from("file://" + from)
                .split(body().tokenize("\n"))
                .streaming()
                .process(websiteLoaderProcessor)
                .process(websiteScrapperProcessor)
                ;
        websiteConfig.getResult().keySet().forEach(key -> {
            CsvDataFormat dataFormat = new CsvDataFormat();
            List<String> headers = websiteConfig.getResult().get(key);
            dataFormat.setHeader(headers.toArray(new String[headers.size()]));
            dataFormat.setDelimiter(',');
//            dataFormat.setSkipHeaderRecord(false);
            from("direct:" + key)
                    .marshal(dataFormat)
                    .log("Route got triggered")
                    .to("file://" + to + "?fileName=" + key.substring(1) + ".csv&fileExist=Append");
            
        });
        //@formatter:on
        
    }
}
