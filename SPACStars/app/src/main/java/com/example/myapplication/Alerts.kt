package com.example.myapplication

import android.R.id.message
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.DialogFragment
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*


var alerthour = -1
var alertminute = -1
var setdate: Date = Date()
val c = Calendar.getInstance()
val dateformat = SimpleDateFormat("MM/dd/yyyy")
var alertsmap: MutableMap<Long, String> = mutableMapOf()

class Alerts : AppCompatActivity() {
    //Initialize values for the alert
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alerts)
        createNotificationChannel()

        //Update the titlebar from "SPAC Stars" to "Alerts"
        val titlebar: ActionBar? = supportActionBar
        if (titlebar != null) {
            titlebar.title = "Alerts"
        }
        readfile()
    }

    //https://developer.android.com/guide/topics/ui/controls/pickers#kotlin
    class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Use the current time as the default values for the picker
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)
            // Create a new instance of TimePickerDialog and return it
            return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
        }

        override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
            alerthour = hourOfDay
            alertminute = minute
            var displayhour = alerthour.toString()
            var displayminute = alertminute.toString()
            var AMorPM = " AM"
            if (alerthour >= 12){
                AMorPM = " PM"
                displayhour = (alerthour % 12).toString()
            }
            if(alerthour == 0 || alerthour == 12){
                displayhour = "12"
            }
            if(alertminute < 10){
                displayminute = "0$alertminute"
            }
            var timestring = "$displayhour:$displayminute$AMorPM"
            val timetext = this.activity?.findViewById<TextView>(R.id.SelectedTimeText)
            if (timetext != null) {
                timetext.text = timestring
            }
        }
    }

    fun showTimePickerDialog(v: View) {
        TimePickerFragment().show(supportFragmentManager, "timePicker")
    }

    class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Use the current date as the default date in the picker
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            val context: Context = this.requireContext()
            // Create a new instance of DatePickerDialog and return it
            return DatePickerDialog(context, this, year, month, day)
        }

        override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int){
            var alertmonth = month + 1
            var alertdaystring = "$day"
            if (day < 10) {
                alertdaystring = "0$day"
            }
            var alertmonthstring = "$alertmonth"
            if (alertmonth < 10) {
                alertmonthstring = "0$alertmonth"
            }
            val alertdatestring = "$alertmonthstring/$alertdaystring/$year"
            setdate = dateformat.parse(alertdatestring)
            val datetext = this.activity?.findViewById<TextView>(R.id.SelectedDateText)
            if (datetext != null) {
                datetext.text = alertdatestring
            }
        }
    }

    fun showDatePickerDialog(v: View) {
        val newFragment = DatePickerFragment()
        newFragment.show(supportFragmentManager, "datePicker")
    }

    //https://developer.android.com/training/notify-user/build-notification
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SPAC Alert Notification"
            val descriptionText = "Custom SPAC Alerts"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("spacstarchannel", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    class timedNotification : BroadcastReceiver(){
        var notiftext = "SPAC Alert!"
        override fun onReceive(context: Context, intent: Intent?) {
            notiftext = intent?.extras?.get("notiftext").toString()
            val builder : NotificationCompat.Builder = NotificationCompat.Builder(context, "spacstarchannel")
                    .setSmallIcon(R.drawable.notif)
                    .setContentTitle("SPAC Alert!")
                    .setContentText(notiftext)

            with(NotificationManagerCompat.from(context)) {
                // notificationId is a unique int for each notification that you must define
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        }

    }

    //https://developer.android.com/reference/android/app/AlarmManager
    fun alarm(v: View){
        val minutetomil: Long = 60000
        val hourtomil: Long = minutetomil * 60
        val alerttime: Long = (setdate.time + (alerthour * hourtomil) + (alertminute * minutetomil))
        if(alerttime <= System.currentTimeMillis()){
            val alert: AlertDialog.Builder = AlertDialog.Builder(this)
            //Set the message of the alert window that appears
            alert.setMessage("Please select a date and time set in the future.")
            alert.setPositiveButton("OK"){ _, _ -> println("BAD TIME, ALARM SET TO PAST OR PRESENT")
            }

            alert.setTitle("Invalid Time")
            //Display the window to the user
            alert.create().show()
            return
        }
        if(!alertsmap[alerttime].isNullOrBlank()){
            val alert: AlertDialog.Builder = AlertDialog.Builder(this)
            //Set the message of the alert window that appears
            alert.setMessage("You already have an alert set at this time.")
            alert.setPositiveButton("OK"){ _, _ -> println("BAD TIME, PreExisting")
            }

            alert.setTitle("Invalid Time")
            //Display the window to the user
            alert.create().show()
            return
        }
        val alarmmanager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, timedNotification::class.java)
        var alertname = findViewById<TextView>(R.id.InputAlertNameButton).text.toString()
        intent.putExtra("notiftext", alertname.toString())
        //New intent code for different times
        val pending = PendingIntent.getBroadcast(this.applicationContext, alerttime.toInt(), intent, 0)
        alarmmanager.set(AlarmManager.RTC_WAKEUP, alerttime, pending)


        var displayhour = alerthour.toString()
        var displayminute = alertminute.toString()
        var AMorPM = " AM"
        if (alerthour >= 12){
            AMorPM = " PM"
            displayhour = (alerthour % 12).toString()
        }
        if(alerthour == 0 || alerthour == 12){
            displayhour = "12"
        }
        if(alertminute < 10){
            displayminute = "0$alertminute"
        }
        val stringtoadd = "${setdate.month+1}/${setdate.date}/${setdate.year+1900}\t$displayhour:$displayminute\t$AMorPM\t\t\t$alertname"
        alertsmap[alerttime] = stringtoadd
        writefile()
        updatethetable()
        println("Alarm Created")
    }

    fun writefile(){
        val alertsfile = File(filesDir, "Alerts")
        if(!alertsfile.exists()){
            alertsfile.mkdir()
        }
        try {
            val testfile = File(alertsfile, "alerts.txt")
            val filewrite = FileWriter(testfile)
            for(i in alertsmap)
            filewrite.append("${i.key},${i.value}\n")
            filewrite.flush()
            filewrite.close()
        } catch (e: Exception){println(e)}
    }

    fun readfile(){
        val filetoread = File(filesDir.path + "/Alerts/alerts.txt")
        try{
            val filereader = BufferedReader(FileReader(filetoread))
            var readtext = filereader.readLine()
            while(readtext != null){
                println(readtext)
                val table = findViewById<TableLayout>(R.id.alertslist)
                val newrow = TextView(applicationContext)
                val split = readtext.split(",")
                alertsmap[split[0].toLong()] = split[1]
                newrow.text = readtext.substringAfterLast(",")
                table.addView(newrow)
                readtext = filereader.readLine()
            }
        }catch(e: Exception){println(e)}
    }

    fun updatethetable(){
        val table = findViewById<TableLayout>(R.id.alertslist)
        table.removeAllViews()
        for(i in alertsmap){
            val rowtext = TextView(applicationContext)
            rowtext.text = i.value
            table.addView(rowtext)
        }
    }

}