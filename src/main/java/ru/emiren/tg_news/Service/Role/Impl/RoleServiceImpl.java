package ru.emiren.tg_news.Service.Role.Impl;

import org.springframework.stereotype.Service;
import ru.emiren.tg_news.Model.Roles;
import ru.emiren.tg_news.Repository.RoleRepository;
import ru.emiren.tg_news.Service.Role.RoleService;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Roles findRole(String name) {
        return roleRepository.findByRole(name).orElse(null);
    }
}
