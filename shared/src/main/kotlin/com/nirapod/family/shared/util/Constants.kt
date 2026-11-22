package com.nirapod.family.shared.util

object Constants {
    const val PREFS_NAME = "nirapod_family_prefs"
    const val KEY_LICENSE_KEY = "license_key"
    const val KEY_JWT_TOKEN = "jwt_token"
    const val KEY_PARENT_ID = "parent_id"
    const val KEY_DEVICE_ID = "device_id"
    const val KEY_IS_SETUP_DONE = "is_setup_done"
    const val KEY_IS_HIDDEN = "is_hidden"
    const val SECRET_DIAL_CODE = "*#9753#"

    const val FIRESTORE_USERS = "users"
    const val FIRESTORE_DEVICES = "devices"
    const val FIRESTORE_LICENSES = "licenses"
    const val FIRESTORE_LOCATIONS = "locations"
    const val REALTIME_SIGNALING = "signaling"

    // FCM Command Types
    const val CMD_START_SCREEN = "start_screen"
    const val CMD_START_CAMERA = "start_camera"
    const val CMD_START_AUDIO = "start_audio"
    const val CMD_STOP_ALL = "stop_all"
    const val CMD_GET_LOCATION = "get_location"
    const val CMD_HIDE_APP = "hide_app"
    const val CMD_UNHIDE_APP = "unhide_app"

    // Notification
    const val CHILD_NOTIFICATION_CHANNEL_ID = "nirapod_child_service_channel"
    const val CHILD_NOTIFICATION_ID = 9753
}
