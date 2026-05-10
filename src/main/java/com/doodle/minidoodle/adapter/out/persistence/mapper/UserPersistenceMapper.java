package com.doodle.minidoodle.adapter.out.persistence.mapper;

import com.doodle.minidoodle.adapter.out.persistence.entity.UserEntity;
import com.doodle.minidoodle.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {

    public User toDomain(UserEntity entity) {
        return new User(entity.getId(), entity.getEmail(), entity.getName(), entity.getCreatedAt());
    }

    public UserEntity toEntity(User user) {
        return UserEntity.builder()
                .id(user.id())
                .email(user.email())
                .name(user.name())
                .createdAt(user.createdAt())
                .build();
    }
}
