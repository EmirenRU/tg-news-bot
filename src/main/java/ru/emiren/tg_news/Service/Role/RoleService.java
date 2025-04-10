package ru.emiren.tg_news.Service.Role;

import ru.emiren.tg_news.Model.Roles;

public interface RoleService {
    Roles findRole(String user);
}
