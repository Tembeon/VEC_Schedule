package com.example.vec_schedule

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.internal.NavigationMenuView
import com.google.android.material.navigation.NavigationView
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.fragment_bottom_navigation_drawer_settings.*
import org.jetbrains.anko.support.v4.browse
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast
import tem.apps.vec_schedule.SettingsActivity


public class mainkt {


}

class BottomNavigationDrawerFragmentSettings: BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bottom_navigation_drawer_settings, container, false)



    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)



        navigation_view.setNavigationItemSelectedListener { menuItem ->
            // Bottom Navigation Drawer menu item clicks
            when (menuItem.itemId) {
                R.id.nav1 -> browse("https://vk.com/temapps")
               
                R.id.app_bar_switch -> toast("Nothing here")

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


