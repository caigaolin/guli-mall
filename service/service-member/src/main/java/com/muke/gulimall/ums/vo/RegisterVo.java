package com.muke.gulimall.ums.vo;

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

    private String userName;

    private String password;

    private String phone;

}
