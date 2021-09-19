package com.example.order.outerservice;

import com.example.common.order_common.OrderDrawable;
import com.example.order.R;

public class OrderDrawableImpl implements OrderDrawable {

    /**
     * 对外的具体实现
     *
     * @return
     */
    @Override
    public int getDrawable() {
        return R.drawable.ic_launcher_background;
    }
}
