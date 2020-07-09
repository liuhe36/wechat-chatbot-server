package com.swordintent.wx.mp.mapper.mcc;

import com.swordintent.wx.mp.pojo.mcc.ProvinceDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProvinceMapper {

  @Select("SELECT * FROM province WHERE p_id = #{provinceid}")
  List<ProvinceDo> find(@Param("provinceid") int provinceid);
}
