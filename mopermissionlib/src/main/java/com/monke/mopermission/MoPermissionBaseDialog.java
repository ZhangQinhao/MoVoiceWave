package com.monke.mopermission;

import android.content.Context;
import android.view.View;

import androidx.appcompat.app.AppCompatDialog;

public abstract class MoPermissionBaseDialog extends AppCompatDialog {
    public MoPermissionBaseDialog(Context context) {
        super(context);
    }

    private View.OnClickListener yesClickListener;
    private View.OnClickListener noClickListener;

    public void show(String titleStr, String descStr, String yesStr, String noStr, View.OnClickListener yesClickListener, View.OnClickListener noClickListener) {
        this.yesClickListener = yesClickListener;
        this.noClickListener = noClickListener;
        if (!isShowing()) {
            show();
        }
    }

    public void clickToRequest(View v) {
        if (yesClickListener != null) {
            yesClickListener.onClick(v);
        }
    }

    public void clickToCancel(View v) {
        if (noClickListener != null) {
            noClickListener.onClick(v);
        }
    }
}
