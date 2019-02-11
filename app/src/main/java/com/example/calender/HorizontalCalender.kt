package com.example.calender

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Build
import android.support.constraint.ConstraintLayout

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.calender.AdapterForDates.Companion.attributes
import com.github.aakira.expandablelayout.ExpandableRelativeLayout
import kotlinx.android.synthetic.main.date_item.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.drawable.GradientDrawable
import com.github.aakira.expandablelayout.ExpandableLayoutListener


class HorizontalCalender @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs)

{    private var mFirstCompleteVisibleItemPosition = -1
    private var mLastCompleteVisibleItemPosition = -1
    private var mEndDate :Date?=null
    private var mMonths: Array<String> = arrayOf("JANUARY","FEBRUARY","MARCH","APRIL","MAY","JUNE","JULY","AUGUST","SEPTEMBER","OCTOBER","NOVEMBER","DECEMBER")
    private var mStartD: Date?=null
    private var today: ImageView?=null
    private lateinit var mBaseDateList: ArrayList<Date>
    private val mCal = Calendar.getInstance()
    private var dates_rv : RecyclerView?=null
    private var month: TextView?=null
    private var show: ImageView?=null
    private var close: ImageView?=null
    private var expandableRelativeLayout: ExpandableRelativeLayout?=null
    private var mFormatter = SimpleDateFormat("dd-MM-yyyy",Locale.US)



    init {

        View.inflate(context,R.layout.calenderview,this)
        attributes = context.obtainStyledAttributes(attrs, R.styleable.HorizontalCalender)
        show = findViewById(R.id.show)
        dates_rv = findViewById(R.id.dates_rv)
        month = findViewById(R.id.month)
        close = findViewById(R.id.close)
        today = findViewById(R.id.today)


        show!!.setImageDrawable(attributes!!.getDrawable(R.styleable.HorizontalCalender_calenderIcon))
        close!!.setImageDrawable(attributes!!.getDrawable(R.styleable.HorizontalCalender_closeIcon))
        today!!.setImageDrawable(attributes!!.getDrawable(R.styleable.HorizontalCalender_todayIcon))


        expandableRelativeLayout = findViewById(R.id.expandableLinearLayout2)
        show!!.setOnClickListener {
        expandableRelativeLayout!!.expand()
            }
        close!!.setOnClickListener {
            expandableRelativeLayout!!.collapse()

        }

        expandableRelativeLayout!!.setListener(object : ExpandableLayoutListener {
            override fun onAnimationStart() {}

            override fun onAnimationEnd() {}

            override fun onPreOpen() {
                show!!.visibility = View.GONE
            }

            override fun onPreClose() {
            }

            override fun onOpened() {
                show!!.visibility = View.GONE

            }

            override fun onClosed() {
                show!!.visibility = View.VISIBLE
            }
        })


        horizontalDates()

       month!!.setTextColor(attributes!!.getColor(R.styleable.HorizontalCalender_textColor,0))


    }



    private fun horizontalDates() {
        mFormatter = SimpleDateFormat("dd-MM-yyyy",Locale.US)
        mStartD = Date()
        val calendar = Calendar.getInstance()
        calendar.time = mStartD
        val currentMonth =mMonths[calendar.get(Calendar.MONTH)]
        val currentYear = calendar.get(Calendar.YEAR).toString()
        month!!.text = "$currentMonth ,$currentYear"
        calendar.add(Calendar.MONTH, -1)
        mEndDate = calendar.time
        mBaseDateList = getDates(mFormatter.format(mEndDate), mFormatter.format(mStartD))
        setAdapter(mBaseDateList)
        val layoutManager3 = dates_rv!!.layoutManager as LinearLayoutManager
        layoutManager3.scrollToPosition(mBaseDateList.size-1)
    }

    private fun getDates(dateString1: String, dateString2: String): ArrayList<Date> {

        val mDates = ArrayList<Date>()
        var startDate: Date? = null
        try {
            startDate = mFormatter.parse(dateString1)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        var endDate: Date? = null
        try {
            endDate = mFormatter.parse(dateString2)
        } catch (e: ParseException) {

            e.printStackTrace()
        }

        val interval = (24 * 1000 * 60 * 60).toLong()
        val endTime = endDate!!.time
        var curTime = startDate!!.time
        while (curTime <= endTime) {
            mDates.add(Date(curTime))
            curTime += interval
        }

        return mDates

    }

    private fun setAdapter(
        dates: ArrayList<Date>
    ){

        val mFinalDates = ArrayList<Model>()
        val clickedDate = ArrayList<String>()


        for (i in 0 until dates.size) {
            val lDate = dates[i]
            val c = Calendar.getInstance()
            c.time = lDate

            val dayOfWeek = c.get(Calendar.DATE)
            val monthh = c.get(Calendar.MONTH)
            val year = c.get(Calendar.YEAR)

            val dayLongName = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
            mFinalDates.add(Model(dayOfWeek.toString(),dayLongName))
            clickedDate.add(String.format("%02d-%02d-%04d",dayOfWeek,monthh+1,year))

        }

        val layoutManager= LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
        layoutManager.stackFromEnd = true
        dates_rv!!.layoutManager = layoutManager
        layoutManager.scrollToPosition(30)

        dates_rv!!.adapter = AdapterForDates(mFinalDates,context!!,clickedDate,dates)

        today!!.setOnClickListener {
            layoutManager.scrollToPosition(mFinalDates.size-1)
        }

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            mCal.set(Calendar.YEAR, year)
            mCal.set(Calendar.MONTH, monthOfYear)
            mCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            Log.d("date",mCal.time.toString())
            val calendarSelect = Calendar.getInstance()
            calendarSelect.time = mCal.time
            calendarSelect.add(Calendar.MONTH,-1)
            val endDate = calendarSelect.time
           month!!.text = mMonths[calendarSelect.get(Calendar.MONTH)]+", "+calendarSelect.get(Calendar.YEAR)
            mBaseDateList = getDates(mFormatter.format(endDate), mFormatter.format(mStartD))
            setAdapter(mBaseDateList)
            mEndDate = endDate
            if(mBaseDateList.size>30) {
                dates_rv!!.smoothScrollToPosition(32)
            }else{
                dates_rv!!.smoothScrollToPosition(30)
            }
        }



        month!!.setOnClickListener {
            DatePickerDialog(context,
                dateSetListener,
                mCal.get(Calendar.YEAR),
                mCal.get(Calendar.MONTH),
                mCal.get(Calendar.DAY_OF_MONTH)).show()
            mCal.add(Calendar.MONTH,-1)
        }


        dates_rv!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManagerForPos = recyclerView.layoutManager
                val totalItemCount = layoutManagerForPos!!.itemCount

                if (layoutManagerForPos is GridLayoutManager) {
                    val gridLayoutManager = layoutManagerForPos as GridLayoutManager?
                    mFirstCompleteVisibleItemPosition = gridLayoutManager!!.findFirstCompletelyVisibleItemPosition()
                    mLastCompleteVisibleItemPosition = gridLayoutManager.findLastCompletelyVisibleItemPosition()
                } else if (layoutManagerForPos is LinearLayoutManager) {
                    val linearLayoutManager = layoutManagerForPos as LinearLayoutManager?
                    mFirstCompleteVisibleItemPosition = linearLayoutManager!!.findFirstCompletelyVisibleItemPosition()
                    mLastCompleteVisibleItemPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition()
                }
                if (mFirstCompleteVisibleItemPosition == 0) {
                    if (dy < 0 || dx < 0) {
                        if (dx < 0) {
                            Log.d("status","Scrolled LEFT")
                            val calendar = Calendar.getInstance()
                            calendar.time = mEndDate
                            calendar.add(Calendar.MONTH,-1)
                            val tempEndDate = calendar.time
                           month!!.text = mMonths[calendar.get(Calendar.MONTH)]+", "+calendar.get(Calendar.YEAR)
                            mBaseDateList = getDates(mFormatter.format(tempEndDate), mFormatter.format(mStartD))
                            setAdapter(mBaseDateList)
                            mEndDate = tempEndDate
                        }
                    }
                } else if (mLastCompleteVisibleItemPosition == totalItemCount - 1) {
                    val calendar = Calendar.getInstance()
                    val date = mBaseDateList[mLastCompleteVisibleItemPosition]
                    calendar.time = date
                    month!!.text = mMonths[calendar.get(Calendar.MONTH)]+", "+calendar.get(Calendar.YEAR)

                    if (dy > 0 || dx > 0) {
                        if (dy > 0) {
                            Log.d("status","Scrolled TOP")
                        }
                        if (dx > 0) {
                            Log.d("status","Scrolled RIGHT")
                        }
                    }
                }
                else{
                    if(dx<0) {
                        val calendar = Calendar.getInstance()
                        val date = mBaseDateList[mFirstCompleteVisibleItemPosition + 1]
                        calendar.time = date
                    month!!.text = mMonths[calendar.get(Calendar.MONTH)] + ", " + calendar.get(Calendar.YEAR)
                    }else{
                        val calendar = Calendar.getInstance()
                        val date = mBaseDateList[mLastCompleteVisibleItemPosition]
                        calendar.time = date
                       month!!.text = mMonths[calendar.get(Calendar.MONTH)] + ", " + calendar.get(Calendar.YEAR)
                    }
                }
            }
        })
    }


}


 private class AdapterForDates(
    private val mModelItems: ArrayList<Model>,
    private val mContext: Context,
    private val mDatesList: ArrayList<String>,
    private val fullFormatDate: ArrayList<Date>
):RecyclerView.Adapter<AdapterForDates.ViewHolder>(){

    var mPreviousIndex : Int? = -1
     var strokeColor : Int? = null
     var unSelectedColor : Int? = null
     var selectedColor : Int? = null
     var strokeWidth : Int? = null


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.date_item,p0,false))

    }

    override fun getItemCount(): Int {
        return mModelItems.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        val mMonths: Array<String> = arrayOf("JANUARY","FEBRUARY","MARCH","APRIL","MAY","JUNE","JULY","AUGUST","SEPTEMBER","OCTOBER","NOVEMBER","DECEMBER")
        p0.date!!.text = mModelItems[p1].date

        p0.date.setTextColor(attributes!!.getColor(R.styleable.HorizontalCalender_textColor,0))

        if(p1==mModelItems.size-1){
            p0.day!!.text = mContext.getString(R.string.today)

        }
        else {

            p0.day!!.text = mModelItems[p1].day[0].toString()

        }

        if(attributes!!.getBoolean(R.styleable.HorizontalCalender_dayView,true)){
            p0.day.visibility = View.VISIBLE
        }else{
            p0.day.visibility = View.GONE
        }

        p0.layout.setOnClickListener {
            val date = mDatesList[p1]
            Toast.makeText(mContext,date, Toast.LENGTH_SHORT).show()

            val calendar = Calendar.getInstance()
            calendar.time = fullFormatDate[p1]
            mMonth = mMonths[calendar.get(Calendar.MONTH)]+", "+calendar.get(Calendar.YEAR)

            mPreviousIndex = p1

            notifyDataSetChanged()

        }


         strokeColor = attributes!!.getColor(R.styleable.HorizontalCalender_strokeColor,0)
         unSelectedColor =attributes!!.getColor(R.styleable.HorizontalCalender_unSelectedColor,0)
         selectedColor = attributes!!.getColor(R.styleable.HorizontalCalender_selectedColor,0)
         strokeWidth = attributes!!.getColor(R.styleable.HorizontalCalender_strokeWidth,0)


        strokeWidth = if (strokeWidth==0){
            4
        }else{
            attributes!!.getInt(R.styleable.HorizontalCalender_strokeWidth,0)

        }

        strokeColor = if ( strokeColor==0 ){
            Color.BLACK
        }else{
            attributes!!.getColor(R.styleable.HorizontalCalender_strokeColor,0)

        }

        unSelectedColor = if (unSelectedColor == 0 ){
            Color.YELLOW
        }else{
            attributes!!.getColor(R.styleable.HorizontalCalender_unSelectedColor,0)
        }


        if (selectedColor == 0){
            Color.WHITE
        }else{
            selectedColor = attributes!!.getColor(R.styleable.HorizontalCalender_selectedColor,0)

        }


        if(mPreviousIndex == p1){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                val shape = GradientDrawable()
                shape.shape = GradientDrawable.OVAL
                shape.setStroke(strokeWidth!!,strokeColor!!)
                shape.setColor(selectedColor!!)

                p0.imageView.background = shape

            }
        }else{

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val shape = GradientDrawable()
                shape.shape = GradientDrawable.OVAL
                shape.setColor(unSelectedColor!!)
                p0.imageView.background = shape
            }

        }


    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        internal val date = view.date
        internal val layout = view.layout
        internal val day = view.day
        internal val imageView = view.circleImageView
    }

    companion object {
        var mMonth :String?=null
        var attributes : TypedArray?=null

    }

}

data class Model(val date: String, val day : String)