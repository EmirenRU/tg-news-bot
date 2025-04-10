package ru.emiren.tg_news.Mapper;

import ru.emiren.tg_news.DTO.RoleDTO;
import ru.emiren.tg_news.Model.Roles;

public class RoleMapper {
    public static Roles mapToRole(RoleDTO roleDTO){
        return Roles.builder()
                .id(roleDTO.getId())
                .role(roleDTO.getRole())
                .build();
    }

    public static RoleDTO mapToRoleDTO(Roles roles){
        return RoleDTO.builder()
                .id(roles.getId())
                .role(roles.getRole())
                .build();
    }
}
