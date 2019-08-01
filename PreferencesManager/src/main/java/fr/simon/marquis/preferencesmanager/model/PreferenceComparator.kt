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

import java.util.Comparator
import kotlin.collections.Map.Entry

internal class PreferenceComparator(private val mType: PreferenceSortType) : Comparator<Entry<Any, Any>> {

    override fun compare(lhs: Entry<Any, Any>?, rhs: Entry<Any, Any>?): Int {
        if (mType == PreferenceSortType.TYPE_AND_ALPHANUMERIC) {
            val l = lhs?.value?.javaClass?.name ?: ""
            val r = rhs?.value?.javaClass?.name ?: ""
            val res = l.compareTo(r, ignoreCase = true)
            if (res != 0) {
                return res
            }
        }
        return (lhs?.key ?: "").toString().compareTo((rhs?.key ?: "") as String, ignoreCase = true)
    }
}