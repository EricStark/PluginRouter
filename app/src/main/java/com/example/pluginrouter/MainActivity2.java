package com.example.pluginrouter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.router_annotaions.Parameter;
import com.example.router_annotaions.PluginRouter;

@PluginRouter(className = "/app/MainActivity2")
public class MainActivity2 extends AppCompatActivity {

    @Parameter
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }
}