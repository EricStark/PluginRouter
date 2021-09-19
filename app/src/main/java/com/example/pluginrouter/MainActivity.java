package com.example.pluginrouter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.router_annotaions.Parameter;
import com.example.router_annotaions.PluginRouter;

@PluginRouter(className = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Parameter
    String name;

    @Parameter
    boolean isMerge;

    @Parameter
    int age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}