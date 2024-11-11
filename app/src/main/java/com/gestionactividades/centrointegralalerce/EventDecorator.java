// EventDecorator.java
package com.gestionactividades.centrointegralalerce;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.HashSet;
import java.util.Set;

public class EventDecorator implements DayViewDecorator {

    private final int color;
    private final Set<CalendarDay> dates;

    public EventDecorator(int color, Set<CalendarDay> dates) {
        this.color = color;
        this.dates = dates;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        Drawable highlight = new ColorDrawable(color);
        view.setBackgroundDrawable(highlight);
    }
}
