package com.muke.gulimall.pms.vo.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/10 12:37
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Catelog3Vo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String catalog2Id;

    private String id;

    private String name;

}
