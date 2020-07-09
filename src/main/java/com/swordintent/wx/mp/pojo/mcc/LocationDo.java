package com.swordintent.wx.mp.pojo.mcc;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LocationDo {
    int id;
    int x_id;
    int p_id;
    int d_id;
    String p_name;
    String d_name;
    String x_name;
}
