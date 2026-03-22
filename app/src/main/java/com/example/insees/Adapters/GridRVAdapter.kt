package com.example.insees.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.insees.Dataclasses.GridViewModal
import com.example.insees.R

// on below line we are creating an
// adapter class for our grid view.
internal class GridRVAdapter(
    // on below line we are creating two
    // variables for course list and context
    private val MemberList: List<GridViewModal>,
    private val context: Context
) :
    BaseAdapter() {
    // in base adapter class we are creating variables
    // for layout inflater, course image view and course text view.
    private var layoutInflater: LayoutInflater? = null
    private lateinit var memberNameTV: TextView
    private lateinit var memberDescTV: TextView
    private lateinit var memberimgIV: ImageView

    // below method is use to return the count of course list
    override fun getCount(): Int {
        return MemberList.size
    }

    // below function is use to return the item of grid view.
    override fun getItem(position: Int): Any? {
        return null
    }

    // below function is use to return item id of grid view.
    override fun getItemId(position: Int): Long {
        return 0
    }

    // in below function we are getting individual item of grid view.
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var convertView = convertView
        // on blow line we are checking if layout inflater
        // is null, if it is null we are initializing it.
        if (layoutInflater == null) {
            layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }
        // on the below line we are checking if convert view is null.
        // If it is null we are initializing it.
        if (convertView == null) {
            // on below line we are passing the layout file
            // which we have to inflate for each item of grid view.
            convertView = layoutInflater!!.inflate(R.layout.member_item_view, null)
        }
        // on below line we are initializing our course image view
        // and course text view with their ids.
        memberimgIV = convertView!!.findViewById(R.id.idIVImage)
        memberNameTV = convertView.findViewById(R.id.idTVName)
        memberDescTV = convertView.findViewById(R.id.idTVDesc)
        // on below line we are setting image for our course image view.
        memberimgIV.setImageResource(MemberList[position].courseImg)
        // on below line we are setting texts in our course text view.
        memberNameTV.text = MemberList[position].MemberName
        memberDescTV.text = MemberList[position].courseDesc
        // at last we are returning our convert view.
        return convertView
    }
}