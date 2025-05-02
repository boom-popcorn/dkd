package com.dkd.manage.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * ClassName: ChannelConfigDTO
 * Package: com.dkd.manage.domain.dto
 *
 * @Author: itheima-ht
 * @Create: 2025/2/11 15:35
 */
@Data
public class ChannelConfigDTO {

    private String innerCode;//售货机编号
    private List<ChannelSkuDTO> channelList;//货道DTO集合
}
