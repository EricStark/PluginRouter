package com.example.common.order_common;

import com.example.common.inter.Call;

/**
 * 这个接口是common提供给其他组件访问order组件的合规
 */
public interface OrderDrawable extends Call {

    /**
     * order提供图片
     *
     * @return 图片 ID
     */
    int getDrawable();

}
