package com.dkd.manage.domain.dto;

import lombok.Data;

/**
 * ClassName: ChannelSkuDTO
 * Package: com.dkd.manage.domain.dto
 *
 * @Author: itheima-ht
 * @Create: 2025/2/11 15:34
 */
@Data
public class ChannelSkuDTO {
    private String innerCode;//售货机编号
    private String channelCode;//货道编号
    private Long skuId;//商品ID
}
