package com.monke.mopermission;

import androidx.lifecycle.Observer;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PermissionJustOnceObserver implements Observer<List<String>> {
    private WeakReference<Object> objectWeakReference; //用来管理生命周期
    private OnRequestPermissionListener requestPermissionListener;

    public PermissionJustOnceObserver(@NotNull Object context, @NotNull OnRequestPermissionListener requestPermissionListener) {
        this.objectWeakReference = new WeakReference<>(context);
        this.requestPermissionListener = requestPermissionListener;
    }

    @Override
    public void onChanged(List<String> strings) {
        if (objectWeakReference != null
                && objectWeakReference.get() != null
                && requestPermissionListener != null) {
            if (strings == null) {
                requestPermissionListener.requestPermission(new ArrayList<String>());
            } else {
                requestPermissionListener.requestPermission(strings);
            }
        }
    }
}
