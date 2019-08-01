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
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView

import fr.simon.marquis.preferencesmanager.R
import java.util.*

internal class RestoreAdapter(private val ctx: Context, private val dialog: RestoreDialogFragment, private var backups: List<String>?, private val listener: RestoreDialogFragment.OnRestoreFragmentInteractionListener, private val fullPath: String) : BaseAdapter() {

    init {
        backups?.sorted()
    }

    override fun getCount(): Int {
        return backups!!.size
    }

    override fun getItem(position: Int): Any {
        return backups!![position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val holder: ViewHolder
        if (view == null) {
            view = LayoutInflater.from(ctx).inflate(R.layout.row_restore, parent, false)
            assert(view != null)
            holder = ViewHolder()
            holder.label = view!!.findViewById(R.id.item_label)
            holder.delete = view.findViewById(R.id.item_delete)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        val backup = backups!![position]
        holder.label!!.text = getDisplayLabel(ctx, backup.toLong())
        holder.delete!!.setOnClickListener {
            backups = listener.onDeleteBackup(backup, fullPath)
            if (backups == null || backups!!.isEmpty()) {
                dialog.noMoreBackup()
            } else {
                notifyDataSetChanged()
            }
        }

        return view
    }

    private class ViewHolder {
        var delete: ImageButton? = null
        var label: TextView? = null
    }

    private fun getDisplayLabel(ctx: Context, time: Long): String {
        return upperFirstLetter(DateUtils.formatDateTime(ctx, time, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_WEEKDAY)) + " (" + lowerFirstLetter(DateUtils.getRelativeTimeSpanString(time, Date().time, DateUtils.SECOND_IN_MILLIS).toString()) + ")"
    }

    private fun upperFirstLetter(original: String): String {
        return if (original.isEmpty()) {
            original
        } else original.substring(0, 1).toUpperCase(Locale.getDefault()) + original.substring(1)
    }

    private fun lowerFirstLetter(original: String): String {
        return if (original.isEmpty()) {
            original
        } else original.substring(0, 1).toLowerCase(Locale.getDefault()) + original.substring(1)
    }
}
