package com.monke.mopermission;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class MoPermissionDialog extends MoPermissionBaseDialog {
    private TextView tvTitle;
    private TextView tvDesc;
    private TextView tvNo;
    private TextView tvYes;

    public MoPermissionDialog(Context context) {
        super(context);
        setContentView(R.layout.mopermission_layout_confirmdialog);
        tvTitle = findViewById(R.id.tv_title);
        tvDesc = findViewById(R.id.tv_desc);
        tvNo = findViewById(R.id.tv_no);
        tvYes = findViewById(R.id.tv_yes);

        tvTitle.setText(titleStr);
        tvDesc.setText(descStr);
        if (TextUtils.isEmpty(descStr)) {
            tvDesc.setVisibility(View.GONE);
        } else {
            tvDesc.setVisibility(View.VISIBLE);
        }
        tvYes.setText(yesStr);
        tvNo.setText(noStr);

        tvNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickToCancel(v);
            }
        });
        tvYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickToRequest(v);
            }
        });

        setCancelable(false);

        Window dialogWindow = getWindow();
        dialogWindow.setDimAmount(0.6f);
        dialogWindow.getDecorView().setBackgroundResource(R.color.transparent);
        dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
        dialogWindow.setGravity(Gravity.CENTER);

        WindowManager.LayoutParams p = dialogWindow.getAttributes();
        if (p != null) {
            p.width = WindowManager.LayoutParams.WRAP_CONTENT;
            p.height = WindowManager.LayoutParams.WRAP_CONTENT;
            p.gravity = Gravity.CENTER;
            dialogWindow.setAttributes(p);
        }
    }

    private String titleStr;
    private String descStr;
    private String yesStr;
    private String noStr;

    @Override
    public void show(String titleStr, String descStr, String yesStr, String noStr, View.OnClickListener yesClickListener, View.OnClickListener noClickListener) {
        super.show(titleStr, descStr, yesStr, noStr, yesClickListener, noClickListener);
        this.titleStr = titleStr;
        this.descStr = descStr;
        this.yesStr = yesStr;
        this.noStr = noStr;
        if (tvTitle != null) {
            tvTitle.setText(titleStr);
        }
        if (tvDesc != null) {
            tvDesc.setText(descStr);
            if (TextUtils.isEmpty(descStr)) {
                tvDesc.setVisibility(View.GONE);
            } else {
                tvDesc.setVisibility(View.VISIBLE);
            }
        }
        if (tvYes != null) {
            tvYes.setText(yesStr);
        }
        if (tvNo != null) {
            tvNo.setText(noStr);
        }
    }
}
