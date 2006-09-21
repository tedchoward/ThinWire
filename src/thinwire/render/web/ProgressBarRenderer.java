package thinwire.render.web;

import thinwire.ui.Component;

public class ProgressBarRenderer extends RangeComponentRenderer {
    private static String PROGRESS_BAR_CLASS = "tw_ProgressBar";
    
    public void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(PROGRESS_BAR_CLASS, wr, c, container);
        super.render(wr, comp, container);
    }
}
