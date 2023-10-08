package org.soulaimane.springcloudstreamkafka.entities;

import lombok.*;

import java.util.Date;
 @Data
 @NoArgsConstructor
 @AllArgsConstructor
 @ToString
 @Builder
public class PageEvent {
    private String pageName;
    private String userName;
    private Date date;
    private long duration;
}
