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
import android.view.animation.AnimationUtils
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.model.AppEntry
import fr.simon.marquis.preferencesmanager.util.MyComparator
import fr.simon.marquis.preferencesmanager.util.createSpannable
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter
import java.util.*
import java.util.regex.Pattern

internal class AppAdapter(private val context: Context, private val applications: ArrayList<AppEntry>, private val emptyView: View) : BaseAdapter(), StickyListHeadersAdapter, Filterable {
    private val layoutInflater: LayoutInflater
    private val color: Int
    private val mLock = Any()

    private var pattern: Pattern? = null
    private var applicationsToDisplay: ArrayList<AppEntry>? = null

    init {
        this.applicationsToDisplay = applications
        this.layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this.color = context.resources.getColor(R.color.header_blue, null)
        updateEmptyView()
    }

    override fun notifyDataSetChanged() {
        synchronized(mLock) {
            Collections.sort(applicationsToDisplay!!, MyComparator())
        }
        updateEmptyView()
        super.notifyDataSetChanged()
    }

    private fun updateEmptyView() {
        if (isEmpty) {
            if (emptyView.visibility == View.GONE) {
                val animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
                if (animation != null) {
                    emptyView.startAnimation(animation)
                }
            }
            emptyView.visibility = View.VISIBLE
        } else {
            emptyView.visibility = View.GONE
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val holder: ViewHolder
        if (view == null) {
            view = layoutInflater.inflate(R.layout.row_application, parent, false)
            assert(view != null)
            holder = ViewHolder()
            holder.textView = view!!.findViewById(R.id.item_text)
            holder.imageView = view.findViewById(R.id.item_image)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        val item = applicationsToDisplay!![position]
        holder.textView!!.text = createSpannable(pattern, color, item.label!!)

        Glide.with(context)
                .load(item.iconUri)
                .apply(RequestOptions()
                        .error(R.drawable.ic_action_settings)
                )
                .into(holder.imageView!!)

        return view
    }

    private class ViewHolder {
        var imageView: ImageView? = null
        var textView: TextView? = null
    }

    override fun getHeaderView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val holder: HeaderViewHolder
        if (view == null) {
            holder = HeaderViewHolder()
            view = layoutInflater.inflate(R.layout.row_header, parent, false)
            assert(view != null)
            holder.text = view!!.findViewById(R.id.text_header)
            view.tag = holder
        } else {
            holder = view.tag as HeaderViewHolder
        }

        val headerText = applicationsToDisplay!![position].headerChar.toString()
        holder.text!!.text = headerText
        return view
    }

    private class HeaderViewHolder {
        internal var text: TextView? = null
    }

    override fun getHeaderId(position: Int): Long {
        return applicationsToDisplay!![position].headerChar.toLong()
    }

    fun setFilter(filter: String?) {
        if (filter.isNullOrEmpty()) {
            pattern = null
        } else {
            pattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE)
        }
    }

    override fun getCount(): Int {
        return applicationsToDisplay!!.size
    }

    override fun getItem(position: Int): Any {
        return applicationsToDisplay!![position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val results = FilterResults()
                if (charSequence == null || charSequence.isEmpty()) {
                    synchronized(mLock) {
                        results.values = applications
                        results.count = applications.size
                    }
                } else {
                    val prefixString = charSequence.toString().toLowerCase(Locale.getDefault()).trim { it <= ' ' }
                    val filterResultsData = ArrayList<AppEntry>()
                    synchronized(mLock) {
                        for (data in applications) {
                            val p = Pattern.compile(prefixString, Pattern.CASE_INSENSITIVE)
                            if (p.matcher(data.label!!.toLowerCase(Locale.getDefault()).trim { it <= ' ' }).find()) {
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
                applicationsToDisplay = filterResults.values as ArrayList<AppEntry>
                notifyDataSetChanged()
            }
        }
    }

    override fun isEmpty(): Boolean {
        return applicationsToDisplay != null && applicationsToDisplay!!.size == 0
    }
}