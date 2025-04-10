package ru.emiren.tg_news.DTO;

import jakarta.persistence.Entity;
import lombok.*;


@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDTO {
    private Long id;
    private String role;
}
