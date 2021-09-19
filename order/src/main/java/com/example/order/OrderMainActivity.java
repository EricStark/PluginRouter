package com.example.order;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.router_annotaions.PluginRouter;

@PluginRouter(className = "/order/OrderMainActivity")
public class OrderMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_main);
    }
}