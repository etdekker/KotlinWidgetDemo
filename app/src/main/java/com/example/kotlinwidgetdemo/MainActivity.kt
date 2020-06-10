package com.example.kotlinwidgetdemo

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import java.util.*
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.main.*

class MainActivity : AppCompatActivity() {
    private lateinit var mAppWidgetManager: AppWidgetManager
    private lateinit var mAppWidgetHost: AppWidgetHost

    //List<AppWidgetProviderInfo> widgetList = null;

    private val APPWIDGET_HOST_ID = 1
    private val REQUEST_PICK_APPWIDGET = 2
    private val REQUEST_CREATE_APPWIDGET = 3

    lateinit var mainlayout: ViewGroup
    lateinit var rvWidgets: RecyclerView
    lateinit var adapter: WidgetAdapter
    var widgetList = mutableListOf<AppWidgetHostView>()

    /**
     * Called on the creation of the activity.
     */

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView,
                                viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder): Boolean {
                val adapter = recyclerView.adapter as WidgetAdapter
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                adapter.moveItem(from, to)
                adapter.notifyItemMoved(from, to)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = recyclerView.adapter as WidgetAdapter
                val from = viewHolder.adapterPosition
                adapter.removeItem(from)
                adapter.notifyItemRemoved(from)
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.alpha = 0.5f
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)

                viewHolder?.itemView?.alpha = 1.0f
            }
        }

        ItemTouchHelper(simpleItemTouchCallback)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main) // changed from main to activity_main

        mainlayout = findViewById(R.id.main)

        mAppWidgetManager = AppWidgetManager.getInstance(this)
        mAppWidgetHost = AppWidgetHost(this, APPWIDGET_HOST_ID)


        recyclerView.layoutManager = LinearLayoutManager(this) as RecyclerView.LayoutManager
        recyclerView.adapter = WidgetAdapter(this)
        itemTouchHelper.attachToRecyclerView(recyclerView)

//        rvWidgets = findViewById<View>(R.id.recyclerView) as RecyclerView
//        adapter = WidgetAdapter(widgetList)
//        rvWidgets.adapter = adapter
    }

    fun startDragging(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    /**
     * Launches the menu to select the widget. The selected widget will be on
     * the result of the activity.
     */
    fun selectWidget() {
        val appWidgetId = mAppWidgetHost!!.allocateAppWidgetId()
        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET)
    }

    /**
     * This avoids a bug in the com.android.settings.AppWidgetPickActivity,
     * which is used to select widgets. This just adds empty extras to the
     * intent, avoiding the bug.
     *
     * See more: http://code.google.com/p/android/issues/detail?id=4272
     */
    fun addEmptyData(pickIntent: Intent) {
        val customInfo = ArrayList<AppWidgetProviderInfo>()
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo)
        val customExtras = ArrayList<Bundle>()
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras)
    }

    /**
     * If the user has selected an widget, the result will be in the 'data' when
     * this function is called.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_APPWIDGET) {
                configureWidget(data)
            } else if (requestCode == REQUEST_CREATE_APPWIDGET) {
                createWidget(data)
            }
        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {
            val appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if (appWidgetId != -1) {
                mAppWidgetHost!!.deleteAppWidgetId(appWidgetId)
            }
        }
    }

    /**
     * Checks if the widget needs any configuration. If it needs, launches the
     * configuration activity.
     */
    private fun configureWidget(data: Intent?) {
        val extras = data!!.extras
        val appWidgetId = extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        val appWidgetInfo = mAppWidgetManager!!.getAppWidgetInfo(appWidgetId)
        if (appWidgetInfo.configure != null) {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
            intent.component = appWidgetInfo.configure
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET)
        } else {
            createWidget(data)
        }
    }

    /**
     * Creates the widget and adds to our view layout.
     */
    fun createWidget(data: Intent?) {
        println(data)
        val extras = data!!.extras
        val appWidgetId = extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        val appWidgetInfo = mAppWidgetManager!!.getAppWidgetInfo(appWidgetId)
        val hostView = mAppWidgetHost!!.createView(this.applicationContext,
                appWidgetId, appWidgetInfo)
        hostView.setAppWidget(appWidgetId, appWidgetInfo)

        //mainlayout.addView(hostView)

        val adapter = recyclerView.adapter as WidgetAdapter
        adapter.addItem(0, hostView)
        adapter.notifyItemInserted(0)

        recyclerView.smoothScrollToPosition(0)

        /**
        // console logging
        println("Main:")
        println("addItem was called with item: " + hostView.appWidgetInfo.provider.className)
        println("widgets list position: " + 0)
        println("widgets[to].appWidgetId: " + hostView.appWidgetId)
        println("widgets[to].appWidgetInfo.provider.className: "+ hostView.appWidgetInfo.provider.className)
        */

    }

    /**
     * Registers the AppWidgetHost to listen for updates to any widgets this app
     * has.
     */
    override fun onStart() {
        super.onStart()
        mAppWidgetHost!!.startListening()
    }

    /**
     * Stop listen for updates for our widgets (saving battery).
     */
    override fun onStop() {
        super.onStop()
        mAppWidgetHost!!.stopListening()
    }

    /**
     * Removes the widget displayed by this AppWidgetHostView.
     */
    fun removeWidget(view: AppWidgetHostView) {

        val adapter = recyclerView.adapter as WidgetAdapter
        val from = 0
        val id = adapter.widgets[from].appWidgetId
        adapter.removeItem(from)
        mAppWidgetHost!!.deleteAppWidgetId(id)

        adapter.notifyItemRemoved(from)

        //mainlayout.removeView(hostView)
    }

    /**
     * Handles the menu.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i(TAG, "Menu selected: " + item.title + " / " + item.itemId + " / " + R.id.addWidget)
        when (item.itemId) {
            R.id.addWidget -> {
                selectWidget()
                return true
            }
            R.id.removeWidget -> {
                removeWidgetMenuSelected()
                return false
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Handle the 'Remove Widget' menu.
     */
    fun removeWidgetMenuSelected() {
        //val childCount: Int = mainlayout.getChildCount()
        if (adapter.widgets != null || adapter.widgets.size > 0) {
            val view: View = adapter.widgets[0]
            if (view is AppWidgetHostView) {
                removeWidget(view)
                Toast.makeText(this, R.string.widget_removed_popup, Toast.LENGTH_SHORT).show()
                return
            }
        }
        Toast.makeText(this, R.string.no_widgets_popup, Toast.LENGTH_SHORT).show()
    }

    /**
     * Creates the menu with options to add and remove widgets.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.widget_menu, menu)
        return true
    }

    companion object {
        const val TAG = "KotlinWidgetHost"
    }
}
