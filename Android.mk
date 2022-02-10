LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

# Include res dir from chips
chips_dir := ../../../../../../frameworks/opt/chips/res
color_picker_dir := ../../../../../../frameworks/opt/colorpicker/res
datetimepicker_dir := ../../../../../../frameworks/opt/datetimepicker/res
timezonepicker_dir := ../../../../../../frameworks/opt/timezonepicker/res
res_dirs := $(chips_dir) $(color_picker_dir) $(datetimepicker_dir) $(timezonepicker_dir) res
src_dirs := src

LOCAL_JACK_COVERAGE_INCLUDE_FILTER := com.android.calendar.*

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under,$(src_dirs))

LOCAL_USE_AAPT2 := true
LOCAL_PRIVATE_PLATFORM_APIS := true
#ALLOW_MISSING_DEPENDENCIES=true
# bundled
#LOCAL_STATIC_JAVA_LIBRARIES += \
#        android-common \
#        libchips \
#        calendar-common

# unbundled
LOCAL_STATIC_JAVA_LIBRARIES := \
        org.apache.http.legacy \
        android-common \
        libchips \
        colorpicker \
        android-opt-datetimepicker \
        android-opt-timezonepicker \
        androidx.legacy_legacy-support-v4 \
        joda-time \
        joda-convert\
        gson \
        android-support-v7 \
        android-support-v7-appcompat \
        calendar-common \
        jxl1
#LOCAL_SDK_VERSION := current

LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))

LOCAL_PACKAGE_NAME := SprdCalendar
LOCAL_OVERRIDES_PACKAGES := Calendar
#LOCAL_DEX_PREOPT:=false
LOCAL_PROGUARD_FLAG_FILES := ../../../../../../frameworks/opt/datetimepicker/proguard.flags
LOCAL_PROGUARD_FLAG_FILES += proguard.flags

LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages com.android.ex.chips
LOCAL_AAPT_FLAGS += --extra-packages com.android.colorpicker
LOCAL_AAPT_FLAGS += --extra-packages com.android.datetimepicker
LOCAL_AAPT_FLAGS += --extra-packages com.android.timezonepicker

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := jxl1:libs/jxl.jar \
                                        joda-time:libs/joda-time-2.3.jar \
                                        joda-convert:libs/joda-convert-1.9.2.jar \
                                        gson:libs/gson-2.8.0.jar \
                                        android-support-v7:libs/android-support-v7-recyclerview.jar \

include $(BUILD_MULTI_PREBUILT)

# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
