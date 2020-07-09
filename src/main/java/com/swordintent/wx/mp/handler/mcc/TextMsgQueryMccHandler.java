package com.swordintent.wx.mp.handler.mcc;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import com.swordintent.wx.mp.builder.TextBuilder;
import com.swordintent.wx.mp.handler.MsgHandler;
import com.swordintent.wx.mp.mapper.mcc.*;
import com.swordintent.wx.mp.pojo.mcc.BankDo;
import com.swordintent.wx.mp.pojo.mcc.BaseMccDo;
import com.swordintent.wx.mp.pojo.mcc.LocationDo;
import com.swordintent.wx.mp.pojo.mcc.ProvinceDo;
import com.swordintent.wx.mp.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author liu
 */
@Component
@AllArgsConstructor
public class TextMsgQueryMccHandler extends MsgHandler {

    private final ExtraMccCodeMapper extraMccCodeMapper;

    private final MccCodeMapper mccCodeMapper;

    private final BankMapper bankMapper;

    private final LocationMapper locationMapper;

    private final ProvinceMapper provinceMapper;

    private static final Pattern longMccPattern = Pattern.compile("^([0-9]{3})([0-9]{4})([0-9]{4})");

    private static Pattern p = Pattern.compile("^[0-9]{4,20}$");

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context, WxMpService weixinService,
                                    WxSessionManager sessionManager) {
        String content = wxMessage.getContent();
        String result = "请检查输入，输入为4位数字（mcc）或大于等于15位的商品编号";
        if (content.length() >= 15) {
            result = getFullMcc(content, result);
        }
        if (content.length() == 4) {
            result = getMcc(content, result);
        }
        this.logger.info("响应：" + JsonUtils.toJson(result));
        System.out.println(result);
        return new TextBuilder(result).build(wxMessage, weixinService);
    }

    private String getMcc(String content, String result) {
        MCC mcc = new MCC();
        mcc.setMccCode(Integer.parseInt(content));
        CompletableFuture<List<BaseMccDo>> baseMcc = getListCompletableFuture(mcc);

        try {
            CompletableFuture.allOf(baseMcc).get();
            Ret ret = new Ret();
            List<BaseMccDo> baseMccs = baseMcc.get();
            if (CollectionUtils.isNotEmpty(baseMccs)) {
                ret.setMccs(convert(baseMccs, true));
            }
            result = buildRet(ret, content);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getFullMcc(String content, String result) {
        MCC mcc = new MCC();
        Matcher matcher = longMccPattern.matcher(content);
        if (matcher.find()) {
            mcc.setMachineBankCode(Integer.parseInt(matcher.group(1)));
            mcc.setChargeLocation(Integer.parseInt(matcher.group(2)));
            mcc.setMccCode(Integer.parseInt(matcher.group(3)));
            mcc.setChargeProvince(mcc.getChargeLocation() / 100);
        }
        CompletableFuture<List<BaseMccDo>> baseMcc = getListCompletableFuture(mcc);
        CompletableFuture<String> bankFuture = getBankFuture(mcc);
        CompletableFuture<String> localFuture = getLocalFuture(mcc);

        try {
            CompletableFuture.allOf(baseMcc, bankFuture, localFuture).get();
            Ret ret = new Ret();
            List<BaseMccDo> baseMccs = baseMcc.get();
            if (CollectionUtils.isNotEmpty(baseMccs)) {
                ret.setMccs(convert(baseMccs, false));
            }
            ret.setBankName(bankFuture.get());
            ret.setLocationName(localFuture.get());
            result = buildRet(ret, content);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }

    private CompletableFuture<String> getLocalFuture(MCC mcc) {
        return CompletableFuture.supplyAsync(() -> {
            List<LocationDo> locations = locationMapper.find(mcc.getChargeLocation());
            if (CollectionUtils.isEmpty(locations)) {
                List<ProvinceDo> provinces = provinceMapper.find(mcc.getChargeProvince());
                if (CollectionUtils.isEmpty(provinces)) {
                    return "未知地区";
                }
                ProvinceDo location = provinces.get(0);
                return location.getP_name();
            } else {
                LocationDo location = locations.get(0);
                List<String> list = new LinkedList<>();
                CollectionUtils.addIgnoreNull(list, location.getP_name());
                CollectionUtils.addIgnoreNull(list, location.getD_name());
                CollectionUtils.addIgnoreNull(list, location.getX_name());
                return StringUtils.join(list, ",");
            }
        });
    }

    private CompletableFuture<String> getBankFuture(MCC mcc) {
        return CompletableFuture.supplyAsync(() -> {
            List<BankDo> banks = bankMapper.find(mcc.getMachineBankCode());
            if (CollectionUtils.isEmpty(banks)) {
                banks = bankMapper.findByBankAndLocal(mcc.getMachineBankCode(), mcc.getChargeLocation());
            }
            if (CollectionUtils.isEmpty(banks)) {
                return "未知银行";
            }
            return banks.get(0).getBank_name();
        });
    }

    private CompletableFuture<List<BaseMccDo>> getListCompletableFuture(MCC mcc) {
        return CompletableFuture.supplyAsync(() -> {
            List<BaseMccDo> mccCodes = mccCodeMapper.find(mcc.getMccCode());
            if (CollectionUtils.isEmpty(mccCodes)) {
                mccCodes = extraMccCodeMapper.findByRange(mcc.getMccCode());
            }
            if (CollectionUtils.isEmpty(mccCodes)) {
                mccCodes = Lists.newLinkedList();
            }
            return mccCodes;
        });
    }

    private List<BaseMccRet> convert(List<BaseMccDo> baseMccs, boolean needDetail) {
        LinkedList<BaseMccRet> baseMccRets = new LinkedList<>();
        if (CollectionUtils.isEmpty(baseMccs)) {
            return baseMccRets;
        }
        for (BaseMccDo baseMcc : baseMccs) {
            BaseMccRet ret = new BaseMccRet();
            ret.setDesc(baseMcc.getDescription());
            ret.setType(baseMcc.getType());
            ret.setHy(baseMcc.getHy());
            if (needDetail) {
                ret.setDetail(truncDetail(baseMcc.getDetail()));
            }
            ret.setTax(baseMcc.getTax());
            JF jf = buildJF(baseMcc);
            ret.setNojf(getJfStr(jf.getNo()));
            ret.setHasjf(getJfStr(jf.getHas()));
            if (needDetail) {
                ret.setUnknow(getJfStr(jf.getUnknow()));
            } else {
                ret.setUnknow(jf.getUnknow().size() + "个");
            }

            baseMccRets.add(ret);
        }
        return baseMccRets;
    }

    private String truncDetail(String detail) {
        return StringUtils.substring(detail, 0, 333) + "……";
    }

    private String getJfStr(Set<String> no) {
        String join = StringUtils.join(no, ",");
        if (StringUtils.isEmpty(join)) {
            return "无";
        }
        return join;
    }

    private JF buildJF(BaseMccDo baseMcc) {
        JF jf = new JF();
        build(jf, baseMcc.getJianhangjf(), "建行");
        build(jf, baseMcc.getGonghangjf(), "工行");
        build(jf, baseMcc.getHuaqijf(), "花旗");
        build(jf, baseMcc.getHuaxiajf(), "华夏");
        build(jf, baseMcc.getJiaohangjf(), "交行");
        build(jf, baseMcc.getMinshengjf(), "民生");
        build(jf, baseMcc.getNonghangjf(), "农行");
        build(jf, baseMcc.getPinganjf(), "平安");
        build(jf, baseMcc.getZhaohangjf(), "招行");
        build(jf, baseMcc.getXingyejf(), "兴业");
        build(jf, baseMcc.getZhongxinjf(), "中信");
        build(jf, baseMcc.getZhongyinjf(), "中银");
        return jf;
    }

    private void build(JF jf, String jfStr, String bank) {
        if (StringUtils.equals(jfStr, "无")) {
            jf.getNo().add(bank);
        } else if (StringUtils.equals(jfStr, "有")) {
            jf.getHas().add(bank);
        } else {
            jf.getUnknow().add(bank);
        }
    }

    private String buildRet(Ret ret, String mccCode) {
        String template1 = "您查询的商户号：%s对应的信息如下\n\n";
        String template2 = "----查询结果开始----\n";
        String template3 = "收单银行-%s，\n";
        String template4 = "行政区划-%s，\n";
        String template5 = "商户信息-%s，\n";
        String template6 = "----查询结果结束----\n\n";
        String template7 = "若系统识别不准，欢迎提供准确信息，可直接回复'我要报错'+'商户号码'+'正确内容'即可,如回复我要报错+100000000000000+中国银联总部消费";
        String bankName = ret.getBankName();
        String locationName = ret.getLocationName();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(template1, mccCode));
        sb.append(template2);
        if (StringUtils.isNotEmpty(bankName)) {
            sb.append(String.format(template3, bankName));
        }
        if (StringUtils.isNotEmpty(locationName)) {
            sb.append(String.format(template4, locationName));
        }
        sb.append(String.format(template5, JsonUtils.toJson(ret.getMccs())));
        sb.append(template6);
        sb.append(template7);
        return sb.toString();
    }

    @Override
    public boolean match(WxMpXmlMessage wxMpXmlMessage) {
        String content = wxMpXmlMessage.getContent();
        return p.matcher(content).matches();
    }

    @Data
    class JF {
        private final Set<String> has = new HashSet<>();
        private final Set<String> no = new HashSet<>();
        private final Set<String> unknow = new HashSet<>();
    }

    @Data
    class MCC {
        private int machineBankCode;
        private int chargeLocation;
        private int chargeProvince;
        private int mccCode;
    }

    @Data
    class Ret {
        private String bankName;
        private String locationName;
        private List<BaseMccRet> mccs;
    }

    @Data
    @ToString
    class BaseMccRet {
        @SerializedName("商户类别")
        private String desc;
        @SerializedName("商户大类")
        private String type;
        @SerializedName("商户所属行业")
        private String hy;
        @SerializedName("商户详细信息")
        private String detail;
        @SerializedName("商户税率")
        private String tax;
        @SerializedName("被列为无积分的银行")
        private String nojf;
        @SerializedName("一般情况下有积分的银行")
        private String hasjf;
        @SerializedName("积分情况未知的银行")
        private String unknow;
    }
}
