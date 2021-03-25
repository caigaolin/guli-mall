package com.muke.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/17 14:17
 */
@Data
public class RegisterVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "用户名不能为空")
    private String userName;

    @NotEmpty(message = "密码不能为空")
    @Length(min = 6, max = 15, message = "密码长度在6-15之间")
    private String password;

    @NotEmpty(message = "手机号不能为空")
    @Pattern(regexp = "^1([3-9])[0-9]{9}$", message = "手机号非法")
    private String phone;

    @NotEmpty(message = "验证码不能为空")
    private String code;
}
