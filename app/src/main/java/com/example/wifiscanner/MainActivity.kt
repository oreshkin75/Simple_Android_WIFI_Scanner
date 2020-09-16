package com.example.wifiscanner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // вызывает метод onCreate() для activity
        super.onCreate(savedInstanceState)

        // устанавливает layout для отображения
        setContentView(R.layout.activity_main)

        // изменяемый список из 5 элементов через lambda-функцию
        val listData = MutableList(5, {x -> "${x+1}"})

        // создаём массив адаптеров, элементами которого будут TextView
        // с текстом из listData
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listData)

        // связываем элемент ListView с id=listView1 с адаптером
        main_list.adapter = adapter
        // задаём функцию-обработчик при нажатие на элемент через lambda-функцию
        main_list.setOnItemClickListener { parent, view, position, id ->
            // создаём intent в контексте MainActivity для вызова ListItemActivity
            val intent = Intent(this, ListItemActivity::class.java)
            // передаём содержимое элемента TextView
            intent.putExtra("text", (view as TextView).text)
            // вызываем ListItemActivity
            startActivity(intent)
        }

    }
}