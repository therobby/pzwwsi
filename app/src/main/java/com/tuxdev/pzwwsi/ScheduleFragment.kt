package com.tuxdev.pzwwsi

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_schedule.*
import org.jetbrains.anko.support.v4.runOnUiThread
import java.util.*
import kotlin.concurrent.thread


class ScheduleFragment : Fragment() {

    private var schedule = PlanProcessor()
    private val time = arrayListOf("8:00 - 8:45",
            "8:50 - 9:35",
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        thread {
            schedule.load(Main.studentWebsiteConnection.getPlan())
            val days = schedule.getDays(Main.studentWebsiteConnection.getStudentGroup())

            val today = ArrayList<Pair<Lecture, Int>>()
            val calendar = Calendar.getInstance()
            calendar.firstDayOfWeek = Calendar.MONDAY
            calendar.time = Date()
            var currdaynr = calendar.get(Calendar.DAY_OF_WEEK) - 1

            Log.e("Schedule", "Day: $currdaynr")

            currdaynr = when (currdaynr) {
                0 -> 7
                5 -> 4
                4 -> 5
                else -> currdaynr
            }

            val nr = Main.studentWebsiteConnection.getCurrentMeet()

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
                Log.e("Schedule",e.message)
                return@thread
            }

            val inflater = requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            today.forEach {
                val view = inflater.inflate(R.layout.template_block_container, null)  // noot noot

                Log.e("Schedule",it.first.toString())

                runOnUiThread {
                    view.findViewById<TextView>(R.id.block_what).text =
                            it.first.name
                    Log.e("Schedule", it.first.name)

                    view.findViewById<TextView>(R.id.block_who).text =
                            it.first.prof
                    Log.e("Schedule", it.first.prof)

                    val room = "${resources.getString(R.string.room)} ${it.first.room}"
                    view.findViewById<TextView>(R.id.block_where).text = room
                    Log.e("Schedule", room)

                    view.findViewById<TextView>(R.id.block_when).text = time[it.second-1]
                    Log.e("Schedule", time[it.second-1])

                    schedule_scroll.addView(view)
                }
            }
        }

    }
}
