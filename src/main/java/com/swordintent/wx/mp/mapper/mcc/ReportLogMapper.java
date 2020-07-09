package com.swordintent.wx.mp.mapper.mcc;

import com.swordintent.wx.mp.pojo.mcc.ReportLogDo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReportLogMapper {

    @Insert("insert into report_log (mcc, desc, raw_desc) values (#{mcc},#{desc},#{raw_desc})")
    void insert(ReportLogDo reportLog);
}
