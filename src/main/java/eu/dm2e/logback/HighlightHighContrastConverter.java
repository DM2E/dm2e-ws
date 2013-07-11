package eu.dm2e.logback;

import static ch.qos.logback.core.pattern.color.ANSIConstants.*;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;


public class HighlightHighContrastConverter extends ForegroundCompositeConverterBase<ILoggingEvent> {

    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        Level level = event.getLevel();
        switch (level.toInt()) {
            case Level.ERROR_INT: return BOLD+RED_FG;
            case Level.WARN_INT: return BOLD+RED_FG;
            case Level.INFO_INT: return BOLD+GREEN_FG;
            case Level.DEBUG_INT: return BOLD+YELLOW_FG;
            default: return DEFAULT_FG;
        }
    }


}
