package com.yun.yunaiagent.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 应用用户数据访问接口。
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    boolean existsByUsername(String username);

    Optional<AppUser> findByUsername(String username);
}
