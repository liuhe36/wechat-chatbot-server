package com.swordintent.wx.mp.pojo.mcc;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ReportLogDo {
    private int id;
    private String mcc;
    private String desc;
    private String raw_desc;
}
