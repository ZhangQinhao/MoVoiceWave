package com.monke.mopermission

/**
 * 非必要权限获取回调
 */
interface OnRequestPermissionListener {
    /**
     * 成功获取的权限
     * permissions is not null 通过判断获取权限列表来自行判断
     */
    fun requestPermission(permissions: List<String>)
}