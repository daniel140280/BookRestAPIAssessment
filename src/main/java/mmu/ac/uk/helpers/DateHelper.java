package mmu.ac.uk.helpers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.Locale;

/**
 * Utility class for normalising and validating date strings from user input or database records. 
 * Accepts a wide range of formats including: numeric dates (dd/MM/yyyy, MM/dd/yy), month names (e.g. "February 2001"), and partial dates (e.g. "2020").
 * 2011 (year only → 01/01/2011). 
 * Out-liers presenting something like "Published" are treated as invalid and
 * return null, allowing the owner to decide how to represent unknown dates.
 *
 * All valid dates are returned in a consistent DD/MM/YYYY format.
 *
 * The method attempts multiple known formats, converts month names to numbers, expands two-digit years to four digits, and validates that the resulting
 * date is a real calendar date. Returns null if the input cannot be parsed.
 */
public class DateHelper {
	
//	private static final int PIVOT_YEAR = 1930;
//	
//	private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//  
//	private static final Locale MONTH_LOCALE = Locale.US;
//
//
//    // Two-digit year formats with correct pivot year (1930)
//    private static final DateTimeFormatter TWO_DIGIT_YEAR_US =
//            new DateTimeFormatterBuilder()
//                    .appendPattern("MM/dd/")
//                    .appendValueReduced(ChronoField.YEAR, 2, 2, PIVOT_YEAR)
//                    .toFormatter(MONTH_LOCALE);
//
//    private static final DateTimeFormatter TWO_DIGIT_YEAR_UK =
//            new DateTimeFormatterBuilder()
//                    .appendPattern("dd/MM/")
//                    .appendValueReduced(ChronoField.YEAR, 2, 2, PIVOT_YEAR)
//                    .toFormatter(MONTH_LOCALE);
//    
//    private static final DateTimeFormatter MONTH_SHORT_DASH_YY =
//            new DateTimeFormatterBuilder()
//		            .parseCaseInsensitive()
//		            .appendText(ChronoField.MONTH_OF_YEAR, TextStyle.SHORT)
//		            .appendLiteral('-')
//		            .appendValueReduced(ChronoField.YEAR, 2, 2, PIVOT_YEAR)
//		            .toFormatter(MONTH_LOCALE);
//
//    private static final DateTimeFormatter MONTH_SHORT_SPACE_YY =
//            new DateTimeFormatterBuilder()
//		            .parseCaseInsensitive()
//		            .appendText(ChronoField.MONTH_OF_YEAR, TextStyle.SHORT)
//		            .appendLiteral(' ')
//		            .appendValueReduced(ChronoField.YEAR, 2, 2, PIVOT_YEAR)
//		            .toFormatter(MONTH_LOCALE);
//
//
//    private static final DateTimeFormatter[] INPUT_FORMATS = new DateTimeFormatter[]{
//            // UK formats
//            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
//            DateTimeFormatter.ofPattern("d/M/yyyy"),
//            TWO_DIGIT_YEAR_UK,
//
//            // US formats
//            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
//            DateTimeFormatter.ofPattern("M/d/yyyy"),
//            TWO_DIGIT_YEAR_US,
//
//            // Month name formats
//            new DateTimeFormatterBuilder().parseCaseInsensitive()
//		            .appendPattern("d MMM yyyy").toFormatter(MONTH_LOCALE),
//		    new DateTimeFormatterBuilder().parseCaseInsensitive()
//		            .appendPattern("d MMMM yyyy").toFormatter(MONTH_LOCALE),
//		    new DateTimeFormatterBuilder().parseCaseInsensitive()
//		            .appendPattern("MMM yyyy").toFormatter(MONTH_LOCALE),
//		    new DateTimeFormatterBuilder().parseCaseInsensitive()
//		            .appendPattern("MMMM yyyy").toFormatter(MONTH_LOCALE),
//            MONTH_SHORT_DASH_YY,
//            MONTH_SHORT_SPACE_YY,
//
//            // Year only
//            DateTimeFormatter.ofPattern("yyyy")
//    };
//
//
//    public static String normalise(String raw) {
//        if (raw == null) {
//        	return null;
//        }
//
//        raw = raw.trim()
//                .replace('–', '-')
//                .replace('—', '-')
//                .replace('−', '-');
//
//        String upper = raw.toUpperCase(MONTH_LOCALE);
//        if (upper.equals("PUBLISHED") || upper.equals("UNKNOWN") || upper.equals("DATE UNKNOWN")) {
//            return null;
//        }
//
//        for (DateTimeFormatter fmt : INPUT_FORMATS) {
//            try {
//                LocalDate date = LocalDate.parse(raw, fmt);
//                return date.format(OUTPUT_FORMAT);
//            } catch (DateTimeParseException ignored) {}
//        }
//
//        return null;
//
//    }

	
    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter[] INPUT_FORMATS = new DateTimeFormatter[]{
    		DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yy"),
            DateTimeFormatter.ofPattern("MM/dd/yy"),
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMMM-yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM-yy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMMM-yy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("yyyy")
    };

    public static String normalise(String raw) {
        if (raw == null) return null;

        raw = raw.trim();

        // Handle "Published" or similar noise
        if (raw.equalsIgnoreCase("published") ||
            raw.equalsIgnoreCase("date unknown") ||
            raw.equalsIgnoreCase("unknown")) {
            return null;
        }
        
        // Expand two digit years before parsing
        raw = expandTwoDigitYear(raw);
        
        // Try all known formats
        for (DateTimeFormatter fmt : INPUT_FORMATS) {
            try {
                LocalDate date = LocalDate.parse(raw, fmt);
                return date.format(OUTPUT_FORMAT);
            } catch (DateTimeParseException ignored) {}
        }

        // Handle year-only manually (e.g., "2011")
        if (raw.matches("\\d{4}")) {
            LocalDate date = LocalDate.of(Integer.parseInt(raw), 1, 1);
            return date.format(OUTPUT_FORMAT);
        }

        // Handle month-year manually (e.g., "Aug-86")
        try {
            String[] parts = raw.split("[- ]");
            if (parts.length == 2) {
                String month = parts[0];
                String year = parts[1];

                int y = (year.length() == 2)
                        ? (year.startsWith("0") || Integer.parseInt(year) > 30
                            ? 1900 + Integer.parseInt(year)
                            : 2000 + Integer.parseInt(year))
                        : Integer.parseInt(year);

                DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
                int m = LocalDate.parse("01-" + month + "-" + y,
                        DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH)).getMonthValue();

                LocalDate date = LocalDate.of(y, m, 1);
                return date.format(OUTPUT_FORMAT);
            }
        } catch (Exception ignored) {}

        return null; // Invalid date based on the criteria selected. The DAO will determine how it handles incorrect/weird formats.
    }
    
    private static String expandTwoDigitYear(String raw) {
        // Find a trailing 2-digit year
        if (raw.matches(".*[^0-9]([0-9]{2})$")) {
            String yy = raw.substring(raw.length() - 2);
            int y = Integer.parseInt(yy);

            int fullYear = (y >= 30 ? 1900 + y : 2000 + y);

            return raw.substring(0, raw.length() - 2) + fullYear;
        }
        return raw;
    }

}
