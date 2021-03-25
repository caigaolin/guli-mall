package com.muke.gulimall.ums.service.impl;

import com.muke.common.constant.MemberConstant;
import com.muke.common.enums.CustomizeExceptionEnum;
import com.muke.common.exception.RRException;
import com.muke.gulimall.ums.entity.MemberLevelEntity;
import com.muke.gulimall.ums.service.MemberLevelService;
import com.muke.gulimall.ums.vo.LoginVo;
import com.muke.gulimall.ums.vo.RegisterVo;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muke.common.utils.PageUtils;
import com.muke.common.utils.Query;

import com.muke.gulimall.ums.dao.MemberDao;
import com.muke.gulimall.ums.entity.MemberEntity;
import com.muke.gulimall.ums.service.MemberService;

import javax.annotation.Resource;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Resource(name = "memberLevelService")
    private MemberLevelService levelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void registerMember(RegisterVo registerVo) {
        // 校验用户名和手机号是否已存在
        checkedMemberUsername(registerVo.getUserName());
        checkedMemberPhone(registerVo.getPhone());
        // 校验通过，进行注册
        MemberEntity memberEntity = new MemberEntity();
        // 1.查询默认的会员等级
        MemberLevelEntity levelEntity = levelService.getOne(new QueryWrapper<MemberLevelEntity>().select("id").eq("default_status", MemberConstant.memberLevel.DEFAULT.getCode()));
        memberEntity.setLevelId(levelEntity.getId());
        memberEntity.setUsername(registerVo.getUserName());
        memberEntity.setMobile(registerVo.getPhone());
        // 2.对密码进行加密保存
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        // 得到加密后的密码
        String encodePassword = passwordEncoder.encode(registerVo.getPassword());
        memberEntity.setPassword(encodePassword);
        // 保存注册的用户信息
        baseMapper.insert(memberEntity);
    }

    @Override
    public MemberEntity loginMember(LoginVo loginVo) {
        // 通过账号查询会员信息
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginVo.getLoginAccount()).or().eq("mobile", loginVo.getLoginAccount()));
        // 校验密码
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        // 匹配密码
        boolean matches = passwordEncoder.matches(loginVo.getPassword(), memberEntity.getPassword());
        // 判断密码是否匹配成功
        if (matches) {
            return memberEntity;
        }
        return null;
    }

    /**
     * 校验手机号
     *
     * @param phone
     */
    private void checkedMemberPhone(String phone) {
        Integer mobileCount = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (mobileCount > 0) {
            throw new RRException(CustomizeExceptionEnum.PHONE_EXITS);
        }
    }

    /**
     * 校验用户名
     *
     * @param userName
     */
    private void checkedMemberUsername(String userName) {
        Integer usernameCount = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (usernameCount > 0) {
            throw new RRException(CustomizeExceptionEnum.USERNAME_EXITS);
        }
    }

}