package ru.emiren.tg_news.Model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity(name = "users_tg")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String username;
    private Date dateOfSubscription;
    private Date endOfSubscription;

    @ManyToOne
    @JoinColumn(name = "role_id", referencedColumnName = "id")
    private Roles role;

}
