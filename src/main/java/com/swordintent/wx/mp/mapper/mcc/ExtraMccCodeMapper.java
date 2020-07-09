package com.swordintent.wx.mp.mapper.mcc;

import com.swordintent.wx.mp.pojo.mcc.BaseMccDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExtraMccCodeMapper {

  @Select("SELECT * FROM extra_mcc_code WHERE start_mcc <= #{mccnum} and end_mcc >= #{mccnum}")
  List<BaseMccDo> findByRange(@Param("mccnum") int mccnum);
}
