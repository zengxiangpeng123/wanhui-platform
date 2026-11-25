package com.wanhui.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanhui.admin.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
    @Select("""
            SELECT * FROM audit_logs
            ORDER BY occurred_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<AuditLog> selectRecent(@Param("limit") int limit);
}
