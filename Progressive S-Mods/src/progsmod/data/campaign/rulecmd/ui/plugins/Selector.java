package progsmod.data.campaign.rulecmd.ui.plugins;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.input.*;
import com.fs.starfarer.api.ui.*;
import progsmod.data.campaign.rulecmd.ui.*;

import java.util.*;

/** Selection plugin for a list of buttons. Assumes that the buttons are
 *  stacked vertically -- in particular that no two buttons have the same y-coordinate --
 *  and that the item list corresponds to buttons starting with the highest y-coordinate and
 *  going downward. */
public abstract class Selector<T extends Selectable> implements CustomUIPanelPlugin, Updatable {
    
    protected List<T> items = new ArrayList<>();

    private final BitSet isSelectedArray = new BitSet();
    private float mouseY;

    protected abstract void onSelected(int index);

    protected abstract void onDeselected(int index);

    /** Doesn't trigger [onSelected] */
    protected void forceSelect(int index) {
        items.get(index).select();
        isSelectedArray.set(index);
    }

    /** Doesn't trigger [onDeselected] */
    protected void forceDeselect(int index) {
        items.get(index).deselect();
        isSelectedArray.clear(index);
    }

    protected void init(List<T> items) {
        this.items = items;
    } 

    protected void addItem(T item) {
        items.add(item);
    }

    public List<T> getItems() {
        return items;
    }

    public List<T> getSelected() {
        List<T> selected = new ArrayList<>();
        for (int i : getSelectedIndices()) {
            selected.add(items.get(i));
        }
        return selected;
    }

    public List<Integer> getSelectedIndices() {
        List<Integer> selectedIndices = new ArrayList<>();
        for (int i = 0; i < isSelectedArray.size(); i++) {
            if (isSelectedArray.get(i)) { 
                selectedIndices.add(i);
            }
        }
        return selectedIndices;
    }

    @Override
    public void buttonPressed(Object o) {
        for (int i = 0; i < items.size(); i++) {
            checkIfModified(i);
        }
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
//        if (items.isEmpty()) {
//            return;
//        }
//
//        for (InputEventAPI event : events) {
//            if (event.isConsumed()) {
//                continue;
//            }
//
//            // Ideally this would be a mouse down event, or even better,
//            // a callback from a button being clicked, but the first
//            // get consumed by the button click and the second doesn't seem to
//            // exist.
//            if (event.isMouseMoveEvent()) {
//                mouseY = event.getY();
//
//                // Need to check two possible entries that the mouse
//                // could be hovering over
//                int toCheck = getLastEntryBeforeMouseY();
//                checkIfModified(toCheck);
//                if (toCheck + 1 < items.size()) {
//                    checkIfModified(toCheck + 1);
//                }
//
//                return;
//            }
//        }
    }

    /** Checks if the item at [index] has had its selection status
     *  modified; if so, call the appropriate handler. */
    private void checkIfModified(int index) {
        T item = items.get(index);
        if (item.isSelected() && !isSelectedArray.get(index)) {
            // Item wasn't selected and just got selected
            onSelected(index);
            // onSelected could clear the selection
            if (item.isSelected()) {
                isSelectedArray.set(index);
            }
        }
        if (!item.isSelected() && isSelectedArray.get(index)) {
            // It was selected and just got deselected
            onDeselected(index);
            // onDeselected could select the entry
            if (!item.isSelected()) {
                isSelectedArray.clear(index);
            }
        }
    }

    /** Finds the index of the last selector list entry whose y-coordinate is 
     *  greater than the y-coordinate of the mouse.
     *  (The list is descending in y)
     *  The selected entry, if any,
     *  must be either this one or the next one. */ 
    private int getLastEntryBeforeMouseY() {
        if (items.size() <= 1) {
            return 0;
        }
        int left = 0, right = items.size() - 1;
        while (left < right) {
            int mid = (left + right + 1) / 2;
            if (items.get(mid).position().getY() > mouseY) {
                left = mid;
            } 
            else {
                right = mid - 1;
            }
        }
        return left;
    }

    public void update() {}

    @Override
    public void advance(float amount) {}

    @Override
    public void positionChanged(PositionAPI position) { }

    @Override
    public void render(float alphaMult) {}

    @Override
    public void renderBelow(float alphaMult) {}
}
