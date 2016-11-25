/* A tool to extract transition information from JIRA for analyzis (jan).
 *
 * Copyright (C) 2016 Sascha Kohlmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.speexx.jira.jan.command.issuequery;

/**
 * Defines the output in CSV files for the temporal information of the changes
 */
enum TemporalChangeOutput {

    /** With <tt>NONE</tt> the output contains no temporal information for the changes.
     * Only the <em>from</em> value and the <em>to</em> value is part of the 
     * CSV file.
     */    
    NONE,
    /** With <tt>TIME</tt> the change datetime for the change is given in ISO 8601
     * format.
     */
    TIME,
    /** With <tt>DURATION</tt> the duration between the former change and the current
     * change is given. For the first change of the field the creation date
     * of the change is used.
     * <p>The duration is given in milli seconds.</p>
     */
    DURATION,
    /**
     * With <tt>BOTH</tt> the temporal information {@linkplain #TIME time} and
     * {@linkplain #DURATION duration} are part of the CSV output.
     */
    BOTH;

}
