package com.muke.gulimall.pms.vo.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/10 12:35
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Catelog2Vo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String catalog1Id;

    private String id;

    private String name;

    private List<Catelog3Vo> catalog3List;

}
