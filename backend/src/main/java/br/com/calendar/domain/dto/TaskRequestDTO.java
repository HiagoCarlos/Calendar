package br.com.calendar.domain.dto;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRequestDTO {

    private String title;

    private String description;

    private String timezone;

    private String location;

    private String status;

    private Instant startsAt;

    private Instant endsAt;

    private Integer repeat;

    private String repeatInterval;

    private Boolean allDay;

    private String categoryId;

    private Instant completedAt;
}
