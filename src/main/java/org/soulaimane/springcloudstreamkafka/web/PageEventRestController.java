package org.soulaimane.springcloudstreamkafka.web;

import lombok.AllArgsConstructor;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.*;
import org.soulaimane.springcloudstreamkafka.entities.PageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@AllArgsConstructor
public class PageEventRestController {
    private StreamBridge streamBridge;
    private InteractiveQueryService interactiveQueryService;

    @GetMapping("/publish/{topic}/{pageName}")
    public PageEvent publish(@PathVariable String topic,@PathVariable String pageName){
        PageEvent pageEvent=new PageEvent(pageName,Math.random()>0.5?"U1":"U2",new Date(),new Random().nextLong(10000));
        streamBridge.send(topic,pageEvent);
        return pageEvent;
    }
    @GetMapping(value = "/analytics",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, Long>> analytics() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(seq -> {
                            Map<String, Long> map = new HashMap<>();
                            ReadOnlyWindowStore<String, Long> windowStore = interactiveQueryService.getQueryableStore("page-count", QueryableStoreTypes.windowStore());
                            Instant now = Instant.now();
                            Instant from = now.minusMillis(5000);
                            KeyValueIterator<Windowed<String>, Long> fetchAll = windowStore.fetchAll(from, now);
                            while (fetchAll.hasNext()) {
                                KeyValue<Windowed<String>, Long> next = fetchAll.next();
                                map.put(next.key.key(),next.value);
                            }
                            return map;
                        }
                ).share();
    }
}
