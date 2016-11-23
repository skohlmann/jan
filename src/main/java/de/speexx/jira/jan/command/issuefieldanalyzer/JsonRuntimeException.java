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
package de.speexx.jira.jan.command.issuefieldanalyzer;

import org.codehaus.jettison.json.JSONException;

/**
 * Runtime encapuslation of a {@link JSONException}.
 */
public class JsonRuntimeException extends RuntimeException {
    
    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause the cause (which is saved for later retrieval by the 
     *              {@link Throwable.getCause()} method). (A <code>null</code>
     *              value is permitted, and indicates that the cause is
     *              nonexistent or unknown.)
     */
    public JsonRuntimeException(final JSONException cause) {
        super(cause);
    }

    /** Returns the message of the cause exception. */
    @Override
    public final String getMessage() {
        final Throwable cause = this.getCause();
        if (cause != null) {
            return cause.getMessage();
        }
        return null;
    }
}
