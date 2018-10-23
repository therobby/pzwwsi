package com.tuxdev.pzwwsi

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_info_messages.*
import org.jetbrains.anko.support.v4.runOnUiThread
import kotlin.concurrent.thread

class InfoMessages : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        thread {
            val inflater = requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            Main.studentWebsiteConnection.getKomunikaty().forEach {
                val view = inflater.inflate(R.layout.template_message_container, null)  // noot noot

                runOnUiThread {
                    view.findViewById<TextView>(R.id.message_title).text =
                            it.getElementsByClass("news_title").text()

                    view.findViewById<TextView>(R.id.message_date).text =
                            it.getElementsByClass("news_podpis").text()

                    view.findViewById<TextView>(R.id.message_data).text =
                            it.getElementsByClass("news_content").text()

                    main_scroll_layout.addView(view)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info_messages, container, false)
    }
}
