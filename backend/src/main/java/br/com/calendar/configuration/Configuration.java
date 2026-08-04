package br.com.calendar.configuration;

import br.com.calendar.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "configuration", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class Configuration extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(length = 20)
    private String theme;

    @Column(name = "time_format", length = 10)
    private String timeFormat;

    @Column(name = "week_start_day")
    private String weekStartDay;

    @Column(name = "default_view", length = 20)
    private String defaultView;
}