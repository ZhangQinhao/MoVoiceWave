package com.monke.mopermission

/**
 * 必要权限获取回调
 */
interface OnRequestNecessaryPermissionListener {
    /**
     * 所有权限获取成功
     */
    fun success(permissions: List<String>)

    /**
     * 权限获取失败或者不全
     */
    fun fail(permissions: List<String>)
}