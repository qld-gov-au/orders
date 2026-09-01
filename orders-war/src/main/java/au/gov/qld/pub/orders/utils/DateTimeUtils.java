package au.gov.qld.pub.orders.utils;

import org.springframework.validation.Errors;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.Date;

import static org.apache.commons.lang3.StringUtils.isBlank;

public final class DateTimeUtils {
    public static final ZoneId BRISBANE = ZoneId.of("Australia/Brisbane");
    public static final ZoneId SYDNEY = ZoneId.of("Australia/Sydney");
    public static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    public static final ZoneId UTC = ZoneId.of("Z");
    public static final String RECEIPT_DISPLAY_FORMAT = "d MMMMM yyyy";
    public static final DateTimeFormatter LOCAL_DATE_TIME_WITH_MILLIS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    public static final DateTimeFormatter ZONED_DATE_TIME_WITH_MILLIS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    public static final DateTimeFormatter DMY_SLASHED_DATE_FORMATTER = DateTimeFormatter.ofPattern("d/MM/yyyy");
    public static final DateTimeFormatter DMY_DATE_FORMATTER = DateTimeFormatter.ofPattern("d/MM/yyyy");
    public static final DateTimeFormatter INPUT_DATE_PARSER = DateTimeFormatter.ofPattern("d/M/yyyy");
    public static final DateTimeFormatter DMY_HYPHENATED_DATE_FORMATTER = DateTimeFormatter.ofPattern("d-MM-yyyy");
    public static final DateTimeFormatter LOCAL_DATE_OPTIONAL_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd['T'HH:mm:ss]");
    public static final DateTimeFormatter BPOINT_SETTLEMENT_FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter ISO_DATETIME_LENIENT_MILLIS = new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
        // accept 3-digit or 7-digit fraction of a second
        .appendFraction(ChronoField.MILLI_OF_SECOND, 3, 7, true)
        .toFormatter().withResolverStyle(ResolverStyle.LENIENT);
    private static Clock systemClock;

    private DateTimeUtils() {}

    public static Clock getSystemClock() {
        return systemClock == null ? Clock.systemDefaultZone() : systemClock;
    }

    public static void setSystemClock(Temporal temporal) {
        if (temporal == null) {
            systemClock = null;
        } else if (temporal instanceof Instant) {
            doSetSystemClock((Instant) temporal);
        } else if (temporal instanceof ZonedDateTime) {
            doSetSystemClock(((ZonedDateTime) temporal).toInstant());
        } else if (temporal instanceof LocalDateTime) {
            doSetSystemClock(((LocalDateTime) temporal).atZone(DEFAULT_ZONE).toInstant());
        } else {
            throw new IllegalArgumentException("Unrecognised clock instant: " + temporal);
        }
    }

    private static void doSetSystemClock(Instant instant) {
        systemClock = Clock.fixed(instant, DEFAULT_ZONE);
    }

    /**
     * Returns a new date with the LocalDate 'now' function,
     * possibly using a custom clock for testing.
     */
    public static LocalDate currentLocalDate() {
        return LocalDate.now(getSystemClock());
    }

    /**
     * Returns a new LocalDateTime with the LocalDateTime 'now' function,
     * possibly using a custom clock for testing.
     */
    public static LocalDateTime currentLocalDateTime() {
        return LocalDateTime.now(getSystemClock());
    }

    /**
     * Returns the number of milliseconds since the epoch (1970-01-01T00:00:00Z),
     * possibly using a custom clock for testing.
     */
    public static Instant currentInstant() {
        return Instant.now(getSystemClock());
    }

    /**
     * Returns the number of milliseconds since the epoch (1970-01-01T00:00:00Z),
     * possibly using a custom clock for testing.
     */
    public static long currentEpochMillis() {
        return currentInstant().toEpochMilli();
    }

    /**
     * Returns a new ZonedDateTime with the ZonedDateTime 'now' function,
     * possibly using a custom clock for testing.
     */
    public static ZonedDateTime currentZonedDateTime() {
        return ZonedDateTime.now(getSystemClock());
    }

    public static LocalDateTime changeLocalZone(LocalDateTime localDateTime, ZoneId sourceZone, ZoneId targetZone) {
        return ZonedDateTime.of(localDateTime, sourceZone).withZoneSameInstant(targetZone).toLocalDateTime();
    }

    public static ZonedDateTime fromMillis(long millis) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), DEFAULT_ZONE);
    }

    public static long toMillis(LocalDateTime localDateTime) {
        return localDateTime.atZone(DEFAULT_ZONE).toInstant().toEpochMilli();
    }

    public static ZonedDateTime fromDate(Date date) {
        return ZonedDateTime.ofInstant(date.toInstant(), DEFAULT_ZONE);
    }

    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(DEFAULT_ZONE).toInstant());
    }

    public static Date toDate(LocalDate localDate) {
        return toDate(localDate.atStartOfDay());
    }

    public static long secondsBetween(Temporal start, Temporal end) {
        return Duration.between(start, end).getSeconds();
    }

    public static LocalDate parseDate(String dateStr, String fieldId, String fieldName, Errors errors) {
        if (isBlank(dateStr)) {
            return null;
        }

        try {
            return LocalDate.parse(dateStr, INPUT_DATE_PARSER);
        } catch (DateTimeParseException e) {
            errors.rejectValue(fieldId, "InvliadDateFormat", fieldName + ": Invalid date format. Please use d/M/yyyy.");
            return null;
        }
    }

    public static LocalDate getDate(String date) {
        return isBlank(date) ? null : LocalDate.parse(date, INPUT_DATE_PARSER);
    }
}
