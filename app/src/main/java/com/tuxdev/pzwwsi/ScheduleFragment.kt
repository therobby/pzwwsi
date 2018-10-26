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
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


class ScheduleFragment : Fragment() {

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loading = ProgressBar(requireContext())
        loading.isIndeterminate = true
        schedule_scroll.addView(loading)

        refresh()
    }

    private fun refresh() {
        thread {

            while (this.isVisible) {
                val days = if (Main.studentWebsiteConnection.isScheduleLoaded())
                    Main.studentWebsiteConnection.getScheduleDays(Main.studentWebsiteConnection.getStudentGroup())
                else {
                    Main.studentWebsiteConnection.loadSchedule()
                    Main.studentWebsiteConnection.getScheduleDays(Main.studentWebsiteConnection.getStudentGroup())
                }

                val today = ArrayList<Pair<Lecture, Int>>()
                val toDisplay = ArrayList<View>()
                val calendar = Calendar.getInstance()
                calendar.firstDayOfWeek = Calendar.MONDAY
                calendar.time = Date()
                var currdaynr = calendar.get(Calendar.DAY_OF_WEEK) - 1

                Log.e("Schedule", "Day: $currdaynr")

                time.forEach {
                    toDisplay.add(emptyHour(it))
                }

                currdaynr = when (currdaynr) {
                    0 -> 7
                    5 -> 4
                    4 -> 5
                    else -> currdaynr
                }

                val nr = Main.studentWebsiteConnection.getCurrentMeet()

                runOnUiThread {
                    try {
                        days!![currdaynr - 1].plan.forEach { plan ->
                            plan.value.forEach { lect ->
                                if (lect.meet.contains("-")) {
                                    val meet = lect.meet.split("-")
                                    if (nr >= meet.first().toInt() && nr <= meet.last().toInt())
                                        today.add(Pair(lect, plan.key))
                                } else {
                                    lect.meet.split(",").forEach {
                                        if (it.toInt() == nr)
                                            today.add(Pair(lect, plan.key))
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("Schedule", e.message)
                        return@runOnUiThread
                    }

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
                    time.forEach {
                        val cal1 = Calendar.getInstance()
                        val cal2 = Calendar.getInstance()
                        cal1.time = SimpleDateFormat("HH:mm").parse(it.takeWhile { it != '-' }.trim())
                        cal2.time = SimpleDateFormat("HH:mm").parse(it.takeLastWhile { it != '-' }.trim())
                        //Log.e("Schedule", "${cal1.time} $currentTime ${cal2.time}")
                        when {
                            (toDisplay[time.indexOf(it)] as LinearLayout).childCount <= 1 -> {
                                toDisplay[time.indexOf(it)].backgroundColor =
                                        resources.getColor(R.color.backgroundGrayLight, requireContext().theme)
                            }
                            currentTime.before(cal1.time) -> {
                                toDisplay[time.indexOf(it)].backgroundColor =
                                        resources.getColor(R.color.backgroundRedLight, requireContext().theme)
                            }
                            currentTime.after(cal1.time) && currentTime.before(cal2.time) -> {
                                toDisplay[time.indexOf(it)].backgroundColor =
                                        resources.getColor(R.color.backgroundYellowLight, requireContext().theme)
                            }
                            currentTime.after(cal2.time) -> {
                                toDisplay[time.indexOf(it)].backgroundColor =
                                        resources.getColor(R.color.backgroundGreenLight, requireContext().theme)
                            }
                        }
                    }

                    schedule_scroll.removeAllViews()
                    toDisplay.take(max + 2).forEach {
                        schedule_scroll.addView(it)

                    }
                }
                Thread.sleep(5 * 1000)  // 5sec refresh rate
            }
        }
    }
}


