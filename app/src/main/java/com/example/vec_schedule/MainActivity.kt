package com.example.vec_schedule

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.example.vec_schedule.R.drawable.*
import com.example.vec_schedule.R.menu.bottomappbar_menu_primary
import com.example.vec_schedule.R.menu.bottomappbar_menu_secondary
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.schedule


class CheckService : Service() {

    private var notificationManager: NotificationManager? = null
    val web1 : WebView? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        toast("Started Service")

        web1?.loadUrl("https://google.com")
        Timer("SettingUp", false).schedule(5000, 300000) {

            checkLessons()
            makeNotification()

        }




        Log.i("Service", "HERE HERE HERE HERE HERE HELLO WORLD")
        return START_STICKY

    }

    override fun onDestroy() {
        super.onDestroy()


       toast("Service destroy")

    }

    fun sendNitificationr(title: String, body: String, Id : Int, is_ongoing: Boolean): Notification.Builder? {
        val resultIntent =  Intent(this, MainActivity::class.java)
        val resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
               PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notification = Notification.Builder(applicationContext, "tem.apps.vec_schedule.check")
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(ic_calendar)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setOngoing(is_ongoing)


            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(Id, notification.build())
    } else {
            //for 7-
        }
        return null
    }


    private fun checkLessons() {


        //background check 4 schedule
        var schedule_url_tomorrow = "https://"
        var schedule_url = "https://"
        var answer = "https://"
        var answer_tomorrow = "https://"
        var day = "1"
        var month = "1"
        var year = "2000"
        var today_str = ""
        var schedule_url_data = "01.01.2000"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("d-L-yyyy")
            answer = current.format(formatter)

            schedule_url = ("http://energocollege.ru/vec_assistant/%D0%A0%D0%B0%D1%81%D0%BF%D0%B8%D1%81%D0%B0%D0%BD%D0%B8%D0%B5/".plus(answer.plus(".jpg")))
            web1?.loadUrl(schedule_url)
        } else {
            var date = Date()
            val formatter = SimpleDateFormat("d-L-yyyy")
            answer = formatter.format(date)

            schedule_url = ("http://energocollege.ru/vec_assistant/%D0%A0%D0%B0%D1%81%D0%BF%D0%B8%D1%81%D0%B0%D0%BD%D0%B8%D0%B5/".plus(answer.plus(".jpg")))
            web1?.loadUrl(schedule_url)

        }
        //get tomorrow data


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            var tomorrow = current.plusDays(0)
            val formatter = DateTimeFormatter.ofPattern("d-L-yyyy")

            var date = Date()
            today_str = SimpleDateFormat("EEEE").format(date)





            if (today_str == "суббота") {
                tomorrow = current.plusDays(2)
            } else {
                if (today_str == "пятница") {
                    tomorrow = current.plusDays(3)
                } else {
                    tomorrow = current.plusDays(1)
                }
            }
            answer_tomorrow = tomorrow.format(formatter)

            schedule_url_tomorrow = ("http://energocollege.ru/vec_assistant/%D0%A0%D0%B0%D1%81%D0%BF%D0%B8%D1%81%D0%B0%D0%BD%D0%B8%D0%B5/".plus(answer_tomorrow.plus(".jpg")))

        } else {
            var date = Date()
            today_str = SimpleDateFormat("EEEE").format(date)
            day = SimpleDateFormat("d").format(date)
            month = SimpleDateFormat("L").format(date)
            year = SimpleDateFormat("yyyy").format(date)

            if (today_str == "суббота") {
                day = (day.toInt() + 2).toString()
            } else {
                if (today_str == "пятница") {
                    day = (day.toInt() + 3).toString()
                } else {
                    day = (day.toInt() + 1).toString()
                }
            }
            day.toString()

            answer_tomorrow = (day.plus("-").plus(month).plus("-").plus(year))

            schedule_url_tomorrow = ("http://energocollege.ru/vec_assistant/%D0%A0%D0%B0%D1%81%D0%BF%D0%B8%D1%81%D0%B0%D0%BD%D0%B8%D0%B5/".plus(answer_tomorrow.plus(".jpg")))








            web1?.webViewClient = object : WebViewClient() {


                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)


                    if (web1?.title!!.contains((answer))) {
                        //nothing to do. Just founded schedule for today


                    } else {
                        if (web1?.title!!.contains(answer_tomorrow)) {
                            makeNotification()


                        } else {
                            if (web1?.title!!.contains(schedule_url_data)) {
                                //nothing to do. Just founded schedule by calendar data


                            } else {
                                // "Расписание не найдено"
                            }
                        }
                    }
                }


            }
        }
    }

    fun makeNotification(){

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val notification = NotificationCompat.Builder(this)
                    .setSmallIcon(ic_calendar)
                    .setContentTitle("ВЭК Расписание")
                    .setContentText("Найдено новое расписание")
                    .build()
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(1, notification)

        } else {

            val mNotificationManager: NotificationManager by lazy {
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            /**
             * Registers notification channels, which can be used later by individual notifications.
             */

                // Create the channel object with the unique ID FOLLOWERS_CHANNEL
                val followersChannel = NotificationChannel(
                        "tem.apps.vec_schedule.check",
                        "Основные уведомления",
                        NotificationManager.IMPORTANCE_MIN)
                // Configure the channel's initial settings
                followersChannel.lightColor = Color.GREEN
                followersChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 500, 200, 500)
                // Submit the notification channel object to the notification manager
                mNotificationManager.createNotificationChannel(followersChannel)

            sendNitificationr("Проверка расписания", "Нажмите, чтобы открыть приложение", 1, true)


        }



    }


}


class MainActivity : AppCompatActivity() {




    private var currentFabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
    var schedule_url_tomorrow = "https://"
    var schedule_url = "https://"
    var answer = "https://"
    var answer_tomorrow = "https://"
    var day = "1"
    var month = "1"
    var year = "2000"
    var today_str = ""
    var schedule_url_data = "01.01.2000"
    var max_date_int = (System.currentTimeMillis() + 2629743000)
    var permission = 0
    var RECORD_REQUEST_CODE = 0
    var is_schedule = false
    var its = "empty"
    var is_fab_red = false
    var it = ""
    var hard_visible = false
    var is_web_changed = false



    private fun setupPermissions() {
        permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                RECORD_REQUEST_CODE)

        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                RECORD_REQUEST_CODE)
    }

    private fun injectCSS() {
        try {
            val inputStream = assets.open("style.css")
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()
            val encoded = Base64.encodeToString(buffer , Base64.NO_WRAP)
            web1.loadUrl(
                    "javascript:(function() {" +
                            "var parent = document.getElementsByTagName('head').item(0);" +
                            "var style = document.createElement('style');" +
                            "style.type = 'text/css';" +
                            // Tell the browser to BASE64-decode the string into your script !!!
                            "style.innerHTML = window.atob('" + encoded + "');" +
                            "parent.appendChild(style)" +
                            "})()"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    override fun onStart() {
        super.onStart()

        //todo
    }

    override fun onStop() {
        super.onStop()

        this.startService(Intent(this, CheckService::class.java))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)





        //get current data

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("d-L-yyyy")
            answer = current.format(formatter)

            schedule_url = ("http://energocollege.ru/vec_assistant/%D0%A0%D0%B0%D1%81%D0%BF%D0%B8%D1%81%D0%B0%D0%BD%D0%B8%D0%B5/".plus(answer.plus(".jpg")))
            web1.loadUrl(schedule_url)
        } else {
            var date = Date()
            val formatter = SimpleDateFormat("d-L-yyyy")
            answer = formatter.format(date)

            schedule_url = ("http://energocollege.ru/vec_assistant/%D0%A0%D0%B0%D1%81%D0%BF%D0%B8%D1%81%D0%B0%D0%BD%D0%B8%D0%B5/".plus(answer.plus(".jpg")))
            web1.loadUrl(schedule_url)

        }
                //get tomorrow data


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            var tomorrow = current.plusDays(0)
            val formatter = DateTimeFormatter.ofPattern("d-L-yyyy")

            var date = Date()
            today_str = SimpleDateFormat("EEEE").format(date)





            if (today_str == "суббота") {
                 tomorrow = current.plusDays(2)
            } else {
                if (today_str == "пятница") {
                     tomorrow = current.plusDays(3)
                } else {
                    tomorrow = current.plusDays(1)
                }
            }
            answer_tomorrow = tomorrow.format(formatter)

            schedule_url_tomorrow = ("http://energocollege.ru/vec_assistant/%D0%A0%D0%B0%D1%81%D0%BF%D0%B8%D1%81%D0%B0%D0%BD%D0%B8%D0%B5/".plus(answer_tomorrow.plus(".jpg")))

        } else {
            var date = Date()
            today_str = SimpleDateFormat("EEEE").format(date)
            day = SimpleDateFormat("d").format(date)
            month = SimpleDateFormat("L").format(date)
            year = SimpleDateFormat("yyyy").format(date)

            if (today_str == "суббота") {
                day = (day.toInt() + 2).toString()
            } else {
                if (today_str == "пятница") {
                    day = (day.toInt() + 3).toString()
                } else {
                    day = (day.toInt() + 1).toString()
                }
            }
            day.toString()

            answer_tomorrow  = (day.plus("-").plus(month).plus("-").plus(year))

             schedule_url_tomorrow = ("http://energocollege.ru/vec_assistant/%D0%A0%D0%B0%D1%81%D0%BF%D0%B8%D1%81%D0%B0%D0%BD%D0%B8%D0%B5/".plus(answer_tomorrow.plus(".jpg")))


        }




        web1.webViewClient = object : WebViewClient() {


            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                injectCSS()









                val colorFrom = resources.getColor(R.color.fab_color)
                val colorTo = resources.getColor(R.color.red)

                if (web1.title.contains((answer))) {
                    web1.visibility = View.VISIBLE
                    textView.visibility = View.GONE
                    is_schedule = true


                    if (is_fab_red) {
                        val valueAnimator = ValueAnimator.ofObject(ArgbEvaluator(), colorTo, colorFrom)
                        valueAnimator.duration = 600

                        valueAnimator.addUpdateListener {

                            val value = it.animatedValue as Int

                            fab.backgroundTintList = ColorStateList.valueOf(value)
                        }
                        valueAnimator.start()

                        fab.setImageResource(ic_arrow_right_white)
                        is_fab_red = false
                    }


                    } else {
                    if (web1.title.contains(answer_tomorrow)) {
                        web1.visibility = View.VISIBLE
                        textView.visibility = View.GONE
                        is_schedule = true

                        if (is_fab_red) {
                            val valueAnimator = ValueAnimator.ofObject(ArgbEvaluator(), colorTo, colorFrom)
                            valueAnimator.duration = 600

                            valueAnimator.addUpdateListener {

                                val value = it.animatedValue as Int

                                fab.backgroundTintList = ColorStateList.valueOf(value)
                            }
                            valueAnimator.start()

                            fab.setImageResource(ic_arrow_right_white)
                            is_fab_red = false
                        }

                    } else {
                        if (web1.title.contains(schedule_url_data)){
                            web1.visibility = View.VISIBLE
                            textView.visibility = View.GONE
                            is_schedule = true

                            if (is_fab_red) {
                                val valueAnimator = ValueAnimator.ofObject(ArgbEvaluator(), colorTo, colorFrom)
                                valueAnimator.duration = 600

                                valueAnimator.addUpdateListener {

                                    val value = it.animatedValue as Int

                                    fab.backgroundTintList = ColorStateList.valueOf(value)
                                }
                                valueAnimator.start()

                                fab.setImageResource(ic_arrow_right_white)
                                is_fab_red = false
                            }

                        } else {
                            if (hard_visible) {
                                web1.visibility = View.VISIBLE
                                textView.visibility = View.GONE
                                is_schedule = true
                            } else {
                            }
                        web1.visibility = View.GONE
                        textView.visibility = View.VISIBLE
                            textView.text = "Расписание не найдено"
                            is_schedule = false



                            val valueAnimator = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
                            valueAnimator.duration = 400

                            valueAnimator.addUpdateListener {

                                val value = it.animatedValue as Int

                                fab.backgroundTintList = ColorStateList.valueOf(value)
                            }
                            valueAnimator.start()

                            fab.setImageResource(ic_error_white)
                            is_fab_red = true
                        }

                    }

                }
            }

        }



            setSupportActionBar(bottom_app_bar)
            var fab_status = 0


            val webSettings = web1.settings
        webSettings.javaScriptEnabled = true
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
            webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false



            val addVisibilityChanged: FloatingActionButton.OnVisibilityChangedListener = object : FloatingActionButton.OnVisibilityChangedListener() {
                override fun onHidden(fab: FloatingActionButton?) {
                    super.onHidden(fab)
                    bottom_app_bar.toggleFabAlignment()
                    bottom_app_bar.replaceMenu(
                            if (currentFabAlignmentMode == BottomAppBar.FAB_ALIGNMENT_MODE_CENTER) bottomappbar_menu_secondary
                            else bottomappbar_menu_primary
                    )
                    fab?.setImageDrawable(
                            if (currentFabAlignmentMode == BottomAppBar.FAB_ALIGNMENT_MODE_CENTER) getDrawable(ic_arrow_left_white)
                            else getDrawable(ic_arrow_right_white)
                    )
                    fab?.show()
                }
            }



            fab.setOnClickListener {
                fab_status++
                if (fab_status == 1) {
                    fab.setImageResource(ic_arrow_left_white)
                    textView.text = "Загрузка..."
                    web1.loadUrl(schedule_url_tomorrow)

                } else {
                    fab.setImageResource(ic_arrow_right_white)
                    textView.text = "Загрузка..."
                    web1.loadUrl(schedule_url)

                    fab_status = 0
                }

            }


        }



       private fun BottomAppBar.toggleFabAlignment() {
            currentFabAlignmentMode = fabAlignmentMode
            fabAlignmentMode = currentFabAlignmentMode.xor(1)
        }


        override fun onCreateOptionsMenu(menu: Menu): Boolean {
            val inflater = menuInflater
            inflater.inflate(R.menu.bottomappbar_menu_primary, menu)
            return true
        }




    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
            when (item!!.itemId) {
                android.R.id.home -> {
                    //menu (vk and settings)
                    val bottomNavDrawerFragment = BottomNavigationDrawerFragmentSettings()
                    bottomNavDrawerFragment.show(supportFragmentManager, bottomNavDrawerFragment.tag)
                }
                R.id.btn_calendar -> {
                    //calendar dialog

                    alert {
                        setTheme(R.style.MyAppTheme)
                        isCancelable = true

                        lateinit var datePicker: DatePicker

                        customView {
                            verticalLayout {
                                datePicker = datePicker {
                                    maxDate = max_date_int

                                }
                            }
                        }

                        yesButton {
                            schedule_url_data = "${datePicker.dayOfMonth}-${datePicker.month + 1}-${datePicker.year}"
                            textView.text = "Загрузка..."
                            web1.loadUrl("http://energocollege.ru/vec_assistant/%D0%A0%D0%B0%D1%81%D0%BF%D0%B8%D1%81%D0%B0%D0%BD%D0%B8%D0%B5/".plus(schedule_url_data.plus(".jpg")))
                        }



                    }.show()
                }
                R.id.btn_share -> {
                    //share image
                    makeRequest()
                    setupPermissions()

                        if (permission != PackageManager.PERMISSION_GRANTED) {
                            toast("Предоставьте разрешение для записи, чтобы делиться расписанием с другими") } else {

                            if (is_schedule) {
                                DownloadAndSaveImageTask(this).execute(web1.url)
                                val builder = StrictMode.VmPolicy.Builder()
                                StrictMode.setVmPolicy(builder.build())
                                //cruth code for share img
                                val shareIntent = Intent()
                                shareIntent.action = Intent.ACTION_SEND
                                toast(Environment.getExternalStorageDirectory()?.toString() + "/TemApps/img.jpg")
                                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + Environment.getExternalStorageDirectory()?.toString() + "/TemApps/img.jpg"))
                                shareIntent.type = "image/*"
                                startActivity(Intent.createChooser(shareIntent, "Поделиться через:"))
                            } else {
                                toast("На данный день нет расписания, отправлять нечего")
                            }
                        }
                }
                R.id.btn_times -> {
                    val bottomNavDrawerFragment = BottomNavigationDrawerFragment()
                    bottomNavDrawerFragment.show(supportFragmentManager, bottomNavDrawerFragment.tag)

                }
            }
            return true
        }



}

class DownloadAndSaveImageTask(context: Context) : AsyncTask<String, Unit, Unit>() {
    private var mContext: WeakReference<Context> = WeakReference(context)

    override fun doInBackground(vararg params: String?) {


        val url = params[0]
        val requestOptions = RequestOptions().override(100)
                .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
                .skipMemoryCache(true)


        mContext.get()?.let {
            val bitmap = Glide.with(it)
                    .asBitmap()
                    .load(url)
                    .apply(requestOptions)
                    .submit()
                    .get()


            try {
                var file = File(Environment.getExternalStorageDirectory(), "/TemApps")
                if (!file.exists()) {
                    file.mkdir()
                }
                file = File(file, "img.jpg")

                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()

                //share


                Log.i("ShareImage", "Image saved.")



            } catch (e: Exception) {
                Log.i("ShareImage", "Failed to save image.")
            }
        }
    }
}


