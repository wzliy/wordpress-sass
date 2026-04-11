package com.wpss.wordpresssass.auth.infrastructure.mapper;

import com.wpss.wordpresssass.auth.infrastructure.dataobject.TenantDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface TenantMapper {

    @Insert("""
            INSERT INTO tenant (name)
            VALUES (#{name})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TenantDO tenantDO);

    @Select("SELECT COUNT(1) FROM tenant")
    long countAll();

    @Select("""
            SELECT id, name
            FROM tenant
            WHERE name = #{name}
            ORDER BY id ASC
            LIMIT 1
            """)
    Optional<TenantDO> selectByName(String name);
}
