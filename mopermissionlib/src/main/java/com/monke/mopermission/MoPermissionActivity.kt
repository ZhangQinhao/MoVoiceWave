package com.monke.mopermission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

@RequiresApi(Build.VERSION_CODES.M)
class MoPermissionActivity : AppCompatActivity() {

    companion object {
        private const val INTENT_KEY_NECESSARY = "intent_key_necessary"
        private const val INTENT_KEY_RESPONSEKEY = "intent_key_responsekey"
        private const val INTENT_KEY_PERMISSION = "intent_key_permission"

        private const val INTENT_KEY_TITLE = "intent_key_title"  //当必要权限申请，未弹出系统权限框的dialog描述
        private const val INTENT_KEY_DESC = "intent_key_desc"
        private const val INTENT_KEY_YES = "intent_key_yes"
        private const val INTENT_KEY_NO = "intent_key_no"
        private const val INTENT_KEY_UI = "intent_key_ui"

        internal fun start(context: Context, necessary: Boolean, responseKey: String, title: String?, warnDesc: String?, yesStr: String?, noStr: String?, uiClass: Class<out MoPermissionBaseDialog>?, vararg permission: String) {
            val intent = Intent(context, MoPermissionActivity::class.java)
            intent.putExtra(INTENT_KEY_NECESSARY, necessary)
            intent.putExtra(INTENT_KEY_RESPONSEKEY, responseKey)
            intent.putExtra(INTENT_KEY_PERMISSION, permission)
            intent.putExtra(INTENT_KEY_TITLE, title)
            intent.putExtra(INTENT_KEY_DESC, warnDesc)
            intent.putExtra(INTENT_KEY_YES, yesStr)
            intent.putExtra(INTENT_KEY_NO, noStr)
            intent.putExtra(INTENT_KEY_UI, uiClass)
            context.startActivity(intent)
        }
    }

    private var necessary: Boolean = false
    private var responseKey: String? = null
    private var permission: Array<String>? = null
    private var specialPermission = arrayListOf<String>() //非必须前提下 特殊权限申请(用来控制次数)
    private var title: String? = null
    private var warnDesc: String? = null
    private var yesStr: String? = null
    private var noStr: String? = null
    private var uiClass: Class<out MoPermissionBaseDialog>? = null  //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent == null) {
            finish()
            return
        }
        necessary = intent.getBooleanExtra(INTENT_KEY_NECESSARY, false)
        responseKey = intent.getStringExtra(INTENT_KEY_RESPONSEKEY)
        if (TextUtils.isEmpty(responseKey)) {
            finish()
            return
        }
        permission = intent.getStringArrayExtra(INTENT_KEY_PERMISSION)
        if (permission == null || permission!!.isEmpty()) {
            finish()
            return
        }
        if (!necessary) {
            if (permission!!.contains(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                specialPermission.add(Manifest.permission.SYSTEM_ALERT_WINDOW)
            }
            if (permission!!.contains(Manifest.permission.WRITE_SETTINGS)) {
                specialPermission.add(Manifest.permission.WRITE_SETTINGS)
            }
        }
        title = intent.getStringExtra(INTENT_KEY_TITLE)
        if (title.isNullOrEmpty()) {
            title = getString(R.string.moperission_warn_default)
        }
        warnDesc = intent.getStringExtra(INTENT_KEY_DESC)
        yesStr = intent.getStringExtra(INTENT_KEY_YES)
        if (yesStr.isNullOrEmpty()) {
            yesStr = getString(R.string.moperission_request)
        }
        noStr = intent.getStringExtra(INTENT_KEY_NO)
        if (noStr.isNullOrEmpty()) {
            noStr = if (necessary) {
                getString(R.string.moperission_exit)
            } else {
                getString(R.string.moperission_cancel)
            }
        }
        uiClass = intent.getSerializableExtra(INTENT_KEY_UI) as Class<out MoPermissionBaseDialog>?
        //申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions()
        } else {
            //不用申请权限
            MoPermissionBus.getInstance()?.sendData(responseKey!!, permission!!.asList())
            finish()
            return
        }
    }

    private var jumpToSystem: Boolean = false
    override fun onResume() {
        super.onResume()
        if (jumpToSystem) {
            jumpToSystem = false
            if (necessary) {
                if (dialogIsShow() && permission?.isNotEmpty() == true) {
                    resultList = arrayListOf<String>()
                    noResultList = arrayListOf<String>()
                    for (item in permission!!) {
                        if (MoPermission.checkPermission(this, item)) {
                            resultList!!.add(item)
                        } else {
                            noResultList!!.add(item)
                        }
                    }
                    if (noResultList.isNullOrEmpty()) {
                        MoPermissionBus.getInstance()?.sendData(responseKey!!, permission!!.asList())
                        finish()
                        return
                    }
                    doubleCheckDialog()
                }
            } else {
                if (dialogIsShow() && permission?.isNotEmpty() == true) {
                    dismissDialog()
                    resultList = arrayListOf<String>()
                    noResultList = arrayListOf<String>()
                    for (item in permission!!) {
                        if (MoPermission.checkPermission(this, item)) {
                            resultList!!.add(item)
                        } else {
                            noResultList!!.add(item)
                        }
                    }
                    if (noResultList.isNullOrEmpty()) {
                        MoPermissionBus.getInstance()?.sendData(responseKey!!, permission!!.asList())
                        finish()
                        return
                    }
                    specialCheckDialog()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MoPermissionBus.getInstance()?.removeLiveData(responseKey)
    }

    private var resultList: ArrayList<String>? = null
    private var noResultList: ArrayList<String>? = null
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 0x101 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resultList = arrayListOf<String>()
            noResultList = arrayListOf<String>()
            for ((index, item) in grantResults.withIndex()) {
                if (item == PackageManager.PERMISSION_GRANTED) {
                    resultList?.add(permissions.get(index = index))
                } else {
                    var i = permissions.get(index = index)
                    when (i) {
                        Manifest.permission.WRITE_SETTINGS -> {
                            if (MoPermission.checkPermission(this, Manifest.permission.WRITE_SETTINGS)) {
                                resultList?.add(i)
                            } else {
                                noResultList?.add(i)
                            }
                        }
                        Manifest.permission.SYSTEM_ALERT_WINDOW -> {
                            if (MoPermission.checkPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                                resultList?.add(i)
                            } else {
                                noResultList?.add(i)
                            }
                        }
                        else -> {
                            noResultList?.add(i)
                        }
                    }
                }
            }
            if (noResultList.isNullOrEmpty()) {
                //所有权限申请成功
                MoPermissionBus.getInstance()?.sendData(responseKey!!, permission!!.asList())
                finish()
                return
            }

            if (!necessary) {
                //如果非必要权限 则直接关闭，返回已获取的权限
                specialCheckDialog()
            } else {
                doubleCheckDialog()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * 权限申请
     */
    private fun requestPermissions() {
        requestPermissions(permission!!, 0x101)
    }

    /**
     * 必须权限获取结果判断专用
     */
    private fun doubleCheckDialog() {
        if (noResultList.isNullOrEmpty()) {
            MoPermissionBus.getInstance()?.sendData(responseKey!!, resultList)
            dismissDialog()
            finish()
            return
        }
        //必要权限，弹框
        if (canContinueRequest(noResultList)) {
            //依然可以使用系统方式获取权限
            showDialog(title, warnDesc, yesStr, noStr, View.OnClickListener {
                requestPermissions()
                dismissDialog()
            }, View.OnClickListener {
                MoPermissionBus.getInstance()?.sendData(responseKey!!, resultList)
                dismissDialog()
                finish()
            })
        } else {
            //用户勾选不再提示，所以转而使用跳转系统设置去修改权限
            showDialog(title, warnDesc, yesStr, noStr, View.OnClickListener {
                //跳转到系统设置去开启权限
                jumpToSystem = true
                when {
                    Manifest.permission.SYSTEM_ALERT_WINDOW == noResultList!![0] -> {
                        //悬浮窗权限 单独处理
                        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                    }
                    Manifest.permission.WRITE_SETTINGS == noResultList!![0] -> {
                        //系统设置权限 单独处理
                        startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName")))
                    }
                    else -> {
                        requestPermissionSetting(this)
                    }
                }
            }, View.OnClickListener {
                MoPermissionBus.getInstance()?.sendData(responseKey!!, resultList)
                dismissDialog()
                finish()
            })
        }
    }

    /**
     * 判断权限是否可以使用系统默认方式申请
     */
    private fun canContinueRequest(permission: List<String>?): Boolean {
        if (permission == null) {
            return false
        }
        for (item in permission) {
            if (shouldShowRequestPermissionRationale(item)) {
                return true
            }
        }
        return false
    }

    /**
     * 非必须权限的特殊权限申请以及结果输出专用
     */
    private fun specialCheckDialog() {
        if (specialPermission.isNullOrEmpty()) {
            MoPermissionBus.getInstance()?.sendData(responseKey!!, resultList)
            finish()
            return
        } else {
            val item = specialPermission.first()
            specialPermission.removeAt(0)
            if (resultList!!.contains(item)) {
                specialCheckDialog()
                return
            }
            when (item) {
                Manifest.permission.SYSTEM_ALERT_WINDOW -> {
                    var t = getString(R.string.mopermission_window_default)
                    if (!TextUtils.isEmpty(title)) {
                        t = title!!
                    }
                    showDialog(t, warnDesc, yesStr, noStr, View.OnClickListener {
                        jumpToSystem = true
                        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                    }, View.OnClickListener {
                        dismissDialog()
                        specialCheckDialog()
                    })
                }
                Manifest.permission.WRITE_SETTINGS -> {
                    var t = getString(R.string.mopermission_system_default)
                    if (!TextUtils.isEmpty(title)) {
                        t = title!!
                    }
                    showDialog(t, warnDesc, yesStr, noStr, View.OnClickListener {
                        jumpToSystem = true
                        startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName")))
                    }, View.OnClickListener {
                        dismissDialog()
                        specialCheckDialog()
                    })
                }
                else -> {
                    specialCheckDialog()
                }
            }
        }
    }

    /**
     * 跳转到系统应用权限设置页
     */
    private fun requestPermissionSetting(from: Context) {
        try {
            val localIntent = Intent()
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (Build.VERSION.SDK_INT >= 9) {
                localIntent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                localIntent.data = Uri.fromParts("package", from.packageName, null)
            } else if (Build.VERSION.SDK_INT <= 8) {
                localIntent.action = Intent.ACTION_VIEW
                localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails")
                localIntent.putExtra("com.android.settings.ApplicationPkgName", from.packageName)
            }
            from.startActivity(localIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var moPermissionDialog: MoPermissionBaseDialog? = null
    private fun showDialog(title: String?, desc: String?, yesStr: String?, noStr: String?, yesClickListener: View.OnClickListener, noClickListener: View.OnClickListener) {
        if (moPermissionDialog == null) {
            moPermissionDialog = if (uiClass == null) {
                MoPermissionDialog(this)
            } else {
                var constructor = uiClass!!.getConstructor(Context::class.java)
                constructor.newInstance(this)
            }
        }
        moPermissionDialog?.show(title, desc, yesStr, noStr, yesClickListener, noClickListener)
    }

    private fun dismissDialog() {
        moPermissionDialog?.dismiss()
    }

    private fun dialogIsShow(): Boolean {
        return moPermissionDialog?.isShowing ?: false
    }
}