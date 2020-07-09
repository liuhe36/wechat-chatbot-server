package com.swordintent.wx.mp.mapper.mcc;

import com.swordintent.wx.mp.pojo.mcc.BaseMccDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MccCodeMapper {

  @Select("SELECT * FROM mcc_code WHERE mcc = #{mccnum}")
  List<BaseMccDo> find(@Param("mccnum") int mccnum);
}
