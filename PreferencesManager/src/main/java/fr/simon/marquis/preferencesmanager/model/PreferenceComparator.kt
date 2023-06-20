package fr.simon.marquis.preferencesmanager.model

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

internal class PreferenceComparator(
    type: EPreferencesSort
) : Comparator<Map.Entry<String?, Any?>?> {

    private var mType: EPreferencesSort = EPreferencesSort.ALPHANUMERIC

    init {
        mType = type
    }

    override fun compare(lhs: Map.Entry<String?, Any?>?, rhs: Map.Entry<String?, Any?>?): Int {
        if (mType === EPreferencesSort.TYPE_AND_ALPHANUMERIC) {
            val l =
                if (lhs == null) "" else if (lhs.value == null) "" else lhs.value!!.javaClass.name
            val r =
                if (rhs == null) "" else if (rhs.value == null) "" else rhs.value!!.javaClass.name
            val res = l.compareTo(r, ignoreCase = true)
            if (res != 0) {
                return res
            }
        }

        return (if (lhs == null) "" else lhs.key)!!.compareTo(
            (if (rhs == null) "" else rhs.key)!!,
            ignoreCase = true
        )
    }
}
