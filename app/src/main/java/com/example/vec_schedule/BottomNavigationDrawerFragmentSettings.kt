package com.example.vec_schedule

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.chibatching.kotpref.KotprefModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.internal.NavigationMenuView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.fragment_bottom_navigation_drawer_settings.*
import org.jetbrains.anko.support.v4.browse
import org.jetbrains.anko.support.v4.share


class InjectableContextSamplePref(context: Context) : KotprefModel(context) {
    object Settings : KotprefModel() {
        var check_off: String by stringPref("false")
        var last_checked_day: String by stringPref("1.1.2000")
    }

    var sampleData by stringPref()
    //SharedPreferences

}

class BottomNavigationDrawerFragmentSettings: BottomSheetDialogFragment() {



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bottom_navigation_drawer_settings, container, false)



    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //Make switch checked when option is active
        if (InjectableContextSamplePref.Settings.check_off == "true") {
            switch_background_check!!.isChecked = true
        }

        switch_background_check.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                //Enable background check
                InjectableContextSamplePref.Settings.check_off = "true"
            } else {
                //Disable background check
                InjectableContextSamplePref.Settings.check_off = "false"

            }
        }


        navigation_view.setNavigationItemSelectedListener { menuItem ->
            // Bottom Navigation Drawer menu item clicks
            when (menuItem.itemId) {
                R.id.nav1 -> browse("https://vk.com/temapps")
                R.id.nav2 -> browse("https://t.me/tem_apps")
                R.id.nav3 -> share("ВЭК Расписание - приложение для просмотра расписания пар" +
                        " для ВЭКа с возможностью отслеживания выхода расписаний и получения" +
                        " уведомления, если расписание найдено." +
                        "\n\nПодробнее:\nhttps://tem-apps.web.app/vec_schedule.html" +
                        "\n\nСкачать приложение:\nhttps://tem-apps.web.app/vec_schedule/latest.html")

            }

            true
        }



        disableNavigationViewScrollbars(navigation_view)

    }


}

    fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialog = onCreateDialog(savedInstanceState) as BottomSheetDialog

    dialog.setOnShowListener { dialog ->
        val d = dialog as BottomSheetDialog

        val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?


    }

    return dialog
}

    private fun disableNavigationViewScrollbars(navigationView: NavigationView?) {
        val navigationMenuView = navigationView?.getChildAt(0) as NavigationMenuView
        navigationMenuView.isVerticalScrollBarEnabled = false
    }


