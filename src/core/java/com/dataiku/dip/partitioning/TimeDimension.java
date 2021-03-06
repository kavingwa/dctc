package com.dataiku.dip.partitioning;

import com.dataiku.dip.utils.ErrorContext;

import java.util.*;

public class TimeDimension extends Dimension {
    public TimeDimension(String name, Period mappedPeriod) {
        super(name);
        this.mappedPeriod = mappedPeriod;
    }

    public String formatValue(TimeDimensionValue tdv, String glob) {
        return tdv.resolvePattern(glob);
    }

    public enum Period {
        YEAR,
        MONTH,
        DAY,
        HOUR;

        public static Period parse(String in) {
            for (Period p : values()) {
                if (p.toString().toLowerCase().equalsIgnoreCase(in)) return p;
            }
            throw ErrorContext.iae("Invalid time period " + in);
        }
        public String pattern() {
            switch (this) {
            case YEAR:
                return "%Y";
            case MONTH:
                return "%M";
            case DAY:
                return "%D";
            case HOUR:
                return "%H";
            default:
                throw new Error("Never reached.");
            }
        }
    }


    public boolean hasYear(){
        return true;
    }
    public boolean hasMonth() {
        return mappedPeriod != Period.YEAR;
    }
    public boolean hasDay() {
        return mappedPeriod != Period.YEAR && mappedPeriod != Period.MONTH;
    }
    public boolean hasHour() {
        return mappedPeriod != Period.YEAR && mappedPeriod != Period.MONTH && mappedPeriod != Period.DAY;
    }

    public Period mappedPeriod;

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TimeDimension)) return false;
        TimeDimension tother = (TimeDimension)other;
        return tother.mappedPeriod.equals(mappedPeriod);
    }

    protected DimensionValue getValueFromSymbolicDate(String date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        if (date.equals("YESTERDAY")) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        } else if(date.equals("TODAY")) {
        } else {
            throw new IllegalArgumentException("Unable to parse date: " + date);
        }
        TimeDimensionValue timeDim = new TimeDimensionValue(this);
        timeDim.setCal(cal);
        return timeDim;
    }

    @SuppressWarnings("fallthrough")
    @Override
    public DimensionValue getValueFromId(String id) {
        try {
            if (id.equals("YESTERDAY") || id.equals("TODAY")) {
                return getValueFromSymbolicDate(id);
            }

            String[] chunks = id.split("-");
            TimeDimensionValue  timeDim = new TimeDimensionValue(this);

            switch (mappedPeriod) {
            case HOUR:
                timeDim.setHour(Integer.parseInt(chunks[3]));
            case DAY:
                timeDim.setDay(Integer.parseInt(chunks[2]));
            case MONTH:
                timeDim.setMonth(Integer.parseInt(chunks[1]));
            case YEAR:
                timeDim.setYear(Integer.parseInt(chunks[0]));

                break;
            default:
                throw new Error("Never reached.");
            }
            return timeDim;
        } catch (Exception e) {
            throw new IllegalArgumentException("Illegal time partitioning value : '" +id + "'");
        }
    }

    @Override
    public List<DimensionValue> getValueFromPattern(String pattern) {
        if (pattern.contains("/")) {
            String[] ids = pattern.split("/", 2);
            TimeDimensionValue first = (TimeDimensionValue) getValueFromId(ids[0]);
            TimeDimensionValue last = (TimeDimensionValue) getValueFromId(ids[1]);
            TimeDimensionValue current = first;
            List<DimensionValue> values = new ArrayList<DimensionValue>();
            while (current.compareTo(last) <= 0) {
                values.add(current);
                current = current.nextPeriod();
            }
            return values;
        } else {
            return Collections.singletonList(getValueFromId(pattern));
        }
    }

    @Override
    public int hashCode() {
        throw new Error("hashCode not designed");
    }
}
