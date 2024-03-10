package com.example.myweatherapp


import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity2 : AppCompatActivity() {
    private lateinit var cityInput: EditText
    private lateinit var button: Button

    private val apiKey: String = "11e94ab942a5ebae73d6de6b4b8de110"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cityInput = findViewById(R.id.city_input)
        button = findViewById(R.id.suchen)



        button.setOnClickListener {
            val newCity = cityInput.text.toString()
            Log.d("MainActivity2", "Button clicked for city: $newCity")
            fetchDataAndHandle(newCity)
        }

        // Initial fetch and handle for default city
        fetchDataAndHandle("Marburg")

    }//fun onCreate

    @OptIn(DelicateCoroutinesApi::class)
    private fun fetchDataAndHandle(city: String) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val result = withContext(Dispatchers.IO) {
                    fetchData(city)
                }
                handleWeatherData(result)
            } catch (e: Exception) {
                handleNetworkError()
            }
        }

    }

    private fun fetchData(city: String): String {
        try {
            val urlString =
                "https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$apiKey"
            Log.d("MainActivity2", "fetchData URL: $urlString") //log * print
            val result = URL(urlString).readText(Charsets.UTF_8)
            Log.d("MainActivity2", "fetchData result: $result")
            return result
        } catch (e: Exception) {
            Log.e("MainActivity2", "Error in fetchData: ${e.message}")
            return throw e
        }
    }//fun fetchData

    private fun handleWeatherData(result: String) {
        try {
            Log.d("MainActivity2", "handleWeatherData called with result: $result")
            val jsonObj = JSONObject(result)
            val main = jsonObj.getJSONObject("main")
            val sys = jsonObj.getJSONObject("sys")
            val wind = jsonObj.getJSONObject("wind")
            val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
            val updatedAt: Long = jsonObj.getLong("dt")
            val updatedAtText =
                "Aktualisiert am: " + SimpleDateFormat("dd.MM.yyyy hh:mm a", Locale.ENGLISH).format(
                    Date(updatedAt * 1000)
                )
            val temp = jsonObj.getString("Ta").toFloat().toInt().toString() + "°C"
            val tempMin =
                "Min Temp: " + main.getString("temp_min").toFloat().toInt().toString() + "°C"
            val tempMax =
                "Max Temp: " + main.getString("temp_max").toFloat().toInt().toString() + "°C"
            val humidity = main.getString("humidity") + "%"
            val windSpeed = wind.getString("speed") + "km/h"
            val weatherDescription = weather.getString("description")
            val address = jsonObj.getString("name") + ", " + sys.getString("country")

            // Check if "rain" exists in the JSON response before trying to access its properties
            val rain = jsonObj.optJSONObject("rain")
            val rainPCP = rain?.optString("1h", "0mm") ?: "0mm"

            findViewById<TextView>(R.id.address).text = address
            findViewById<TextView>(R.id.aktualisiert_am).text = updatedAtText
            findViewById<TextView>(R.id.status).text = weatherDescription
            findViewById<TextView>(R.id.temp).text = temp
            findViewById<TextView>(R.id.temp_min).text = tempMin
            findViewById<TextView>(R.id.temp_max).text = tempMax
            findViewById<TextView>(R.id.feucht).text = humidity
            findViewById<TextView>(R.id.wind).text = windSpeed
            findViewById<TextView>(R.id.regen).text = rainPCP

            findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.GONE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.e("MainActivity2", "Error in handleWeatherData: ${e.message}")
            handleDataParsingError()
        }
    }//fun handleWeatherData


    private fun handleNetworkError() {
        findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
        findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
        findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE

    }//fun handleNetworkError

    private fun handleDataParsingError() {
        findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
        findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
        findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
        findViewById<LinearLayout>(R.id.search_bar).visibility = View.VISIBLE
    }//fun handleDataParsingError()

}//class MainActivity2