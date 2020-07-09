package com.swordintent.wx.mp.mapper.mcc;

import com.swordintent.wx.mp.pojo.mcc.LocationDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LocationMapper {

  @Select("SELECT * FROM location WHERE x_id = #{locationid}")
  List<LocationDo> find(@Param("locationid") int locationid);
}
