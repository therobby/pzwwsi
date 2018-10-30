package com.tuxdev.pzwwsi

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_schedule.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.support.v4.runOnUiThread
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class ScheduleFragment : Fragment() {

    private var day = 0
    private var meeting = 1
    private var forceRefresh = false
    private val time = arrayListOf("8:00 - 8:45",
            "8:50 - 9:35",
            "9:35 - 10:30",
            "10:35 - 11:20",
            "11:45 - 12:30",
            "12:35 - 13:20",
            "13:30 - 14:15",
            "14:20 - 15:05",
            "15:15 - 16:00",
            "16:15 - 17:00",
            "17:05 - 17:50",
            "18:05 - 18:50",
            "18:55 - 19:40",
            "19:55 - 10:40",
            "20:45 - 21:30")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    private fun emptyHour(hour: String): LinearLayout {
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.HORIZONTAL
        val text = TextView(requireContext())
        text.text = hour
        text.textSize = 18f

        layout.addView(text)
        return layout
    }

    private fun currentDay(): Int {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.time = Date()
        val currdaynr = calendar.get(Calendar.DAY_OF_WEEK)

        Log.e("S", currdaynr.toString())

        return when (currdaynr) {
            1 -> 0
            2 -> 0
            3 -> 1
            4 -> 2
            5 -> 3
            6 -> 0
            7 -> 0
            else -> 4
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loading = ProgressBar(requireContext())
        loading.isIndeterminate = true
        schedule_scroll.addView(loading)

        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.time = Date()
        var currdaynr = calendar.get(Calendar.DAY_OF_WEEK)

        Log.e("S", currdaynr.toString())
        val add = if (currdaynr == 1 || currdaynr > 6) 1 else 0

        currdaynr = when (currdaynr) {
            1 -> 0
            2 -> 0
            3 -> 1
            4 -> 2
            5 -> 3
            6 -> 0
            7 -> 0
            else -> 4
        }


        val nr = Main.studentWebsiteConnection.getCurrentMeet() + add

        day = currdaynr
        meeting = nr

        schedule_next.setOnClickListener {
            if (day >= 3) {
                if (meeting < 15) {
                    meeting++
                    day = 0
                }
            } else
                day++
            updateData()
            forceRefresh = true
        }

        schedule_prew.setOnClickListener {
            if (day <= 0) {
                if (meeting > 1) {
                    meeting--
                    day = 3
                }
            } else
                day--
            updateData()
            forceRefresh = true
        }

        updateData()
        refresh()
    }

    private fun updateData() {
        schedule_current_meeting.text = ("${resources.getString(R.string.meeting)} $meeting")
        schedule_current_day.text = when (day) {
            0 -> resources.getString(R.string.Monday)
            1 -> resources.getString(R.string.Tuesday)
            2 -> resources.getString(R.string.Wednesday)
            else -> resources.getString(R.string.Friday)
        }
    }

    private fun displaySchedule() {
        val days = if (Main.studentWebsiteConnection.isScheduleLoaded())
            Main.studentWebsiteConnection.getScheduleDays(Main.studentWebsiteConnection.getStudentGroup())
        else {
            Main.studentWebsiteConnection.loadSchedule()
            Main.studentWebsiteConnection.getScheduleDays(Main.studentWebsiteConnection.getStudentGroup())
        }

        val today = ArrayList<Pair<Lecture, Int>>()
        val toDisplay = ArrayList<View>()

        Log.e("Schedule", "Day: $day")
        //Log.e("Schedule", days!!.size.toString())

        time.forEach {
            toDisplay.add(emptyHour(it))
        }

        try {
            days!![day].plan.forEach { plan ->
                plan.value.forEach { lect ->
                    if (lect.meet.contains("-")) {
                        val meet = lect.meet.split("-")
                        if (meeting >= meet.first().toInt() && meeting <= meet.last().toInt())
                            today.add(Pair(lect, plan.key))
                    } else {
                        lect.meet.split(",").forEach {
                            if (it.toInt() == meeting)
                                today.add(Pair(lect, plan.key))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Schedule", e.message ?: e.toString())
            return
        }

        runOnUiThread {
            val inflater = requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            var max = 0

            today.forEach {
                val view = inflater.inflate(R.layout.template_block_container, null)  // noot noot

                //Log.e("Schedule", it.first.toString())

                view.findViewById<TextView>(R.id.block_what).text =
                        it.first.name
                //Log.e("Schedule", it.first.name)

                view.findViewById<TextView>(R.id.block_who).text =
                        it.first.prof
                //Log.e("Schedule", it.first.prof)

                val room = "${resources.getString(R.string.room)} ${it.first.room}"
                view.findViewById<TextView>(R.id.block_where).text = room
                //Log.e("Schedule", room)

                view.findViewById<TextView>(R.id.block_when).text = time[it.second]
                //Log.e("Schedule", time[it.second])

                if (max < it.second) max = it.second
                toDisplay[it.second] = view

            }

            val calendar = Calendar.getInstance()
            val currentTime = SimpleDateFormat("HH:mm")
                    .parse(SimpleDateFormat("HH:mm").format(calendar.time))

            var i = 0

            time.forEach { timeString ->
                val cal1 = Calendar.getInstance()
                val cal2 = Calendar.getInstance()
                cal1.time = SimpleDateFormat("HH:mm").parse(timeString.takeWhile { char ->
                    char != '-'
                }.trim())
                cal2.time = SimpleDateFormat("HH:mm").parse(timeString.takeLastWhile { char ->
                    char != '-'
                }.trim())
                //Log.e("Schedule", "${cal1.time} $currentTime ${cal2.time}")

                when {
                    (toDisplay[time.indexOf(timeString)] as LinearLayout).childCount <= 2 -> {
                        toDisplay[time.indexOf(timeString)].backgroundColor =
                                resources.getColor(R.color.backgroundGrayLight, requireContext().theme)
                    }
                    Main.studentWebsiteConnection.getCurrentMeet() > meeting ||
                            (day < currentDay() && Main.studentWebsiteConnection.getCurrentMeet() == meeting) ||
                            (day == currentDay() && currentTime.after(cal2.time) && Main.studentWebsiteConnection.getCurrentMeet() == meeting) -> {
                        toDisplay[time.indexOf(timeString)].backgroundColor =
                                resources.getColor(R.color.backgroundGreenLight, requireContext().theme)
                    }
                    currentTime.before(cal1.time) && Main.studentWebsiteConnection.getCurrentMeet() == meeting ||
                            Main.studentWebsiteConnection.getCurrentMeet() < meeting ||
                            day > currentDay() -> {
                        toDisplay[time.indexOf(timeString)].backgroundColor =
                                resources.getColor(R.color.backgroundRedLight, requireContext().theme)
                    }
                    (currentTime.after(cal1.time) && currentTime.before(cal2.time) ||
                            cal1.time == currentTime ||
                            currentTime == cal2.time) && Main.studentWebsiteConnection.getCurrentMeet() == meeting-> {
                        toDisplay[time.indexOf(timeString)].backgroundColor =
                                resources.getColor(R.color.backgroundYellowLight, requireContext().theme)
                    }
                    else -> {
                        Log.e("Schedule", (toDisplay[time.indexOf(timeString)] as LinearLayout).childCount.toString())
                        Log.e("Schedule", (currentTime.before(cal1.time) ||
                                Main.studentWebsiteConnection.getCurrentMeet() < meeting).toString())
                        Log.e("Schedule", (currentTime.after(cal1.time) && currentTime.before(cal2.time) ||
                                cal1.time == currentTime || currentTime == cal2.time).toString())
                        Log.e("Schedule", currentTime.after(cal2.time).toString())
                    }
                }

                Log.e("Schedule", (i++).toString())
            }

            schedule_scroll.removeAllViews()
            toDisplay.take(max + 2).forEach {
                schedule_scroll.addView(it)

            }
        }
    }


    private fun refresh() {
        thread {
            while (this.isVisible) {
                displaySchedule()
                for (i in 0 until 100)   // 10 sec refresh rate
                    if (forceRefresh) {
                        forceRefresh = false
                        break
                    } else
                        Thread.sleep(100)
            }
        }
    }

}


