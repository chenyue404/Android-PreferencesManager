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
package fr.simon.marquis.preferencesmanager.model

import java.io.File

data class BackupContainer(
    val pkgName: String,
    val backupList: MutableList<BackupContainerInfo>,
)

data class BackupContainerInfo(
    val backupDate: String, // Date of backup from millis
    val backupFile: File, // The path of the backup file
    val backupXmlName: String, // The xml file of the package backup
    val size: Long, // Size of the file
) {
    override fun toString(): String {
        return StringBuilder()
            .append("BackupContainerInfo:\n")
            .append("backupDate=$backupDate\n")
            .append("backupFile=${backupFile.path}\n")
            .append("backupXmlName=$backupXmlName\n")
            .append("size=$size\n")
            .append("----")
            .toString()
    }
}
