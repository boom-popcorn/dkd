package com.dkd.manage.domain.dto;

import lombok.Data;

/**
 * ClassName: TaskDetailsDTO
 * Package: com.dkd.manage.domain.dto
 *
 * @Author: itheima-ht
 * @Create: 2025/2/14 12:30
 */
@Data
public class TaskDetailsDTO {
    private String channelCode;// 货道编号
    private Long expectCapacity;// 补货数量
    private Long skuId;// 商品id
    private String skuName;// 商品名称
    private String skuImage;// 商品图片
}
