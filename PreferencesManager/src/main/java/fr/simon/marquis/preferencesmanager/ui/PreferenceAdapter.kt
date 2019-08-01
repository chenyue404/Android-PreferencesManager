/*
 * Copyright (C) 2013 Simon Marquis (http://www.simon-marquis.fr)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package fr.simon.marquis.preferencesmanager.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.model.PreferenceType
import fr.simon.marquis.preferencesmanager.util.createSpannable
import java.util.*
import java.util.regex.Pattern

class PreferenceAdapter(context: Context, private val mPreferencesFragment: PreferencesFragment) : BaseAdapter(), Filterable {

    private val layoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val color: Int = context.resources.getColor(R.color.blue, null)
    private val mLock = Any()
    private val mCheckedPositions: MutableMap<MutableMap.MutableEntry<Any, Any>, Boolean>
    private var pattern: Pattern? = null
    private var mListToDisplay: MutableList<MutableMap.MutableEntry<Any, Any>>? = null

    init {
        this.mCheckedPositions = HashMap()
        this.mListToDisplay = mPreferencesFragment.preferenceFile!!.list
    }

    override fun getCount(): Int {
        return mListToDisplay!!.size
    }

    override fun getItem(position: Int): Any {
        return mListToDisplay!![position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var convertView = view
        val holder: ViewHolder
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.row_preference, parent, false)
            holder = ViewHolder()
            holder.background = convertView
            holder.name = convertView!!.findViewById<TextView>(R.id.item_name)
            holder.border = convertView.findViewById<View>(R.id.item_border)
            holder.value = convertView.findViewById<TextView>(R.id.item_value)
            holder.selector = convertView.findViewById<LinearLayout>(R.id.item_selector)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        val item = mListToDisplay!![position]
        val checked = mCheckedPositions[item]
        holder.border!!.setBackgroundResource(PreferenceType.getDialogLayout(item.value))
        holder.name!!.text = createSpannable(pattern, color, item.key.toString())
        holder.value!!.text = createSpannable(pattern, color, truncate(item.value.toString(), 100)!!)
        holder.selector!!.setBackgroundResource(if (checked != null && checked) R.drawable.list_focused else R.drawable.selectable_background)

        return convertView
    }

    private class ViewHolder {
        var background: View? = null
        var name: TextView? = null
        var border: View? = null
        var value: TextView? = null
        var selector: LinearLayout? = null
    }

    fun setFilter(filter: String?) {
        pattern = if (filter.isNullOrBlank()) null else Pattern.compile(filter, Pattern.CASE_INSENSITIVE)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val results = FilterResults()
                if (charSequence == null || charSequence.isEmpty()) {
                    synchronized(mLock) {
                        results.values = mPreferencesFragment.preferenceFile!!.list
                        results.count = mPreferencesFragment.preferenceFile!!.list.size
                    }
                } else {
                    val prefixString = charSequence.toString().toLowerCase(Locale.getDefault()).trim { it <= ' ' }
                    val filterResultsData = ArrayList<MutableMap.MutableEntry<Any, Any>>()
                    synchronized(mLock) {
                        for (data in mPreferencesFragment.preferenceFile!!.list) {
                            val p = Pattern.compile(prefixString, Pattern.CASE_INSENSITIVE)
                            if (p.matcher(data.key.toString().toLowerCase(Locale.getDefault()).trim { it <= ' ' }).find() || p.matcher(data.value.toString().toLowerCase(Locale.getDefault()).trim { it <= ' ' }).find()) {
                                filterResultsData.add(data)
                            }
                        }
                    }
                    synchronized(mLock) {
                        results.values = filterResultsData
                        results.count = filterResultsData.size
                    }
                }

                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                mListToDisplay = filterResults.values as MutableList<MutableMap.MutableEntry<Any, Any>>
                notifyDataSetChanged()
            }
        }
    }

    fun resetSelection() {
        mCheckedPositions.clear()
        notifyDataSetChanged()
    }

    fun itemCheckedStateChanged(position: Int, checked: Boolean) {
        mCheckedPositions[mListToDisplay!![position]] = checked
        super.notifyDataSetChanged()
    }

    fun deleteSelection() {
        val temp = ArrayList<MutableMap.MutableEntry<Any, Any>>()
        for (item in mListToDisplay!!) {
            if (!mCheckedPositions.containsKey(item) || mCheckedPositions[item] == false) {
                mCheckedPositions.remove(item)
                temp.add(item)
            }
        }
        mPreferencesFragment.preferenceFile!!.list = temp
        mListToDisplay = temp
    }

    companion object {

        fun truncate(str: String?, length: Int): String? {
            return if (str != null && str.length > length) str.substring(0, length) else str
        }
    }
}