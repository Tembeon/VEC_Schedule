package tem.apps.vec_schedule

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.chibatching.kotpref.Kotpref
import com.chibatching.kotpref.KotprefModel
import tem.apps.vec_schedule.R
import tem.apps.vec_schedule.R.drawable.*
import tem.apps.vec_schedule.R.menu.bottomappbar_menu_primary
import tem.apps.vec_schedule.R.menu.bottomappbar_menu_secondary
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.textView
import org.jetbrains.anko.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*



class MainActivity : AppCompatActivity() {
    object Settings : KotprefModel() {
        var check_off: String by stringPref("false")
        var last_checked_day: String by stringPref("1.1.2000")
        var first_join : String by stringPref("")
    }



    private var currentFabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
    var schedule_url_tomorrow = "https://"
    var schedule_url = "https://"
    var answer = "https://"
    var answer_tomorrow = "https://"
    var answer_tomorrow_check = "https://"
    var day = "1"
    var month = "1"
    var year = "2000"
    var today_str = ""
    var schedule_url_data = "01.01.2000"
    var max_date_int = (System.currentTimeMillis() + 2629743000)
    var permission = 0
    var RECORD_REQUEST_CODE = 0
    var is_schedule = false
    var is_fab_red = false
    var hard_visible = false
    var hour = "0"
    var fab_status = 0

    //SharedPreferences variables





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

    private fun makeShortNotification(title: String, body: String, Id : Int, is_ongoing: Boolean): Notification.Builder? {
        val resultIntent =  Intent(this, MainActivity::class.java)
        val resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //create channel

            val mNotificationManager: NotificationManager by lazy {
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }

            // Create the channel object with the unique ID FOLLOWERS_CHANNEL
            val shortChannel = NotificationChannel(
                    "tem.apps.vec_schedule.check_alert",
                    "Важные уведомления",
                    NotificationManager.IMPORTANCE_DEFAULT)
            // Configure the channel's initial settings
            shortChannel.lightColor = Color.GREEN
            shortChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 500, 200, 500)
            // Submit the notification channel object to the notification manager
            mNotificationManager.createNotificationChannel(shortChannel)

            //create notification
            val notification = Notification.Builder(applicationContext, "tem.apps.vec_schedule.check_alert")
                    .setContentTitle(title)
                    .setColor(Color.parseColor("#123676"))
                    .setContentText(body)
                    .setSmallIcon(ic_notifcation_alert)
                    .setAutoCancel(true)
                    .setContentIntent(resultPendingIntent)
                    .setOngoing(is_ongoing)


            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(Id, notification.build())
        } else {
            //for Android 7-

            val notification = NotificationCompat.Builder(this)
                    .setSmallIcon(ic_notifcation_alert)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setColor(Color.parseColor("#123676"))
                    .setOngoing(is_ongoing)
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build()
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(Id, notification)

        }
        return null
    }

    private fun makeLongNotification(title: String, body: String, Id : Int, is_ongoing: Boolean): Notification.Builder? {
        val resultIntent =  Intent(this, MainActivity::class.java)
        val resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //create channel

            val mNotificationManager: NotificationManager by lazy {
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }

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

            //create notification
            val notification = Notification.Builder(applicationContext, "tem.apps.vec_schedule.check")
                    .setContentTitle(title)
                    .setContentText(body)
                    .setColor(Color.parseColor("#123676"))
                    .setSmallIcon(ic_notifcation_silent)
                    .setAutoCancel(false)
                    .setContentIntent(resultPendingIntent)
                    .setOngoing(is_ongoing)


            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(Id, notification.build())
        } else {
            //for Android 7-


            val notification = NotificationCompat.Builder(this)
                    .setSmallIcon(ic_notifcation_silent)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(false)
                    .setColor(Color.parseColor("#123676"))
                    .setOngoing(is_ongoing)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .build()
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(Id, notification)

        }
        return null
    }

    private fun applicationTutorial(){
        var mFabPrompt: MaterialTapTargetPrompt? = null
        mFabPrompt = MaterialTapTargetPrompt.Builder(this)
                .setTarget(findViewById<View>(R.id.fab))
                .setPrimaryText("Просмотр расписания на завтра")
                .setSecondaryText("Нажми, чтобы посмотреть расписание на завтра. Нажми ещё раз, чтобы вернуть расписание на сегодня")
                .setAnimationInterpolator(FastOutSlowInInterpolator())
                .setPromptStateChangeListener { prompt, state ->
                    if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                        mFabPrompt = MaterialTapTargetPrompt.Builder(this)
                                .setTarget(text_placeholder)
                                .setPrimaryText("Здесь показывается расписание")
                                .setSecondaryText("Небольшая информация: после 5 часов вечера здесь показывается расписание на завтра автоматически. Также на выходных приложение показывает тебе расписание на понедельник")
                                .setAnimationInterpolator(FastOutSlowInInterpolator())
                                .setPromptStateChangeListener { prompt, state ->
                                    if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                                        mFabPrompt = MaterialTapTargetPrompt.Builder(this)
                                                .setTarget(bottom_app_bar)
                                                .setPrimaryText("Навигационное меню")
                                                .setSecondaryText("Здесь можно настроить приложение, посмотреть расписание на нужную дату, а также поделиться им")
                                                .setAnimationInterpolator(FastOutSlowInInterpolator())
                                                .setPromptStateChangeListener { prompt, state ->
                                                    if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                                                        Settings.first_join = "long time ago"

                                                    }
                                                }
                                                .create()
                                        mFabPrompt!!.show()

                                    }
                                }
                                .create()
                        mFabPrompt!!.show()

                    }
                }
                .create()
        mFabPrompt!!.show()
    }


    private fun getData() {
        //get current data

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("d-L-yyyy")
            val hour_formatter = DateTimeFormatter.ofPattern("H")
            hour = current.format(hour_formatter)
            answer = current.format(formatter)

            schedule_url = ("http://energocollege.ru/vec_assistant/%D0%A0%D0%B0%D1%81%D0%BF%D0%B8%D1%81%D0%B0%D0%BD%D0%B8%D0%B5/".plus(answer.plus(".jpg")))
            web1.loadUrl(schedule_url)
        } else {
            var date = Date()
            val formatter = SimpleDateFormat("d-L-yyyy")
            val hour_formatter = SimpleDateFormat("H")
            answer = formatter.format(date)
            hour = hour_formatter.format(date)
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







            when (today_str) {
                "суббота" -> tomorrow = current.plusDays(2)
                "пятница" -> tomorrow = current.plusDays(3)
                else -> tomorrow = current.plusDays(1)
            }

            answer_tomorrow_check = tomorrow.format(formatter)
            answer_tomorrow = tomorrow.format(formatter)
            schedule_url_tomorrow = ("http://energocollege.ru/vec_assistant/%D0%A0%D0%B0%D1%81%D0%BF%D0%B8%D1%81%D0%B0%D0%BD%D0%B8%D0%B5/".plus(answer_tomorrow.plus(".jpg")))

        } else {
            var date_t = Date()
            today_str = SimpleDateFormat("EEEE").format(date_t)
            day = SimpleDateFormat("d").format(date_t)
            month = SimpleDateFormat("L").format(date_t)
            year = SimpleDateFormat("yyyy").format(date_t)



            when (today_str) {
                "суббота" -> day = (day.toInt() + 2).toString()
                "пятница" -> day = (day.toInt() + 3).toString()
                else -> day = (day.toInt() + 1).toString()
            }
            day.toString()

            answer_tomorrow_check  = (day.plus("-").plus(month).plus("-").plus(year))
            answer_tomorrow  = (day.plus("-").plus(month).plus("-").plus(year))
            schedule_url_tomorrow = ("http://energocollege.ru/vec_assistant/%D0%A0%D0%B0%D1%81%D0%BF%D0%B8%D1%81%D0%B0%D0%BD%D0%B8%D0%B5/".plus(answer_tomorrow.plus(".jpg")))


        }


        if (hour.toInt() > 16) {
            web1.loadUrl(schedule_url_tomorrow)
            fab.setImageResource(ic_arrow_left_white)
            fab_status = 1

        } else {
            when (today_str) {
                "суббота" -> {
                    fab.setImageResource(ic_arrow_left_white)
                    fab_status = 1
                    web1.loadUrl(schedule_url_tomorrow)
                }
                "воскрсенье" -> {
                    fab.setImageResource(ic_arrow_left_white)
                    fab_status = 1
                    web1.loadUrl(schedule_url_tomorrow)
            }
            }
        }


    }


    private fun displayMaterialSnackBar(text : String, length : Int) {
        val marginSide = 10
        val marginBottom = 150
        val snackbar = Snackbar.make(
                web1,
                text,
                length
        )
        val snackbarView = snackbar.view
        val params = snackbarView.layoutParams as CoordinatorLayout.LayoutParams

        params.setMargins(
                params.leftMargin + marginSide,
                params.topMargin,
                params.rightMargin + marginSide,
                params.bottomMargin + marginBottom

        )



        snackbarView.layoutParams = params
        snackbar.show()
    }

    fun onFabRedAnimation() {
        val colorFrom = resources.getColor(R.color.fab_color)
        val colorTo = resources.getColor(R.color.red)
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




    override fun onStart() {
        super.onStart()
        getData()
        //DON'T FORGET ABOUT CANCEL SCHEDULE NOTIFICATION
        stopService(intent)

    }



    override fun onStop() {
        super.onStop()

        //start background check
        if (Settings.check_off == "true") {

            makeLongNotification("Проверка расписания", "Нажмите, чтобы открыть приложение", 1, true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            }
            val timer = Timer()

            val task = object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        getData()
                        web_check.loadUrl(schedule_url_tomorrow)


                    }
                    }
            }
            timer.schedule(task, 0, 60000)

            /*intent = Intent(this, CheckService::class.java)
            startService(intent)*/
        } else {
            notificationManager.cancelAll()
        }




    }

    override fun onDestroy() {
        super.onDestroy()
        if (Settings.check_off == "true") {
        makeLongNotification("Что-то не так", "Приложение перестало проверять расписание", 1, true)
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Kotpref.init(this)

        if (Settings.first_join == ""){
            applicationTutorial()
        }


        //default WebView for content
        web1.webViewClient = object : WebViewClient() {


            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                //animate FAB color
                val colorFrom = resources.getColor(R.color.fab_color)
                val colorTo = resources.getColor(R.color.red)

                if (web1.title.contains((answer))) {
                    web1.visibility = View.VISIBLE
                    textView.visibility = View.GONE
                    is_schedule = true

                    if (is_fab_red) {
                        onFabRedAnimation()

                    }
                } else {
                    if (web1.title.contains(answer_tomorrow)) {
                        web1.visibility = View.VISIBLE
                        textView.visibility = View.GONE
                        is_schedule = true
                        Settings.last_checked_day = answer_tomorrow
                        if (is_fab_red) {
                            onFabRedAnimation()

                        }
                    } else {
                        if (web1.title.contains(schedule_url_data)){
                            web1.visibility = View.VISIBLE
                            textView.visibility = View.GONE
                            is_schedule = true

                            if (is_fab_red) {
                                onFabRedAnimation()
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

                            //animate to red
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


        //WebView for background check
        web_check?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)

                if (web_check.title!!.contains((answer))) {
                    //nothing to do. Just founded schedule for today
                } else {
                    if (web_check.title!!.contains(answer_tomorrow_check)) {
                        if (Settings.last_checked_day != answer_tomorrow_check) {
                            Settings.last_checked_day = answer_tomorrow_check
                            makeShortNotification("Найдено расписание", "Нажмите, чтобы открыть приложение", 1, false)
                            stopService(intent)

                        }
                    } else {
                        if (web_check.title!!.contains(schedule_url_data)) {
                            //nothing to do. Just founded schedule by calendar data
                        } else {
                            // "Расписание не найдено"
                        }
                    }
                }
            }


        }






            setSupportActionBar(bottom_app_bar)

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

/*
class CheckService : Service() {
    var schedule_url_tomorrow = "https://"
    var answer = "https://"
    var answer_tomorrow = "https://"
    var answer_tomorrow_check = "https://"
    var day = "1"
    var month = "1"
    var year = "2000"
    var today_str = ""
    var schedule_url_data = "01.01.2000"


    private val TAG = "CheckService"


        //HERE

    private val web_check by lazy {
         webView()
     }




    override fun onCreate() {

        Log.i(TAG, "Service onCreate")
        makeLongNotification("Проверка расписания", "Нажмите, чтобы открыть приложение", 1, true)
        web_check.loadUrl("https://google.com")
        val timer = Timer()

        val task = object : TimerTask() {
            override fun run() {
                getData()
                web_check.loadUrl(schedule_url_tomorrow)
                makeShortNotification("onCreate", "Short", 2, false)
            }
        }
        timer.schedule(task, 0, 60000)


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.i(TAG, "Service onStartCommand $startId")




        //WebView for background check
        web_check?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                makeLongNotification("onPageFinished", "longNotify", 4, false)
                makeShortNotification("onPageFinished", "ShortNotify", 3, false)
                if (web_check?.title!!.contains((answer))) {
                    //nothing to do. Just founded schedule for today
                } else {
                    if (web_check?.title!!.contains(answer_tomorrow_check)) {
                        if (MainActivity.Settings.last_checked_day !== answer_tomorrow_check) {
                            makeShortNotification("Найдено расписание", "Нажмите, чтобы открыть приложение", 1, false)
                            stopService(intent)
                            MainActivity.Settings.last_checked_day = answer_tomorrow_check
                        }
                    } else {
                        if (web_check?.title!!.contains(schedule_url_data)) {
                            //nothing to do. Just founded schedule by calendar data
                        } else {
                            // "Расписание не найдено"
                        }
                    }
                }
            }


        }


        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.i(TAG, "Service onBind")
        return null
    }

    override fun onDestroy() {
        Log.i(TAG, "Service onDestroy")

    }

    //my fun


    private fun makeLongNotification(title: String, body: String, Id : Int, is_ongoing: Boolean): Notification.Builder? {
        val resultIntent =  Intent(this, MainActivity::class.java)
        val resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //create channel

            val mNotificationManager: NotificationManager by lazy {
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }

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

            //create notification
            val notification = Notification.Builder(applicationContext, "tem.apps.vec_schedule.check")
                    .setContentTitle(title)
                    .setContentText(body)
                    .setColor(Color.parseColor("#123676"))
                    .setSmallIcon(ic_notifcation_silent)
                    .setAutoCancel(false)
                    .setContentIntent(resultPendingIntent)
                    .setOngoing(is_ongoing)


            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(Id, notification.build())
        } else {
            //for Android 7-

            val notification = NotificationCompat.Builder(this)
                    .setSmallIcon(ic_notifcation_silent)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(false)
                    .setColor(Color.parseColor("#123676"))
                    .setOngoing(is_ongoing)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .build()
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(Id, notification)

        }
        return null
    }

    private fun makeShortNotification(title: String, body: String, Id : Int, is_ongoing: Boolean): Notification.Builder? {
        val resultIntent =  Intent(this, MainActivity::class.java)
        val resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //create channel

            val mNotificationManager: NotificationManager by lazy {
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }

            // Create the channel object with the unique ID FOLLOWERS_CHANNEL
            val shortChannel = NotificationChannel(
                    "tem.apps.vec_schedule.check_alert",
                    "Важные уведомления",
                    NotificationManager.IMPORTANCE_DEFAULT)
            // Configure the channel's initial settings
            shortChannel.lightColor = Color.GREEN
            shortChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 500, 200, 500)
            // Submit the notification channel object to the notification manager
            mNotificationManager.createNotificationChannel(shortChannel)

            //create notification
            val notification = Notification.Builder(applicationContext, "tem.apps.vec_schedule.check_alert")
                    .setContentTitle(title)
                    .setColor(Color.parseColor("#123676"))
                    .setContentText(body)
                    .setSmallIcon(ic_notifcation_alert)
                    .setAutoCancel(true)
                    .setContentIntent(resultPendingIntent)
                    .setOngoing(is_ongoing)


            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(Id, notification.build())
        } else {
            //for Android 7-

            val notification = NotificationCompat.Builder(this)
                    .setSmallIcon(ic_notifcation_alert)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setColor(Color.parseColor("#123676"))
                    .setOngoing(is_ongoing)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build()
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(Id, notification)

        }
        return null
    }

    private fun getData() {
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
            answer_tomorrow_check = tomorrow.format(formatter)
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

            answer_tomorrow_check  = (day.plus("-").plus(month).plus("-").plus(year))
            answer_tomorrow  = (day.plus("-").plus(month).plus("-").plus(year))
            schedule_url_tomorrow = ("http://energocollege.ru/vec_assistant/%D0%A0%D0%B0%D1%81%D0%BF%D0%B8%D1%81%D0%B0%D0%BD%D0%B8%D0%B5/".plus(answer_tomorrow.plus(".jpg")))


        }
    }
}
*/