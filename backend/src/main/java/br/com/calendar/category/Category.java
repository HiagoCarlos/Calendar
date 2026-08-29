package br.com.calendar.category;

import java.time.Instant;

import org.hibernate.annotations.SQLDelete;

import br.com.calendar.common.BaseEntity;
import br.com.calendar.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "category")
@SQLDelete(sql = "UPDATE category SET deleted_at = now() WHERE id = ?")
public class Category extends BaseEntity {

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String title;

    @Column(length = 6)
    private String color;

    @Column(length = 255)
    private String icon;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
