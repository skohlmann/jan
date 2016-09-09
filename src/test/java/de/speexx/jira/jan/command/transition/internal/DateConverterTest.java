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
package de.speexx.jira.jan.command.transition.internal;

import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DateConverterTest {

    DateConverter converter = new DateConverter();

    @Test
    public void iso8601_strict() {
        assertEquals(LocalDate.of(2016, 9, 9), this.converter.convert("2016-09-09"));
    }

    @Test
    public void iso8601_lazyMonth() {
        assertEquals(LocalDate.of(2016, 9, 9), this.converter.convert("2016-9-09"));
    }
    
    @Test
    public void iso8601_lazyDay() {
        assertEquals(LocalDate.of(2016, 9, 9), this.converter.convert("2016-09-9"));
    }
    
    @Test
    public void german_strict() {
        assertEquals(LocalDate.of(2016, 9, 9), this.converter.convert("09.09.2016"));
    }

    @Test
    public void german_lazyMonth() {
        assertEquals(LocalDate.of(2016, 9, 9), this.converter.convert("09.9.2016"));
    }
    
    @Test
    public void german_lazyDay() {
        assertEquals(LocalDate.of(2016, 9, 9), this.converter.convert("9.09.2016"));
    }
    
    @Test
    public void us_strict() {
        assertEquals(LocalDate.of(2016, 9, 9), this.converter.convert("09/09/2016"));
    }

    @Test
    public void us_lazyMonth() {
        assertEquals(LocalDate.of(2016, 9, 9), this.converter.convert("9/09/2016"));
    }
    
    @Test
    public void us_lazyDay() {
        assertEquals(LocalDate.of(2016, 9, 9), this.converter.convert("09/9/2016"));
    }
    
    @Test
    public void yearMonth_iso() {
        assertEquals(LocalDate.of(2016, 11, 1), this.converter.convert("2016-11"));
    }
    
    @Test
    public void yearMonth_iso_lazy() {
        assertEquals(LocalDate.of(2016, 8, 1), this.converter.convert("2016-8"));
    }
    
    
    @Test
    public void yearMonth_iso_09() {
        assertEquals(LocalDate.of(2016, 9, 1), this.converter.convert("2016-09"));
    }
    
    @Test
    public void yearMonth_german() {
        assertEquals(LocalDate.of(2016, 11, 1), this.converter.convert("11.2016"));
    }
    
    @Test
    public void yearMonth_german_lazy() {
        assertEquals(LocalDate.of(2016, 8, 1), this.converter.convert("8.2016"));
    }
        
    @Test
    public void yearMonth_german_09() {
        assertEquals(LocalDate.of(2016, 8, 1), this.converter.convert("08.2016"));
    }
    
    @Test
    public void yearMonth_us_09() {
        assertEquals(LocalDate.of(2016, 8, 1), this.converter.convert("08/2016"));
    }
    
    @Test
    public void yearMonth_us() {
        assertEquals(LocalDate.of(2016, 11, 1), this.converter.convert("11/2016"));
    }
    
    @Test
    public void yearMonth_us_lazy() {
        assertEquals(LocalDate.of(2016, 8, 1), this.converter.convert("8/2016"));
    }
    
    @Test
    public void toMonthInt_with_singleChar1() {
        assertEquals(1, this.converter.toMonthInt("1"));
    }
    
    @Test
    public void toMonthInt_with_singleChar11() {
        assertEquals(11, this.converter.toMonthInt("11"));
    }
    
    @Test
    public void toMonthInt_with_singleChar12() {
        assertEquals(12, this.converter.toMonthInt("12"));
    }
    
    @Test
    public void toMonthInt_with_singleChar00() {
        final Throwable ex = Assertions.expectThrows(IllegalArgumentException.class, () -> {
            this.converter.toMonthInt("00");
        });
        assertEquals("Zero is illegal month value. Must be between 1 and 12.", ex.getMessage());
    }
    
    @Test
    public void toMonthInt_with_singleChar13() {
        final Throwable ex = Assertions.expectThrows(IllegalArgumentException.class, () -> {
            this.converter.toMonthInt("13");
        });
        assertEquals("13 is illegal month value. Must be between 1 and 12.", ex.getMessage());
    }
    
    @Test
    public void toMonthInt_with_singleChar9() {
        assertEquals(9, this.converter.toMonthInt("9"));
    }
    
    @Test
    public void toMonthInt_with_singleChar0() {
        final Throwable ex = Assertions.expectThrows(IllegalArgumentException.class, () -> {
            this.converter.toMonthInt("0");
        });
        assertEquals("0 is illegal month value. Must be between 1 and 12.", ex.getMessage());
    }
    
    @BeforeEach
    public void createConverter() {
        this.converter = new DateConverter();
    }
}
