package com.dkd.manage.domain.vo;

import com.dkd.manage.domain.Channel;
import com.dkd.manage.domain.Sku;
import lombok.Data;

/**
 * ClassName: ChannelVO
 * Package: com.dkd.manage.domain.vo
 *
 * @Author: itheima-ht
 * @Create: 2025/2/11 15:09
 */
@Data
public class ChannelVO extends Channel {
    //商品对象
    private Sku sku;
}
