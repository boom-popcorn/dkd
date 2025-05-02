package com.dkd.manage.domain.vo;

import com.dkd.manage.domain.Partner;
import lombok.Data;

/**
 * ClassName: PartnerVo
 * Package: com.dkd.manage.domain.vo
 *
 * @Author: itheima-ht
 * @Create: 2024/8/29 22:23
 */
@Data
public class PartnerVo extends Partner {

    // 点位数量
    private Integer nodeCount;
}
