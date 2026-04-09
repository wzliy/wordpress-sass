package com.wpss.wordpresssass.post.infrastructure.mapper;

import com.wpss.wordpresssass.post.infrastructure.dataobject.PostDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PostMapper {

    @Insert("""
            INSERT INTO post (tenant_id, title, content, status, created_at)
            VALUES (#{tenantId}, #{title}, #{content}, #{status}, #{createdAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PostDO postDO);

    @Select("""
            SELECT id, tenant_id, title, content, status, created_at
            FROM post
            WHERE tenant_id = #{tenantId}
            ORDER BY created_at DESC, id DESC
            """)
    List<PostDO> selectByTenantId(Long tenantId);

    @Select("""
            SELECT id, tenant_id, title, content, status, created_at
            FROM post
            WHERE id = #{id} AND tenant_id = #{tenantId}
            LIMIT 1
            """)
    Optional<PostDO> selectByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
