package com.monke.mopermission;

import androidx.lifecycle.Observer;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class NecessaryPermissionJustOnceObserver implements Observer<List<String>> {
    private WeakReference<Object> objectWeakReference; //用来管理生命周期
    private OnRequestNecessaryPermissionListener requestNecessaryPermissionListener;
    private int permissionCount;

    public NecessaryPermissionJustOnceObserver(@NotNull Object context, @NotNull OnRequestNecessaryPermissionListener requestNecessaryPermissionListener, int permissionCount) {
        this.objectWeakReference = new WeakReference<>(context);
        this.requestNecessaryPermissionListener = requestNecessaryPermissionListener;
        this.permissionCount = permissionCount;
    }

    @Override
    public void onChanged(List<String> strings) {
        if (objectWeakReference != null
                && objectWeakReference.get() != null
                && requestNecessaryPermissionListener != null) {
            if (strings == null) {
                requestNecessaryPermissionListener.fail(new ArrayList<String>());
            } else {
                if (strings.size() >= permissionCount) {
                    requestNecessaryPermissionListener.success(strings);
                } else {
                    requestNecessaryPermissionListener.fail(strings);
                }
            }
        }
    }
}
