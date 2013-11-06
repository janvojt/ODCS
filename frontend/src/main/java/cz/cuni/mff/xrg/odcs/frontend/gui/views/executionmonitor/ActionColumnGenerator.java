package cz.cuni.mff.xrg.odcs.frontend.gui.views.executionmonitor;

import com.vaadin.ui.Button;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.HorizontalLayout;
import java.util.LinkedList;
import java.util.List;

/**
 * Generate column with action buttons.
 *
 * @author Petyr
 */
public class ActionColumnGenerator implements CustomTable.ColumnGenerator {

    /**
     * Abstract class for reaction on action button click.
     */
    public static abstract class Action implements Button.ClickListener {
       
        @Override
        public void buttonClick(Button.ClickEvent event) {
            // in button.data the row id is stored
            action((long)event.getButton().getData());
        }
        
        protected abstract void action(long id);
        
    }
    
    /**
     * Interface for class that determines if the button is visible for given
     * item.
     */
    public static interface ButtonShowCondition {

        public boolean show(CustomTable source, long id);

    }

    /**
     * Contains information about single action button.
     */
    private class ActionButtonInfo {

        /**
         * Button name.
         */
        String name;

        /**
         * Button width.
         */
        String width;

        /**
         * Action to execute if button is clicked.
         */
        Action action;

        /**
         * Show condition, if null the button is always shown.
         */
        ButtonShowCondition showCondition;

        ActionButtonInfo(String name, String width, Action action, ButtonShowCondition showCondition) {
            this.name = name;
            this.width = width;
            this.action = action;
            this.showCondition = showCondition;
        }
    }

    private final List<ActionButtonInfo> actionButtons = new LinkedList<>();

    @Override
    public Object generateCell(CustomTable source, Object itemId, Object columnId) {
        HorizontalLayout box = new HorizontalLayout();
        box.setSpacing(true);
        // add buttons
        for (ActionButtonInfo template : actionButtons) {
            if (template.showCondition == null
                || template.showCondition.show(source, (Long) itemId)) {
                // we show button
                Button button = new Button(template.name);
                button.setWidth(template.width);
                button.addClickListener(template.action);
                // set button data as id
                button.setData(itemId);
                // add to the component
                box.addComponent(button);
            } else {
                // do not show this button
            }
        }
        return box;
    }

    /**
     * Add template for action button.
     *
     * @param name
     * @param width
     * @param action
     */
    public void addButton(String name, String width, Action action) {
        actionButtons.add(new ActionButtonInfo(name, width, action, null));
    }

    /**
     * Add template for action button.
     *
     * @param name
     * @param width
     * @param action
     * @param showCondition
     */
    public void addButton(String name, String width, Action action, ButtonShowCondition showCondition) {
        actionButtons.add(new ActionButtonInfo(name, width, action, showCondition));
    }

}
