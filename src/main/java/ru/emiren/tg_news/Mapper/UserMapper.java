package ru.emiren.tg_news.Mapper;

import ru.emiren.tg_news.DTO.UserDTO;
import ru.emiren.tg_news.Model.User;

public class UserMapper {
    public static User mapToUser(UserDTO userDTO) {
        if (userDTO == null) return null;
        return User.builder()
                .id(userDTO.getId())
                .username(userDTO.getUsername())
                .role(RoleMapper.mapToRole(userDTO.getRole()))
                .build();
    }

    public static UserDTO mapToUserDTO(User user) {
        if (user == null) return null;
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(RoleMapper.mapToRoleDTO(user.getRole()))
                .build();
    }
}
