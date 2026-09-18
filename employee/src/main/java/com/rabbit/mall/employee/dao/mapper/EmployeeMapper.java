package com.rabbit.mall.employee.dao.mapper;

import com.rabbit.mall.employee.dao.po.EmployeePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EmployeeMapper {
    Integer save(EmployeePO po);

    EmployeePO getEmployee(@Param("shopId") Integer shopId, @Param("username") String username);
}
