package ru.emiren.tg_news.DTO;

import lombok.*;

import java.util.Date;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private Long userId;
    private Date dateOfSubscription;
    private Date endOfSubscription;
    private RoleDTO role;


}
