/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.DataHolder;
import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.Date;
import sun.security.acl.OwnerImpl;

/**
 *
 * @author Charon
 */
public class TimeSpan {

    private Tribe validFor = null;
    private Date atDate = null;
    private Point span = null;

    public TimeSpan(Point pSpan) {
        span = pSpan;
        validFor = null;
        atDate = null;
    }

    public TimeSpan(Date pAtDate, Point pSpan) {
        atDate = pAtDate;
        span = pSpan;
        validFor = null;
    }

    public TimeSpan(Point pSpan, Tribe pTribe) {
        span = pSpan;
        validFor = pTribe;
    }

    public TimeSpan(Date pAtDate, Point pSpan, Tribe pTribe) {
        atDate = pAtDate;
        span = pSpan;
        validFor = pTribe;
    }

    public Date getAtDate() {
        return atDate;
    }

    public Point getSpan() {
        return span;
    }

    public void setSpan(Point pSpan) {
        span = pSpan;
    }

    public Tribe isValidFor() {
        return validFor;
    }

    public boolean intersects(TimeSpan pSpan) {
        Tribe thisTribe = isValidFor();
        Tribe theOtherTribe = pSpan.isValidFor();

        if (thisTribe != null && theOtherTribe != null && !thisTribe.equals(theOtherTribe)) {
            //both spans are for different tribes, so they can't intersect by definition
            return false;
        }

        Date thisDay = getAtDate();
        Date theOtherDay = pSpan.getAtDate();

        if (thisDay == null && theOtherDay == null) {
            //both spans are for all days, so we only have to check the span itself
        }
        if (thisDay != null && theOtherDay == null) {
            //the day for this span is not null, but intersections can only happen on this single day.
            //so we assume this day also for the other span.
        }
        if (thisDay == null && theOtherDay != null) {
            //the day for the other span is not null, but intersections can only happen on this single day.
            //so we assume the other day also for this span.
        } else {
            //here we arrive if both days are not null. If the days are not the same, no intersection will be possible.
            //if both days are the same simply check as in case 1
        }

        return true;
    }

    public static TimeSpan fromPropertyString(String pString) {
        String[] elems = pString.split(",");
        Date date = null;
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy");
        try {
            date = f.parse(elems[0]);
        } catch (Exception e) {
            date = null;
        }
        int spanStart = 0;
        int spanEnd = 0;
        try {
            spanStart = Integer.parseInt(elems[1]);
            spanEnd = Integer.parseInt(elems[2]);
        } catch (Exception e) {
            //invalid!?
            return null;
        }
        Tribe t = null;
        try {
            t = DataHolder.getSingleton().getTribeByName(elems[3]);
        } catch (Exception e) {
        }
        if (date == null && t == null) {
            return new TimeSpan(new Point(spanStart, spanEnd));
        } else if (date != null && t == null) {
            return new TimeSpan(date, new Point(spanStart, spanEnd));
        } else {
            return new TimeSpan(date, new Point(spanStart, spanEnd), t);
        }
    }

    public String toPropertyString() {
        String res = "";
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy");
        if (atDate != null) {
            res = f.format(atDate) + "," + span.x + "," + span.y + ",";
            if (validFor != null) {
                res += validFor;
            } else {
                res += "*";
            }
        } else {
            res = "*," + span.x + "," + span.y + ",";
            //res = "Täglich, von " + span.x + " Uhr bis " + span.y + " Uhr";
            if (validFor != null) {
                res += validFor;
            } else {
                res += "*";
            }
        }
        return res;
    }

    @Override
    public String toString() {
        String result = null;
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy");
        if (atDate != null) {
            result = "Am " + f.format(atDate) + ", von " + span.x + " Uhr bis " + span.y + " Uhr";
            if (validFor != null) {
                result += " (" + validFor.toString() + ")";
            } else {
                result += " (Alle)";
            }
        } else {
            result = "Täglich, von " + span.x + " Uhr bis " + span.y + " Uhr";
            if (validFor != null) {
                result += " (" + validFor.toString() + ")";
            } else {
                result += " (Alle)";
            }
        }

        return result;
    }
}
