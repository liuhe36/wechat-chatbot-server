package com.swordintent.wx.mp.mapper.mcc;

import com.swordintent.wx.mp.pojo.mcc.BankDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BankMapper {

    @Select("SELECT * FROM bank WHERE bank_id = #{bankid} and x_id = #{locationid}")
    List<BankDo> findByBankAndLocal(@Param("bankid") int bankid,
                                    @Param("locationid") int locationid);

    @Select("SELECT * FROM bank WHERE bank_id = #{bankid}")
    List<BankDo> find(@Param("bankid") int bankid);
}
