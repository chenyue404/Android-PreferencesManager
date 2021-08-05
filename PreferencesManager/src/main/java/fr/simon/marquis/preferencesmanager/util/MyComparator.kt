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
package fr.simon.marquis.preferencesmanager.util

import fr.simon.marquis.preferencesmanager.model.AppEntry
import java.text.Collator
import java.util.Comparator

class MyComparator : Comparator<AppEntry> {
    private val sCollator = Collator.getInstance()

    init {
        // Ignore case and accents
        sCollator.strength = Collator.SECONDARY
    }

    override fun compare(obj1: AppEntry, obj2: AppEntry): Int =
        sCollator.compare(obj1.sortingValue, obj2.sortingValue)
}
