package com.example.common.manager;

import android.content.Context;
import android.os.Bundle;

import com.example.common.inter.Call;

import java.io.Serializable;

import androidx.annotation.NonNull;

public class BundleManager {

    private Bundle bundle = new Bundle();

    public Bundle getBundle() {
        return this.bundle;
    }

    private Call call;

    Call getCall() {
        return call;
    }

    void setCall(Call call) {
        this.call = call;
    }

    public BundleManager withString(@NonNull String key, @NonNull String val) {
        bundle.putString(key, val);
        return this;
    }

    public BundleManager withBoolean(@NonNull String key, @NonNull Boolean val) {
        bundle.putBoolean(key, val);
        return this;
    }

    public BundleManager withInt(@NonNull String key, @NonNull int val) {
        bundle.putInt(key, val);
        return this;
    }

    public BundleManager withSerializable(@NonNull String key, @NonNull Serializable val) {
        bundle.putSerializable(key, val);
        return this;
    }

    public BundleManager withBundle(@NonNull String key, @NonNull Bundle val) {
        bundle.putBundle(key, val);
        return this;
    }

    public void navigationTo(Context context) {
        RouterManager.getInstance().navigationTo(context, this);
    }
}
