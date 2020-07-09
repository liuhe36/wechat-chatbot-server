package com.swordintent.wx.mp.handler.mcc;

import com.swordintent.wx.mp.builder.TextBuilder;
import com.swordintent.wx.mp.handler.MsgHandler;
import com.swordintent.wx.mp.mapper.mcc.ReportLogMapper;
import com.swordintent.wx.mp.pojo.mcc.ReportLogDo;
import com.swordintent.wx.mp.utils.JsonUtils;
import lombok.AllArgsConstructor;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author liu
 */
@Component
@AllArgsConstructor
public class TextMsgReportErrorHandler extends MsgHandler {

    private final ReportLogMapper reportLogMapper;

    private final Pattern longMccPattern = Pattern.compile("^我要报错\\+([0-9]{4,20})\\+(.*)");

    private static Pattern p = Pattern.compile("^我要报错(.*)");

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context, WxMpService weixinService,
                                    WxSessionManager sessionManager) {
        String content = wxMessage.getContent();
        String result = insertBaoCuo(content);
        this.logger.info("响应：" + JsonUtils.toJson(result));
        return new TextBuilder(result).build(wxMessage, weixinService);
    }

    private String insertBaoCuo(String content) {
        try {
            Matcher matcher = longMccPattern.matcher(content);
            if (matcher.find()) {
                String mcc = matcher.group(1);
                String prop = matcher.group(2);
                ReportLogDo reportLog = new ReportLogDo();
                reportLog.setMcc(mcc);
                reportLog.setDesc(prop);
                reportLog.setRaw_desc(content);
                reportLogMapper.insert(reportLog);
                return "感谢您的反馈，谢谢！";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "感谢您的反馈，谢谢！";
    }

    @Override
    public boolean match(WxMpXmlMessage wxMpXmlMessage) {
        String content = wxMpXmlMessage.getContent();
        return p.matcher(content).matches();
    }
}
