package com.wpss.wordpresssass.auth.infrastructure.mapper;

import com.wpss.wordpresssass.auth.infrastructure.dataobject.UserAccountDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface UserAccountMapper {

    @Select("""
            SELECT id, tenant_id, username, password, email, nickname, role, status, created_at
            FROM user
            WHERE username = #{username}
            LIMIT 1
            """)
    Optional<UserAccountDO> selectByUsername(String username);

    @Select("""
            SELECT id, tenant_id, username, password, email, nickname, role, status, created_at
            FROM user
            WHERE id = #{id}
            LIMIT 1
            """)
    Optional<UserAccountDO> selectById(Long id);

    @Select("""
            SELECT id, tenant_id, username, password, email, nickname, role, status, created_at
            FROM user
            WHERE tenant_id = #{tenantId}
            ORDER BY created_at DESC, id DESC
            """)
    java.util.List<UserAccountDO> selectByTenantId(Long tenantId);

    @Insert("""
            INSERT INTO user (tenant_id, username, password, email, nickname, role, status, created_at)
            VALUES (#{tenantId}, #{username}, #{password}, #{email}, #{nickname}, #{role}, #{status}, #{createdAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserAccountDO userAccountDO);

    @org.apache.ibatis.annotations.Update("""
            UPDATE user
            SET password = #{password}
            WHERE id = #{id}
              AND tenant_id = #{tenantId}
            """)
    int updatePassword(@org.apache.ibatis.annotations.Param("id") Long id,
                       @org.apache.ibatis.annotations.Param("tenantId") Long tenantId,
                       @org.apache.ibatis.annotations.Param("password") String password);

    @org.apache.ibatis.annotations.Update("""
            UPDATE user
            SET status = #{status}
            WHERE id = #{id}
              AND tenant_id = #{tenantId}
            """)
    int updateStatus(@org.apache.ibatis.annotations.Param("id") Long id,
                     @org.apache.ibatis.annotations.Param("tenantId") Long tenantId,
                     @org.apache.ibatis.annotations.Param("status") String status);

    @org.apache.ibatis.annotations.Update("""
            UPDATE user
            SET email = #{email},
                nickname = #{nickname}
            WHERE id = #{id}
              AND tenant_id = #{tenantId}
            """)
    int updateProfile(@org.apache.ibatis.annotations.Param("id") Long id,
                      @org.apache.ibatis.annotations.Param("tenantId") Long tenantId,
                      @org.apache.ibatis.annotations.Param("email") String email,
                      @org.apache.ibatis.annotations.Param("nickname") String nickname);

    @Select("SELECT COUNT(1) FROM user")
    long countAll();
}
