/* CSV query table to work with CSV files and SQL like statements.
 *
 * Copyright (C) 2016  Sascha Kohlmann
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.speexx.jira.jan;

public class JiraAnalyzeException extends RuntimeException {

    /**
     * Creates a new instance of <code>JiraAnalyzeException</code> without
     * detail message.
     */
    public JiraAnalyzeException() {
    }

    /**
     * Constructs an instance of <code>JiraAnalyzeException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public JiraAnalyzeException(final String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>JiraAnalyzeException</code> with the specified
     * detail message and a cause.
     *
     * @param msg the detail message.
     * @param cause the cause (which is saved for later retrieval by the 
     *              {@link Throwable.getCause()} method). (A <code>null</code>
     *              value is permitted, and indicates that the cause is
     *              nonexistent or unknown.)
     */
    public JiraAnalyzeException(final String msg, final Exception cause) {
        super(msg, cause);
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause the cause (which is saved for later retrieval by the 
     *              {@link Throwable.getCause()} method). (A <code>null</code>
     *              value is permitted, and indicates that the cause is
     *              nonexistent or unknown.)
     */
    public JiraAnalyzeException(final Exception cause) {
        super(cause);
    }
}
