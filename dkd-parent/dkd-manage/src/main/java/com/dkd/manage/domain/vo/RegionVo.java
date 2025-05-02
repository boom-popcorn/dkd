package com.dkd.manage.domain.vo;

import com.dkd.manage.domain.Region;
import lombok.Data;

/**
 * ClassName: RegionVo
 * Package: com.dkd.manage.domain.vo
 *
 * @Author: itheima-ht
 * @Create: 2024/8/29 20:54
 */
@Data
public class RegionVo extends Region {

    // 点位数量
    private Integer nodeCount;
}
