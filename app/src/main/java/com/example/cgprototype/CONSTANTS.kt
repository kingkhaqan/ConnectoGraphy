package com.example.cgprototype

import com.google.android.gms.maps.GoogleMap
import com.google.type.LatLng
import ja.burhanrashid52.photoeditor.PhotoFilter

val SHAPE_SCALE_FACTOR = 8

val SELECTED_MAP_TYPE = GoogleMap.MAP_TYPE_NORMAL

val TOOLS_CROPING = "croping"
val TOOLS_FILTERS = "filters"
val TOOLS_TEXT = "text"
val TOOLS_ERASER = "eraser"
val TOOLS_BRUSH = "brush"

val ACTIVITY_RESULT_DOWNLOAD_IMAGE = 3456

val INSTANT_GRATITUDE_STRING = "Your generous contribution of time and energy to this volunteer work was incredible. Your spirits are inspiring – we appreciate all your enthusiasm, thank you so much."
val ZOOM_ON_CLUSTER_TOUCH = 2f


val PAKISTAN_LOCATION = com.google.android.gms.maps.model.LatLng(30.3753, 69.3451)
val PAKISTAN_lOCATION_ZOOM_LEVEL = 5f

val BITMAP_IMAGE_WIDTH = 600
val BITMAP_IMAGE_HEIGHT = 600

val EXTRA_IMAGE_BITMAP = "image_bitmap"
val EXTRA_IMAGE_BYTE_ARRAY = "image_byte_array"
val EXTRA_IMAGE_PATH = "image_path"
val EXTRA_TEXT = "text"
val REQUEST_PERMISSION_FINE_LOCATION = 1010
val EXTRA_USER_ID = "user_id"

val EXTRA_PERMISSIONS = 9999

val filterItems = arrayOf(

            FilterItem(R.drawable.gcu_filter, PhotoFilter.NONE, "None"),
            FilterItem(R.drawable.filter_auto_fix, PhotoFilter.AUTO_FIX, "Auto Fix"),
            FilterItem(R.drawable.filter_black_white, PhotoFilter.BLACK_WHITE, "Black White"),
            FilterItem(R.drawable.filter_contrast, PhotoFilter.CONTRAST, "Contrast"),
            FilterItem(R.drawable.filter_cross_process, PhotoFilter.CROSS_PROCESS, "Cross Process"),
            FilterItem(R.drawable.filter_documentary, PhotoFilter.DOCUMENTARY, "Documentary"),
            FilterItem(R.drawable.filter_due_tone, PhotoFilter.DUE_TONE, "Due Tone") ,
            FilterItem(R.drawable.filter_fill_light, PhotoFilter.FILL_LIGHT, "Fill Light"),
            FilterItem(R.drawable.filter_fish_eye, PhotoFilter.FISH_EYE, "Fish Eye"),
            FilterItem(R.drawable.filter_gray_scale, PhotoFilter.GRAY_SCALE, "Gray Scale"),
            FilterItem(R.drawable.filter_negative, PhotoFilter.NEGATIVE, "Negative"),
            FilterItem(R.drawable.filter_grain, PhotoFilter.GRAIN, "Grain"),
            FilterItem(R.drawable.filter_lomish, PhotoFilter.LOMISH, "Lomish"),
            FilterItem(R.drawable.filter_posterize, PhotoFilter.POSTERIZE, "Posterize"),
            FilterItem(R.drawable.filter_saturate, PhotoFilter.SATURATE, "Saturate"),
            FilterItem(R.drawable.filter_sepia, PhotoFilter.SEPIA, "Sepia"),
            FilterItem(R.drawable.filter_sharpen, PhotoFilter.SHARPEN, "Sharpen"),
            FilterItem(R.drawable.filter_temprature, PhotoFilter.TEMPERATURE, "Temprature"),
            FilterItem(R.drawable.filter_tint, PhotoFilter.TINT, "Tint"),
            FilterItem(R.drawable.filter_vignette, PhotoFilter.VIGNETTE, "Vignette"),
            FilterItem(R.drawable.filter_brightness, PhotoFilter.BRIGHTNESS, "Brightness")


)