package com.hx.infusionchairplateproject

import android.app.Activity
import android.util.Log

/**
 * 自定义Activity栈，用于统一管理所有Activity
 * 便于统一管理
 */
class ActivityTask {
    companion object{
        private var activities = mutableListOf<Activity>()

        fun addActivity(activity: Activity) {
            if (!activities.contains(activity)) {
                activities.add(activity)
            }
        }

        fun removeActivity(activity: Activity) {
            activities.remove(activity)
        }

        /**
         * 结束指定的Activity
         * @param activityName Activity 全类名
         */
        fun finishOneActivity(activityName: String) {
            for (activity in activities) {
                val name = activity.javaClass.name
                if (name == activityName) {
                    if(activity.isFinishing){
                        activities.remove(activity)
                    }else{
                        activity.finish()
                        activities.remove(activity)
                    }
                }
            }
        }
    }
}