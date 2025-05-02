package com.dkd.manage.domain.vo;

import com.dkd.manage.domain.Node;
import com.dkd.manage.domain.Partner;
import com.dkd.manage.domain.Region;
import lombok.Data;

/**
 * ClassName: NodeVo
 * Package: com.dkd.manage.domain.vo
 *
 * @Author: itheima-ht
 * @Create: 2024/8/30 12:58
 */
@Data
public class NodeVo extends Node {

    //设备数量
    private Integer vmCount;

    //区域信息
    private Region region;

    //合作商信息
    private Partner partner;
}
