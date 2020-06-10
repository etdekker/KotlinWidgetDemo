package com.example.kotlinwidgetdemo

import android.appwidget.AppWidgetHostView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
//import com.sun.org.apache.xerces.internal.util.DOMUtil.getParent


class WidgetAdapter (val activity: MainActivity):
    RecyclerView.Adapter<WidgetAdapter.WidgetViewHolder>() {

    var widgets = listOf<AppWidgetHostView>().toMutableList()

    fun moveItem(from: Int, to: Int) {
        val fromWidget = widgets[from]
        widgets.removeAt(from)
        if (to < from) {
            widgets.add(to, fromWidget)
        } else {
            widgets.add(to - 1, fromWidget)
        }
    }

    fun addItem(to: Int, item: AppWidgetHostView) {
        widgets.add(to, item)

        /**
        // Console Debugging Statements
        println("Adapter:")
        println("addItem was called with item: " + item.appWidgetInfo.provider.className)
        println("widgets list position: " + to)
        println("widgets[to].appWidgetId: " + widgets[to].appWidgetId)
        println("widgets[to].appWidgetInfo.provider.className: "+ widgets[to].appWidgetInfo.provider.className)
        println("remaining widgets:")
        for (i in 0 until widgets.size){
            println("[" + i + "] id: " + widgets[i].appWidgetId + " class: " + widgets[i].appWidgetInfo.provider.className)
        }
        */
    }

    fun removeItem(from: Int) : Int{
        val id = widgets[from].appWidgetId

        /**
        // console logging:
        println("Adapter:")
        println("removeItem was called with position: " + from)
        println("widgets list position: " + from)
        println("widgets[from].appWidgetId: " + widgets[from].appWidgetId)
        println("widgets[from].appWidgetInfo.provider.className: "+ widgets[from].appWidgetInfo.provider.className)
        */

        widgets.removeAt(from)

        /**
        println("widgets list:")
        for (i in 0 until widgets.size){
            println("[" + i + "] id: " + widgets[i].appWidgetId + " class: " + widgets[i].appWidgetInfo.provider.className)
        }
        */

        return id
    }


    override fun getItemCount() = widgets.size

    override fun onBindViewHolder(viewHolder: WidgetViewHolder, position: Int) {
        val item = widgets[position]
        /**
        println("onBindViewHolder:")
        println("widgets[to].appWidgetId: " + item.appWidgetId)
        println("widgets[to].appWidgetInfo.provider.className: "+ item.appWidgetInfo.provider.className)
        */
        viewHolder.setWidget(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.app_widget_layout, parent, false)
        val viewHolder = WidgetViewHolder(itemView)
        /**
//        viewHolder.itemView.handleView.setOnTouchListener { view, event ->
//            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
//                activity.startDragging(viewHolder)
//            }
//            return@setOnTouchListener true
//        }
        */
        return viewHolder
    }

    class WidgetViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val linearLayout = itemView.findViewById<LinearLayout>(R.id.layout)
        fun setWidget(view : AppWidgetHostView) {
            if(view.parent != null) {
                (view.parent as ViewGroup).removeView(view)
            }
            linearLayout.removeAllViews()
            linearLayout.addView(view)
        }
    }
}

