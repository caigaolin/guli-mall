package com.muke.gulimall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.muke.common.to.member.MemberReceiveAddressTo;
import com.muke.common.utils.R;
import com.muke.common.vo.MemberRespVo;
import com.muke.gulimall.wms.feign.MemberFeign;
import com.muke.gulimall.wms.vo.MemberReceiveAddressRespVo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.Query;

import com.muke.gulimall.wms.dao.WareInfoDao;
import com.muke.gulimall.wms.entity.WareInfoEntity;
import com.muke.gulimall.wms.service.WareInfoService;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Resource
    private MemberFeign memberFeign;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.eq("id", key).or().like("name", key).or().like("address", key);
            });
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public MemberReceiveAddressRespVo getFare(Long addrId) {
        MemberReceiveAddressRespVo respVo = new MemberReceiveAddressRespVo();
        // 远程调用会员服务，获取到收货地址
        R info = memberFeign.info(addrId);
        if (info.getCode().equals(0)) {
            Object addressObj = info.get("memberReceiveAddress");
            String addrStr = JSON.toJSONString(addressObj);
            MemberReceiveAddressTo addressTo = JSON.parseObject(addrStr, MemberReceiveAddressTo.class);
            // 计算运费
            String phone = addressTo.getPhone();
            BigDecimal fare = new BigDecimal(phone.substring(phone.length() - 1));
            respVo.setFare(fare);
            respVo.setMemberAddress(addressTo);
        }
        return respVo;
    }

}