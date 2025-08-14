package com.example.pgfinder

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class FilterActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SELECTED_CITY = "selectedCity"
    }

    // Yaha apni cities add/modify kar sakte ho
    private val cities = listOf(
        "Ahmedabad",
        "Surat",
        "Rajkot",
        "Vadodara",
        "Bhavnagar",
        "Gandhinagar",
        "Jamnagar",
        "Junagadh"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)

        val listView = findViewById<ListView>(R.id.cityListView)
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, cities)

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedCity = cities[position]
            setResult(
                Activity.RESULT_OK,
                Intent().putExtra(EXTRA_SELECTED_CITY, selectedCity)
            )
            finish()
        }
    }
}
